package com.example.sb4jackson;

import com.example.sb4jackson.model.TestModelV2a;
import io.restassured.RestAssured;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import tools.jackson.databind.json.JsonMapper;

import java.util.Date;

import static io.restassured.RestAssured.given;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class TestApiControllerTest {
    private static String V2_JSON = """
                        {
                          "stringField": "hello-v2",
                          "integerField": 456,
                          "dateFiled": "2026-06-25 01:02:03"
                        }
                        """;
    private static String V3_JSON = """
                        {
                          "stringField": "hello-v3",
                          "integerField": 123,
                          "dateFiled": "2026-06-25 03:02:01"
                        }
                        """;

    @LocalServerPort
    private int port;

    @Test
    void annotateWithV3UsesIntegerFieldAndFormattedDate() {
        RestAssured.port = port;

        given()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .body(V3_JSON)
                .when()
                .post("/test-api/annotate-with-v3")
                .then()
                .statusCode(HttpStatus.OK.value())
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .body("stringField", org.hamcrest.Matchers.equalTo("hello-v3"))
                .body("integerField", org.hamcrest.Matchers.equalTo(123))
                .body("dateFiled", org.hamcrest.Matchers.equalTo("2026-06-25 03:02:01"));
    }

    @Test
    void annotateWithV3UsesIntegerFieldAndFormattedDate_a_model() {
        RestAssured.port = port;

        given()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .body(V3_JSON)
        .when()
                .post("/test-api/annotate-with-v3a")
        .then()
                .statusCode(HttpStatus.OK.value())
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .body("stringField", org.hamcrest.Matchers.equalTo("hello-v3"))
                .body("integerField", org.hamcrest.Matchers.equalTo(123))
                .body("dateFiled", org.hamcrest.Matchers.equalTo("2026-06-25 03:02:01"));
    }

    @Test
    void annotateWithV2UsesIntegerFieldAndFormattedDate() {
        RestAssured.port = port;

        given()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .body(V2_JSON)
        .when()
                .post("/test-api/annotate-with-v2")
        .then()
                .statusCode(HttpStatus.OK.value())
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .body("stringField", org.hamcrest.Matchers.equalTo("hello-v2"))
                .body("integerField", org.hamcrest.Matchers.equalTo(456))
                .body("dateFiled", org.hamcrest.Matchers.equalTo("2026-06-25 01:02:03"));
    }

    @Test
    void annotateWithV2UsesIntegerFieldAndFormattedDate_a_model_failed_dueToNoMessageConverterToParseDate() {
        RestAssured.port = port;

        given()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .body(V2_JSON)
                .when()
                .post("/test-api/annotate-with-v2a")
                .then()
                .statusCode(HttpStatus.BAD_REQUEST.value());
    }

    @Test
    void annotateWithV2UsesIntegerFieldAndFormattedDate_a_model_with_v2a_json() {
        RestAssured.port = port;

        TestModelV2a v2a = new TestModelV2a();
        v2a.setStringField("hello-v2");
        v2a.setIntField(456);
        v2a.setDateFiled(new Date());
        JsonMapper jsonMapper = new JsonMapper();
        String v2aJson = jsonMapper.writeValueAsString(v2a);
        System.out.println(v2aJson);
        String expectedDate = jsonMapper.writeValueAsString(v2a.getDateFiled());
        if (expectedDate.startsWith("\"")) {
            expectedDate = expectedDate.substring(1,  expectedDate.length() - 1);
        }

        given()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .body(v2aJson)
                .when()
                .post("/test-api/annotate-with-v2a")
                .then()
                .statusCode(HttpStatus.OK.value())
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .body("stringField", org.hamcrest.Matchers.equalTo("hello-v2"))
                .body("integerField", org.hamcrest.Matchers.equalTo(456))
                .body("dateFiled", org.hamcrest.Matchers.equalTo(expectedDate));
    }
}
