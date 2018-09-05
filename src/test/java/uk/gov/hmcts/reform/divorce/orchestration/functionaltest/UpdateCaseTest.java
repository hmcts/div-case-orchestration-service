package uk.gov.hmcts.reform.divorce.orchestration.functionaltest;

import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.junit.WireMockClassRule;
import com.github.tomakehurst.wiremock.matching.EqualToPattern;
import org.junit.Before;
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

import uk.gov.hmcts.reform.divorce.orchestration.OrchestrationServiceApplication;
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
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_EVENT_ID;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CASE_EVENT_DATA_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CASE_EVENT_ID_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.ID;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.SUCCESS_STATUS;
import static uk.gov.hmcts.reform.divorce.orchestration.testutil.ObjectMapperTestUtil.convertObjectToJsonString;

@RunWith(SpringRunner.class)
@ContextConfiguration(classes = OrchestrationServiceApplication.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@PropertySource(value = "classpath:application.yml")
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
public class UpdateCaseTest {

    private static final String CASE_ID = "1234567890";
    private static final String EVENT_ID = "updateEvent";
    private static final String AUTH_TOKEN = "authToken";

    private static final String API_URL = String.format("/transformationapi/version/1/updateCase/%s", CASE_ID);

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

    @ClassRule
    public static WireMockClassRule formatterServiceServer = new WireMockClassRule(4011);

    @ClassRule
    public static WireMockClassRule maintenanceServiceServer = new WireMockClassRule(4010);

    @Before
    public void setup() {
        eventData = new HashMap<>();
        eventData.put(CASE_EVENT_DATA_JSON_KEY, caseData);
        eventData.put(CASE_EVENT_ID_JSON_KEY, EVENT_ID);
    }

    @Test
    public void givenEventDataAndAuth_whenEventDataIsSubmitted_thenReturnSuccess() throws Exception {
        Map<String, Object> responseData = Collections.singletonMap(ID, TEST_CASE_ID);

        stubFormatterServerEndpoint(caseData, caseData);
        stubMaintenanceServerEndpointForUpdate(caseData, responseData);

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

    private void stubFormatterServerEndpoint(Map<String, Object> transformToCCDFormat, Map<String, Object> response)
            throws Exception {
        formatterServiceServer.stubFor(WireMock.post(CCD_FORMAT_CONTEXT_PATH)
                .withRequestBody(equalToJson(convertObjectToJsonString(transformToCCDFormat)))
                .willReturn(aResponse()
                        .withStatus(HttpStatus.OK.value())
                        .withHeader(CONTENT_TYPE, APPLICATION_JSON_UTF8_VALUE)
                        .withBody(convertObjectToJsonString(response))));
    }

    private void stubMaintenanceServerEndpointForUpdate(Map<String, Object> eventData, Map<String, Object> response)
            throws Exception {
        maintenanceServiceServer.stubFor(WireMock.post(UPDATE_CONTEXT_PATH)
                .withRequestBody(equalToJson(convertObjectToJsonString(eventData)))
                .withHeader(AUTHORIZATION, new EqualToPattern(AUTH_TOKEN))
                .willReturn(aResponse()
                        .withStatus(HttpStatus.OK.value())
                        .withHeader(CONTENT_TYPE, APPLICATION_JSON_UTF8_VALUE)
                        .withBody(convertObjectToJsonString(response))));
    }
}
