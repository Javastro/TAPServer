package org.javastro.ivoa.tap;

import io.quarkus.test.junit.QuarkusTest;
import org.javastro.ivoa.schema.SchemaMap;
import org.javastro.ivoa.schema.XMLValidator;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;


import javax.xml.transform.stream.StreamSource;
import java.io.StringReader;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;

@QuarkusTest
class VOSIResourceTest {

    @Test
    void testCapabilitiesEndpoint() {
       String caps = given()
             .when().get("/VOSI/capabilities")
             .then()
             .statusCode(200)
             .body("capabilities.capability.size()",equalTo(4))
             .extract().body().asString();

             XMLValidator xmlValidator = new XMLValidator(SchemaMap.getAllSchemaAsSources());
             xmlValidator.validate(new StreamSource(new StringReader(caps)));
             if(!xmlValidator.wasValid())
             {

                xmlValidator.printErrors(System.err);
                System.err.println(caps);

             }
             Assertions.assertTrue(xmlValidator.wasValid() ,"Capabilities validation failed");
    }


   @Test
   void testAvailabilityEndpoint() {
      given()
            .when().get("/VOSI/availability")
            .then()
            .statusCode(200)
      ;
   }

   @Test
   void testTablesEndpoint() {
     String tables =  given()
            .when().get("/tables")
            .then()
            .statusCode(200)
            .log().body()
            .extract().body().asString();

      XMLValidator xmlValidator = new XMLValidator(SchemaMap.getAllSchemaAsSources());
      xmlValidator.validate(new StreamSource(new StringReader(tables)));
      if(!xmlValidator.wasValid())
      {

         xmlValidator.printErrors(System.err);

      }
      Assertions.assertTrue(xmlValidator.wasValid() ,"tables validation failed");

   }

}