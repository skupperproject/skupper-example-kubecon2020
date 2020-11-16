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
package io.skupper.throttleService;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import io.vertx.core.Vertx;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

@Path("/hello")
public class ThrottleService {

    @Inject
    Vertx vertx;

    private int ratePerTenth;
    private String hostname  = System.getenv("HOSTNAME");
    private int sendSlots    = 1;
    private int count        = 0;
    private List<CompletableFuture<String>> pendingFutures   = new ArrayList<CompletableFuture<String>>();
    private List<String>                    pendingResponses = new ArrayList<String>();
    
    public void SetupTimer() {
        //
        // Attempt to get the rate limit from the environment.  If unable, default to
        // a ratePerTenth of 1 (10 responses per second).
        //
        try {
            ratePerTenth = Integer.parseInt(System.getenv("RATE_LIMIT"));
        } catch (NumberFormatException e) {
            ratePerTenth = 1;
        }

        //
        // Set up a timer to process deferred responses in batches every 100 milliseconds.
        //
        vertx.setPeriodic(100, l -> {
            sendSlots = ratePerTenth;
            while (sendSlots > 0 && !pendingResponses.isEmpty()) {
                sendSlots--;
                String                    response = pendingResponses.remove(0);
                CompletableFuture<String> future   = pendingFutures.remove(0);
                future.complete(response);
            }
        });
    }

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public CompletionStage<String> hello() {
        if (count == 0)
            SetupTimer();

        //
        // Create a response to the GET that includes the pod name and the request count.
        //
        count++;
        String response = String.format("{\"pod\":\"%s\",\"count\":\"%d\"}", hostname, count);
        CompletableFuture<String> future = new CompletableFuture<>();

        //
        // If there is at least one available slot, respond immediately.  If not, we are being
        // rate-limited and the response must be deferred.
        //
        if (sendSlots > 0) {
            sendSlots--;
            future.complete(response);
        } else {
            pendingResponses.add(response);
            pendingFutures.add(future);
        }

        return future;
    }
}
