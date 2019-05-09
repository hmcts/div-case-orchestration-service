package uk.gov.hmcts.reform.divorce.orchestration.functionaltest;

import com.github.tomakehurst.wiremock.junit.WireMockClassRule;
import com.github.tomakehurst.wiremock.matching.EqualToPattern;
import org.junit.ClassRule;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.idam.AuthenticateUserResponse;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.idam.Pin;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.idam.PinRequest;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.idam.TokenExchangeResponse;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.idam.UserDetails;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalToJson;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.AUTH_TOKEN_1;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_CODE;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_EMAIL;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_LETTER_HOLDER_ID_CODE;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_PIN;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_USER_ID;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.BASIC;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.LOCATION_HEADER;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.PIN_PREFIX;
import static uk.gov.hmcts.reform.divorce.orchestration.testutil.ObjectMapperTestUtil.convertObjectToJsonString;

public class IdamTestSupport {
    private static final String IDAM_PIN_DETAILS_CONTEXT_PATH = "/pin";
    private static final String IDAM_AUTHORIZE_CONTEXT_PATH = "/oauth2/authorize";
    private static final String IDAM_EXCHANGE_CODE_CONTEXT_PATH = "/oauth2/token";
    private static final String IDAM_USER_DETAILS_CONTEXT_PATH = "/details";
    private static final String CODE = "code";
    private static final String AUTHORIZATION_CODE = "authorization_code";
    private static final String PIN_AUTHORIZATION =
        PIN_PREFIX + new String(Base64.getEncoder().encode(TEST_PIN.getBytes()));
    private static final String PIN_AUTH_URL_WITH_REDIRECT = "http://www.redirect.url?code=" + TEST_CODE;


    private static final AuthenticateUserResponse AUTHENTICATE_USER_RESPONSE =
        AuthenticateUserResponse.builder()
            .code(TEST_CODE)
            .build();

    static final String AUTHENTICATE_USER_RESPONSE_JSON = convertObjectToJsonString(AUTHENTICATE_USER_RESPONSE);

    private static final UserDetails USER_DETAILS_PIN_USER =
        UserDetails.builder().id(TEST_LETTER_HOLDER_ID_CODE).build();

    static final String USER_DETAILS_PIN_USER_JSON = convertObjectToJsonString(USER_DETAILS_PIN_USER);

    private static final UserDetails USER_DETAILS =
        UserDetails.builder().id(TEST_USER_ID).email(TEST_EMAIL).build();

    static final String USER_DETAILS_JSON = convertObjectToJsonString(USER_DETAILS);

    private static final TokenExchangeResponse TOKEN_EXCHANGE_RESPONSE =
        TokenExchangeResponse.builder()
            .accessToken(AUTH_TOKEN_1)
            .build();

    static final String TOKEN_EXCHANGE_RESPONSE_1_JSON = convertObjectToJsonString(TOKEN_EXCHANGE_RESPONSE);

    @Value("${idam.api.redirect-url}")
    private String authRedirectUrl;

    @Value("${auth2.client.id}")
    private String authClientId;

    @Value("${auth2.client.secret}")
    private String authClientSecret;

    @Value("${idam.citizen.username}")
    private String citizenUserName;

    @Value("${idam.citizen.password}")
    private String citizenPassword;

    @Value("${idam.caseworker.username}")
    private String caseworkerUserName;

    @Value("${idam.caseworker.password}")
    private String caseworkerPassword;

    @Value("${idam.strategic.enabled}")
    private boolean sidamEnabled;

    @ClassRule
    public static WireMockClassRule idamServer = new WireMockClassRule(4503);

    void stubUserDetailsEndpoint(HttpStatus status, String authHeader, String message) {
        idamServer.stubFor(get(IDAM_USER_DETAILS_CONTEXT_PATH)
            .withHeader(AUTHORIZATION, new EqualToPattern(authHeader))
            .willReturn(aResponse()
                .withStatus(status.value())
                .withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
                .withBody(message)));
    }

