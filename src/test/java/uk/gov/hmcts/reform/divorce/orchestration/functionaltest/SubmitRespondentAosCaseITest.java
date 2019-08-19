package uk.gov.hmcts.reform.divorce.orchestration.functionaltest;

import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.matching.EqualToPattern;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import uk.gov.hmcts.reform.divorce.orchestration.util.CcdUtil;

import java.util.HashMap;
import java.util.Map;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalToJson;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static java.util.Collections.emptyMap;
import static java.util.Collections.singletonMap;
import static org.hamcrest.CoreMatchers.containsString;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.MediaType.APPLICATION_JSON_UTF8_VALUE;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_ERROR;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.ADULTERY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.AOS_NOMINATE_SOLICITOR;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.AWAITING_ANSWER_AOS_EVENT_ID;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.AWAITING_DN_AOS_EVENT_ID;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CCD_CASE_DATA_FIELD;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.COMPLETED_AOS_EVENT_ID;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D_8_REASON_FOR_DIVORCE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NO_VALUE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.RECEIVED_AOS_FROM_RESP;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.RECEIVED_AOS_FROM_RESP_DATE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.RESP_ADMIT_OR_CONSENT_TO_FACT;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.RESP_WILL_DEFEND_DIVORCE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.SEPARATION_2YRS;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.YES_VALUE;
import static uk.gov.hmcts.reform.divorce.orchestration.testutil.ObjectMapperTestUtil.convertObjectToJsonString;

@RunWith(SpringRunner.class)
public class SubmitRespondentAosCaseITest extends MockedFunctionalTest {
    private static final String API_URL = String.format("/submit-aos/%s", TEST_CASE_ID);

    private static final String FORMAT_TO_AOS_CASE_CONTEXT_PATH = "/caseformatter/version/1/to-aos-submit-format";
    private static final String UPDATE_CONTEXT_PATH = "/casemaintenance/version/1/updateCase/" + TEST_CASE_ID + "/";
    private static final String RETRIEVE_CASE_CONTEXT_PATH = String.format(
        "/casemaintenance/version/1/case/%s",
        TEST_CASE_ID
    );

    private static final String FIXED_DATE = "2019-05-11";

    @Autowired
    private MockMvc webClient;

    @MockBean
    private CcdUtil ccdUtil;

    @Before
    public void setup() {
        when(ccdUtil.getCurrentDateCcdFormat()).thenReturn(FIXED_DATE);
    }

    @Test
    public void givenNoAuthToken_whenSubmitAos_thenReturnBadRequest() throws Exception {
        webClient.perform(MockMvcRequestBuilders.post(API_URL)
            .content(convertObjectToJsonString(buildRespondentResponse(YES_VALUE, true)))
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isBadRequest());
    }

    @Test
    public void givenNoPayload_whenSubmitAos_thenReturnBadRequest() throws Exception {
        webClient.perform(MockMvcRequestBuilders.post(API_URL)
            .header(AUTHORIZATION, AUTH_TOKEN)
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isBadRequest());
    }

    @Test
    public void givenCaseFormatterFails_whenSubmitAos_thenPropagateTheException() throws Exception {
        final Map<String, Object> caseData = buildRespondentResponse(YES_VALUE, true);

        stubFormatterServerEndpoint(BAD_REQUEST, caseData, TEST_ERROR);

        webClient.perform(MockMvcRequestBuilders.post(API_URL)
            .header(AUTHORIZATION, AUTH_TOKEN)
            .content(convertObjectToJsonString(caseData))
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isBadRequest())
            .andExpect(content().string(containsString(TEST_ERROR)));
    }

    @Test
    public void givenCaseRetrievalFails_whenSubmitAos_thenPropagateTheException() throws Exception {
        final Map<String, Object> caseData = buildRespondentResponse(YES_VALUE, false);

        stubFormatterServerEndpoint(OK, caseData, convertObjectToJsonString(caseData));

        stubMaintenanceServerEndpointForRetrieveCaseById(NOT_FOUND, emptyMap());

        webClient.perform(MockMvcRequestBuilders.post(API_URL)
            .header(AUTHORIZATION, AUTH_TOKEN)
            .content(convertObjectToJsonString(caseData))
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNotFound());
    }

