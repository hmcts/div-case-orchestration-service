package uk.gov.hmcts.reform.divorce.orchestration.functionaltest;

import com.github.tomakehurst.wiremock.junit.WireMockClassRule;
import com.github.tomakehurst.wiremock.matching.EqualToPattern;
import com.google.common.collect.ImmutableMap;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.PropertySource;
import org.springframework.http.HttpStatus;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import uk.gov.hmcts.reform.divorce.orchestration.OrchestrationServiceApplication;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CaseDetails;
import uk.gov.hmcts.reform.divorce.orchestration.util.CcdUtil;

import java.util.HashMap;
import java.util.Map;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.delete;
import static com.github.tomakehurst.wiremock.client.WireMock.deleteRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.equalToJson;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.verify;
import static org.hamcrest.CoreMatchers.containsString;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.HttpStatus.UNAUTHORIZED;
import static org.springframework.http.MediaType.APPLICATION_JSON_UTF8_VALUE;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.AOS_AWAITING_STATE;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.AWAITING_CONSIDERATION_GENERAL_APPLICATION;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.BEARER_AUTH_TOKEN;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.BEARER_AUTH_TOKEN_1;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_CODE;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_COURT;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_EMAIL;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_ERROR;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_LETTER_HOLDER_ID_CODE;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_PIN;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CO_RESPONDENT_LETTER_HOLDER_ID;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CO_RESP_EMAIL_ADDRESS;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CO_RESP_LINKED_TO_CASE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CO_RESP_LINKED_TO_CASE_DATE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D_8_DIVORCE_UNIT;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.LINK_RESPONDENT_GENERIC_EVENT_ID;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.RESPONDENT_EMAIL_ADDRESS;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.RESPONDENT_LETTER_HOLDER_ID;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.START_AOS_EVENT_ID;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.YES_VALUE;
import static uk.gov.hmcts.reform.divorce.orchestration.testutil.ObjectMapperTestUtil.convertObjectToJsonString;

