package uk.gov.hmcts.reform.divorce.orchestration.functionaltest;

import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.matching.EqualToPattern;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CaseResponse;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalToJson;
import static org.hamcrest.CoreMatchers.containsString;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.MediaType.APPLICATION_JSON_UTF8_VALUE;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CASE_EVENT_DATA_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CASE_EVENT_ID_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.ID;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.SUCCESS_STATUS;
import static uk.gov.hmcts.reform.divorce.orchestration.testutil.ObjectMapperTestUtil.convertObjectToJsonString;

@RunWith(SpringRunner.class)
public class UpdateCaseTest extends MockedFunctionalTest {

    private static final String CASE_ID = "1234567890";
    private static final String EVENT_ID = "updateEvent";
    private static final String AUTH_TOKEN = "authToken";

    private static final String API_URL = String.format("/updateCase/%s", CASE_ID);

    private static final String RETRIEVE_CASE_CONTEXT_PATH = String.format(
        "/casemaintenance/version/1/case/%s",
        CASE_ID
    );
    private static final String CCD_FORMAT_CONTEXT_PATH = "/caseformatter/version/1/to-ccd-format";
    private static final String UPDATE_CONTEXT_PATH = String.format(
        "/casemaintenance/version/1/updateCase/%s/%s",
        CASE_ID,
        EVENT_ID
    );

    private static final Map<String, Object> caseData = Collections.emptyMap();

    private Map<String, Object> eventData;

    @Autowired
    private MockMvc webClient;

    @Before
    public void setup() {
        eventData = new HashMap<>();
        eventData.put(CASE_EVENT_DATA_JSON_KEY, caseData);
        eventData.put(CASE_EVENT_ID_JSON_KEY, EVENT_ID);
    }

    @Test
    public void givenEventDataAndAuth_whenEventDataIsSubmitted_thenReturnSuccess() throws Exception {
        Map<String, Object> responseData = Collections.singletonMap(ID, TEST_CASE_ID);

        stubMaintenanceServerEndpointForRetrieveCaseById();
        stubFormatterServerEndpoint();
        stubMaintenanceServerEndpointForUpdate(responseData);

        CaseResponse updateResponse = CaseResponse.builder()
                .caseId(TEST_CASE_ID)
                .status(SUCCESS_STATUS)
                .build();

        webClient.perform(post(API_URL)
                .header(AUTHORIZATION, AUTH_TOKEN)
                .content(convertObjectToJsonString(eventData))
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is2xxSuccessful())
                .andExpect(content().string(containsString(convertObjectToJsonString(updateResponse))));
    }

    @Test
    public void givenNoPayload_whenEventDataIsSubmitted_thenReturnBadRequest() throws Exception {
        webClient.perform(post(API_URL)
                .header(AUTHORIZATION, AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void givenNoAuthToken_whenEventDataIsSubmitted_thenReturnBadRequest() throws Exception {
        webClient.perform(post(API_URL)
                .content(convertObjectToJsonString(eventData))
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    private void stubMaintenanceServerEndpointForRetrieveCaseById() {
        maintenanceServiceServer.stubFor(WireMock.get(RETRIEVE_CASE_CONTEXT_PATH)
                .withHeader(AUTHORIZATION, new EqualToPattern(AUTH_TOKEN))
                .willReturn(aResponse()
                        .withStatus(HttpStatus.OK.value())
                        .withHeader(CONTENT_TYPE, APPLICATION_JSON_UTF8_VALUE)
                        .withBody(convertObjectToJsonString(caseData))));
    }

    private void stubFormatterServerEndpoint() {
        formatterServiceServer.stubFor(WireMock.post(CCD_FORMAT_CONTEXT_PATH)
                .withRequestBody(equalToJson(convertObjectToJsonString(caseData)))
                .willReturn(aResponse()
                        .withStatus(HttpStatus.OK.value())
                        .withHeader(CONTENT_TYPE, APPLICATION_JSON_UTF8_VALUE)
                        .withBody(convertObjectToJsonString(caseData))));
    }

    private void stubMaintenanceServerEndpointForUpdate(Map<String, Object> response) {
        maintenanceServiceServer.stubFor(WireMock.post(UPDATE_CONTEXT_PATH)
                .withRequestBody(equalToJson(convertObjectToJsonString(caseData)))
                .withHeader(AUTHORIZATION, new EqualToPattern(AUTH_TOKEN))
                .willReturn(aResponse()
                        .withStatus(HttpStatus.OK.value())
                        .withHeader(CONTENT_TYPE, APPLICATION_JSON_UTF8_VALUE)
                        .withBody(convertObjectToJsonString(response))));
    }
}