    @Test
    public void givenCaseUpdateFails_whenSubmitAos_thenPropagateTheException() throws Exception {
        final Map<String, Object> caseData = buildRespondentResponse(YES_VALUE, false);

        stubFormatterServerEndpoint(OK, caseData, convertObjectToJsonString(caseData));

        final Map<String, Object> existingCaseData = new HashMap<>();
        existingCaseData.put(CCD_CASE_DATA_FIELD, emptyMap());
        stubMaintenanceServerEndpointForRetrieveCaseById(OK, existingCaseData);

        stubMaintenanceServerEndpointForUpdate(BAD_REQUEST, AWAITING_DN_AOS_EVENT_ID, caseData, TEST_ERROR);

        webClient.perform(MockMvcRequestBuilders.post(API_URL)
            .header(AUTHORIZATION, AUTH_TOKEN)
            .content(convertObjectToJsonString(caseData))
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isBadRequest())
            .andExpect(content().string(containsString(TEST_ERROR)));
    }

    @Test
    public void givenConsentAndDefend_whenSubmitAos_thenProceedAsExpected() throws Exception {
        final Map<String, Object> caseData = buildRespondentResponse(YES_VALUE, true);
        final String caseDataString = convertObjectToJsonString(caseData);

        stubFormatterServerEndpoint(OK, caseData, caseDataString);

        final Map<String, Object> existingCaseData = new HashMap<>();
        existingCaseData.put(CCD_CASE_DATA_FIELD, emptyMap());
        stubMaintenanceServerEndpointForRetrieveCaseById(OK, existingCaseData);

        stubMaintenanceServerEndpointForUpdate(OK, AWAITING_ANSWER_AOS_EVENT_ID, caseData, caseDataString);

        webClient.perform(MockMvcRequestBuilders.post(API_URL)
            .header(AUTHORIZATION, AUTH_TOKEN)
            .content(convertObjectToJsonString(caseData))
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content().json(caseDataString));
    }

    @Test
    public void givenNoConsentAndDefend_whenSubmitAos_thenProceedAsExpected() throws Exception {
        final Map<String, Object> caseData = buildRespondentResponse(NO_VALUE, true);

        final String caseDataString = convertObjectToJsonString(caseData);

        stubFormatterServerEndpoint(OK, caseData, caseDataString);

        final Map<String, Object> existingCaseData = new HashMap<>();
        existingCaseData.put(CCD_CASE_DATA_FIELD, emptyMap());

        stubMaintenanceServerEndpointForRetrieveCaseById(OK, existingCaseData);

        stubMaintenanceServerEndpointForUpdate(OK, AWAITING_ANSWER_AOS_EVENT_ID, caseData, caseDataString);

        webClient.perform(MockMvcRequestBuilders.post(API_URL)
            .header(AUTHORIZATION, AUTH_TOKEN)
            .content(convertObjectToJsonString(caseData))
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content().json(caseDataString));
    }

    @Test
    public void givenAdulteryNoConsentAndNoDefend_whenSubmitAos_thenProceedAsExpected() throws Exception {
        final Map<String, Object> caseData = buildRespondentResponse(NO_VALUE, false);
        caseData.put(RECEIVED_AOS_FROM_RESP, YES_VALUE);
        caseData.put(RECEIVED_AOS_FROM_RESP_DATE, FIXED_DATE);

        final String caseDataString = convertObjectToJsonString(caseData);

        stubFormatterServerEndpoint(OK, caseData, caseDataString);

        final Map<String, Object> existingCaseData = new HashMap<>();
        existingCaseData.put(CCD_CASE_DATA_FIELD, singletonMap(D_8_REASON_FOR_DIVORCE, ADULTERY));
        stubMaintenanceServerEndpointForRetrieveCaseById(OK, existingCaseData);

        stubMaintenanceServerEndpointForUpdate(OK, COMPLETED_AOS_EVENT_ID, caseData, caseDataString);

        webClient.perform(MockMvcRequestBuilders.post(API_URL)
            .header(AUTHORIZATION, AUTH_TOKEN)
            .content(convertObjectToJsonString(caseData))
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content().json(caseDataString));
    }

    @Test
    public void given2YearSeparationNoConsentAndNoDefend_whenSubmitAos_thenProceedAsExpected() throws Exception {
        final Map<String, Object> caseData = buildRespondentResponse(NO_VALUE, false);
        final String caseDataString = convertObjectToJsonString(caseData);

        final Map<String, Object> existingCaseData = new HashMap<>();
        existingCaseData.put(CCD_CASE_DATA_FIELD, singletonMap(D_8_REASON_FOR_DIVORCE, SEPARATION_2YRS));
        stubMaintenanceServerEndpointForRetrieveCaseById(OK, existingCaseData);

        stubFormatterServerEndpoint(OK, caseData, caseDataString);
        stubMaintenanceServerEndpointForUpdate(OK, COMPLETED_AOS_EVENT_ID, caseData, caseDataString);

        webClient.perform(MockMvcRequestBuilders.post(API_URL)
            .header(AUTHORIZATION, AUTH_TOKEN)
            .content(convertObjectToJsonString(caseData))
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content().json(caseDataString));
    }