@RunWith(SpringRunner.class)
@ContextConfiguration(classes = OrchestrationServiceApplication.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@PropertySource(value = "classpath:application.yml")
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public abstract class LinkRespondentIdamITest extends IdamTestSupport {
    private static final String RETRIEVE_AOS_CASE_CONTEXT_PATH = "/casemaintenance/version/1/retrieveAosCase";
    private static final String RETRIEVE_CASE_CONTEXT_PATH = "/casemaintenance/version/1/case/" + TEST_CASE_ID;
    private static final String API_URL = "/link-respondent/" + TEST_CASE_ID + "/" + TEST_PIN;
    private static final String LINK_RESPONDENT_CONTEXT_PATH = "/casemaintenance/version/1/link-respondent/"
        + TEST_CASE_ID + "/" + TEST_LETTER_HOLDER_ID_CODE;
    private static final String UNLINK_USER_CONTEXT_PATH = "/casemaintenance/version/1/link-respondent/"
        + TEST_CASE_ID;
    private static final String UPDATE_CONTEXT_PATH_AOS = String.format(
        "/casemaintenance/version/1/updateCase/%s/%s",
        TEST_CASE_ID,
        START_AOS_EVENT_ID
    );
    private static final String UPDATE_CONTEXT_PATH_NOT_AOS = String.format(
        "/casemaintenance/version/1/updateCase/%s/%s",
        TEST_CASE_ID,
        LINK_RESPONDENT_GENERIC_EVENT_ID
    );
    private static final Map<String, Object> CASE_DATA_RESPONDENT =
        ImmutableMap.of(
            D_8_DIVORCE_UNIT, TEST_COURT,
            RESPONDENT_LETTER_HOLDER_ID, TEST_LETTER_HOLDER_ID_CODE
        );
    private static final CaseDetails CASE_DETAILS_AOS =
        CaseDetails.builder()
            .caseId(TEST_CASE_ID)
            .state(AOS_AWAITING_STATE)
            .caseData(CASE_DATA_RESPONDENT)
            .build();
    private static final CaseDetails CASE_DETAILS_NO_AOS =
        CaseDetails.builder()
            .caseId(TEST_CASE_ID)
            .state(AWAITING_CONSIDERATION_GENERAL_APPLICATION)
            .caseData(CASE_DATA_RESPONDENT)
            .build();

    @Autowired
    private CcdUtil ccdUtil;

    @Autowired
    private MockMvc webClient;

    @ClassRule
    public static WireMockClassRule maintenanceServiceServer = new WireMockClassRule(4010);

    private Map<String, Object> caseDataAos;
    private Map<String, Object> caseDataNonAos;
    private Map<String, Object> caseDataCoRespondentUpdate;

    @Before
    public void setup() {

        caseDataAos = ImmutableMap.of(
            RESPONDENT_EMAIL_ADDRESS, TEST_EMAIL
        );
        caseDataNonAos = ImmutableMap.of(
            RESPONDENT_EMAIL_ADDRESS, TEST_EMAIL
        );
        caseDataCoRespondentUpdate = ImmutableMap.of(
            CO_RESP_EMAIL_ADDRESS, TEST_EMAIL,
            CO_RESP_LINKED_TO_CASE, YES_VALUE,
            CO_RESP_LINKED_TO_CASE_DATE, ccdUtil.getCurrentDateCcdFormat()
        );
    }

    @Test
    public void givenAuthTokenIsNull_whenLinkRespondent_thenReturnBadRequest() throws Exception {
        webClient.perform(MockMvcRequestBuilders.post(API_URL)
            .header(CONTENT_TYPE, APPLICATION_JSON_VALUE))
            .andExpect(status().isBadRequest());
    }

    @Test
    public void givenPinAuthFails_whenLinkRespondent_thenReturnBadRequest() throws Exception {
        stubPinAuthoriseEndpoint(UNAUTHORIZED, TEST_ERROR);

        webClient.perform(MockMvcRequestBuilders.post(API_URL)
            .header(AUTHORIZATION, AUTH_TOKEN)
            .header(CONTENT_TYPE, APPLICATION_JSON_VALUE))
            .andExpect(status().isUnauthorized());
    }

    @Test
    public void givenExchangeCodeFails_whenLinkRespondent_thenReturnBadRequest() throws Exception {
        stubPinAuthoriseEndpoint(OK, AUTHENTICATE_USER_RESPONSE_JSON);
        stubTokenExchangeEndpoint(BAD_REQUEST, TEST_CODE, TEST_ERROR);

        webClient.perform(MockMvcRequestBuilders.post(API_URL)
            .header(AUTHORIZATION, AUTH_TOKEN)
            .header(CONTENT_TYPE, APPLICATION_JSON_VALUE))
            .andExpect(status().isBadRequest())
            .andExpect(content().string(containsString(TEST_ERROR)));
    }

    @Test
    public void givenUserDetailsThrowException_whenLinkRespondent_thenReturnBadRequest() throws Exception {
        stubPinAuthoriseEndpoint(OK, AUTHENTICATE_USER_RESPONSE_JSON);
        stubTokenExchangeEndpoint(OK, TEST_CODE, TOKEN_EXCHANGE_RESPONSE_1_JSON);
        stubUserDetailsEndpoint(BAD_REQUEST, BEARER_AUTH_TOKEN_1, TEST_ERROR);

        webClient.perform(MockMvcRequestBuilders.post(API_URL)
            .header(AUTHORIZATION, AUTH_TOKEN)
            .header(CONTENT_TYPE, APPLICATION_JSON_VALUE))
            .andExpect(status().isBadRequest())
            .andExpect(content().string(containsString(TEST_ERROR)));
    }

    @Test
    public void givenUserDetailsReturnNull_whenLinkRespondent_thenReturnUnAuthorized() throws Exception {
        stubPinAuthoriseEndpoint(OK, AUTHENTICATE_USER_RESPONSE_JSON);
        stubTokenExchangeEndpoint(OK, TEST_CODE, TOKEN_EXCHANGE_RESPONSE_1_JSON);
        stubUserDetailsEndpoint(OK, BEARER_AUTH_TOKEN_1, null);

        webClient.perform(MockMvcRequestBuilders.post(API_URL)
            .header(AUTHORIZATION, AUTH_TOKEN)
            .header(CONTENT_TYPE, APPLICATION_JSON_VALUE))
            .andExpect(status().isUnauthorized());
    }

    @Test
    public void givenLinkFailsInCms_whenLinkRespondent_thenPropagateException() throws Exception {
        stubPinAuthoriseEndpoint(OK, AUTHENTICATE_USER_RESPONSE_JSON);
        stubTokenExchangeEndpoint(OK, TEST_CODE, TOKEN_EXCHANGE_RESPONSE_1_JSON);
        stubUserDetailsEndpoint(OK, BEARER_AUTH_TOKEN_1, USER_DETAILS_PIN_USER_JSON);
        stubMaintenanceServerEndpointForLinkRespondent(UNAUTHORIZED);

        webClient.perform(MockMvcRequestBuilders.post(API_URL)
            .header(AUTHORIZATION, AUTH_TOKEN)
            .header(CONTENT_TYPE, APPLICATION_JSON_VALUE))
            .andExpect(status().isUnauthorized());
    }

    @Test
    public void givenGetRespondentDetailsFails_whenLinkRespondent_thenPropagateException() throws Exception {
        stubPinAuthoriseEndpoint(OK, AUTHENTICATE_USER_RESPONSE_JSON);
        stubTokenExchangeEndpoint(OK, TEST_CODE, TOKEN_EXCHANGE_RESPONSE_1_JSON);
        stubUserDetailsEndpoint(OK, BEARER_AUTH_TOKEN_1, USER_DETAILS_PIN_USER_JSON);
        stubMaintenanceServerEndpointForLinkRespondent(OK);
        stubUserDetailsEndpoint(UNAUTHORIZED, BEARER_AUTH_TOKEN, TEST_ERROR);
        stubRetrieveCaseFromCMS(CASE_DETAILS_AOS);

        webClient.perform(MockMvcRequestBuilders.post(API_URL)
            .header(AUTHORIZATION, AUTH_TOKEN)
            .header(CONTENT_TYPE, APPLICATION_JSON_VALUE))
            .andExpect(status().isUnauthorized())
            .andExpect(content().string(containsString(TEST_ERROR)));
    }

    @Test
    public void givenUpdateRespondentDetailsFails_whenLinkRespondent_thenPropagateException() throws Exception {
        stubPinAuthoriseEndpoint(OK, AUTHENTICATE_USER_RESPONSE_JSON);
        stubTokenExchangeEndpoint(OK, TEST_CODE, TOKEN_EXCHANGE_RESPONSE_1_JSON);
        stubUserDetailsEndpoint(OK, BEARER_AUTH_TOKEN_1, USER_DETAILS_PIN_USER_JSON);
        stubMaintenanceServerEndpointForLinkRespondent(OK);
        stubUserDetailsEndpoint(OK, BEARER_AUTH_TOKEN, USER_DETAILS_JSON);
        stubSignInForCaseworker();

        stubMaintenanceServerEndpointForUpdateAos(BAD_REQUEST, TEST_ERROR);
        stubRetrieveCaseFromCMS(CASE_DETAILS_AOS);
        stubMaintenanceServerEndpointForRemoveUser(OK);
        stubRetrieveCaseByIdFromCMS(HttpStatus.OK, convertObjectToJsonString(CASE_DETAILS_AOS));

        webClient.perform(MockMvcRequestBuilders.post(API_URL)
            .header(AUTHORIZATION, AUTH_TOKEN)
            .header(CONTENT_TYPE, APPLICATION_JSON_VALUE))
            .andExpect(status().isBadRequest())
            .andExpect(content().string(containsString(TEST_ERROR)));
    }

    @Test
    public void givenAllGoesWell_whenLinkRespondent_thenProceedAsExpected() throws Exception {
        stubPinAuthoriseEndpoint(OK, AUTHENTICATE_USER_RESPONSE_JSON);
        stubTokenExchangeEndpoint(OK, TEST_CODE, TOKEN_EXCHANGE_RESPONSE_1_JSON);
        stubUserDetailsEndpoint(OK, BEARER_AUTH_TOKEN_1, USER_DETAILS_PIN_USER_JSON);
        stubMaintenanceServerEndpointForLinkRespondent(OK);
        stubUserDetailsEndpoint(OK, BEARER_AUTH_TOKEN, USER_DETAILS_JSON);
        stubMaintenanceServerEndpointForUpdateAos(OK, convertObjectToJsonString(caseDataAos));
        stubRetrieveCaseByIdFromCMS(OK, convertObjectToJsonString(CASE_DETAILS_AOS));
        stubSignInForCaseworker();

        webClient.perform(MockMvcRequestBuilders.post(API_URL)
            .header(AUTHORIZATION, AUTH_TOKEN)
            .header(CONTENT_TYPE, APPLICATION_JSON_VALUE))
            .andExpect(status().isOk());
    }

    @Test
    public void givenAllGoesWell_whenLinkCoRespondent_thenProceedAsExpected() throws Exception {
        Map<String, Object> caseData = new HashMap<>();
        caseData.put(CO_RESPONDENT_LETTER_HOLDER_ID, TEST_LETTER_HOLDER_ID_CODE);

        stubSignInForCaseworker();
        stubPinAuthoriseEndpoint(OK, AUTHENTICATE_USER_RESPONSE_JSON);
        stubTokenExchangeEndpoint(OK, TEST_CODE, TOKEN_EXCHANGE_RESPONSE_1_JSON);
        stubUserDetailsEndpoint(OK, BEARER_AUTH_TOKEN_1, USER_DETAILS_PIN_USER_JSON);

        CaseDetails caseDetailsCoResp = CaseDetails.builder()
            .caseId(TEST_CASE_ID)
            .state(AOS_AWAITING_STATE)
            .caseData(caseData)
            .build();
        stubRetrieveCaseByIdFromCMS(OK, convertObjectToJsonString(caseDetailsCoResp));
        stubMaintenanceServerEndpointForLinkRespondent(OK);
        stubUserDetailsEndpoint(OK, BEARER_AUTH_TOKEN, USER_DETAILS_JSON);
        String coRespondentCaseData = convertObjectToJsonString(caseDataCoRespondentUpdate);
        stubMaintenanceServerEndpointForUpdateNotAos(OK, coRespondentCaseData, coRespondentCaseData);

        webClient.perform(MockMvcRequestBuilders.post(API_URL)
            .header(AUTHORIZATION, AUTH_TOKEN)
            .header(CONTENT_TYPE, APPLICATION_JSON_VALUE))
            .andExpect(status().isOk());
    }

    @Test
    public void givenNoCaseFound_whenLinkRespondent_thenReturn404() throws Exception {
        stubPinAuthoriseEndpoint(OK, AUTHENTICATE_USER_RESPONSE_JSON);
        stubTokenExchangeEndpoint(OK, TEST_CODE, TOKEN_EXCHANGE_RESPONSE_1_JSON);
        stubUserDetailsEndpoint(OK, BEARER_AUTH_TOKEN_1, USER_DETAILS_PIN_USER_JSON);
        stubMaintenanceServerEndpointForLinkRespondent(OK);
        stubUserDetailsEndpoint(OK, BEARER_AUTH_TOKEN, USER_DETAILS_JSON);
        stubMaintenanceServerEndpointForUpdateAos(OK, convertObjectToJsonString(caseDataCoRespondentUpdate));
        stubRetrieveCaseByIdFromCMS(NOT_FOUND, convertObjectToJsonString(""));

        webClient.perform(MockMvcRequestBuilders.post(API_URL)
            .header(AUTHORIZATION, AUTH_TOKEN)
            .header(CONTENT_TYPE, APPLICATION_JSON_VALUE))
            .andExpect(status().isNotFound());
    }

    @Test
    public void givenLinkingFails_whenLinkRespondent_thenReturn404() throws Exception {
        stubPinAuthoriseEndpoint(OK, AUTHENTICATE_USER_RESPONSE_JSON);
        stubTokenExchangeEndpoint(OK, TEST_CODE, TOKEN_EXCHANGE_RESPONSE_1_JSON);
        stubUserDetailsEndpoint(OK, BEARER_AUTH_TOKEN_1, USER_DETAILS_PIN_USER_JSON);
        stubMaintenanceServerEndpointForLinkRespondent(NOT_FOUND);
        stubUserDetailsEndpoint(OK, BEARER_AUTH_TOKEN, USER_DETAILS_JSON);
        stubMaintenanceServerEndpointForUpdateAos(OK, convertObjectToJsonString(caseDataCoRespondentUpdate));
        stubRetrieveCaseFromCMS(CASE_DETAILS_AOS);

        webClient.perform(MockMvcRequestBuilders.post(API_URL)
            .header(AUTHORIZATION, AUTH_TOKEN)
            .header(CONTENT_TYPE, APPLICATION_JSON_VALUE))
            .andExpect(status().isNotFound());
    }

    @Test
    public void givenNonStandardLinking_whenLinkRespondent_thenProceedAsExpected() throws Exception {
        stubSignInForCaseworker();
        stubPinAuthoriseEndpoint(OK, AUTHENTICATE_USER_RESPONSE_JSON);
        stubTokenExchangeEndpoint(OK, TEST_CODE, TOKEN_EXCHANGE_RESPONSE_1_JSON);
        stubUserDetailsEndpoint(OK, BEARER_AUTH_TOKEN_1, USER_DETAILS_PIN_USER_JSON);
        stubMaintenanceServerEndpointForLinkRespondent(OK);
        stubUserDetailsEndpoint(OK, BEARER_AUTH_TOKEN, USER_DETAILS_JSON);
        stubMaintenanceServerEndpointForUpdateNotAos(OK, convertObjectToJsonString(caseDataNonAos));
        stubRetrieveCaseByIdFromCMS(OK, convertObjectToJsonString(CASE_DETAILS_NO_AOS));

        webClient.perform(MockMvcRequestBuilders.post(API_URL)
            .header(AUTHORIZATION, AUTH_TOKEN)
            .header(CONTENT_TYPE, APPLICATION_JSON_VALUE))
            .andExpect(status().isOk());
    }

    @Test
    public void givenErrorUpdatingRespondentDetails_whenLinkRespondent_thenNoChangesAreDone() throws Exception {
        stubPinAuthoriseEndpoint(OK, AUTHENTICATE_USER_RESPONSE_JSON);
        stubTokenExchangeEndpoint(OK, TEST_CODE, TOKEN_EXCHANGE_RESPONSE_1_JSON);
        stubUserDetailsEndpoint(OK, BEARER_AUTH_TOKEN_1, USER_DETAILS_PIN_USER_JSON);
        stubMaintenanceServerEndpointForLinkRespondent(OK);
        stubUserDetailsEndpoint(OK, BEARER_AUTH_TOKEN, USER_DETAILS_JSON);
        stubMaintenanceServerEndpointForUpdateNotAos(INTERNAL_SERVER_ERROR, convertObjectToJsonString(caseDataNonAos));
        stubRetrieveCaseFromCMS(CASE_DETAILS_NO_AOS);
        stubMaintenanceServerEndpointForRemoveUser(OK);

        webClient.perform(MockMvcRequestBuilders.post(API_URL)
            .header(AUTHORIZATION, AUTH_TOKEN)
            .header(CONTENT_TYPE, APPLICATION_JSON_VALUE))
            .andExpect(status().is5xxServerError());

        verify(deleteRequestedFor(urlEqualTo(UNLINK_USER_CONTEXT_PATH)));
    }

    private void stubMaintenanceServerEndpointForLinkRespondent(HttpStatus status) {
        maintenanceServiceServer.stubFor(post(LINK_RESPONDENT_CONTEXT_PATH)
            .withHeader(AUTHORIZATION, new EqualToPattern(AUTH_TOKEN))
            .withHeader(CONTENT_TYPE, new EqualToPattern(APPLICATION_JSON_VALUE))
            .willReturn(aResponse()
                .withStatus(status.value())
            ));
    }

    private void stubMaintenanceServerEndpointForUpdateAos(HttpStatus status, String response) {
        maintenanceServiceServer.stubFor(post(UPDATE_CONTEXT_PATH_AOS)
            .withRequestBody(equalToJson(convertObjectToJsonString(caseDataAos)))
            .withHeader(AUTHORIZATION, new EqualToPattern(AUTH_TOKEN))
            .willReturn(aResponse()
                .withStatus(status.value())
                .withHeader(CONTENT_TYPE, APPLICATION_JSON_UTF8_VALUE)
                .withBody(response)));
    }

    private void stubMaintenanceServerEndpointForUpdateNotAos(HttpStatus status, String request, String response) {
        maintenanceServiceServer.stubFor(post(urlEqualTo(UPDATE_CONTEXT_PATH_NOT_AOS))
            .withRequestBody(equalToJson(request))
            .withHeader(AUTHORIZATION, new EqualToPattern(AUTH_TOKEN))
            .willReturn(aResponse()
                .withStatus(status.value())
                .withHeader(CONTENT_TYPE, APPLICATION_JSON_UTF8_VALUE)
                .withBody(response)));
    }

    private void stubMaintenanceServerEndpointForUpdateNotAos(HttpStatus status, String response) {
        stubMaintenanceServerEndpointForUpdateNotAos(status, convertObjectToJsonString(caseDataNonAos), response);
    }

    private void stubMaintenanceServerEndpointForRemoveUser(HttpStatus status) {
        maintenanceServiceServer.stubFor(delete(urlEqualTo(UNLINK_USER_CONTEXT_PATH))
            .withHeader(AUTHORIZATION, new EqualToPattern(AUTH_TOKEN))
            .willReturn(aResponse()
                .withStatus(status.value())
                .withHeader(CONTENT_TYPE, APPLICATION_JSON_UTF8_VALUE)));
    }

    private void stubRetrieveCaseFromCMS(CaseDetails caseDetails) {
        stubRetrieveCaseFromCMS(OK, convertObjectToJsonString(caseDetails));
    }

    private void stubRetrieveCaseFromCMS(HttpStatus status, String message) {
        maintenanceServiceServer.stubFor(get(urlEqualTo(RETRIEVE_AOS_CASE_CONTEXT_PATH))
            .willReturn(aResponse()
                .withStatus(status.value())
                .withHeader(CONTENT_TYPE, APPLICATION_JSON_UTF8_VALUE)
                .withBody(message)));
    }

    private void stubRetrieveCaseByIdFromCMS(HttpStatus status, String message) {
        maintenanceServiceServer.stubFor(get(urlEqualTo(RETRIEVE_CASE_CONTEXT_PATH))
            .willReturn(aResponse()
                .withStatus(status.value())
                .withHeader(CONTENT_TYPE, APPLICATION_JSON_UTF8_VALUE)
                .withBody(message)));
    }
}
