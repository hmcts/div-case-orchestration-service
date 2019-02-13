package uk.gov.hmcts.reform.divorce.orchestration.functionaltest;

import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.junit.WireMockClassRule;
import com.github.tomakehurst.wiremock.matching.EqualToPattern;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.PropertySource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import uk.gov.hmcts.reform.divorce.orchestration.OrchestrationServiceApplication;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CaseDetails;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalToJson;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static org.hamcrest.CoreMatchers.containsString;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.MediaType.APPLICATION_JSON_UTF8_VALUE;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_CHECK_CCD;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_ERROR;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.ADULTERY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.AWAITING_ANSWER_AOS_EVENT_ID;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.AWAITING_DN_AOS_EVENT_ID;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CCD_CASE_DATA_FIELD;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.COMPLETED_AOS_EVENT_ID;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DIVORCE_SESSION_EXISTING_PAYMENTS;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D_8_REASON_FOR_DIVORCE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NO_VALUE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.RECEIVED_AOS_FROM_RESP_DATE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.RESP_ADMIT_OR_CONSENT_TO_FACT;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.RESP_WILL_DEFEND_DIVORCE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.YES_VALUE;
import static uk.gov.hmcts.reform.divorce.orchestration.testutil.ObjectMapperTestUtil.convertObjectToJsonString;

