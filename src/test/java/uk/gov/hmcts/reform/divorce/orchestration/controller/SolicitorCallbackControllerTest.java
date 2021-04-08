package uk.gov.hmcts.reform.divorce.orchestration.controller;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
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
import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpStatus.OK;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.CcdFields.PBA_NUMBERS;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.FEE_PAY_BY_ACCOUNT;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.SOLICITOR_HOW_TO_PAY_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.DynamicList.asDynamicList;
import static uk.gov.hmcts.reform.divorce.orchestration.util.ControllerUtils.ccdRequestWithData;
import static uk.gov.hmcts.reform.divorce.orchestration.util.ControllerUtils.ccdResponseWithData;

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

        when(solicitorService.validateForPersonalServicePack(request, AUTH_TOKEN))
                .thenReturn(divorceSession);

        ResponseEntity<CcdCallbackResponse> response = classUnderTest.issuePersonalServicePack(AUTH_TOKEN, request);

        assertThat(response.getStatusCode(), is(HttpStatus.OK));
        assertThat(response.getBody().getData(), is(divorceSession));
        assertThat(response.getBody().getErrors(), is(nullValue()));

        verify(solicitorService).validateForPersonalServicePack(request, AUTH_TOKEN);
    }

    @Test
    public void whenExceptionIsThrown_thenCatchAndProceedAsExpected() throws WorkflowException {
        final Map<String, Object> divorceSession = Collections.singletonMap("key", "value");
        CcdCallbackRequest request = CcdCallbackRequest.builder()
                .caseDetails(CaseDetails.builder().caseData(divorceSession).build())
                .build();

        when(solicitorService.validateForPersonalServicePack(request, AUTH_TOKEN))
                .thenThrow(new RuntimeException("test"));

        ResponseEntity<CcdCallbackResponse> response = classUnderTest.issuePersonalServicePack(AUTH_TOKEN, request);

        assertThat(response.getStatusCode(), is(HttpStatus.OK));
        assertThat(response.getBody().getErrors().size(), is(1));
        assertThat(response.getBody().getErrors(), contains("Failed to issue solicitor personal service - test"));

        verify(solicitorService).validateForPersonalServicePack(request, AUTH_TOKEN);
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

    @Test
    public void givenPbaNumbersFoundAndPaymentMethodIsPba_whenRetrievePbaNumbers_thenReturnCcdResponse() throws WorkflowException {
        final Map<String, Object> caseDataReturnedFromService = ImmutableMap.of(
            SOLICITOR_HOW_TO_PAY_JSON_KEY, FEE_PAY_BY_ACCOUNT,
            PBA_NUMBERS, asDynamicList(ImmutableList.of("pbaNumber1", "pbaNumber2"))
        );

        CcdCallbackResponse expectedResponse = CcdCallbackResponse.builder().data(caseDataReturnedFromService).build();

        whenRetrievePbaNumbersExpect(expectedResponse, caseDataReturnedFromService);
    }

    @Test
    public void givenPaymentMethodIsNotPba_whenRetrievePbaNumbers_thenReturnCcdResponse() throws WorkflowException {
        final Map<String, Object> caseDataReturnedFromService = ImmutableMap.of(
            SOLICITOR_HOW_TO_PAY_JSON_KEY, "NotByAccount"
        );

        CcdCallbackResponse expectedResponse = ccdResponseWithData(caseDataReturnedFromService);

        final CcdCallbackRequest ccdCallbackRequest = ccdRequestWithData(caseDataReturnedFromService);

        ResponseEntity<CcdCallbackResponse> response = classUnderTest.retrievePbaNumbers(AUTH_TOKEN, ccdCallbackRequest);

        assertEquals(OK, response.getStatusCode());
        assertEquals(expectedResponse, response.getBody());
        verifyNoInteractions(solicitorService);
    }

    @Test
    public void givenNoPbaNumbersAndPaymentMethodIsPba_whenRetrievePbaNumbers_thenReturnCcdResponseWithError() throws WorkflowException {
        final Map<String, Object> caseDataReturnedFromService = ImmutableMap.of(
            SOLICITOR_HOW_TO_PAY_JSON_KEY, FEE_PAY_BY_ACCOUNT
        );

        CcdCallbackResponse expectedResponse = CcdCallbackResponse.builder()
            .errors(ImmutableList.of("No PBA number found for this account, please contact your organisation."))
            .build();

        whenRetrievePbaNumbersExpect(expectedResponse, caseDataReturnedFromService);
    }

    @Test
    public void testServiceMethodIsCalled_WhenSolicitorConfirmPersonalService() throws WorkflowException {
        when(solicitorService.solicitorConfirmPersonalService(any())).thenReturn(singletonMap("testKey", "testValue"));

        CcdCallbackRequest callbackRequest = CcdCallbackRequest.builder()
            .caseDetails(CaseDetails.builder().caseData(singletonMap("testKey", "testValue")).build())
            .build();
        ResponseEntity<CcdCallbackResponse> response = classUnderTest.solicitorConfirmPersonalService(callbackRequest);

        assertThat(response.getStatusCode(), CoreMatchers.is(OK));
        assertThat(response.getBody().getData(), hasEntry("testKey", "testValue"));
        verify(solicitorService).solicitorConfirmPersonalService(callbackRequest);
    }

    private void whenRetrievePbaNumbersExpect(CcdCallbackResponse expectedResponse, Map<String, Object> caseData) throws WorkflowException {
        final CaseDetails caseDetails = CaseDetails.builder()
            .caseData(caseData)
            .build();

        final CcdCallbackRequest ccdCallbackRequest = new CcdCallbackRequest();
        ccdCallbackRequest.setCaseDetails(caseDetails);

        when(solicitorService.retrievePbaNumbers(ccdCallbackRequest, AUTH_TOKEN)).thenReturn(caseData);

        ResponseEntity<CcdCallbackResponse> response = classUnderTest.retrievePbaNumbers(AUTH_TOKEN, ccdCallbackRequest);

        assertEquals(OK, response.getStatusCode());
        assertEquals(expectedResponse, response.getBody());
        verify(solicitorService).retrievePbaNumbers(ccdCallbackRequest, AUTH_TOKEN);
    }
}
