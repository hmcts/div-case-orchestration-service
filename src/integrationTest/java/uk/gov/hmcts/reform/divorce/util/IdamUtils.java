package uk.gov.hmcts.reform.divorce.util;

import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.springframework.beans.factory.annotation.Value;
import uk.gov.hmcts.reform.divorce.model.RegisterUserRequest;
import uk.gov.hmcts.reform.divorce.model.UserDetails;
import uk.gov.hmcts.reform.divorce.model.UserGroup;

import java.util.Base64;

public class IdamUtils {

    @Value("${auth.idam.client.baseUrl}")
    private String idamUserBaseUrl;

    @Value("${auth.idam.client.redirectUri}")
    private String idamRedirectUri;

    @Value("${auth.idam.client.secret}")
    private String idamSecret;


    public void createUser(String username, String password, String... roles) {
        RegisterUserRequest registerUserRequest =
            RegisterUserRequest.builder()
                .email(username)
                .forename("test")
                .surname("test")
                .password(password)
                .roles(roles)
                .userGroup(UserGroup.builder().code("caseworker").build())
            .build();

        RestAssured.given()
                .header("Content-Type", "application/json")
                .relaxedHTTPSValidation()
                .body(ResourceLoader.objectToJson(registerUserRequest))
                .post(idamCreateUrl());
    }

    public String getUserId(String jwt) {
        Response response = RestAssured.given()
                .header("Authorization", jwt)
                .relaxedHTTPSValidation()
                .get(idamUserBaseUrl + "/details");

        return response.getBody().path("id").toString();
    }

    public String generateUserTokenWithNoRoles(String username, String password) {
        String userLoginDetails = String.join(":", username, password);
        final String authHeader = "Basic " + new String(Base64.getEncoder().encode((userLoginDetails).getBytes()));

        Response response = RestAssured.given()
                .header("Authorization", authHeader)
                .relaxedHTTPSValidation()
                .post(idamCodeUrl());

        if (response.getStatusCode() >= 300) {
            throw new IllegalStateException("Token generation failed with code: " + response.getStatusCode()
                + " body: " + response.getBody().prettyPrint());
        }

        response = RestAssured.given()
                .relaxedHTTPSValidation()
                .post(idamTokenUrl(response.getBody().path("code")));

        String token = response.getBody().path("access_token");
        return "Bearer " + token;
    }

    private String idamCreateUrl() {
        return idamUserBaseUrl + "/testing-support/accounts";
    }

    private String idamCodeUrl() {
        return idamUserBaseUrl + "/oauth2/authorize"
                + "?response_type=code"
                + "&client_id=divorce"
                + "&redirect_uri=" + idamRedirectUri;
    }

    private String idamTokenUrl(String code) {
        return idamUserBaseUrl + "/oauth2/token"
                + "?code=" + code
                + "&client_id=divorce"
                + "&client_secret=" + idamSecret
                + "&redirect_uri=" + idamRedirectUri
                + "&grant_type=authorization_code";
    }
}
