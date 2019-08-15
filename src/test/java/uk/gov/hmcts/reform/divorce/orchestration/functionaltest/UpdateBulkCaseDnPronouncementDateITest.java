package uk.gov.hmcts.reform.divorce.orchestration.functionaltest;

import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.http.RequestMethod;
import com.github.tomakehurst.wiremock.matching.RequestPatternBuilder;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalToJson;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static org.awaitility.Awaitility.await;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.HttpMethod.POST;
import static org.springframework.http.MediaType.APPLICATION_JSON_UTF8_VALUE;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.BulkCaseConstants.PRONOUNCED_EVENT;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.UPDATE_BULK_DN_PRONOUNCEMENT_DETAILS_EVENT;
import static uk.gov.hmcts.reform.divorce.orchestration.testutil.ResourceLoader.loadResourceAsString;

@RunWith(SpringRunner.class)
public class UpdateBulkCaseDnPronouncementDateITest extends IdamTestSupport {

    private static final String API_URL = "/bulk/pronounce/submit";

    private static final String CMS_UPDATE_CASE_PATH = "/casemaintenance/version/1/updateCase/%s/%s";
    private static final String CMS_UPDATE_BULK_CASE_PATH = "/casemaintenance/version/1/bulk/updateCase/%s/%s";

    private static final String REQUEST_JSON_PATH = "jsonExamples/payloads/bulkCaseCcdCallbackRequest.json";
    private static final String MISSING_JUDGE_REQUEST_JSON_PATH = "jsonExamples/payloads/bulkCaseCcdCallbackRequestNoJudge.json";
    private static final String EXPECTED_CASE_UPDATE_JSON_PATH = "jsonExamples/payloads/bulkCasePronouncementDate.json";
    private static final String BULK_CASE_ID = "1505150515051550";
    private static final String CASE_ID_FIRST = "1558711395612316";
    private static final String CASE_ID_SECOND = "1558711407435839";

    private static final String TEST_AUTH_TOKEN = "testAuthToken";

    @Autowired
    ThreadPoolTaskExecutor asyncTaskExecutor;

    @Autowired
    private MockMvc webClient;

    @Before
    public void setup() {
        maintenanceServiceServer.resetAll();
    }

    @Test
    public void givenCallbackRequestWithDnPronouncementDateBulkCaseData_thenReturnCallbackResponse() throws Exception {
        String updateCaseOnePath = String.format(CMS_UPDATE_CASE_PATH, CASE_ID_FIRST, UPDATE_BULK_DN_PRONOUNCEMENT_DETAILS_EVENT);
        String updateCaseTwoPath = String.format(CMS_UPDATE_CASE_PATH, CASE_ID_SECOND, UPDATE_BULK_DN_PRONOUNCEMENT_DETAILS_EVENT);
        String updateBulkCasePath = String.format(CMS_UPDATE_BULK_CASE_PATH, BULK_CASE_ID, PRONOUNCED_EVENT);

        stubCmsServerEndpoint(updateCaseOnePath, HttpStatus.OK, "{}", POST);
        stubCmsServerEndpoint(updateCaseTwoPath, HttpStatus.OK, "{}", POST);
        stubCmsServerEndpoint(updateBulkCasePath, HttpStatus.OK, "{}", POST);

        // Matching request json
        String courtHearingDate = "2000-01-01";
        String eligibleDate = "2000-02-13";
        String pronouncementJudge = "District Judge";

        webClient.perform(MockMvcRequestBuilders.post(API_URL)
                .header(AUTHORIZATION, TEST_AUTH_TOKEN)
                .content(loadResourceAsString(REQUEST_JSON_PATH))
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.DecreeNisiGrantedDate", equalTo(courtHearingDate)))
                .andExpect(jsonPath("$.data.DAEligibleFromDate", equalTo(eligibleDate)))
                .andExpect(jsonPath("$.data.PronouncementJudge", equalTo(pronouncementJudge)))
                .andExpect(jsonPath("$.errors", nullValue()));

        waitAsyncCompleted();

        verifyCmsServerEndpoint(1, updateCaseOnePath, RequestMethod.POST, loadResourceAsString(EXPECTED_CASE_UPDATE_JSON_PATH));
        verifyCmsServerEndpoint(1, updateCaseTwoPath, RequestMethod.POST, loadResourceAsString(EXPECTED_CASE_UPDATE_JSON_PATH));
        verifyCmsServerEndpoint(1, updateBulkCasePath, RequestMethod.POST, "{}");
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
                        .withHeader(CONTENT_TYPE, APPLICATION_JSON_UTF8_VALUE)
                        .withBody(body)));
    }

    private void verifyCmsServerEndpoint(int times, String path, RequestMethod method, String body) {
        maintenanceServiceServer.verify(times, new RequestPatternBuilder(method, urlEqualTo(path))
                .withHeader(CONTENT_TYPE, WireMock.equalTo(APPLICATION_JSON_VALUE))
                .withRequestBody(equalToJson(body)));
    }
}