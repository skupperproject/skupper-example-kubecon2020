package io.skupper.restclient;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.is;

@QuarkusTest
public class LoadGenTest {

    @Test
    public void testHelloEndpoint() {
        given()
          .when().get("/loadgen/set/0")
          .then()
             .statusCode(200)
             .body(is("Concurrency set to 0 (in-flight: 0, total: 0, failures: 0, last_status: <none>)"));
    }

}
