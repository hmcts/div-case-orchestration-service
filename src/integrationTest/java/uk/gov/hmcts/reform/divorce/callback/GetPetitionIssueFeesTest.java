package uk.gov.hmcts.reform.divorce.callback;

import com.fasterxml.jackson.core.type.TypeReference;
import io.restassured.response.Response;
import net.serenitybdd.rest.SerenityRest;
import org.apache.http.entity.ContentType;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import uk.gov.hmcts.reform.divorce.context.IntegrationTest;
import uk.gov.hmcts.reform.divorce.model.ccd.CaseLink;
import uk.gov.hmcts.reform.divorce.model.idam.UserDetails;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CaseDetails;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CcdCallbackRequest;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CcdCallbackResponse;
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
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasEntry;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.CcdEvents.SOLICITOR_SUBMIT;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.CcdFields.PETITIONER_SOLICITOR_ORGANISATION_POLICY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.PETITION_ISSUE_ORDER_SUMMARY_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.PREVIOUS_CASE_ID_CCD_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.SERVICE_AUTHORIZATION_HEADER;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.SOL_APPLICATION_FEE_IN_POUNDS_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.testutil.ObjectMapperTestUtil.getJsonFromResourceFile;

public class GetPetitionIssueFeesTest extends IntegrationTest {

    private static final String NEW_CASE_FEE_IN_POUNDS = "550";
    private static final String AMEND_CASE_FEE_IN_POUNDS = "95";

    @Value("${case.orchestration.solicitor.petition-issue-fees.context-path}")
    private String petitionIssueFeesContextPath;

    @Value("${idam.s2s-auth.url}")
    private String idamS2sAuthUrl;

    @Autowired
    protected CcdClientSupport ccdClientSupport;

    protected static final String BASE_CASE_RESPONSE = "/fixtures/solicitor/case-data.json";

    protected UserDetails solicitorUser;

    @Test
    public void givenCallbackRequest_whenGetPetitionIssueFees_thenReturnUpdatedData() throws IOException {
        uk.gov.hmcts.reform.ccd.client.model.CaseDetails caseDetails = createCaseWithSolicitor(true);

        Response response = prepareAndCallCosEndpoint(caseDetails, serverUrl + petitionIssueFeesContextPath);

        assertThat(response.getStatusCode(), is(HttpStatus.OK.value()));
        CcdCallbackResponse ccdCallbackResponse = response.getBody().as(CcdCallbackResponse.class);
        assertThat("Response should not contain errors", ccdCallbackResponse.getErrors(), is(nullValue()));
        Map<String, Object> responseData = ccdCallbackResponse.getData();
        OrderSummary orderSummary = ObjectMapperTestUtil
            .convertObject(responseData.get(PETITION_ISSUE_ORDER_SUMMARY_JSON_KEY), OrderSummary.class);
        assertThat(orderSummary, is(notNullValue()));
        assertThat(responseData, hasEntry(SOL_APPLICATION_FEE_IN_POUNDS_JSON_KEY, orderSummary.getPaymentTotalInPounds()));
        assertThat(responseData, allOf(
            hasEntry(SOL_APPLICATION_FEE_IN_POUNDS_JSON_KEY, orderSummary.getPaymentTotalInPounds()),
            hasEntry(SOL_APPLICATION_FEE_IN_POUNDS_JSON_KEY, NEW_CASE_FEE_IN_POUNDS)
        ));
    }

    @Test
    public void givenCallbackRequest_whenGetPetitionIssueFees_AndPetitionerSolicitorIsNotDigital_thenReturnUpdatedData() throws IOException {
        uk.gov.hmcts.reform.ccd.client.model.CaseDetails caseDetails = createCaseWithSolicitor(false);

        Response response = prepareAndCallCosEndpoint(caseDetails, serverUrl + petitionIssueFeesContextPath);

        assertThat(response.getStatusCode(), is(HttpStatus.OK.value()));
        CcdCallbackResponse ccdCallbackResponse = response.getBody().as(CcdCallbackResponse.class);
        assertThat("Response should not contain errors", ccdCallbackResponse.getErrors(), is(nullValue()));
        Map<String, Object> responseData = ccdCallbackResponse.getData();
        OrderSummary orderSummary = ObjectMapperTestUtil
            .convertObject(responseData.get(PETITION_ISSUE_ORDER_SUMMARY_JSON_KEY), OrderSummary.class);
        assertThat(orderSummary, is(notNullValue()));
        assertThat(responseData, hasEntry(SOL_APPLICATION_FEE_IN_POUNDS_JSON_KEY, orderSummary.getPaymentTotalInPounds()));
        assertThat(responseData, allOf(
            hasEntry(SOL_APPLICATION_FEE_IN_POUNDS_JSON_KEY, orderSummary.getPaymentTotalInPounds()),
            hasEntry(SOL_APPLICATION_FEE_IN_POUNDS_JSON_KEY, NEW_CASE_FEE_IN_POUNDS)
        ));
    }

