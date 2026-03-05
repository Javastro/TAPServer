package org.javastro.ivoa.tap;

import io.quarkus.test.junit.QuarkusTest;
import org.javastro.ivoa.schema.XMLValidator;
import org.junit.jupiter.api.Test;


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
             .body("capabilities.capability.size()",equalTo(3))
             .extract().body().asString();
       //FIXME do validation once the XMLValidator has been extended to allow non-registry schema

//             XMLValidator xmlValidator = new XMLValidator(SchemaMap.getAllSchemaAsSources());
//             xmlValidator.validate(new StreamSource(new StringReader(caps)));
//             if(!xmlValidator.wasValid())
//             {
//
//                xmlValidator.printErrors(System.err);
//                System.err.println(caps);
//
//             }
//             Assertions.assertTrue(xmlValidator.wasValid() ,"Capabilities validation failed");
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
      given()
            .when().get("/tables")
            .then()
            .statusCode(200)
      ;//TODO test return
   }

}