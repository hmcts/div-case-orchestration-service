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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static java.util.Collections.emptyList;
import static java.util.Collections.emptyMap;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_STATE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.CcdStates.AWAITING_DA;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CASE_ID_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CASE_STATE_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CCD_CASE_ID;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DECREE_NISI_GRANTED_DATE_CCD_FIELD;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.ID;
import static uk.gov.hmcts.reform.divorce.orchestration.testutil.ObjectMapperTestUtil.convertObjectToJsonString;
import static uk.gov.hmcts.reform.divorce.orchestration.util.elasticsearch.CMSElasticSearchSupport.buildDateForTodayMinusGivenPeriod;

@RunWith(SpringRunner.class)
public class MakeCasesDAOverdueITest extends MockedFunctionalTest {

    private static final String API_URL = "/cases/da/make-overdue";
    private static final String CASE_MAINTENANCE_CLIENT_UPDATE_URL = "/casemaintenance/version/1/updateCase/test.case.id/DecreeAbsoluteOverdue";
    private static final String TEST_CASE_REFERENCE_ONE = "1519-8183-5982-2007";
    private static final String TEST_CASE_REFERENCE_TWO = "1519-8183-5982-2008";
    private static final String DN_GRANTED_DATE = "2019-03-31";
    private static final String DN_GRANTED_DATA_REFERENCE = "data.DecreeNisiGrantedDate";
    private final List<CaseDetails> cases = new ArrayList<>();
    private QueryBuilder query;

    @Autowired
    private MockMvc webClient;

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

        query = QueryBuilders.boolQuery()
            .filter(QueryBuilders.matchQuery(CASE_STATE_JSON_KEY, AWAITING_DA))
            .filter(QueryBuilders.rangeQuery(DN_GRANTED_DATA_REFERENCE).lte(buildDateForTodayMinusGivenPeriod("1y")));
    }

    @Test
    public void givenCaseIsInAwaitingDA_WhenMakeCaseOverdueForDAIsCalled_CaseMaintenanceServiceIsCalled() throws Exception {
        Map<String, Object> responseData = Collections.singletonMap(ID, TEST_CASE_ID);
        stubCaseMaintenanceUpdateEndpoint(responseData);
        stubCaseMaintenanceSearchEndpoint(cases, query);

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
        stubCaseMaintenanceUpdateEndpoint(emptyMap());
        stubCaseMaintenanceSearchEndpoint(emptyList(), query);

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
        stubCaseMaintenanceUpdateEndpoint(emptyMap());
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

    private void stubCaseMaintenanceSearchEndpointToReturnHttp500() {
        maintenanceServiceServer.stubFor(WireMock.post(CASE_MAINTENANCE_CLIENT_SEARCH_URL)
            .willReturn(aResponse()
                .withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
                .withStatus(HttpStatus.INTERNAL_SERVER_ERROR.value())));
    }

}