    void stubPinDetailsEndpoint(String authHeader, PinRequest pinRequest, Pin response) {
        idamServer.stubFor(post(IDAM_PIN_DETAILS_CONTEXT_PATH)
            .withHeader(AUTHORIZATION, new EqualToPattern(authHeader))
            .withHeader(CONTENT_TYPE, new EqualToPattern(APPLICATION_JSON_VALUE))
            .withRequestBody(equalToJson(convertObjectToJsonString(pinRequest)))
            .willReturn(aResponse()
                .withStatus(HttpStatus.OK.value())
                .withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
                .withBody(convertObjectToJsonString(response))));
    }

    void stubSignIn() {
        try {
            stubAuthoriseEndpoint(getBasicAuthHeader(citizenUserName, citizenPassword),
                convertObjectToJsonString(AUTHENTICATE_USER_RESPONSE));

            stubTokenExchangeEndpoint(HttpStatus.OK, TEST_CODE, convertObjectToJsonString(TOKEN_EXCHANGE_RESPONSE));

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    void stubSignInForCaseworker() {
        try {
            stubAuthoriseEndpoint(getBasicAuthHeader(caseworkerUserName, caseworkerPassword),
                    convertObjectToJsonString(AUTHENTICATE_USER_RESPONSE));

            stubTokenExchangeEndpoint(HttpStatus.OK, TEST_CODE, convertObjectToJsonString(TOKEN_EXCHANGE_RESPONSE));

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void stubAuthoriseEndpoint(String authorisation, String responseBody)
        throws UnsupportedEncodingException {
        idamServer.stubFor(post(IDAM_AUTHORIZE_CONTEXT_PATH
            + "?response_type=" + CODE
            + "&client_id=" + authClientId
            + "&redirect_uri=" + URLEncoder.encode(authRedirectUrl, StandardCharsets.UTF_8.name()))
            .withHeader(AUTHORIZATION, new EqualToPattern(authorisation))
            .withHeader(CONTENT_TYPE, new EqualToPattern(MediaType.APPLICATION_FORM_URLENCODED_VALUE))
            .willReturn(aResponse()
                .withStatus(HttpStatus.OK.value())
                .withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
                .withBody(responseBody)));
    }

    void stubPinAuthoriseEndpoint(HttpStatus status, String responseBody)
        throws UnsupportedEncodingException {

        if (sidamEnabled) {
            idamServer.stubFor(get(IDAM_PIN_DETAILS_CONTEXT_PATH
                    + "?client_id=" + authClientId
                    + "&redirect_uri=" + URLEncoder.encode(authRedirectUrl, StandardCharsets.UTF_8.name()))
                .withHeader("pin", new EqualToPattern(TEST_PIN))
                .willReturn(aResponse()
                    .withStatus(status.value())
                    .withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
                    .withHeader(LOCATION_HEADER, PIN_AUTH_URL_WITH_REDIRECT)
                    .withBody(responseBody)));
        } else {
            idamServer.stubFor(post(IDAM_AUTHORIZE_CONTEXT_PATH
                + "?response_type=" + CODE
                + "&client_id=" + authClientId
                + "&redirect_uri=" + URLEncoder.encode(authRedirectUrl, StandardCharsets.UTF_8.name()))
                .withHeader(AUTHORIZATION, new EqualToPattern(PIN_AUTHORIZATION))
                .willReturn(aResponse()
                    .withStatus(status.value())
                    .withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
                    .withBody(responseBody)));
        }
    }

    void stubTokenExchangeEndpoint(HttpStatus status, String authCode, String responseBody)
        throws UnsupportedEncodingException {
        idamServer.stubFor(post(IDAM_EXCHANGE_CODE_CONTEXT_PATH
            + "?code=" + authCode
            + "&grant_type=" + AUTHORIZATION_CODE
            + "&redirect_uri=" + URLEncoder.encode(authRedirectUrl, StandardCharsets.UTF_8.name())
            + "&client_id=" + authClientId
            + "&client_secret=" + authClientSecret)
            .withHeader(CONTENT_TYPE, new EqualToPattern(MediaType.APPLICATION_FORM_URLENCODED_VALUE))
            .willReturn(aResponse()
                .withStatus(status.value())
                .withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
                .withBody(responseBody)));
    }

    private String getBasicAuthHeader(String username, String password) {
        String authorisation = username + ":" + password;
        return BASIC + Base64.getEncoder().encodeToString(authorisation.getBytes());
    }
}
