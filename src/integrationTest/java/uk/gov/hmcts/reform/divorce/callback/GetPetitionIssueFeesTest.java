package uk.gov.hmcts.reform.divorce.callback;

import com.fasterxml.jackson.core.type.TypeReference;
import io.restassured.response.Response;
import org.apache.http.entity.ContentType;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import uk.gov.hmcts.reform.divorce.context.IntegrationTest;
import uk.gov.hmcts.reform.divorce.model.ccd.CaseLink;
import uk.gov.hmcts.reform.divorce.model.idam.UserDetails;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CaseDetails;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CcdCallbackRequest;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.fees.OrderSummary;
import uk.gov.hmcts.reform.divorce.orchestration.testutil.ObjectMapperTestUtil;
import uk.gov.hmcts.reform.divorce.support.CcdClientSupport;
import uk.gov.hmcts.reform.divorce.util.RestUtil;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasEntry;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.PETITION_ISSUE_ORDER_SUMMARY_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.PREVIOUS_CASE_ID_CCD_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.SOLICITOR_SUBMIT_EVENT;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.SOL_APPLICATION_FEE_IN_POUNDS_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.testutil.ObjectMapperTestUtil.getJsonFromResourceFile;

public class GetPetitionIssueFeesTest extends IntegrationTest {

    private static final String NEW_CASE_FEE_IN_POUNDS = "550";
    private static final String AMEND_CASE_FEE_IN_POUNDS = "95";

    @Value("${case.orchestration.solicitor.petition-issue-fees.context-path}")
    private String contextPath;

    @Autowired
    private CcdClientSupport ccdClientSupport;

    private static final String BASE_CASE_RESPONSE = "/fixtures/solicitor/case-data.json";

    private UserDetails solicitorUser;

    private uk.gov.hmcts.reform.ccd.client.model.CaseDetails caseDetails;
    private Map<String, Object> baseCaseData;

    @Before
    public void setUp() throws IOException {
        solicitorUser = createSolicitorUser();

        baseCaseData = getJsonFromResourceFile(BASE_CASE_RESPONSE, new TypeReference<HashMap<String, Object>>() {
        });
        caseDetails = ccdClientSupport.submitSolicitorCase(baseCaseData, solicitorUser);
    }

    @Test
    public void givenCallbackRequest_whenGetPetitionIssueFees_thenReturnUpdatedData() {
        final CcdCallbackRequest callbackData = CcdCallbackRequest.builder()
            .eventId(SOLICITOR_SUBMIT_EVENT)
            .caseDetails(CaseDetails.builder()
                .caseId(String.valueOf(caseDetails.getId()))
                .state(caseDetails.getState())
                .caseData(caseDetails.getData())
                .build())
            .build();

        Response response = RestUtil.postToRestService(
            serverUrl + contextPath,
            getRequestHeaders(),
            ObjectMapperTestUtil.convertObjectToJsonString(callbackData)
        );

        assertThat(response.getStatusCode(), is(HttpStatus.OK.value()));
        Map<String, Object> responseData = response.getBody().path(DATA);
        OrderSummary orderSummary = ObjectMapperTestUtil.convertObject(responseData.get(PETITION_ISSUE_ORDER_SUMMARY_JSON_KEY), OrderSummary.class);
        assertThat(orderSummary, is(notNullValue()));
        assertThat(responseData, hasEntry(SOL_APPLICATION_FEE_IN_POUNDS_JSON_KEY, orderSummary.getPaymentTotalInPounds()));
        assertThat(responseData, allOf(
            hasEntry(SOL_APPLICATION_FEE_IN_POUNDS_JSON_KEY, orderSummary.getPaymentTotalInPounds()),
            hasEntry(SOL_APPLICATION_FEE_IN_POUNDS_JSON_KEY, NEW_CASE_FEE_IN_POUNDS)
        ));
    }

    @Test
    public void givenAmendCaseCallbackRequest_whenGetPetitionIssueFees_thenReturnUpdatedData() {
        Map<String, Object> newCaseData = new HashMap<>(baseCaseData);
        newCaseData.put(PREVIOUS_CASE_ID_CCD_KEY, CaseLink.builder().caseReference(String.valueOf(caseDetails.getId())).build());
        uk.gov.hmcts.reform.ccd.client.model.CaseDetails newCaseDetails = ccdClientSupport.submitSolicitorCase(newCaseData, solicitorUser);

        final CcdCallbackRequest callbackData = CcdCallbackRequest.builder()
            .eventId(SOLICITOR_SUBMIT_EVENT)
            .caseDetails(CaseDetails.builder()
                .caseId(String.valueOf(newCaseDetails.getId()))
                .state(newCaseDetails.getState())
                .caseData(newCaseDetails.getData())
                .build())
            .build();

        Response response = RestUtil.postToRestService(
            serverUrl + contextPath,
            getRequestHeaders(),
            ObjectMapperTestUtil.convertObjectToJsonString(callbackData)
        );

        assertThat(response.getStatusCode(), is(HttpStatus.OK.value()));
        Map<String, Object> responseData = response.getBody().path(DATA);
        OrderSummary orderSummary = ObjectMapperTestUtil.convertObject(responseData.get(PETITION_ISSUE_ORDER_SUMMARY_JSON_KEY), OrderSummary.class);
        assertThat(orderSummary, is(notNullValue()));
        assertThat(responseData, allOf(
            hasEntry(SOL_APPLICATION_FEE_IN_POUNDS_JSON_KEY, orderSummary.getPaymentTotalInPounds()),
            hasEntry(SOL_APPLICATION_FEE_IN_POUNDS_JSON_KEY, AMEND_CASE_FEE_IN_POUNDS)
        ));
    }

    private Map<String, Object> getRequestHeaders() {
        Map<String, Object> requestHeaders = new HashMap<>();
        requestHeaders.put(HttpHeaders.CONTENT_TYPE, ContentType.APPLICATION_JSON.toString());
        requestHeaders.put(HttpHeaders.AUTHORIZATION, solicitorUser.getAuthToken());
        return requestHeaders;
    }

}