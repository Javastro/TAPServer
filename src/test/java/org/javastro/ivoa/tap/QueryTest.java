/*
 * Copyright (c) 2026. Paul Harrison, University of Manchester.
 *
 */

package org.javastro.ivoa.tap;


import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;
import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.fail;


/*
 * Created on 05/03/2026 by Paul Harrison (paul.harrison@manchester.ac.uk).
 */
@QuarkusTest
public class QueryTest extends AbstractTAPTest {
   private static final String ASYNC_ENDPOINT = "/async";
   private static final String SYNC_ENDPOINT = "/sync";
   @Test
   public void testAsyncQuery() {
      Response createResponse = given()
            .formParam("QUERY", "select * from TAP_SCHEMA.tables")
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

      //Do some job management operations (e.g. query the job phase, query the job parameters, etc.)
      given()
            .when().get(jobUrl + "/phase")
            .then()
            .statusCode(200)
            .body(comparesEqualTo("PENDING"));
      given()
            .when().get(jobUrl + "/parameters")
            .then()
            .statusCode(200)
            .body("parameters.parameter.size()", greaterThan(0))
            .body("parameters.parameter.find { it.@id == 'QUERY' }", equalTo("select * from TAP_SCHEMA.tables"))
      ;

      given()
            .when().get(jobUrl + "/executionduration")
            .then()
            .statusCode(200)
            .body(comparesEqualTo("0")); //IMPL have to do string comparison?
// TODO add more tests when all of UWS is implemented.
//      given()
//            .when().get(jobUrl + "/destruction")
//            .then()
//            .statusCode(200)
//            .log().body();

      startAndTestJob(jobUrl);
   }


   @Test
   public void testAbortJob(){
      Response createResponse = given()
            .formParam("QUERY", "select * from TAP_SCHEMA.schemas")
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
            .formParam("QUERY", "select * from TAP_SCHEMA.columns")
            .when().post(SYNC_ENDPOINT)
            .then()
            .log().body()
            .statusCode(200); //TODO validate the VOTable
   }

   @Test
   public void testErrorQuery() {
      given()
            .formParam("QUERY", "select * from TAP_SCHEMA.column") //FIXME this is not returning a VOTable...
            .when().post(SYNC_ENDPOINT)
            .then()
            .log().body()
            .statusCode(200); //TODO validate the VOTable
   }
}