    @Test
    public void givenAmendCaseCallbackRequest_whenGetPetitionIssueFees_thenReturnUpdatedData() throws IOException {
        uk.gov.hmcts.reform.ccd.client.model.CaseDetails caseDetails = createCaseWithSolicitor(true);

        Map<String, Object> newCaseData = new HashMap<>(caseDetails.getData());
        newCaseData.put(PREVIOUS_CASE_ID_CCD_KEY, CaseLink.builder()
            .caseReference(String.valueOf(caseDetails.getId()))
            .build());

        uk.gov.hmcts.reform.ccd.client.model.CaseDetails newCaseDetails = ccdClientSupport
            .submitSolicitorCase(newCaseData, solicitorUser);

        Response response = prepareAndCallCosEndpoint(newCaseDetails, serverUrl + petitionIssueFeesContextPath);

        assertThat(response.getStatusCode(), is(HttpStatus.OK.value()));
        CcdCallbackResponse ccdCallbackResponse = response.getBody().as(CcdCallbackResponse.class);
        assertThat("Response should not contain errors", ccdCallbackResponse.getErrors(), is(nullValue()));
        Map<String, Object> responseData = ccdCallbackResponse.getData();
        OrderSummary orderSummary = ObjectMapperTestUtil
            .convertObject(responseData.get(PETITION_ISSUE_ORDER_SUMMARY_JSON_KEY), OrderSummary.class);
        assertThat(orderSummary, is(notNullValue()));
        assertThat(responseData, allOf(
            hasEntry(SOL_APPLICATION_FEE_IN_POUNDS_JSON_KEY, orderSummary.getPaymentTotalInPounds()),
            hasEntry(SOL_APPLICATION_FEE_IN_POUNDS_JSON_KEY, AMEND_CASE_FEE_IN_POUNDS)
        ));
    }

    private uk.gov.hmcts.reform.ccd.client.model.CaseDetails createCaseWithSolicitor(boolean petitionerSolicitorIsDigital) throws IOException {
        solicitorUser = createSolicitorUser();

        Map<String, Object> baseCaseData = getJsonFromResourceFile(BASE_CASE_RESPONSE, new TypeReference<HashMap<String, Object>>() {
        });
        if (!petitionerSolicitorIsDigital) {
            baseCaseData.remove(PETITIONER_SOLICITOR_ORGANISATION_POLICY);
        }

        return ccdClientSupport.submitSolicitorCase(baseCaseData, solicitorUser);
    }

    protected Response prepareAndCallCosEndpoint(
        uk.gov.hmcts.reform.ccd.client.model.CaseDetails caseDetails,
        String endpoint) {
        final CcdCallbackRequest callbackData = CcdCallbackRequest.builder()
            .eventId(SOLICITOR_SUBMIT)
            .caseDetails(CaseDetails.builder()
                .caseId(String.valueOf(caseDetails.getId()))
                .state(caseDetails.getState())
                .caseData(caseDetails.getData())
                .build())
            .build();

        return RestUtil.postToRestService(
            endpoint,
            getRequestHeaders(),
            ObjectMapperTestUtil.convertObjectToJsonString(callbackData)
        );
    }

    private Map<String, Object> getRequestHeaders() {
        Map<String, Object> requestHeaders = new HashMap<>();
        requestHeaders.put(HttpHeaders.CONTENT_TYPE, ContentType.APPLICATION_JSON.toString());
        requestHeaders.put(HttpHeaders.AUTHORIZATION, solicitorUser.getAuthToken());
        requestHeaders.put(SERVICE_AUTHORIZATION_HEADER, getS2sAuth());
        return requestHeaders;
    }

    private String getS2sAuth() {
        Response response = SerenityRest.given()
            .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
            .relaxedHTTPSValidation()
            .body(String.format("{\"microservice\": \"divorce_frontend\"}"))
            .post(idamS2sAuthUrl + "/testing-support/lease");

        String token = response.getBody().asString();

        return "Bearer " + token;
    }
}