    @Test
    public void givenConsentAndNoDefend_whenSubmitAos_thenProceedAsExpected() throws Exception {
        final Map<String, Object> caseData = buildRespondentResponse(YES_VALUE, false);
        final String caseDataString = convertObjectToJsonString(caseData);

        stubFormatterServerEndpoint(OK, caseData, caseDataString);
        stubMaintenanceServerEndpointForUpdate(OK, AWAITING_DN_AOS_EVENT_ID, caseData, caseDataString);

        webClient.perform(MockMvcRequestBuilders.post(API_URL)
            .header(AUTHORIZATION, AUTH_TOKEN)
            .content(convertObjectToJsonString(caseData))
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content().json(caseDataString));
    }

    @Test
    public void givenRespondentSolicitorRepresented_whenSubmitAos_thenProceedAsExpected() throws Exception {
        final Map<String, Object> caseData = buildSolicitorRepresentationResponse();
        final String caseDataString = convertObjectToJsonString(caseData);

        stubFormatterServerEndpoint(OK, caseData, caseDataString);
        stubMaintenanceServerEndpointForUpdate(OK, AOS_NOMINATE_SOLICITOR, caseData, caseDataString);

        webClient.perform(MockMvcRequestBuilders.post(API_URL)
                .header(AUTHORIZATION, AUTH_TOKEN)
                .content(convertObjectToJsonString(caseData))
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().json(caseDataString));
    }

    private void stubFormatterServerEndpoint(HttpStatus status, Map<String, Object> caseData, String response) {
        formatterServiceServer.stubFor(post(FORMAT_TO_AOS_CASE_CONTEXT_PATH)
            .withRequestBody(equalToJson(convertObjectToJsonString(caseData)))
            .willReturn(aResponse()
                .withStatus(status.value())
                .withHeader(CONTENT_TYPE, APPLICATION_JSON_UTF8_VALUE)
                .withBody(response)));
    }

    private void stubMaintenanceServerEndpointForUpdate(HttpStatus status, String caseEventId,
                                                        Map<String, Object> caseData, String response) {
        maintenanceServiceServer.stubFor(post(UPDATE_CONTEXT_PATH + caseEventId)
            .withRequestBody(equalToJson(convertObjectToJsonString(caseData)))
            .withHeader(AUTHORIZATION, new EqualToPattern(AUTH_TOKEN))
            .willReturn(aResponse()
                .withStatus(status.value())
                .withHeader(CONTENT_TYPE, APPLICATION_JSON_UTF8_VALUE)
                .withBody(response)));
    }

    private void stubMaintenanceServerEndpointForRetrieveCaseById(HttpStatus status, Map<String, Object> cmsData) {
        maintenanceServiceServer.stubFor(WireMock.get(RETRIEVE_CASE_CONTEXT_PATH)
            .withHeader(AUTHORIZATION, new EqualToPattern(AUTH_TOKEN))
            .willReturn(aResponse()
                .withStatus(status.value())
                .withHeader(CONTENT_TYPE, APPLICATION_JSON_UTF8_VALUE)
                .withBody(convertObjectToJsonString(cmsData))));
    }

    private Map<String, Object> buildRespondentResponse(String consent, boolean defended) {
        Map<String, Object> caseData = new HashMap<>();
        caseData.put(RESP_ADMIT_OR_CONSENT_TO_FACT, consent);

        if (defended) {
            caseData.put(RESP_WILL_DEFEND_DIVORCE, YES_VALUE);
        } else {
            caseData.put(RESP_WILL_DEFEND_DIVORCE, NO_VALUE);
        }
        caseData.put(RECEIVED_AOS_FROM_RESP, YES_VALUE);
        caseData.put(RECEIVED_AOS_FROM_RESP_DATE, FIXED_DATE);

        return caseData;
    }

    private Map<String, Object> buildSolicitorRepresentationResponse() {
        Map<String, Object> caseData = new HashMap<>();
        caseData.put("respondentSolicitorRepresented", YES_VALUE);
        caseData.put("D8RespondentSolicitorName", "Some name");
        caseData.put("D8RespondentSolicitorCompany", "Awesome Solicitors LLP");
        caseData.put("D8RespondentSolicitorEmail", "solicitor@localhost.local");
        caseData.put("D8RespondentSolicitorPhone", "2222222222");
        caseData.put("respondentSolicitorReference", "2334234");

        return caseData;
    }
}
