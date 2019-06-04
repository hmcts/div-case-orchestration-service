package uk.gov.hmcts.reform.divorce.orchestration.functionaltest;

import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.http.RequestMethod;
import com.github.tomakehurst.wiremock.junit.WireMockClassRule;
import com.github.tomakehurst.wiremock.matching.RequestPatternBuilder;
import com.google.common.collect.ImmutableMap;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.PropertySource;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import uk.gov.hmcts.reform.divorce.orchestration.OrchestrationServiceApplication;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CaseDetails;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CollectionMember;
import uk.gov.hmcts.reform.divorce.orchestration.testutil.ObjectMapperTestUtil;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.equalToJson;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static org.awaitility.Awaitility.await;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.HttpMethod.POST;
import static org.springframework.http.MediaType.APPLICATION_JSON_UTF8_VALUE;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.BulkCaseConstants.LISTED_EVENT;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DATETIME_OF_HEARING_CCD_FIELD;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DATE_OF_HEARING_CCD_FIELD;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.TIME_OF_HEARING_CCD_FIELD;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.UPDATE_COURT_HEARING_DETAILS_EVENT;
import static uk.gov.hmcts.reform.divorce.orchestration.testutil.ResourceLoader.loadResourceAsString;

@RunWith(SpringRunner.class)
@ContextConfiguration(classes = OrchestrationServiceApplication.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@PropertySource(value = "classpath:application.yml")
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
@AutoConfigureMockMvc
public class UpdateBulkCaseHearingDetailsITest extends IdamTestSupport {

    private static final String API_URL = "/bulk/schedule/listing";

    private static final String CMS_RETRIEVE_CASE_PATH = "/casemaintenance/version/1/case/%s";
    private static final String CMS_UPDATE_CASE_PATH = "/casemaintenance/version/1/updateCase/%s/%s";
    private static final String CMS_UPDATE_BULK_CASE_PATH = "/casemaintenance/version/1/bulk/updateCase/%s/%s";

    private static final String REQUEST_JSON_PATH = "jsonExamples/payloads/bulkCaseCcdCallbackRequest.json";
    private static final String EXPECTED_CASE_UPDATE_JSON_PATH = "jsonExamples/payloads/bulkCaseUpdateCourtHearingDetails.json";
    private static final String EXPECTED_CASE_UPDATE_EXISTING_HEARING_JSON_PATH = "jsonExamples/payloads/bulkCaseUpdateExistingHearingDetails.json";
    private static final String BULK_CASE_ID = "1505150515051550";
    private static final String CASE_ID_FIRST = "1558711395612316";
    private static final String CASE_ID_SECOND = "1558711407435839";

    private static final String TEST_AUTH_TOKEN = "testAuthToken";

    @ClassRule
    public static WireMockClassRule cmsServiceServer = new WireMockClassRule(4010);

    @Autowired
    ThreadPoolTaskExecutor asyncTaskExecutor;

    @Autowired
    private MockMvc webClient;

    @Before
    public void setup() {
        cmsServiceServer.resetAll();
    }

    @Test
    public void givenCallbackRequestWithTwoCaseLinks_thenTriggerBulkCaseUpdateEvent() throws Exception {
        stubSignInForCaseworker();

        String retrieveCaseOnePath = String.format(CMS_RETRIEVE_CASE_PATH, CASE_ID_FIRST);

        stubCmsServerEndpoint(retrieveCaseOnePath, HttpStatus.OK, caseDataToCaseDetailsJson(Collections.emptyMap()), GET);

        CollectionMember<Map<String, Object>> existingCourtHearing = new CollectionMember<>();
        existingCourtHearing.setId("someRandomId");
        existingCourtHearing.setValue(ImmutableMap.of(
                DATE_OF_HEARING_CCD_FIELD, "2011-11-11",
                TIME_OF_HEARING_CCD_FIELD, "11:11"
        ));
        List<CollectionMember> courtHearings = Collections.singletonList(existingCourtHearing);

        String retrieveCaseTwoPath = String.format(CMS_RETRIEVE_CASE_PATH, CASE_ID_SECOND);

        stubCmsServerEndpoint(retrieveCaseTwoPath, HttpStatus.OK,
                caseDataToCaseDetailsJson(Collections.singletonMap(DATETIME_OF_HEARING_CCD_FIELD, courtHearings)), GET);

        String updateCaseOnePath = String.format(CMS_UPDATE_CASE_PATH, CASE_ID_FIRST, UPDATE_COURT_HEARING_DETAILS_EVENT);
        String updateCaseTwoPath = String.format(CMS_UPDATE_CASE_PATH, CASE_ID_SECOND, UPDATE_COURT_HEARING_DETAILS_EVENT);
        String updateBulkCasePath = String.format(CMS_UPDATE_BULK_CASE_PATH, BULK_CASE_ID, LISTED_EVENT);

        stubCmsServerEndpoint(updateCaseOnePath, HttpStatus.OK, "{}", POST);
        stubCmsServerEndpoint(updateCaseTwoPath, HttpStatus.OK, "{}", POST);
        stubCmsServerEndpoint(updateBulkCasePath, HttpStatus.OK, "{}", POST);

        webClient.perform(MockMvcRequestBuilders.post(API_URL)
                .header(AUTHORIZATION, TEST_AUTH_TOKEN)
                .content(loadResourceAsString(REQUEST_JSON_PATH))
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        waitAsyncCompleted();

        verifyCmsServerEndpoint(1, updateCaseOnePath, RequestMethod.POST, loadResourceAsString(EXPECTED_CASE_UPDATE_JSON_PATH));
        verifyCmsServerEndpoint(1, updateCaseTwoPath, RequestMethod.POST,
                loadResourceAsString(EXPECTED_CASE_UPDATE_EXISTING_HEARING_JSON_PATH));
        verifyCmsServerEndpoint(1, updateBulkCasePath, RequestMethod.POST, "{}");
    }

    private void waitAsyncCompleted() {
        await().until(() -> asyncTaskExecutor.getThreadPoolExecutor().getActiveCount() == 0);
    }

    private void stubCmsServerEndpoint(String path, HttpStatus status, String body, HttpMethod method) {
        cmsServiceServer.stubFor(WireMock.request(method.name(),urlEqualTo(path))
                .willReturn(aResponse()
                        .withStatus(status.value())
                        .withHeader(CONTENT_TYPE, APPLICATION_JSON_UTF8_VALUE)
                        .withBody(body)));
    }

    private void verifyCmsServerEndpoint(int times, String path, RequestMethod method, String body) {
        cmsServiceServer.verify(times, new RequestPatternBuilder(method, urlEqualTo(path))
                .withHeader(CONTENT_TYPE, equalTo(APPLICATION_JSON_VALUE))
                .withRequestBody(equalToJson(body)));
    }

    private String caseDataToCaseDetailsJson(Map<String, Object> caseData) {
        return ObjectMapperTestUtil.convertObjectToJsonString(
            CaseDetails.builder().caseData(caseData).build()
        );
    }
}