package uk.gov.hmcts.reform.divorce.orchestration.functionaltest;

import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.http.RequestMethod;
import com.github.tomakehurst.wiremock.matching.RequestPatternBuilder;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.awaitility.Awaitility.await;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.HttpMethod.POST;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.UPDATE_BULK_DN_PRONOUNCEMENT_DETAILS_EVENT;
import static uk.gov.hmcts.reform.divorce.orchestration.testutil.ResourceLoader.loadResourceAsString;

public class SetDNGrantedDateITest extends IdamTestSupport {

    private static final String CMS_UPDATE_CASE_PATH = "/casemaintenance/version/1/updateCase/%s/%s";
    private static final String API_URL = "/dn-pronounced-manual";
    private static final String MISSING_JUDGE_REQUEST_JSON_PATH = "jsonExamples/payloads/bulkCaseCcdCallbackRequestNoJudge.json";
    private static final String REQUEST_JSON_PATH = "jsonExamples/payloads/dnGrantedCcdCallbackRequest.json";
    private static final String EXPECTED_CASE_UPDATE_JSON_PATH = "jsonExamples/payloads/singleCasePronouncementDate.json";
    private static final String TEST_AUTH_TOKEN = "testAuthToken";

    private static final String CASE_ID = "1558711395612316";



    @Autowired
    ThreadPoolTaskExecutor asyncTaskExecutor;

    @Autowired
    private MockMvc webClient;

    @Before
    public void setup() {
        maintenanceServiceServer.resetAll();
    }

    @Test
    public void givenCallbackRequestWithDnPronouncementDateCaseData_thenReturnCallbackResponse() throws Exception {
        String updateCasePath = String.format(CMS_UPDATE_CASE_PATH, CASE_ID, UPDATE_BULK_DN_PRONOUNCEMENT_DETAILS_EVENT);
        stubCmsServerEndpoint(updateCasePath, HttpStatus.OK, "{}", POST);


        // Matching request json
        String dnGrantedDate = "2000-01-01";
        String daEligibleDate = "2000-02-13";
        String pronouncementJudge = "District Judge";

        webClient.perform(MockMvcRequestBuilders.post(API_URL)
                .header(AUTHORIZATION, TEST_AUTH_TOKEN)
                .content(loadResourceAsString(REQUEST_JSON_PATH))
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.DecreeNisiGrantedDate", equalTo(dnGrantedDate)))
                .andExpect(jsonPath("$.data.DAEligibleFromDate", equalTo(daEligibleDate)))
                .andExpect(jsonPath("$.data.PronouncementJudge", equalTo(pronouncementJudge)))
                .andExpect(jsonPath("$.errors", nullValue()));

        waitAsyncCompleted();

        verifyCmsServerEndpoint(1, updateCasePath, RequestMethod.POST, loadResourceAsString(EXPECTED_CASE_UPDATE_JSON_PATH));
    }

    @Test
    public void givenCallbackRequestWithNoJudgeCaseData_thenReturnCallbackResponseWithError() throws Exception {
        webClient.perform(MockMvcRequestBuilders.post(API_URL)
                .header(AUTHORIZATION, TEST_AUTH_TOKEN)
                .content(loadResourceAsString(MISSING_JUDGE_REQUEST_JSON_PATH))
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.errors", notNullValue()));
    }

    private void waitAsyncCompleted() {
        await().until(() -> asyncTaskExecutor.getThreadPoolExecutor().getActiveCount() == 0);
    }

    private void stubCmsServerEndpoint(String path, HttpStatus status, String body, HttpMethod method) {
        maintenanceServiceServer.stubFor(WireMock.request(method.name(),urlEqualTo(path))
            .willReturn(aResponse()
                .withStatus(status.value())
                .withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
                .withBody(body)));
    }

    private void verifyCmsServerEndpoint(int times, String path, RequestMethod method, String body) {
        maintenanceServiceServer.verify(times, new RequestPatternBuilder(method, urlEqualTo(path))
            .withHeader(CONTENT_TYPE, WireMock.equalTo(APPLICATION_JSON_VALUE))
            .withRequestBody(equalToJson(body)));
    }
}