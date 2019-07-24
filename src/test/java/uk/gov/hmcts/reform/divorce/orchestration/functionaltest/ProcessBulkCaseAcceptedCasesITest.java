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
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import uk.gov.hmcts.reform.divorce.orchestration.TestConstants;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CaseDetails;
import uk.gov.hmcts.reform.divorce.orchestration.util.CaseDataUtils;

import java.util.Map;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static org.awaitility.Awaitility.await;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.HttpMethod.POST;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.http.MediaType.APPLICATION_JSON_UTF8_VALUE;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_STATE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.BULK_LISTING_CASE_ID_FIELD;
import static uk.gov.hmcts.reform.divorce.orchestration.testutil.ObjectMapperTestUtil.convertObjectToJsonString;
import static uk.gov.hmcts.reform.divorce.orchestration.testutil.ResourceLoader.loadResourceAsString;

@RunWith(SpringRunner.class)
public class ProcessBulkCaseAcceptedCasesITest extends IdamTestSupport {

    private static final String API_URL = "/bulk/remove";
    private static final String CMS_UPDATE_CASE = "/casemaintenance/version/1/updateCase/%s/unlinkBulkCaseReference";
    private static final String CMS_GET_CASE_PATH = "/casemaintenance/version/1/case/%s";

    private static final String CMS_RESPONSE_BODY_FILE = "jsonExamples/payloads/removeCaseBulkCaseCallbackRequest.json";

    private static final String CASE_ID1 = "1558711407435839";
    private static final String CASE_ID2 = "1558711407435840";
    private static final String UPDATE_BODY = "{}";

    @Autowired
    private ThreadPoolTaskExecutor asyncTaskExecutor;

    @Autowired
    private MockMvc webClient;

    @Before
    public void cleanUp() {
        maintenanceServiceServer.resetAll();
    }

    @Test
    public void givenCaseList_thenCreateBulkCaseAndUpdateAllCases() throws Exception {
        final Map<String, Object> caseData = CaseDataUtils.createCaseLinkField(BULK_LISTING_CASE_ID_FIELD, "1505150515051550");

        final CaseDetails caseDetails = CaseDetails.builder()
                                            .caseId(CASE_ID1)
                                            .state(TEST_STATE)
                                            .caseData(caseData)
                                            .build();


        stubCmsServerEndpoint(String.format(CMS_GET_CASE_PATH, CASE_ID1), HttpStatus.OK, convertObjectToJsonString(caseDetails), GET);
        stubCmsServerEndpoint(String.format(CMS_UPDATE_CASE, CASE_ID1), HttpStatus.OK, UPDATE_BODY, POST);

        stubCmsServerEndpoint(String.format(CMS_GET_CASE_PATH, CASE_ID2), HttpStatus.OK, convertObjectToJsonString(caseDetails), GET);
        stubCmsServerEndpoint(String.format(CMS_UPDATE_CASE, CASE_ID2), HttpStatus.OK, UPDATE_BODY, POST);

        stubSignInForCaseworker();
        webClient.perform(post(API_URL)
            .header(AUTHORIZATION, TestConstants.AUTH_TOKEN)
            .contentType(APPLICATION_JSON)
            .content(getCCDCallbackBody())
            .accept(APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.CaseList[*].value.CaseReference.CaseReference").value("1558711395612316"));

        waitAsyncCompleted();
        verifyCmsServerEndpoint(1, String.format(CMS_UPDATE_CASE, CASE_ID1), RequestMethod.POST, UPDATE_BODY);
        verifyCmsServerEndpoint(1, String.format(CMS_UPDATE_CASE, CASE_ID2), RequestMethod.POST, UPDATE_BODY);
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

    private void verifyCmsServerEndpoint(int times, String path, RequestMethod method) {
        maintenanceServiceServer.verify(times, new RequestPatternBuilder(method, urlEqualTo(path))
            .withHeader(CONTENT_TYPE, equalTo(APPLICATION_JSON_VALUE)));
    }

    private void verifyCmsServerEndpoint(int times, String path, RequestMethod method, String body) {
        maintenanceServiceServer.verify(times, new RequestPatternBuilder(method, urlEqualTo(path))
            .withHeader(CONTENT_TYPE, equalTo(APPLICATION_JSON_VALUE))
            .withRequestBody(equalTo(body)));
    }

    private String getCCDCallbackBody() throws Exception {
        return loadResourceAsString(CMS_RESPONSE_BODY_FILE);
    }
}