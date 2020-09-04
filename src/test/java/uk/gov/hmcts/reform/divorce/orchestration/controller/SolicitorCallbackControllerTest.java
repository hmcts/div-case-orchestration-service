package uk.gov.hmcts.reform.divorce.orchestration.controller;

import org.hamcrest.CoreMatchers;
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
import uk.gov.hmcts.reform.divorce.orchestration.service.SolicitorService;

import java.util.Collections;
import java.util.Map;

import static java.util.Collections.singletonMap;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.collection.IsMapContaining.hasEntry;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpStatus.OK;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.AUTH_TOKEN;

@RunWith(MockitoJUnitRunner.class)
public class SolicitorCallbackControllerTest {

    @Mock
    SolicitorService solicitorService;

    @InjectMocks
    SolicitorCallbackController classUnderTest;

    @Test
    public void whenIssuePersonalServicePack_thenProceedAsExpected() throws WorkflowException {
        final Map<String, Object> divorceSession = Collections.singletonMap("key", "value");
        CcdCallbackRequest request = CcdCallbackRequest.builder()
                .caseDetails(CaseDetails.builder().caseData(divorceSession).build())
                .build();

        when(solicitorService.issuePersonalServicePack(request, AUTH_TOKEN))
                .thenReturn(divorceSession);

        ResponseEntity<CcdCallbackResponse> response = classUnderTest.issuePersonalServicePack(AUTH_TOKEN, request);

        assertThat(response.getStatusCode(), is(HttpStatus.OK));
        assertThat(response.getBody().getData(), is(divorceSession));
        assertThat(response.getBody().getErrors(), is(nullValue()));

        verify(solicitorService).issuePersonalServicePack(request, AUTH_TOKEN);
    }

    @Test
    public void whenExceptionIsThrown_thenCatchAndProceedAsExpected() throws WorkflowException {
        final Map<String, Object> divorceSession = Collections.singletonMap("key", "value");
        CcdCallbackRequest request = CcdCallbackRequest.builder()
                .caseDetails(CaseDetails.builder().caseData(divorceSession).build())
                .build();

        when(solicitorService.issuePersonalServicePack(request, AUTH_TOKEN))
                .thenThrow(new RuntimeException("test"));

        ResponseEntity<CcdCallbackResponse> response = classUnderTest.issuePersonalServicePack(AUTH_TOKEN, request);

        assertThat(response.getStatusCode(), is(HttpStatus.OK));
        assertThat(response.getBody().getErrors().size(), is(1));
        assertThat(response.getBody().getErrors(), contains("Failed to issue solicitor personal service - test"));

        verify(solicitorService).issuePersonalServicePack(request, AUTH_TOKEN);
    }

    @Test
    public void testServiceMethodIsCalled_WhenSendSolicitorPersonalServiceEmail() throws WorkflowException {
        when(solicitorService.sendSolicitorPersonalServiceEmail(any())).thenReturn(singletonMap("returnedKey", "returnedValue"));

        CcdCallbackRequest callbackRequest = CcdCallbackRequest.builder()
            .caseDetails(CaseDetails.builder().caseData(singletonMap("incomingKey", "incomingValue")).build())
            .build();
        ResponseEntity<CcdCallbackResponse> response = classUnderTest.sendSolicitorPersonalServiceEmail(callbackRequest);

        assertThat(response.getStatusCode(), CoreMatchers.is(OK));
        assertThat(response.getBody().getData(), hasEntry("returnedKey", "returnedValue"));
        verify(solicitorService).sendSolicitorPersonalServiceEmail(callbackRequest);
    }
}
