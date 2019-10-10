package uk.gov.hmcts.reform.divorce.orchestration.functionaltest;

import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.http.RequestMethod;
import com.github.tomakehurst.wiremock.matching.RequestPatternBuilder;
import com.google.common.collect.ImmutableMap;
import lombok.extern.slf4j.Slf4j;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import uk.gov.hmcts.reform.divorce.orchestration.TestConstants;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CaseDetails;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CaseLink;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.SearchResult;
import uk.gov.hmcts.reform.divorce.orchestration.exception.BulkUpdateException;

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
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.BulkCaseConstants.CREATE_EVENT;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.BULK_LISTING_CASE_ID_FIELD;
import static uk.gov.hmcts.reform.divorce.orchestration.testutil.ObjectMapperTestUtil.convertObjectToJsonString;
import static uk.gov.hmcts.reform.divorce.orchestration.testutil.ResourceLoader.loadResourceAsString;

@Slf4j
@RunWith(SpringRunner.class)
public class ProcessBulkCaseITest extends IdamTestSupport {

    private static final String CMS_SEARCH = "/casemaintenance/version/1/search";
    private static final String CMS_BULK_CASE_SUBMIT = "/casemaintenance/version/1/bulk/submit";
    private static final String CMS_UPDATE_CASE = "/casemaintenance/version/1/updateCase/%s/linkBulkCaseReference";
    private static final String API_URL = "/bulk/case";
    private static final String CMS_UPDATE_BULK_CASE_PATH = "/casemaintenance/version/1/bulk/updateCase/%s/%s";


    private static final String CMS_RESPONSE_BODY_FILE = "jsonExamples/payloads/cmsBulkCaseCreatedResponse.json";

    private static final String CASE_ID1 = "1546883073634741";
    private static final String CASE_ID2 = "1546883073634742";
    private static final String CASE_ID3 = "1546883073634743";
    private static final String BULK_CASE_ID = "1557223513377278";
    private static final String UPDATE_BODY = convertObjectToJsonString(
        ImmutableMap.of(BULK_LISTING_CASE_ID_FIELD, new CaseLink(BULK_CASE_ID)));

    @Value("${bulk-action.retries.max:4}")
    private int maxRetries;

