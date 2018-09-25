package uk.gov.hmcts.reform.divorce.support;

import io.restassured.response.Response;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.Map;

import static net.serenitybdd.rest.SerenityRest.given;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class EvidenceManagementUtil {

    public static Response readDataFromEvidenceManagement(String uri, String serviceAuthToken, String userId) {
        Map<String, Object> headers = new HashMap<>();
        headers.put("ServiceAuthorization", serviceAuthToken);
        headers.put("user-id", userId);
        headers.put("user-roles", "caseworker-divorce.support");
        return given()
            .contentType("application/json")
            .headers(headers)
            .when()
            .get(uri)
            .andReturn();
    }
}
