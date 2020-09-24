package uk.gov.hmcts.reform.divorce.support;

import io.restassured.response.Response;
import lombok.extern.slf4j.Slf4j;
import net.serenitybdd.rest.SerenityRest;
import org.apache.http.entity.ContentType;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import uk.gov.hmcts.reform.divorce.model.GeneratePinRequest;
import uk.gov.hmcts.reform.divorce.model.PinResponse;
import uk.gov.hmcts.reform.divorce.model.RegisterUserRequest;
import uk.gov.hmcts.reform.divorce.model.UserDetails;
import uk.gov.hmcts.reform.divorce.model.UserGroup;

import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.stream.Stream;

@Slf4j
public class IdamUtils {

    @Value("${idam.client.baseUrl}")
    private String idamUserBaseUrl;

    @Value("${idam.s2s-auth.url}")
    private String idamS2sAuthUrl;

    @Value("${idam.client.redirectUri}")
    private String idamRedirectUri;

    @Value("${idam.client.secret}")
    private String idamSecret;

    public PinResponse generatePin(String firstName, String lastName, String authToken) {
        final GeneratePinRequest generatePinRequest =
            GeneratePinRequest.builder()
                .firstName(firstName)
                .lastName(lastName)
                .build();

        Response pinResponse =  SerenityRest.given()
            .header(HttpHeaders.AUTHORIZATION, authToken)
            .header(HttpHeaders.CONTENT_TYPE, ContentType.APPLICATION_JSON.toString())
            .body(generatePinRequest)
            .post(idamUserBaseUrl + "/pin")
            .andReturn();

        return PinResponse.builder()
            .pin(pinResponse.jsonPath().get("pin").toString())
            .userId(pinResponse.jsonPath().get("userId").toString())
            .build();
    }

    public void createUser(String username, String password, String userGroup, String... roles) {
        List<UserGroup> rolesList = new ArrayList<>();
        Stream.of(roles).forEach(role -> rolesList.add(UserGroup.builder().code(role).build()));
        UserGroup[] rolesArray = new UserGroup[roles.length];

        RegisterUserRequest registerUserRequest =
            RegisterUserRequest.builder()
                .email(username)
                .forename("Test")
                .surname("User")
                .password(password)
                .roles(rolesList.toArray(rolesArray))
                .userGroup(UserGroup.builder().code(userGroup).build())
                .build();

        SerenityRest.given()
            .header("Content-Type", "application/json")
            .relaxedHTTPSValidation()
            .body(registerUserRequest)
            .post(idamCreateUrl());
    }

    public String getUserId(String jwt) {
        Response response = SerenityRest.given()
            .header("Authorization", jwt)
            .relaxedHTTPSValidation()
            .get(idamUserBaseUrl + "/details");

        return response.getBody().path("id").toString();
    }

    public String getPin(final String letterHolderId) {
        return SerenityRest.given()
            .relaxedHTTPSValidation()
            .get(idamUserBaseUrl + "/testing-support/accounts/pin/" + letterHolderId)
            .getBody()
            .asString();
    }

    public void deleteUser(final UserDetails user) {
        Thread userDeletionThread = new Thread(() -> {
            log.info("Deleting user " + user.getEmailAddress());

            SerenityRest.given()
                .relaxedHTTPSValidation()
                .delete(idamDeleteUrl(user.getEmailAddress()));
        });
        userDeletionThread.setDaemon(true);
        userDeletionThread.start();
    }

    public String generateUserTokenWithNoRoles(String username, String password) {
        String userLoginDetails = String.join(":", username, password);
        final String authHeader = "Basic " + new String(Base64.getEncoder().encode(userLoginDetails.getBytes()));

        int retryCount = 0;
        Response response = null;
        do {
            response = SerenityRest.given()
                .header("Authorization", authHeader)
                .header("Content-Type", MediaType.APPLICATION_FORM_URLENCODED_VALUE)
                .relaxedHTTPSValidation()
                .post(idamCodeUrl());
            retryCount++;
        }
        while (response.getStatusCode() > 300 && retryCount <= 3);

        if (response.getStatusCode() >= 300) {
            throw new IllegalStateException("Token generation failed with code: " + response.getStatusCode()
                + " body: " + response.getBody().prettyPrint());
        }

        response = SerenityRest.given()
            .header("Content-Type", MediaType.APPLICATION_FORM_URLENCODED_VALUE)
            .relaxedHTTPSValidation()
            .post(idamTokenUrl(response.getBody().path("code")));

        assert response.getStatusCode() == 200 : "Error generating code from IDAM: " + response.getStatusCode();

        String token = response.getBody().path("access_token");
        return "Bearer " + token;
    }

    public String generateUserTokenWithValidMicroService(String microServiceName) {

        Response response = SerenityRest.given()
            .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
            .relaxedHTTPSValidation()
            .body(String.format("{\"microservice\": \"%s\"}", microServiceName))
            .post(idamS2sAuthUrl + "/testing-support/lease");

        if (response.getStatusCode() >= 300) {
            throw new IllegalStateException("Token generation failed with code: " + response.getStatusCode()
                + " body: " + response.getBody().prettyPrint());
        }

        assert response.getStatusCode() == 200 : "Error generating code from IDAM: " + response.getStatusCode();
        String token = response.getBody().asString();
        return "Bearer " + token;
    }

    private String idamDeleteUrl(String accountEmail) {
        return idamUserBaseUrl + "/testing-support/accounts/" + accountEmail;
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
