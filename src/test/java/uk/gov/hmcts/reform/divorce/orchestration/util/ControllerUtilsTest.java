package uk.gov.hmcts.reform.divorce.orchestration.util;

import org.junit.Test;
import org.springframework.http.ResponseEntity;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CaseDetails;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CcdCallbackRequest;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CcdCallbackResponse;

import java.util.List;
import java.util.Map;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonMap;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_PETITIONER_FIRST_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D_8_PETITIONER_FIRST_NAME;

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
}
