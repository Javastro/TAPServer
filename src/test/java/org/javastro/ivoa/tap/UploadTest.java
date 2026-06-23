package org.javastro.ivoa.tap;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.net.URISyntaxException;
import java.util.Objects;

import static io.restassured.RestAssured.given;

@QuarkusTest
public class UploadTest {

    //Location of a simple and valid VOTable file
    static final String REMOTE_FILE_URL = "https://gist.githubusercontent.com/slloyd-src/7300663888d7e78994eab792ac232253/raw/4eb09a722c9afb6e72e0898d545283941ddd5881/gistfile1.txt";

    //Test a local file upload
    @Test
    public void testResourceUpload() {
        try {
            File votable = getTestFile();

            given()
                    .multiPart("QUERY", "SELECT * FROM mytable")
                    .multiPart("UPLOAD", "mytable,param:mytableFile")
                    .multiPart("mytableFile", votable, "application/x-votable+xml")
                    .when()
                    .post("/sync")
                    .then()
                    .statusCode(200);
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
    }

    //Test a remote file upload using POST
    @Test
    public void testPostRemoteUpload() {
        given()
                .multiPart("QUERY", "SELECT * FROM mytable")
                .multiPart("UPLOAD", "mytable," + REMOTE_FILE_URL)
                .when()
                .post("/sync")
                .then()
                .statusCode(200);
    }

    //Test a remote file upload using GET
    @Test
    public void testGetRemoteUpload() {
        given().get("/sync?QUERY=select%20*%20from%20table3&UPLOAD=table3," + REMOTE_FILE_URL)
                .then()
                .statusCode(200);
    }

    //Get the test file from the resources folder
    private File getTestFile() throws URISyntaxException {
        return new File(
                Objects.requireNonNull(getClass()
                                .getResource("/uploads/example-votable.vot"))
                        .toURI()
        );
    }
}
