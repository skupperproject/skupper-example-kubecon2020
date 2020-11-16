/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
*/
package io.skupper.restclient;

import java.util.ArrayList;
import java.util.HashMap;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import io.vertx.axle.ext.web.client.WebClient;
import io.vertx.core.json.JsonObject;
import io.vertx.axle.core.Vertx;
import io.vertx.ext.web.client.WebClientOptions;

@Path("/loadgen")
public class LoadGen {

    int concurrency = 0;
    int inFlight    = 0;
    int total       = 0;
    int failures    = 0;
    int maxHistory  = 100;
    String lastStatus = "<none>";
    String lastError;
    ArrayList<String>        podHistory = new ArrayList<String>();
    HashMap<String, Integer> histogram  = new HashMap<String, Integer>();

    @Inject
    Vertx vertx;

    private WebClient client;

    @PostConstruct
    void initialize() {
        client = WebClient.create(vertx,
            new WebClientOptions()
                .setDefaultHost("greeting")
                .setDefaultPort(80));
    }

    private void sendRequest() {
        inFlight++;
        total++;
        client.get("/hello")
            .send()
            .whenComplete((resp, exception) -> {
                inFlight--;
                if (exception == null) {
                    lastStatus = resp.statusMessage();
                    JsonObject body = resp.bodyAsJsonObject();
                    if (body.containsKey("pod")) {
                        String pod = body.getString("pod");
                        podHistory.add(pod);
                        if (histogram.containsKey(pod)) {
                            Integer currentCount = histogram.get(pod);
                            histogram.put(pod, currentCount + 1);
                        } else {
                            histogram.put(pod, 1);
                        }

                        //
                        // If the pod history is full, remove the oldest pod and account
                        // for the deletion in the histogram.
                        //
                        if (podHistory.size() > maxHistory) {
                            String  removed      = podHistory.remove(0);
                            Integer removedCount = histogram.get(removed);
                            histogram.put(removed, removedCount - 1);
                        }
                    }
                } else {
                    lastError = exception.toString();
                    failures++;
                }
                if (inFlight < concurrency) {
                    sendRequest();
                }
        });
    }

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    @Path("/set/{val}")
    public String setLoad(@PathParam("val") String val) {
        int newVal;
        try {
            newVal = Integer.parseInt(val);
        } catch (Exception e) {
            newVal = concurrency;
        }

        concurrency = newVal;

        while (concurrency > inFlight) {
            sendRequest();
        }

        return String.format("Concurrency set to %d (in-flight: %d, total: %d, failures: %d, last_status: %s)",
            concurrency, inFlight, total, failures, lastStatus);
    }

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    @Path("/status")
    public String getStatus() {
        int count = podHistory.size();
        String result = String.format("\nWorkload Status (last %d items, concurrency: %d):\n", count, concurrency);
        if (lastError != null) {
            result += String.format("Last error: %s\n", lastError);
        }
        for (String pod : histogram.keySet()) {
            Integer podCount = histogram.get(pod);
            if (podCount > 0) {
                result += String.format("%4d %3d%% %s\n", podCount, (podCount * 100) / count, pod);
            }
        }

        return result;
    }
}

