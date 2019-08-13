package uk.gov.hmcts.reform.divorce.orchestration.controller;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CaseDetails;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CcdCallbackRequest;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CcdCallbackResponse;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.WorkflowException;
import uk.gov.hmcts.reform.divorce.orchestration.service.CaseOrchestrationService;

import java.util.Collections;
import java.util.Map;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.DOCUMENT_TYPE;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.DUMMY_CASE_DATA;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.FILE_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEMPLATE_ID;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_CASE_ID;

@RunWith(MockitoJUnitRunner.class)
public class BulkCaseControllerTest {

    @Mock
    private CaseOrchestrationService caseOrchestrationService;

    @InjectMocks
    private BulkCaseController classUnderTest;


    @Test
    public void whenSearchCases_thenReturnExpectedResponse() throws WorkflowException {
        Map<String, Object> expected = Collections.emptyMap();

        when(caseOrchestrationService.generateBulkCaseForListing()).thenReturn(expected);

        ResponseEntity<Map<String, Object>> response = classUnderTest.createBulkCase();

        assertThat(response.getStatusCode(), is(HttpStatus.OK));
        assertThat(response.getBody(), is(expected));

    }

    @Test
    public void whenScheduleCases_thenReturnExpectedResponse() throws WorkflowException {
        CcdCallbackRequest request = CcdCallbackRequest.builder().build();
        String authToken = "authToken";

        when(caseOrchestrationService.processBulkCaseScheduleForHearing(request, authToken))
                .thenReturn(Collections.emptyMap());

        ResponseEntity<CcdCallbackResponse> response = classUnderTest.scheduleBulkCaseForHearing(authToken, request);

        assertThat(response.getStatusCode(), is(HttpStatus.OK));
        assertThat(response.getBody(), is(CcdCallbackResponse.builder().build()));
    }

    @Test
    public void whenValidateBulkCaseListingData_thenReturnExpectedResponse() throws WorkflowException {
        CaseDetails caseDetails = CaseDetails.builder().caseData(Collections.emptyMap()).build();
        CcdCallbackRequest request = CcdCallbackRequest.builder().caseDetails(caseDetails).build();

        when(caseOrchestrationService.validateBulkCaseListingData(Collections.emptyMap()))
                .thenReturn(Collections.emptyMap());

        ResponseEntity<CcdCallbackResponse> response = classUnderTest.validateBulkCaseListingData(request);

        assertThat(response.getStatusCode(), is(HttpStatus.OK));
        assertThat(response.getBody(), is(CcdCallbackResponse.builder().build()));
    }

    @Test
    public void whenValidateBulkCaseListingDataThrowsError_thenReturnExpectedResponse() throws WorkflowException {
        CaseDetails caseDetails = CaseDetails.builder().caseData(Collections.emptyMap()).build();
        CcdCallbackRequest request = CcdCallbackRequest.builder().caseDetails(caseDetails).build();
        String error = "error has occurred";

        when(caseOrchestrationService.validateBulkCaseListingData(Collections.emptyMap()))
                .thenThrow(new WorkflowException(error));

        ResponseEntity<CcdCallbackResponse> response = classUnderTest.validateBulkCaseListingData(request);

        assertThat(response.getStatusCode(), is(HttpStatus.OK));
        assertThat(response.getBody(), is(CcdCallbackResponse.builder().errors(Collections.singletonList(error)).build()));
    }

    @Test
    public void whenUpdateBulkCasePronouncementDate_thenReturnExpectedResponse() throws WorkflowException {
        CaseDetails caseDetails = CaseDetails.builder().caseId(TEST_CASE_ID).caseData(Collections.emptyMap()).build();
        CcdCallbackRequest request = CcdCallbackRequest.builder().caseDetails(caseDetails).build();

        when(caseOrchestrationService.updateBulkCaseDnPronounce(caseDetails, AUTH_TOKEN))
                .thenReturn(Collections.emptyMap());

        ResponseEntity<CcdCallbackResponse> response = classUnderTest.updateCaseDnPronounce(AUTH_TOKEN, request);

        assertThat(response.getStatusCode(), is(HttpStatus.OK));
        assertThat(response.getBody(), is(CcdCallbackResponse.builder().data(Collections.emptyMap()).build()));
    }

