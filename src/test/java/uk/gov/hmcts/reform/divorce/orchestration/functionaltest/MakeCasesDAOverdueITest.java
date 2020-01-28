package uk.gov.hmcts.reform.divorce.orchestration.functionaltest;

import com.github.tomakehurst.wiremock.client.WireMock;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CaseDetails;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.SearchResult;
import uk.gov.hmcts.reform.divorce.orchestration.service.SearchSourceFactory;
import uk.gov.hmcts.reform.divorce.orchestration.util.CcdUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_STATE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.AWAITING_DA;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CASE_ID_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CASE_STATE_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CCD_CASE_ID;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DECREE_NISI_GRANTED_DATE_CCD_FIELD;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.ID;
import static uk.gov.hmcts.reform.divorce.orchestration.testutil.ObjectMapperTestUtil.convertObjectToJsonString;

@RunWith(SpringRunner.class)
public class MakeCasesDAOverdueITest extends MockedFunctionalTest {

    private static final String API_URL = "/cases/da/make-overdue";
    private static final String CASE_MAINTENANCE_CLIENT_UPDATE_URL = "/casemaintenance/version/1/updateCase/test.case.id/DecreeAbsoluteOverdue";
    private static final String CASE_MAINTENANCE_CLIENT_SEARCH_URL = "/casemaintenance/version/1/search";
    private static final String TEST_CASE_REFERENCE_ONE = "1519-8183-5982-2007";
    private static final String TEST_CASE_REFERENCE_TWO = "1519-8183-5982-2008";
    private static final String DN_GRANTED_DATE = "2019-03-31";
    private static final String DN_GRANTED_DATA_REFERENCE = "data.DecreeNisiGrantedDate";
    private SearchResult searchResult;
    private List<String> caseIds = new ArrayList<>();
    private List<CaseDetails> cases = new ArrayList<>();
    String expectedRequestBody;

    @Autowired
    private MockMvc webClient;

    @Autowired
    private CcdUtil ccdUtil;

    @Before
    public void setup() {
        maintenanceServiceServer.resetAll();
        Map<String, Object> caseData1 = new HashMap<>();
        caseData1.put(DECREE_NISI_GRANTED_DATE_CCD_FIELD, DN_GRANTED_DATE);
        caseData1.put(CASE_STATE_JSON_KEY, AWAITING_DA);
        caseData1.put(CASE_ID_JSON_KEY, TEST_CASE_REFERENCE_ONE);

        Map<String, Object> caseData2 = new HashMap<>();
        caseData2.put(DECREE_NISI_GRANTED_DATE_CCD_FIELD, DN_GRANTED_DATE);
        caseData2.put(CASE_STATE_JSON_KEY, AWAITING_DA);
        caseData2.put(CCD_CASE_ID, TEST_CASE_REFERENCE_TWO);

        CaseDetails caseDetails1 = CaseDetails.builder()
            .caseId(TEST_CASE_ID)
            .state(TEST_STATE)
            .caseData(caseData1)
            .build();

        CaseDetails caseDetails2 = CaseDetails.builder()
            .caseId(TEST_CASE_ID)
            .state(TEST_STATE)
            .caseData(caseData2)
            .build();

        cases.add(caseDetails1);
        cases.add(caseDetails2);

        caseIds.add((String) caseData1.get(CASE_ID_JSON_KEY));
        caseIds.add((String) caseData2.get(CASE_ID_JSON_KEY));

        QueryBuilder stateQuery = QueryBuilders.matchQuery(CASE_STATE_JSON_KEY, AWAITING_DA);
        QueryBuilder dateFilter = QueryBuilders.rangeQuery(DN_GRANTED_DATA_REFERENCE).lte(buildCoolOffPeriodForDAOverdue("1y"));

        expectedRequestBody = SearchSourceFactory.buildCMSBooleanSearchSource(0, 50, stateQuery, dateFilter);
    }

