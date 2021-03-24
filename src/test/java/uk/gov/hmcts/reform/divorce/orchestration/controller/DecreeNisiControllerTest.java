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
import uk.gov.hmcts.reform.divorce.orchestration.service.CaseOrchestrationServiceException;
import uk.gov.hmcts.reform.divorce.orchestration.service.DecreeNisiService;

import java.util.Collections;
import java.util.Map;
import java.util.Objects;

import static java.util.Collections.singletonMap;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpStatus.OK;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.AUTH_TOKEN;

@RunWith(MockitoJUnitRunner.class)
public class DecreeNisiControllerTest {

    @Mock
    private DecreeNisiService decreeNisiService;

    @InjectMocks
    private DecreeNisiController classUnderTest;

    @Test
    public void whenGenerateManualDnPronouncedDocuments_thenExecuteService() throws CaseOrchestrationServiceException {
        Map<String, Object> payload = singletonMap("testKey", "testValue");
        CcdCallbackRequest incomingRequest = CcdCallbackRequest.builder()
            .caseDetails(CaseDetails.builder()
                .caseData(payload)
                .build())
            .build();

        when(decreeNisiService
            .handleManualDnPronouncementDocumentGeneration(incomingRequest, AUTH_TOKEN))
            .thenReturn(payload);

        ResponseEntity<CcdCallbackResponse> response = classUnderTest.generateManualDnDocuments(AUTH_TOKEN, incomingRequest);

        assertThat(response.getStatusCode(), is(OK));
    }

    @Test
    public void whenGenerateManualDnPronouncedDocuments_exceptionOccurred_returnsResponseError() throws Exception {
        final Map<String, Object> caseData = Collections.emptyMap();
        final CaseDetails caseDetails = CaseDetails.builder()
            .caseData(caseData)
            .build();
        final CcdCallbackRequest ccdCallbackRequest = new CcdCallbackRequest();
        ccdCallbackRequest.setCaseDetails(caseDetails);

        final String TEST_MSG = "Test message";
        when(decreeNisiService.handleManualDnPronouncementDocumentGeneration(ccdCallbackRequest, AUTH_TOKEN))
            .thenThrow(new CaseOrchestrationServiceException(TEST_MSG));

        ResponseEntity<CcdCallbackResponse> response = classUnderTest.generateManualDnDocuments(AUTH_TOKEN, ccdCallbackRequest);
        assertTrue(Objects.requireNonNull(response.getBody()).getErrors().contains(TEST_MSG));
    }

    @Test
    public void whenHandleDnManualPronouncement_thenExecuteService() throws CaseOrchestrationServiceException {
        Map<String, Object> payload = singletonMap("testKey", "testValue");
        CcdCallbackRequest incomingRequest = CcdCallbackRequest.builder()
            .caseDetails(CaseDetails.builder()
                .caseData(payload)
                .build())
            .build();

        when(decreeNisiService
            .setDNGrantedManual(incomingRequest, AUTH_TOKEN))
            .thenReturn(payload);

        ResponseEntity<CcdCallbackResponse> response = classUnderTest.dnPronouncedManual(AUTH_TOKEN, incomingRequest);

        assertThat(response.getStatusCode(), is(OK));
    }

    @Test
    public void whenHandleDnManualPronouncement_exceptionOccurred_returnsResponseError() throws Exception {
        final Map<String, Object> caseData = Collections.emptyMap();
        final CaseDetails caseDetails = CaseDetails.builder()
            .caseData(caseData)
            .build();
        final CcdCallbackRequest ccdCallbackRequest = new CcdCallbackRequest();
        ccdCallbackRequest.setCaseDetails(caseDetails);

        final String TEST_MSG = "Test message";
        when(decreeNisiService.setDNGrantedManual(ccdCallbackRequest, AUTH_TOKEN))
            .thenThrow(new CaseOrchestrationServiceException(TEST_MSG));

        ResponseEntity<CcdCallbackResponse> response = classUnderTest.dnPronouncedManual(AUTH_TOKEN, ccdCallbackRequest);
        assertTrue(Objects.requireNonNull(response.getBody()).getErrors().contains(TEST_MSG));
    }
}
