package uk.gov.hmcts.reform.divorce.util;

import io.restassured.response.Response;
import net.serenitybdd.rest.SerenityRest;

import java.util.Collections;
import java.util.Map;

public class RestUtil {
    public static Response postToRestService(String url, Map<String, Object> headers, String requestBody) {
        return postToRestService(url, headers, requestBody, Collections.emptyMap());
    }

    public static Response putToRestService(String url, Map<String, Object> headers, String requestBody) {
        return putToRestService(url, headers, requestBody, Collections.emptyMap());
    }

    public static Response postToRestService(String url, Map<String, Object> headers, String requestBody,
                                             Map<String, Object> params) {
        if (requestBody != null) {
            return SerenityRest.given()
                .headers(headers)
                .queryParams(params)
                .body(requestBody)
                .when()
                .post(url)
                .andReturn();
        } else {
            return SerenityRest.given()
                .headers(headers)
                .queryParams(params)
                .when()
                .post(url)
                .andReturn();
        }
    }

    public static Response putToRestService(String url, Map<String, Object> headers, String requestBody,
                                             Map<String, Object> params) {
        if (requestBody != null) {
            return SerenityRest.given()
                    .headers(headers)
                    .queryParams(params)
                    .body(requestBody)
                    .when()
                    .put(url)
                    .andReturn();
        } else {
            return SerenityRest.given()
                    .headers(headers)
                    .queryParams(params)
                    .when()
                    .put(url)
                    .andReturn();
        }
    }

    public static Response getFromRestService(String url, Map<String, Object> headers, Map<String, Object> params) {
        if (params != null) {
            return SerenityRest.given()
                .headers(headers)
                .params(params)
                .when()
                .get(url)
                .andReturn();
        } else {
            return SerenityRest.given()
                .headers(headers)
                .when()
                .get(url)
                .andReturn();
        }
    }
}