    @Test
    public void whenUpdateBulkCasePronouncementDateThrowsError_thenReturnExpectedResponse() throws WorkflowException {
        CaseDetails caseDetails = CaseDetails.builder().caseId(TEST_CASE_ID).caseData(Collections.emptyMap()).build();
        CcdCallbackRequest request = CcdCallbackRequest.builder().caseDetails(caseDetails).build();
        String error = "error has occurred";

        when(caseOrchestrationService.updateBulkCaseDnPronounce(caseDetails, AUTH_TOKEN))
                .thenThrow(new WorkflowException(error));

        ResponseEntity<CcdCallbackResponse> response = classUnderTest.updateCaseDnPronounce(AUTH_TOKEN, request);

        assertThat(response.getStatusCode(), is(HttpStatus.OK));
        assertThat(response.getBody(), is(CcdCallbackResponse.builder().errors(Collections.singletonList(error)).build()));
    }


    @Test
    public void whenRemoveCasesFromBulk_thenReturnExpectedResponse() throws WorkflowException {
        CaseDetails caseDetails = CaseDetails.builder().caseId(TEST_CASE_ID).caseData(Collections.emptyMap()).build();
        CcdCallbackRequest request = CcdCallbackRequest.builder().caseDetails(caseDetails).build();

        when(caseOrchestrationService.updateBulkCaseAcceptedCases(caseDetails, AUTH_TOKEN))
            .thenReturn(DUMMY_CASE_DATA);

        ResponseEntity<CcdCallbackResponse> response = classUnderTest.removeCasesFromBulk(AUTH_TOKEN, request);

        assertThat(response.getStatusCode(), is(HttpStatus.OK));
        assertThat(response.getBody(), is(CcdCallbackResponse.builder().data(DUMMY_CASE_DATA).build()));
    }

    @Test
    public void whenRemoveCasesFromBulkThrowsError_thenReturnExpectedResponse() throws WorkflowException {
        CaseDetails caseDetails = CaseDetails.builder().caseId(TEST_CASE_ID).caseData(Collections.emptyMap()).build();
        CcdCallbackRequest request = CcdCallbackRequest.builder().caseDetails(caseDetails).build();
        String error = "error has occurred";

        when(caseOrchestrationService.updateBulkCaseAcceptedCases(caseDetails, AUTH_TOKEN))
            .thenThrow(new WorkflowException(error));

        ResponseEntity<CcdCallbackResponse> response = classUnderTest.removeCasesFromBulk(AUTH_TOKEN, request);

        assertThat(response.getStatusCode(), is(HttpStatus.OK));
        assertThat(response.getBody(), is(CcdCallbackResponse.builder().errors(Collections.singletonList(error)).build()));
    }

    @Test
    public void whenEditValidBulk_thenReturnExpectedResponse() throws WorkflowException {
        CaseDetails caseDetails = CaseDetails.builder().caseId(TEST_CASE_ID).caseData(Collections.emptyMap()).build();
        CcdCallbackRequest request = CcdCallbackRequest.builder().caseDetails(caseDetails).build();


        when(caseOrchestrationService.editBulkCaseListingData(request, FILE_NAME, TEMPLATE_ID, DOCUMENT_TYPE, AUTH_TOKEN))
            .thenReturn(DUMMY_CASE_DATA);

        ResponseEntity<CcdCallbackResponse> response = classUnderTest
            .editBulkCaseListingData(AUTH_TOKEN, TEMPLATE_ID, DOCUMENT_TYPE, FILE_NAME, request);

        assertThat(response.getStatusCode(), is(HttpStatus.OK));
        assertThat(response.getBody(), is(CcdCallbackResponse.builder().data(DUMMY_CASE_DATA).build()));
    }

    @Test
    public void whenEditBulkThrowsError_thenReturnExpectedResponse() throws WorkflowException {
        CaseDetails caseDetails = CaseDetails.builder().caseId(TEST_CASE_ID).caseData(Collections.emptyMap()).build();
        CcdCallbackRequest request = CcdCallbackRequest.builder().caseDetails(caseDetails).build();
        String error = "error has occurred";

        when(caseOrchestrationService.editBulkCaseListingData(request, FILE_NAME, TEMPLATE_ID, DOCUMENT_TYPE, AUTH_TOKEN))
            .thenThrow(new WorkflowException(error));

        ResponseEntity<CcdCallbackResponse> response = classUnderTest
            .editBulkCaseListingData(AUTH_TOKEN, TEMPLATE_ID, DOCUMENT_TYPE, FILE_NAME, request);

        assertThat(response.getStatusCode(), is(HttpStatus.OK));
        assertThat(response.getBody(), is(CcdCallbackResponse.builder().errors(Collections.singletonList(error)).build()));
    }
}
