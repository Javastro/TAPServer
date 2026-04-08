/*
 * Copyright (c) 2026. Paul Harrison, University of Manchester.
 *
 */

package org.javastro.ivoa.tap;


import io.quarkus.test.junit.QuarkusTest;
import io.restassured.RestAssured;
import io.restassured.filter.log.LogDetail;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.junit.jupiter.api.Test;

import java.time.Duration;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;
import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.fail;


/*
 * Created on 05/03/2026 by Paul Harrison (paul.harrison@manchester.ac.uk).
 */
@QuarkusTest
public class QueryTest {
   private static final String ASYNC_ENDPOINT = "/async";
   private static final String SYNC_ENDPOINT = "/sync";
   @Test
   public void testAsyncQuery() {
      Response createResponse = given()
            .formParam("query", "select * from TAP_SCHEMA.schemas")
            .redirects().follow(false)
            .when().post(ASYNC_ENDPOINT)
            .then()
            .statusCode(303)
            .header("Location", notNullValue())
            .extract().response();
      String jobFullUrl = createResponse.getHeader("Location");
      String jobId = jobFullUrl.substring(jobFullUrl.lastIndexOf('/') + 1);

      System.out.println(jobFullUrl);
      String jobUrl = ASYNC_ENDPOINT+"/"+jobId;
      //Verify Initial State (PENDING)
      given()
            .when().get(jobUrl)
            .then()
            .statusCode(200)
            .body("job.phase", equalTo("PENDING"));

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
               .statusCode(200);
         //TODO get the result into a file and verify that it is an OK VOTable.

      }
       else
      {
         fail("Unexpected job status "+status);
      }
   }


   @Test
   public void testAbortJob(){
      Response createResponse = given()
            .formParam("query", "select * from TAP_SCHEMA.schemas")
            .redirects().follow(false)
            .when().post(ASYNC_ENDPOINT)
            .then()
            .statusCode(303)
            .header("Location", notNullValue())
            .extract().response();
      String jobFullUrl = createResponse.getHeader("Location");
      String jobId = jobFullUrl.substring(jobFullUrl.lastIndexOf('/') + 1);

      System.out.println(jobFullUrl);
      String jobUrl = ASYNC_ENDPOINT+"/"+jobId;
      //Verify Initial State (PENDING)
      given()
            .when().get(jobUrl)
            .then()
            .statusCode(200)
            .body("job.phase", equalTo("PENDING"));

      //  Start the Job (POST to /phase with PHASE=RUN)
      given()
            .contentType(ContentType.URLENC)
            .redirects().follow(false)
            .formParam("PHASE", "ABORT")
            .when()
            .post(jobUrl + "/phase")
            .then()
            .statusCode(303);

   }

   @Test
   public void testSyncQuery() {
      given()
            .formParam("query", "select * from TAP_SCHEMA.columns")
            .when().post(SYNC_ENDPOINT)
            .then()
            .log().body()
            .statusCode(200); //TODO validate the VOTable
   }
   @Test
   public void testErrorQuery() {
      given()
            .formParam("query", "select * from TAP_SCHEMA.column") //FIXME this is not returning a VOTable...
            .when().post(SYNC_ENDPOINT)
            .then()
            .log().body()
            .statusCode(200); //TODO validate the VOTable
   }


}
