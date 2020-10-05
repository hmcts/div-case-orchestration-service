package uk.gov.hmcts.reform.divorce.orchestration.util;

import org.junit.Test;
import org.springframework.http.ResponseEntity;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.CcdStates;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CaseDetails;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CcdCallbackRequest;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CcdCallbackResponse;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static java.util.Collections.singletonMap;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.core.Is.is;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_ERROR;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_ERROR_CONTENT;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_PETITIONER_FIRST_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_STATE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D_8_PETITIONER_FIRST_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.FEE_PAY_BY_ACCOUNT;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.SOLICITOR_HOW_TO_PAY_JSON_KEY;

public class ControllerUtilsTest {

    @Test
    public void givenData_whenResponseWithData_thenReturnResponseWithData() {
        Map<String, Object> caseData = singletonMap(D_8_PETITIONER_FIRST_NAME, TEST_PETITIONER_FIRST_NAME);
        ResponseEntity<CcdCallbackResponse> expectedResult = ResponseEntity.ok(CcdCallbackResponse.builder()
            .data(caseData)
            .build());
        assertThat(ControllerUtils.responseWithData(caseData), is(expectedResult));
    }

    @Test
    public void givenNull_whenResponseWithData_thenReturnResponseWithData() {
        ResponseEntity<CcdCallbackResponse> expectedResult = ResponseEntity.ok(CcdCallbackResponse.builder()
            .data(null)
            .build());
        assertThat(ControllerUtils.responseWithData(null), is(expectedResult));
    }

    @Test
    public void givenData_whenResponseWithErrors_thenReturnResponseWithErrors() {
        List<String> errors = asList("error message");
        ResponseEntity<CcdCallbackResponse> expectedResult = ResponseEntity.ok(CcdCallbackResponse.builder()
            .errors(errors)
            .build());
        assertThat(ControllerUtils.responseWithErrors(errors), is(expectedResult));
    }

    @Test
    public void givenNull_whenResponseWithErrors_thenReturnResponseWithErrors() {
        ResponseEntity<CcdCallbackResponse> expectedResult = ResponseEntity.ok(CcdCallbackResponse.builder()
            .errors(null)
            .build());
        assertThat(ControllerUtils.responseWithErrors(null), is(expectedResult));
    }

    @Test
    public void givenData_whenCcdRequestWithData_thenReturnCcdRequestWithData() {
        Map<String, Object> caseData = singletonMap(D_8_PETITIONER_FIRST_NAME, TEST_PETITIONER_FIRST_NAME);
        CaseDetails caseDetails = CaseDetails.builder()
            .caseData(caseData)
            .build();
        CcdCallbackRequest expectedResult = CcdCallbackRequest.builder()
            .caseDetails(caseDetails)
            .build();
        assertThat(ControllerUtils.ccdRequestWithData(caseData), is(expectedResult));
    }

    @Test
    public void givenNull_whenCcdRequestWithData_thenReturnCcdRequestWithData() {
        CaseDetails caseDetails = CaseDetails.builder()
            .caseData(null)
            .build();
        CcdCallbackRequest expectedResult = CcdCallbackRequest.builder()
            .caseDetails(caseDetails)
            .build();
        assertThat(ControllerUtils.ccdRequestWithData(null), is(expectedResult));
    }

    @Test
    public void givenData_whenCcdResponseWithData_thenReturnCcdResponseWithData() {
        Map<String, Object> caseData = singletonMap(D_8_PETITIONER_FIRST_NAME, TEST_PETITIONER_FIRST_NAME);
        CcdCallbackResponse expectedResult = CcdCallbackResponse.builder()
            .data(caseData)
            .build();
        assertThat(ControllerUtils.ccdResponseWithData(caseData), is(expectedResult));
    }

    @Test
    public void givenNull_whenCcdResponseWithData_thenReturnCcdResponseWithData() {
        CcdCallbackResponse expectedResult = CcdCallbackResponse.builder()
            .data(null)
            .build();
        assertThat(ControllerUtils.ccdResponseWithData(null), is(expectedResult));
    }

    @Test
    public void givenData_whenCaseDetailsWithData_thenReturnCaseDetailsWithData() {
        Map<String, Object> caseData = singletonMap(D_8_PETITIONER_FIRST_NAME, TEST_PETITIONER_FIRST_NAME);
        CaseDetails expectedResult = CaseDetails.builder()
            .caseData(caseData)
            .build();
        assertThat(ControllerUtils.caseDetailsWithData(caseData), is(expectedResult));
    }

    @Test
    public void givenNull_whenCaseDetailsWithData_thenReturnCaseDetailsWithData() {
        CaseDetails expectedResult = CaseDetails.builder()
            .caseData(null)
            .build();
        assertThat(ControllerUtils.caseDetailsWithData(null), is(expectedResult));
    }

    @Test
    public void givenNonPbaCase_whenPbaPayment_thenReturnWithSameState() {
        assertCaseStateAsExpected(buildRequestWithSolicitorHowToPay("NotByAccount"), TEST_STATE);
    }

    @Test
    public void givenPbaCase_whenPbaPayment_thenReturnWithUpdatedState() {
        assertCaseStateAsExpected(buildRequestWithSolicitorHowToPay(FEE_PAY_BY_ACCOUNT), CcdStates.SUBMITTED);
    }

    @Test
    public void givenValidErrorResponse_thenContainsErrors() {
        Map<String, Object> errors = new HashMap<>();
        errors.put(TEST_ERROR, singletonList(TEST_ERROR_CONTENT));

        assertThat(ControllerUtils.isResponseErrors(TEST_ERROR, errors), is(true));
    }

    @Test
    public void givenValidResponse_thenContainsNoErrors() {
        assertThat(ControllerUtils.isResponseErrors(TEST_ERROR, Collections.emptyMap()), is(false));
    }

    @Test
    public void givenNullError_thenContainsNoErrors() {
        assertThat(ControllerUtils.isResponseErrors(TEST_ERROR, null), is(false));
    }

    @Test
    public void givenValidErrorResponse_thenReturnErrorList() {
        Map<String, Object> errors = new HashMap<>();
        errors.put(TEST_ERROR, singletonList(TEST_ERROR_CONTENT));

        assertThat(ControllerUtils.getResponseErrors(TEST_ERROR, errors), notNullValue());
        assertThat(ControllerUtils.getResponseErrors(TEST_ERROR, errors), hasSize(1));
        assertThat(ControllerUtils.getResponseErrors(TEST_ERROR, errors), hasItem(TEST_ERROR_CONTENT));
    }

    @Test
    public void givenEmptyErrorResponse_thenReturnErrorList() {
        assertThat(ControllerUtils.getResponseErrors(TEST_ERROR, null), nullValue());
        assertThat(ControllerUtils.getResponseErrors(null, Collections.emptyMap()), nullValue());
    }

    private CcdCallbackRequest buildRequestWithSolicitorHowToPay(String paymentMethod) {
        return CcdCallbackRequest.builder()
            .caseDetails(CaseDetails.builder()
                .caseData(singletonMap(SOLICITOR_HOW_TO_PAY_JSON_KEY, paymentMethod))
                .state(TEST_STATE)
                .build())
            .build();
    }

    private void assertCaseStateAsExpected(CcdCallbackRequest ccdCallbackRequest, String testState) {
        assertThat(ControllerUtils.getPbaSubmittedState(ccdCallbackRequest), is(testState));
    }

}
