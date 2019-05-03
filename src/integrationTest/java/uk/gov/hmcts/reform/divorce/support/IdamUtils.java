package uk.gov.hmcts.reform.divorce.support;

import com.google.common.collect.ImmutableMap;
import io.restassured.config.HttpClientConfig;
import io.restassured.config.RestAssuredConfig;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import net.serenitybdd.rest.SerenityRest;
import org.apache.http.client.HttpClient;
import org.apache.http.entity.ContentType;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.client.DefaultHttpRequestRetryHandler;
import org.apache.http.params.CoreConnectionPNames;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import uk.gov.hmcts.reform.divorce.model.GeneratePinRequest;
import uk.gov.hmcts.reform.divorce.model.PinResponse;
import uk.gov.hmcts.reform.divorce.model.RegisterUserRequest;
import uk.gov.hmcts.reform.divorce.model.UserGroup;
import uk.gov.hmcts.reform.divorce.util.ResourceLoader;

import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.stream.Stream;

public class IdamUtils {

    @Value("${auth.idam.client.baseUrl}")
    private String idamUserBaseUrl;

    @Value("${auth.idam.client.redirectUri}")
    private String idamRedirectUri;

    @Value("${auth2.client.secret}")
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
            .body(ResourceLoader.objectToJson(generatePinRequest))
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

        getSerenityRetryableClient()
            .header("Content-Type", "application/json")
            .relaxedHTTPSValidation()
            .body(ResourceLoader.objectToJson(registerUserRequest))
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

    private HttpClientConfig.HttpClientFactory config() {
        return  new HttpClientConfig.HttpClientFactory() {

            @Override
            public HttpClient createHttpClient() {
                final DefaultHttpClient client = new DefaultHttpClient();
                client.setHttpRequestRetryHandler(new DefaultHttpRequestRetryHandler(3, true));
                return client;
            }
        };
    }

    private RequestSpecification  getSerenityRetryableClient() {
        return SerenityRest.given()
            .config(RestAssuredConfig.config()
                .httpClient(HttpClientConfig
                    .httpClientConfig()
                    .addParams(ImmutableMap.of(CoreConnectionPNames.CONNECTION_TIMEOUT, 500,
                        CoreConnectionPNames.SO_TIMEOUT, 500))
                    .httpClientFactory(config())));
    }

    public String generateUserTokenWithNoRoles(String username, String password) {
        String userLoginDetails = String.join(":", username, password);
        final String authHeader = "Basic " + new String(Base64.getEncoder().encode(userLoginDetails.getBytes()));

        Response response = getSerenityRetryableClient()
            .header("Authorization", authHeader)
            .header("Content-Type", MediaType.APPLICATION_FORM_URLENCODED_VALUE)
            .relaxedHTTPSValidation()
            .post(idamCodeUrl());


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