@RunWith(SpringRunner.class)
@ContextConfiguration(classes = OrchestrationServiceApplication.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@PropertySource(value = "classpath:application.yml")
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
public class SubmitAosCaseITest {
    private static final String API_URL = String.format("/submit-aos/%s", TEST_CASE_ID);

    private static final String FORMAT_TO_AOS_CASE_CONTEXT_PATH = "/caseformatter/version/1/to-aos-submit-format";
    private static final String UPDATE_CONTEXT_PATH = "/casemaintenance/version/1/updateCase/" + TEST_CASE_ID + "/";
    private static final String RETRIEVE_AOS_CASE_CONTEXT_PATH = "/casemaintenance/version/1/retrieveAosCase";
    private static final String AOS_RESPONSE_DATE = "2018-10-22";
    private static final String RETRIEVE_CASE_CONTEXT_PATH = String.format(
            "/casemaintenance/version/1/case/%s",
            TEST_CASE_ID
    );
    private static final CaseDetails AOS_CASE_DETAILS =
        CaseDetails.builder()
            .caseData(
                Collections.singletonMap(
                    RECEIVED_AOS_FROM_RESP_DATE, AOS_RESPONSE_DATE
                )
            ).build();

    @Autowired
    private MockMvc webClient;

    @ClassRule
    public static WireMockClassRule formatterServiceServer = new WireMockClassRule(4011);

    @ClassRule
    public static WireMockClassRule maintenanceServiceServer = new WireMockClassRule(4010);

    @Test
    public void givenNoAuthToken_whenSubmitAos_thenReturnBadRequest() throws Exception {
        webClient.perform(MockMvcRequestBuilders.post(API_URL)
            .content(convertObjectToJsonString(getCaseData(YES_VALUE, true)))
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
        final Map<String, Object> caseData = getCaseData(YES_VALUE, true);
        final Map<String, Object> cmsData = Collections.emptyMap();

        stubFormatterServerEndpoint(BAD_REQUEST, caseData, TEST_ERROR);
        stubMaintenanceServerEndpointForRetrieveCaseById(cmsData);

        webClient.perform(MockMvcRequestBuilders.post(API_URL)
            .header(AUTHORIZATION, AUTH_TOKEN)
            .content(convertObjectToJsonString(caseData))
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isBadRequest())
            .andExpect(content().string(containsString(TEST_ERROR)));
    }

    @Test
    public void givenCaseUpdateFails_whenSubmitAos_thenPropagateTheException() throws Exception {
        final Map<String, Object> caseData = getCaseData(YES_VALUE, false);
        final Map<String, Object> cmsData = Collections.emptyMap();

        stubFormatterServerEndpoint(OK, caseData, convertObjectToJsonString(caseData));
        stubMaintenanceServerEndpointForUpdate(BAD_REQUEST, AWAITING_DN_AOS_EVENT_ID, caseData, TEST_ERROR);
        stubMaintenanceServerEndpointForRetrieveCaseById(cmsData);

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
        final Map<String, Object> caseData = getCaseData(YES_VALUE, true);
        final String caseDataString = convertObjectToJsonString(caseData);
        final Map<String, Object> cmsData = Collections.emptyMap();

        stubFormatterServerEndpoint(OK, caseData, caseDataString);
        stubMaintenanceServerEndpointForUpdate(OK, AWAITING_ANSWER_AOS_EVENT_ID, caseData, caseDataString);
        stubMaintenanceServerEndpointForRetrieveCaseById(cmsData);

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
        final Map<String, Object> caseData = getCaseData(NO_VALUE, true);
        final String caseDataString = convertObjectToJsonString(caseData);
        final Map<String, Object> cmsData = Collections.emptyMap();

        stubFormatterServerEndpoint(OK, caseData, caseDataString);
        stubMaintenanceServerEndpointForUpdate(OK, AWAITING_ANSWER_AOS_EVENT_ID, caseData, caseDataString);
        stubMaintenanceServerEndpointForRetrieveCaseById(cmsData);

        webClient.perform(MockMvcRequestBuilders.post(API_URL)
            .header(AUTHORIZATION, AUTH_TOKEN)
            .content(convertObjectToJsonString(caseData))
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content().json(caseDataString));
    }

    @Test
    public void givenNoConsentAndNoDefend_whenSubmitAos_thenProceedAsExpected() throws Exception {
        final Map<String, Object> caseData = getCaseData(NO_VALUE, false);
        final String caseDataString = convertObjectToJsonString(caseData);
        final Map<String, Object> cmsData = new HashMap<>();

        cmsData.put(CCD_CASE_DATA_FIELD, Collections.singletonMap(D_8_REASON_FOR_DIVORCE, ADULTERY));

        stubFormatterServerEndpoint(OK, caseData, caseDataString);
        stubRetrieveAosCaseFromCMS(convertObjectToJsonString(AOS_CASE_DETAILS));
        stubMaintenanceServerEndpointForRetrieveCaseById(cmsData);
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
        final Map<String, Object> caseData = getCaseData(YES_VALUE, false);
        final String caseDataString = convertObjectToJsonString(caseData);
        final Map<String, Object> cmsData = Collections.emptyMap();

        stubFormatterServerEndpoint(OK, caseData, caseDataString);
        stubMaintenanceServerEndpointForUpdate(OK, AWAITING_DN_AOS_EVENT_ID, caseData, caseDataString);
        stubMaintenanceServerEndpointForRetrieveCaseById(cmsData);

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

    private void stubMaintenanceServerEndpointForRetrieveCaseById(Map<String, Object> cmsData) {
        maintenanceServiceServer.stubFor(WireMock.get(RETRIEVE_CASE_CONTEXT_PATH)
            .withHeader(AUTHORIZATION, new EqualToPattern(AUTH_TOKEN))
            .willReturn(aResponse()
                .withStatus(HttpStatus.OK.value())
                .withHeader(CONTENT_TYPE, APPLICATION_JSON_UTF8_VALUE)
                .withBody(convertObjectToJsonString(cmsData))));
    }

    private void stubRetrieveAosCaseFromCMS(String message) {
        maintenanceServiceServer.stubFor(WireMock.get(RETRIEVE_AOS_CASE_CONTEXT_PATH + "?checkCcd=" + TEST_CHECK_CCD)
            .withHeader(AUTHORIZATION, new EqualToPattern(AUTH_TOKEN))
            .willReturn(aResponse()
                .withStatus(HttpStatus.OK.value())
                .withHeader(CONTENT_TYPE, APPLICATION_JSON_UTF8_VALUE)
                .withBody(message)));
    }

    private Map<String, Object> getCaseData(String consent, boolean defended) {
        Map<String, Object> caseData = new HashMap<>();
        caseData.put(RESP_ADMIT_OR_CONSENT_TO_FACT, consent);
        caseData.put(DIVORCE_SESSION_EXISTING_PAYMENTS, null);

        if (defended) {
            caseData.put(RESP_WILL_DEFEND_DIVORCE, YES_VALUE);
        } else {
            caseData.put(RESP_WILL_DEFEND_DIVORCE, NO_VALUE);
        }

        return caseData;
    }
}