    @Value("${bulk-action.retries.backoff.base-rate:1000}")
    private int backoffBaseRate;

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
        maintenanceServiceServer.resetAll();
    }

    @Test
    public void givenCaseList_thenCreateBulkCaseAndUpdateAllCases() throws Exception {
        SearchResult result = SearchResult.builder()
            .cases(Arrays.asList(prepareBulkCase(), prepareBulkCase()))
            .build();

        stubCmsServerEndpoint(CMS_SEARCH, HttpStatus.OK, convertObjectToJsonString(result), POST);
        stubCmsServerEndpoint(CMS_BULK_CASE_SUBMIT, HttpStatus.OK, getCmsBulkCaseResponse(), POST);
        stubCmsServerEndpoint(String.format(CMS_UPDATE_CASE, CASE_ID1), HttpStatus.OK, getCmsBulkCaseResponse(), POST);
        stubCmsServerEndpoint(String.format(CMS_UPDATE_CASE, CASE_ID2), HttpStatus.OK, getCmsBulkCaseResponse(), POST);
        stubCmsServerEndpoint(String.format(CMS_UPDATE_CASE, CASE_ID3), HttpStatus.OK, getCmsBulkCaseResponse(), POST);
        stubCmsServerEndpoint(String.format(CMS_UPDATE_BULK_CASE_PATH, BULK_CASE_ID, CREATE_EVENT), HttpStatus.OK, getCmsBulkCaseResponse(), POST);

        stubSignInForCaseworker();
        webClient.perform(post(API_URL)
            .contentType(APPLICATION_JSON)
            .accept(APPLICATION_JSON))
            .andExpect(status().isOk());

        waitAsyncCompleted();
        verifyCmsServerEndpoint(1, String.format(CMS_UPDATE_CASE, CASE_ID1), RequestMethod.POST, UPDATE_BODY);
        verifyCmsServerEndpoint(1, String.format(CMS_UPDATE_CASE, CASE_ID2), RequestMethod.POST, UPDATE_BODY);
        verifyCmsServerEndpoint(1, String.format(CMS_UPDATE_CASE, CASE_ID3), RequestMethod.POST, UPDATE_BODY);
        verifyCmsServerEndpoint(1, String.format(CMS_UPDATE_BULK_CASE_PATH, BULK_CASE_ID, CREATE_EVENT), RequestMethod.POST);
    }

    @Test
    public void givenError_whenCreateBulkCase_thenCasesAreNotUpdated() throws Exception {
        SearchResult result = SearchResult.builder()
            .cases(Arrays.asList(prepareBulkCase(), prepareBulkCase()))
            .total(2)
            .build();

        stubCmsServerEndpoint(CMS_SEARCH, HttpStatus.OK, convertObjectToJsonString(result), POST);
        stubCmsServerEndpoint(CMS_BULK_CASE_SUBMIT, HttpStatus.NOT_FOUND, getCmsBulkCaseResponse(), POST);

        stubSignInForCaseworker();
        webClient.perform(post(API_URL)
            .contentType(APPLICATION_JSON)
            .accept(APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath(BULK_CASE_LIST_KEY).isEmpty());

        verifyCmsServerEndpoint(0, CMS_UPDATE_CASE, RequestMethod.POST);
    }

    @Test
    public void give4XError_whenUpdateDivorceCase_thenProcessOtherCases() throws Exception {
        SearchResult result = SearchResult.builder()
            .cases(Arrays.asList(prepareBulkCase(), prepareBulkCase(), prepareBulkCase()))
            .build();

        stubCmsServerEndpoint(CMS_SEARCH, HttpStatus.OK, convertObjectToJsonString(result), POST);
        stubCmsServerEndpoint(CMS_BULK_CASE_SUBMIT, HttpStatus.OK, getCmsBulkCaseResponse(), POST);
        stubCmsServerEndpoint(String.format(CMS_UPDATE_CASE, CASE_ID1), HttpStatus.NOT_ACCEPTABLE, getCmsBulkCaseResponse(), POST);
        stubCmsServerEndpoint(String.format(CMS_UPDATE_CASE, CASE_ID2), HttpStatus.OK, getCmsBulkCaseResponse(), POST);
        stubCmsServerEndpoint(String.format(CMS_UPDATE_CASE, CASE_ID3), HttpStatus.OK, getCmsBulkCaseResponse(), POST);

        stubSignInForCaseworker();
        try {
            webClient.perform(post(API_URL)
                .contentType(APPLICATION_JSON)
                .accept(APPLICATION_JSON))
                .andExpect(status().isOk());

            waitAsyncCompleted();
        } catch (BulkUpdateException e) {
            /*This exception normally happens before waitAsync so the mock client context silence the exception however if the execution takes longer
              to execute an the exception happen after mock client context this will be propagated to the main context making this test fail
              intermittently.
            */
            log.info("BulkUpdateException expected", e);
        }
        verifyCmsServerEndpoint(1, String.format(CMS_UPDATE_CASE, CASE_ID1), RequestMethod.POST, UPDATE_BODY);
        verifyCmsServerEndpoint(1, String.format(CMS_UPDATE_CASE, CASE_ID2), RequestMethod.POST, UPDATE_BODY);
        verifyCmsServerEndpoint(1, String.format(CMS_UPDATE_CASE, CASE_ID3), RequestMethod.POST, UPDATE_BODY);
        verifyCmsServerEndpoint(0, String.format(CMS_UPDATE_BULK_CASE_PATH, BULK_CASE_ID, CREATE_EVENT), RequestMethod.POST);
    }

    @Test
    public void givenSearchWithoutMinimumCases_whenCreateBulkCase_thenBulkCasesIsNotCreated() throws Exception {
        SearchResult result = SearchResult.builder()
            .cases(Arrays.asList(prepareBulkCase()))
            .total(1)
            .build();

        stubCmsServerEndpoint(CMS_SEARCH, HttpStatus.OK, convertObjectToJsonString(result), POST);
        stubCmsServerEndpoint(CMS_BULK_CASE_SUBMIT, HttpStatus.OK, getCmsBulkCaseResponse(), POST);

        stubSignInForCaseworker();
        webClient.perform(post(API_URL)
            .contentType(APPLICATION_JSON)
            .accept(APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath(BULK_CASE_LIST_KEY).isEmpty());

        verifyCmsServerEndpoint(0, CMS_BULK_CASE_SUBMIT, RequestMethod.POST);

        verifyCmsServerEndpoint(0, CMS_UPDATE_CASE, RequestMethod.POST, UPDATE_BODY);
    }

    @Test(expected = BulkUpdateException.class)
    public void give5XError_whenUpdateDivorceCase_thenRetryCasesProcessOtherCases() throws Exception {
        SearchResult result = SearchResult.builder()
            .cases(Arrays.asList(prepareBulkCase(), prepareBulkCase(), prepareBulkCase()))
            .build();
        CaseDetails.builder()
            .caseId(TestConstants.TEST_CASE_ID)
            .caseData(ImmutableMap.of(CASE_LIST_KEY, result.getCases()))
            .build();

        stubCmsServerEndpoint(CMS_SEARCH, HttpStatus.OK, convertObjectToJsonString(result), POST);
        stubCmsServerEndpoint(CMS_BULK_CASE_SUBMIT, HttpStatus.OK, getCmsBulkCaseResponse(), POST);
        stubCmsServerEndpoint(String.format(CMS_UPDATE_CASE, CASE_ID1), HttpStatus.SERVICE_UNAVAILABLE, getCmsBulkCaseResponse(), POST);
        stubCmsServerEndpoint(String.format(CMS_UPDATE_CASE, CASE_ID2), HttpStatus.OK, getCmsBulkCaseResponse(), POST);
        stubCmsServerEndpoint(String.format(CMS_UPDATE_CASE, CASE_ID3), HttpStatus.UNAUTHORIZED, getCmsBulkCaseResponse(), POST);

        stubSignInForCaseworker();
        webClient.perform(post(API_URL)
            .contentType(APPLICATION_JSON)
            .accept(APPLICATION_JSON))
            .andExpect(status().isOk());

        waitAsyncCompleted();

        verifyCmsServerEndpoint(maxRetries, String.format(CMS_UPDATE_CASE, CASE_ID1), RequestMethod.POST, UPDATE_BODY);
        verifyCmsServerEndpoint(1, String.format(CMS_UPDATE_CASE, CASE_ID2), RequestMethod.POST, UPDATE_BODY);
        verifyCmsServerEndpoint(1, String.format(CMS_UPDATE_CASE, CASE_ID3), RequestMethod.POST, UPDATE_BODY);
    }

    @Test
    public void give422Error_whenUpdateDivorceCase_thenUpdateBulkCaseWithFilteredCaseList() throws Exception {
        SearchResult result = SearchResult.builder()
                .cases(Arrays.asList(prepareBulkCase(), prepareBulkCase(), prepareBulkCase()))
                .build();

        stubCmsServerEndpoint(CMS_SEARCH, HttpStatus.OK, convertObjectToJsonString(result), POST);
        stubCmsServerEndpoint(CMS_BULK_CASE_SUBMIT, HttpStatus.OK, getCmsBulkCaseResponse(), POST);
        stubCmsServerEndpoint(String.format(CMS_UPDATE_CASE, CASE_ID1), HttpStatus.UNPROCESSABLE_ENTITY, getCmsBulkCaseResponse(), POST);
        stubCmsServerEndpoint(String.format(CMS_UPDATE_CASE, CASE_ID2), HttpStatus.OK, getCmsBulkCaseResponse(), POST);
        stubCmsServerEndpoint(String.format(CMS_UPDATE_CASE, CASE_ID3), HttpStatus.OK, getCmsBulkCaseResponse(), POST);
        stubCmsServerEndpoint(String.format(CMS_UPDATE_BULK_CASE_PATH, BULK_CASE_ID, CREATE_EVENT), HttpStatus.OK, getCmsBulkCaseResponse(), POST);

        stubSignInForCaseworker();

        webClient.perform(post(API_URL)
                .contentType(APPLICATION_JSON)
                .accept(APPLICATION_JSON))
                .andExpect(status().isOk());

        waitAsyncCompleted();

        verifyCmsServerEndpoint(1, String.format(CMS_UPDATE_CASE, CASE_ID1), RequestMethod.POST, UPDATE_BODY);
        verifyCmsServerEndpoint(1, String.format(CMS_UPDATE_CASE, CASE_ID2), RequestMethod.POST, UPDATE_BODY);
        verifyCmsServerEndpoint(1, String.format(CMS_UPDATE_CASE, CASE_ID3), RequestMethod.POST, UPDATE_BODY);
        verifyCmsServerEndpoint(1, String.format(CMS_UPDATE_BULK_CASE_PATH, BULK_CASE_ID, CREATE_EVENT), RequestMethod.POST);
    }

    @Test
    public void give404Error_whenUpdateDivorceCase_thenUpdateBulkCaseWithFilteredCaseList() throws Exception {
        SearchResult result = SearchResult.builder()
                .cases(Arrays.asList(prepareBulkCase(), prepareBulkCase(), prepareBulkCase()))
                .build();

        stubCmsServerEndpoint(CMS_SEARCH, HttpStatus.OK, convertObjectToJsonString(result), POST);
        stubCmsServerEndpoint(CMS_BULK_CASE_SUBMIT, HttpStatus.OK, getCmsBulkCaseResponse(), POST);
        stubCmsServerEndpoint(String.format(CMS_UPDATE_CASE, CASE_ID1), HttpStatus.NOT_FOUND, getCmsBulkCaseResponse(), POST);
        stubCmsServerEndpoint(String.format(CMS_UPDATE_CASE, CASE_ID2), HttpStatus.OK, getCmsBulkCaseResponse(), POST);
        stubCmsServerEndpoint(String.format(CMS_UPDATE_CASE, CASE_ID3), HttpStatus.OK, getCmsBulkCaseResponse(), POST);
        stubCmsServerEndpoint(String.format(CMS_UPDATE_BULK_CASE_PATH, BULK_CASE_ID, CREATE_EVENT), HttpStatus.OK, getCmsBulkCaseResponse(), POST);

        stubSignInForCaseworker();

        webClient.perform(post(API_URL)
                .contentType(APPLICATION_JSON)
                .accept(APPLICATION_JSON))
                .andExpect(status().isOk());

        waitAsyncCompleted();

        verifyCmsServerEndpoint(1, String.format(CMS_UPDATE_CASE, CASE_ID1), RequestMethod.POST, UPDATE_BODY);
        verifyCmsServerEndpoint(1, String.format(CMS_UPDATE_CASE, CASE_ID2), RequestMethod.POST, UPDATE_BODY);
        verifyCmsServerEndpoint(1, String.format(CMS_UPDATE_CASE, CASE_ID3), RequestMethod.POST, UPDATE_BODY);
        verifyCmsServerEndpoint(1, String.format(CMS_UPDATE_BULK_CASE_PATH, BULK_CASE_ID, CREATE_EVENT), RequestMethod.POST);
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

        maintenanceServiceServer.stubFor(WireMock.request(method.name(),urlEqualTo(path))
            .willReturn(aResponse()
                .withStatus(status.value())
                .withHeader(CONTENT_TYPE, APPLICATION_JSON_UTF8_VALUE)
                .withBody(body)));
    }

    private void verifyCmsServerEndpoint(int times, String path, RequestMethod method) {
        maintenanceServiceServer.verify(times, new RequestPatternBuilder(method, urlEqualTo(path))
            .withHeader(CONTENT_TYPE, equalTo(APPLICATION_JSON_VALUE)));
    }

    private void verifyCmsServerEndpoint(int times, String path, RequestMethod method, String body) {
        maintenanceServiceServer.verify(times, new RequestPatternBuilder(method, urlEqualTo(path))
            .withHeader(CONTENT_TYPE, equalTo(APPLICATION_JSON_VALUE))
            .withRequestBody(equalTo(body)));
    }

    private String getCmsBulkCaseResponse() throws Exception {
        return loadResourceAsString(CMS_RESPONSE_BODY_FILE);
    }
}