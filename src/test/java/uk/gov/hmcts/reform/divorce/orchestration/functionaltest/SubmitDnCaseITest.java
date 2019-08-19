package uk.gov.hmcts.reform.divorce.orchestration.functionaltest;

import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.matching.EqualToPattern;
import com.google.common.collect.ImmutableMap;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

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
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_ERROR;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.AOS_AWAITING;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.AOS_COMPLETED;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CASE_STATE_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CCD_CASE_DATA_FIELD;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DN_RECEIVED;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DN_RECEIVED_AOS_COMPLETE;
import static uk.gov.hmcts.reform.divorce.orchestration.testutil.ObjectMapperTestUtil.convertObjectToJsonString;

@RunWith(SpringRunner.class)
public class SubmitDnCaseITest extends MockedFunctionalTest {
    private static final String API_URL = String.format("/submit-dn/%s", TEST_CASE_ID);
    private static final String FORMAT_TO_DN_CASE_CONTEXT_PATH = "/caseformatter/version/1/to-dn-submit-format";
    private static final String UPDATE_CONTEXT_PATH = "/casemaintenance/version/1/updateCase/" + TEST_CASE_ID + "/";
    private static final String RETRIEVE_CASE_CONTEXT_PATH = String.format(
            "/casemaintenance/version/1/case/%s",
            TEST_CASE_ID
    );

    @Autowired
    private MockMvc webClient;

    @Test
    public void givenNoAuthToken_whenSubmitDn_thenReturnBadRequest() throws Exception {
        webClient.perform(MockMvcRequestBuilders.post(API_URL)
            .content(convertObjectToJsonString(getCaseData()))
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isBadRequest());
    }

    @Test
    public void givenNoPayload_whenSubmitDn_thenReturnBadRequest() throws Exception {
        webClient.perform(MockMvcRequestBuilders.post(API_URL)
            .header(AUTHORIZATION, AUTH_TOKEN)
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isBadRequest());
    }

    @Test
    public void givenCaseFormatterFails_whenSubmitDn_thenPropagateTheException() throws Exception {
        final Map<String, Object> caseData = getCaseData();

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
    public void givenCaseUpdateFails_whenSubmitDn_thenPropagateTheException() throws Exception {
        final Map<String, Object> caseData = new HashMap<>();
        final String caseDataString = convertObjectToJsonString(caseData);
        final Map<String, Object> caseDetails = new HashMap<>();

        caseDetails.put(CASE_STATE_JSON_KEY, AOS_AWAITING);
        caseDetails.put(CCD_CASE_DATA_FIELD, caseData);

        stubMaintenanceServerEndpointForRetrieveCaseById(OK, caseDetails);
        stubMaintenanceServerEndpointForUpdate(OK, DN_RECEIVED, caseData, caseDataString);
        stubFormatterServerEndpoint(OK, caseData, convertObjectToJsonString(caseData));
        stubMaintenanceServerEndpointForUpdate(BAD_REQUEST, DN_RECEIVED, caseData, TEST_ERROR);

        webClient.perform(MockMvcRequestBuilders.post(API_URL)
            .header(AUTHORIZATION, AUTH_TOKEN)
            .content(convertObjectToJsonString(caseData))
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isBadRequest())
            .andExpect(content().string(containsString(TEST_ERROR)));
    }

    @Test
    public void givenDnReceivedAndAosNotCompleted_whenSubmitDn_thenProceedAsExpected() throws Exception {
        final Map<String, Object> caseData = getCaseData();
        final String caseDataString = convertObjectToJsonString(caseData);
        final Map<String, Object> caseDetails = new HashMap<>();

        caseDetails.put(CASE_STATE_JSON_KEY, AOS_AWAITING);
        caseDetails.put(CCD_CASE_DATA_FIELD, caseData);

        stubMaintenanceServerEndpointForRetrieveCaseById(OK, caseDetails);
        stubFormatterServerEndpoint(OK, caseData, caseDataString);
        stubMaintenanceServerEndpointForUpdate(OK, DN_RECEIVED, caseData, caseDataString);

        webClient.perform(MockMvcRequestBuilders.post(API_URL)
            .header(AUTHORIZATION, AUTH_TOKEN)
            .content(convertObjectToJsonString(caseData))
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content().json(caseDataString));
    }

    @Test
    public void givenDnReceivedAndAosCompleted_whenSubmitDn_thenProceedAsExpected() throws Exception {
        final Map<String, Object> caseData = getCaseData();
        final String caseDataString = convertObjectToJsonString(caseData);
        final Map<String, Object> caseDetails = new HashMap<>();

        caseDetails.put(CASE_STATE_JSON_KEY, AOS_COMPLETED);
        caseDetails.put(CCD_CASE_DATA_FIELD, caseData);

        stubMaintenanceServerEndpointForRetrieveCaseById(OK, caseDetails);
        stubFormatterServerEndpoint(OK, caseData, caseDataString);
        stubMaintenanceServerEndpointForUpdate(OK, DN_RECEIVED_AOS_COMPLETE, caseData, caseDataString);

        webClient.perform(MockMvcRequestBuilders.post(API_URL)
            .header(AUTHORIZATION, AUTH_TOKEN)
            .content(convertObjectToJsonString(caseData))
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content().json(caseDataString));
    }

    private void stubFormatterServerEndpoint(HttpStatus status, Map<String, Object> caseData, String response) {
        formatterServiceServer.stubFor(post(FORMAT_TO_DN_CASE_CONTEXT_PATH)
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

    private Map<String, Object> getCaseData() {
        return ImmutableMap.of();
    }
}