    @Test
    public void givenCaseIsInAwaitingDA_WhenMakeCaseOverdueForDAIsCalled_CaseMaintenanceServiceIsCalled() throws Exception {

        searchResult = SearchResult.builder()
            .total(2)
            .cases(cases)
            .build();

        Map<String, Object> responseData = Collections.singletonMap(ID, TEST_CASE_ID);
        stubCaseMaintenanceUpdateEndpoint(responseData);
        stubCaseMaintenanceSearchEndpoint(expectedRequestBody, searchResult);

        webClient.perform(post(API_URL)
            .header(AUTHORIZATION, AUTH_TOKEN)
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk());

        maintenanceServiceServer.verify(1, postRequestedFor(urlPathEqualTo(CASE_MAINTENANCE_CLIENT_SEARCH_URL)));
        maintenanceServiceServer.verify(2, postRequestedFor(urlPathEqualTo(CASE_MAINTENANCE_CLIENT_UPDATE_URL)));
    }

    @Test
    public void givenNoCasesInAwaitingDA_WhenMakeCaseOverdueForDAIsCalled_UpdateInCcdIsNotCalled() throws Exception {

        searchResult = SearchResult.builder()
            .total(0)
            .cases(Collections.emptyList())
            .build();

        stubCaseMaintenanceUpdateEndpoint(Collections.emptyMap());
        stubCaseMaintenanceSearchEndpoint(expectedRequestBody, searchResult);

        webClient.perform(post(API_URL)
            .header(AUTHORIZATION, AUTH_TOKEN)
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk());

        maintenanceServiceServer.verify(1, postRequestedFor(urlPathEqualTo(CASE_MAINTENANCE_CLIENT_SEARCH_URL)));
        maintenanceServiceServer.verify(0, postRequestedFor(urlPathEqualTo(CASE_MAINTENANCE_CLIENT_UPDATE_URL)));
    }

    @Test
    public void givenExceptionEncounteredInJob_WhenMakeCaseOverdueForDAIsCalled_ReturnInternalServerError() throws Exception {

        searchResult = SearchResult.builder()
            .total(0)
            .cases(Collections.emptyList())
            .build();

        stubCaseMaintenanceUpdateEndpoint(Collections.emptyMap());
        stubCaseMaintenanceSearchEndpointToReturnHttp500();

        webClient.perform(post(API_URL)
            .header(AUTHORIZATION, AUTH_TOKEN)
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isInternalServerError());

        maintenanceServiceServer.verify(1, postRequestedFor(urlPathEqualTo(CASE_MAINTENANCE_CLIENT_SEARCH_URL)));
        maintenanceServiceServer.verify(0, postRequestedFor(urlPathEqualTo(CASE_MAINTENANCE_CLIENT_UPDATE_URL)));
    }

    private void stubCaseMaintenanceUpdateEndpoint(Map<String, Object> response) {
        maintenanceServiceServer.stubFor(WireMock.post(CASE_MAINTENANCE_CLIENT_UPDATE_URL)
            .willReturn(aResponse()
                .withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
                .withStatus(HttpStatus.OK.value())
                .withBody(convertObjectToJsonString(response))));
    }

    private void stubCaseMaintenanceSearchEndpoint(String body, SearchResult response) {
        maintenanceServiceServer.stubFor(WireMock.post(CASE_MAINTENANCE_CLIENT_SEARCH_URL)
            .withRequestBody(equalTo(body))
            .willReturn(aResponse()
                .withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
                .withStatus(HttpStatus.OK.value())
                .withBody(convertObjectToJsonString(response))));
    }

    private void stubCaseMaintenanceSearchEndpointToReturnHttp500() {
        maintenanceServiceServer.stubFor(WireMock.post(CASE_MAINTENANCE_CLIENT_SEARCH_URL)
            .willReturn(aResponse()
                .withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
                .withStatus(HttpStatus.INTERNAL_SERVER_ERROR.value())));
    }

    private static String buildCoolOffPeriodForDAOverdue(final String coolOffPeriod) {
        String timeUnit = String.valueOf(coolOffPeriod.charAt(coolOffPeriod.length() - 1));
        return String.format("now/%s-%s", timeUnit, coolOffPeriod);
    }
}
