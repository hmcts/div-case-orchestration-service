package uk.gov.hmcts.reform.divorce.orchestration.controller;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.ResponseEntity;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CaseDetails;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CcdCallbackRequest;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CcdCallbackResponse;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.WorkflowException;
import uk.gov.hmcts.reform.divorce.orchestration.service.DecreeAbsoluteService;

import java.util.Collections;
import java.util.Map;
import java.util.Objects;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpStatus.OK;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.AUTH_TOKEN;

@RunWith(MockitoJUnitRunner.class)
public class DecreeAbsoluteControllerTest {

    @Mock
    private DecreeAbsoluteService decreeAbsoluteService;

    @InjectMocks
    private DecreeAbsoluteController classUnderTest;

    @Test
    public void notifyRespondentOfDARequested_happyPath() throws Exception {
        final Map<String, Object> caseData = Collections.emptyMap();
        final CaseDetails caseDetails = CaseDetails.builder()
            .caseData(caseData)
            .build();
        final CcdCallbackRequest ccdCallbackRequest = new CcdCallbackRequest();
        ccdCallbackRequest.setCaseDetails(caseDetails);

        when(decreeAbsoluteService.notifyRespondentOfDARequested(ccdCallbackRequest, AUTH_TOKEN)).thenReturn(caseData);
        ResponseEntity<CcdCallbackResponse> response = classUnderTest.notifyRespondentOfDARequested(AUTH_TOKEN, ccdCallbackRequest);

        assertEquals(OK, response.getStatusCode());
        CcdCallbackResponse expectedResponse = CcdCallbackResponse.builder().data(caseData).build();
        assertEquals(expectedResponse, response.getBody());
    }

    @Test
    public void notifyRespondentOfDARequested_exceptionOccurred_returnsResponseError() throws Exception {
        final Map<String, Object> caseData = Collections.emptyMap();
        final CaseDetails caseDetails = CaseDetails.builder()
            .caseData(caseData)
            .build();
        final CcdCallbackRequest ccdCallbackRequest = new CcdCallbackRequest();
        ccdCallbackRequest.setCaseDetails(caseDetails);

        final String TEST_MSG = "Test message";
        when(decreeAbsoluteService.notifyRespondentOfDARequested(ccdCallbackRequest, AUTH_TOKEN))
            .thenThrow(new WorkflowException(TEST_MSG));

        ResponseEntity<CcdCallbackResponse> response = classUnderTest.notifyRespondentOfDARequested(AUTH_TOKEN, ccdCallbackRequest);
        assert (Objects.requireNonNull(response.getBody()).getErrors().contains(TEST_MSG));
    }
}
