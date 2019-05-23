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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.PropertySource;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import uk.gov.hmcts.reform.divorce.orchestration.OrchestrationServiceApplication;
import uk.gov.hmcts.reform.divorce.orchestration.TestConstants;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CaseDetails;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CaseLink;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.SearchResult;

import java.util.Arrays;
import java.util.Collections;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static org.awaitility.Awaitility.await;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.HttpMethod.POST;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.http.MediaType.APPLICATION_JSON_UTF8_VALUE;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.BulkCaseConstants.BULK_CASE_LIST_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.BulkCaseConstants.CASE_LIST_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.BULK_LISTING_CASE_ID_FIELD;
import static uk.gov.hmcts.reform.divorce.orchestration.testutil.ObjectMapperTestUtil.convertObjectToJsonString;
import static uk.gov.hmcts.reform.divorce.orchestration.testutil.ResourceLoader.loadResourceAsString;

@RunWith(SpringRunner.class)
@ContextConfiguration(classes = OrchestrationServiceApplication.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@PropertySource(value = "classpath:application.yml")
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
@AutoConfigureMockMvc
public class ProcessBulkCaseITest extends IdamTestSupport {

    private static final String CMS_SEARCH = "/casemaintenance/version/1/search";
    private static final String CMS_BULK_CASE_SUBMIT = "/casemaintenance/version/1/bulk/submit";
    private static final String CMS_UPDATE_CASE = "/casemaintenance/version/1/updateCase/%s/linkBulkCaseReference";
    private static final String API_URL = "/bulk/case";

    private static final String CMS_RESPONSE_BODY_FILE = "jsonExamples/payloads/cmsBulkCaseCreatedResponse.json";

    private static final String CASE_ID1 = "1546883073634741";
    private static final String CASE_ID2 = "1546883073634742";
    private static final String BULK_CASE_ID = "1557223513377278";
    private static final String UPDATE_BODY = convertObjectToJsonString(
        ImmutableMap.of(BULK_LISTING_CASE_ID_FIELD, new CaseLink(BULK_CASE_ID)));
    @ClassRule
    public static WireMockClassRule cmsServiceServer = new WireMockClassRule(4010);

    @Autowired
    ThreadPoolTaskExecutor asyncTaskExecutor;

    @Autowired
    private MockMvc webClient;

    @Value("${auth2.client.id}")
    private String authClientId;

    @Value("${idam.api.redirect-url}")
    private String authRedirectUrl;

    @Before
    public void cleanUp() {
        cmsServiceServer.resetAll();
    }

    @Test
    public void givenCaseList_thenCreateBulkCaseAndUpdateAllCases() throws Exception {
        SearchResult result = SearchResult.builder()
            .cases(Arrays.asList(prepareBulkCase()))
            .build();

        stubCmsServerEndpoint(CMS_SEARCH, HttpStatus.OK, convertObjectToJsonString(result), POST);
        stubCmsServerEndpoint(CMS_BULK_CASE_SUBMIT, HttpStatus.OK, getCmsBulkCaseResponse(), POST);
        stubCmsServerEndpoint(String.format(CMS_UPDATE_CASE, CASE_ID1), HttpStatus.OK, getCmsBulkCaseResponse(), POST);
        stubCmsServerEndpoint(String.format(CMS_UPDATE_CASE, CASE_ID2), HttpStatus.OK, getCmsBulkCaseResponse(), POST);
        stubSignInForCaseworker();
        webClient.perform(post(API_URL)
            .contentType(APPLICATION_JSON)
            .accept(APPLICATION_JSON))
            .andExpect(status().isOk());

        waitAsyncCompleted();
        verifyCmsServerEndpoint(1, String.format(CMS_UPDATE_CASE, CASE_ID1), RequestMethod.POST, UPDATE_BODY);
        verifyCmsServerEndpoint(1, String.format(CMS_UPDATE_CASE, CASE_ID2), RequestMethod.POST, UPDATE_BODY);
    }

    @Test
    public void givenError_whenCreateBulkCase_thenCasesAreNotUpdated() throws Exception {
        SearchResult result = SearchResult.builder()
            .cases(Arrays.asList(prepareBulkCase()))
            .build();

        stubCmsServerEndpoint(CMS_SEARCH, HttpStatus.OK, convertObjectToJsonString(result), POST);
        stubCmsServerEndpoint(CMS_BULK_CASE_SUBMIT, HttpStatus.NOT_FOUND, getCmsBulkCaseResponse(), POST);

        stubSignInForCaseworker();
        webClient.perform(post(API_URL)
            .contentType(APPLICATION_JSON)
            .accept(APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath(BULK_CASE_LIST_KEY).isEmpty());

        verifyCmsServerEndpoint(0, CMS_UPDATE_CASE, RequestMethod.POST, UPDATE_BODY);
    }

    @Test
    public void giveError_whenUpdateDivorceCase_thenProcessOtherCases() throws Exception {
        SearchResult result = SearchResult.builder()
            .cases(Arrays.asList(prepareBulkCase()))
            .build();
        CaseDetails.builder()
            .caseId(TestConstants.TEST_CASE_ID)
            .caseData(ImmutableMap.of(CASE_LIST_KEY, result.getCases()))
            .build();

        stubCmsServerEndpoint(CMS_SEARCH, HttpStatus.OK, convertObjectToJsonString(result), POST);
        stubCmsServerEndpoint(CMS_BULK_CASE_SUBMIT, HttpStatus.OK, getCmsBulkCaseResponse(), POST);
        stubCmsServerEndpoint(String.format(CMS_UPDATE_CASE, CASE_ID1), HttpStatus.NOT_FOUND, getCmsBulkCaseResponse(), POST);
        stubCmsServerEndpoint(String.format(CMS_UPDATE_CASE, CASE_ID2), HttpStatus.OK, getCmsBulkCaseResponse(), POST);

        stubSignInForCaseworker();
        webClient.perform(post(API_URL)
            .contentType(APPLICATION_JSON)
            .accept(APPLICATION_JSON))
            .andExpect(status().isOk());

        waitAsyncCompleted();

        verifyCmsServerEndpoint(1, String.format(CMS_UPDATE_CASE, CASE_ID1), RequestMethod.POST, UPDATE_BODY);
        verifyCmsServerEndpoint(1, String.format(CMS_UPDATE_CASE, CASE_ID2), RequestMethod.POST, UPDATE_BODY);
    }

    private void waitAsyncCompleted() {
        await().until(() -> asyncTaskExecutor.getThreadPoolExecutor().getActiveCount() == 0);
    }

    private CaseDetails prepareBulkCase() {
        return CaseDetails.builder()
            .caseId(TestConstants.TEST_CASE_ID)
            .caseData(Collections.emptyMap())
            .build();
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
            .withRequestBody(equalTo(body)));
    }

    private String getCmsBulkCaseResponse() throws Exception {
        return loadResourceAsString(CMS_RESPONSE_BODY_FILE);
    }
}