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
import uk.gov.hmcts.reform.divorce.orchestration.service.AosService;

import java.util.Collections;
import java.util.Map;

import static org.hamcrest.CoreMatchers.hasItem;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpStatus.OK;

@RunWith(MockitoJUnitRunner.class)
public class AosControllerTest {

    @Mock
    private AosService aosService;

    @InjectMocks
    private AosController classUnderTest;

    @Test
    public void whenAOSOverdueCallback_thenReturnCcdResponse() throws Exception {
        final Map<String, Object> caseData = Collections.emptyMap();
        final CaseDetails caseDetails = CaseDetails.builder()
                .caseData(caseData)
                .build();

        final CcdCallbackRequest ccdCallbackRequest = new CcdCallbackRequest();
        ccdCallbackRequest.setCaseDetails(caseDetails);

        when(aosService.sendPetitionerAOSOverdueNotificationEmail(ccdCallbackRequest)).thenReturn(caseData);

        ResponseEntity<CcdCallbackResponse> response = classUnderTest.notifyPetitionerOfAOSOverdue(ccdCallbackRequest);

        CcdCallbackResponse expectedResponse = CcdCallbackResponse.builder().data(caseData).build();

        assertEquals(OK, response.getStatusCode());
        assertEquals(expectedResponse, response.getBody());
    }

    @Test
    public void whenAOSOverdueCallback_exceptionOccured_thenReturnErrorResponse() throws Exception {
        final Map<String, Object> caseData = Collections.emptyMap();
        final CaseDetails caseDetails = CaseDetails.builder()
                .caseData(caseData)
                .build();

        final CcdCallbackRequest ccdCallbackRequest = new CcdCallbackRequest();
        ccdCallbackRequest.setCaseDetails(caseDetails);

        final String testErrorMessage = "Test error message";
        when(aosService.sendPetitionerAOSOverdueNotificationEmail(ccdCallbackRequest))
                .thenThrow(new WorkflowException(testErrorMessage));

        ResponseEntity<CcdCallbackResponse> response = classUnderTest.notifyPetitionerOfAOSOverdue(ccdCallbackRequest);

        assertEquals(OK, response.getStatusCode());
        assertThat(response.getBody().getErrors(), hasItem(testErrorMessage));
    }
}