/*
 * Copyright (c) 2026. Paul Harrison, University of Manchester.
 *
 */

package org.javastro.ivoa.tap;


import io.restassured.filter.log.LogDetail;
import io.restassured.http.ContentType;

import java.time.Duration;

import static io.restassured.RestAssured.given;
import static org.awaitility.Awaitility.await;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.fail;

public abstract class AbstractTAPTest {
   protected static void startAndTestJob(String jobUrl) {
      //  Start the Job (POST to /phase with PHASE=RUN)
      given()
            .contentType(ContentType.URLENC)
            .redirects().follow(false)
            .formParam("PHASE", "RUN")
            .when()
            .post(jobUrl + "/phase")
            .then()
            .statusCode(303);

      // Poll until
      await().atMost(Duration.ofSeconds(30))
            .pollInterval(Duration.ofMillis(500))
            .untilAsserted(() -> {
               given()
                     .when().get(jobUrl + "/phase")
                     .then()
                     .statusCode(200)
                     .body(not(comparesEqualTo("RUNNING")));
            })
            ;
      String status = given()
            .when().get(jobUrl + "/phase")
            .then()
            .statusCode(200)
            .extract().body().asString();
      if (status.equals("ERROR"))
      {

       given()
             .when().get(jobUrl + "/error")
             .then()
             .statusCode(200)
             .log().body();
       fail("Job ended in error state");

      }
      else if (status.equals("COMPLETED")) {


         // Retrieve Results
         given()
               .when().get(jobUrl + "/results")
               .then()
               .log().ifValidationFails(LogDetail.BODY)
               .statusCode(200)
               .body("results.result.size()", greaterThan(0));

         //retrieve the actual result
         given()
               .when().get(jobUrl + "/results/result")
               .then()
               .statusCode(200)
               .log().body();
         ;

         //TODO get the result into a file and verify that it is an OK VOTable.

      }
       else
      {
         fail("Unexpected job status "+status);
      }
   }
}
