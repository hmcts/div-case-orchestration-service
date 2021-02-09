package uk.gov.hmcts.reform.divorce.orchestration.service.impl;

import com.google.common.collect.ImmutableMap;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.CcdFields;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.CcdStates;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CaseDetails;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CcdCallbackRequest;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CcdCallbackResponse;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.WorkflowException;
import uk.gov.hmcts.reform.divorce.orchestration.service.CaseOrchestrationServiceException;
import uk.gov.hmcts.reform.divorce.orchestration.workflows.FurtherPaymentWorkflow;
import uk.gov.hmcts.reform.divorce.orchestration.workflows.generalreferral.GeneralConsiderationWorkflow;
import uk.gov.hmcts.reform.divorce.orchestration.workflows.generalreferral.GeneralReferralWorkflow;
import uk.gov.hmcts.reform.divorce.orchestration.workflows.generalreferral.SetupGeneralReferralPaymentWorkflow;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_STATE;
import static uk.gov.hmcts.reform.divorce.orchestration.controller.util.CallbackControllerTestUtils.assertCaseOrchestrationServiceExceptionIsSetProperly;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NO_VALUE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.YES_VALUE;
import static uk.gov.hmcts.reform.divorce.orchestration.testutil.GeneralReferralUtil.buildCallbackRequest;
import static uk.gov.hmcts.reform.divorce.orchestration.testutil.GeneralReferralUtil.buildCaseDataWithGeneralReferralFee;

@RunWith(MockitoJUnitRunner.class)
public class GeneralReferralServiceImplTest {

    @Mock
    private GeneralConsiderationWorkflow generalConsiderationWorkflow;

    @Mock
    private GeneralReferralWorkflow generalReferralWorkflow;

    @Mock
    private SetupGeneralReferralPaymentWorkflow setupGeneralReferralPaymentWorkflow;

    @Mock
    private FurtherPaymentWorkflow furtherPaymentWorkflow;

    @InjectMocks
    private GeneralReferralServiceImpl generalReferralService;

    private CcdCallbackResponse ccdCallbackResponse;
    private CcdCallbackRequest ccdCallbackRequest;

    @Test
    public void whenGeneralReferralFee_Yes_ThenState_Is_AwaitingGeneralReferralPayment()
        throws CaseOrchestrationServiceException, WorkflowException {
        Map<String, Object> caseData = buildCaseDataWithGeneralReferralFee(YES_VALUE);
        ccdCallbackRequest = buildCallbackRequest(caseData, null);

        ccdCallbackResponse = generalReferralService.receiveReferral(ccdCallbackRequest);

        assertThat(ccdCallbackResponse.getState(), is(CcdStates.AWAITING_GENERAL_REFERRAL_PAYMENT));
        verify(generalReferralWorkflow).run(ccdCallbackRequest.getCaseDetails());
    }

    @Test
    public void whenGeneralReferralFee_No_ThenState_Is_AwaitingGeneralConsideration()
        throws CaseOrchestrationServiceException, WorkflowException {
        Map<String, Object> caseData = buildCaseDataWithGeneralReferralFee(NO_VALUE);
        ccdCallbackRequest = buildCallbackRequest(caseData, null);

        ccdCallbackResponse = generalReferralService.receiveReferral(ccdCallbackRequest);

        assertThat(ccdCallbackResponse.getState(), is(CcdStates.AWAITING_GENERAL_CONSIDERATION));
        verify(generalReferralWorkflow).run(ccdCallbackRequest.getCaseDetails());
    }

    @Test
    public void whenStateAwaitingGeneralReferralPayment_GeneralReferralFee_No_ThenState_Is_AwaitingGeneralConsideration()
        throws CaseOrchestrationServiceException, WorkflowException {
        Map<String, Object> caseData = buildCaseDataWithGeneralReferralFee(NO_VALUE);
        ccdCallbackRequest = buildCallbackRequest(caseData, CcdStates.AWAITING_GENERAL_REFERRAL_PAYMENT);

        ccdCallbackResponse = generalReferralService.receiveReferral(ccdCallbackRequest);

        assertThat(ccdCallbackResponse.getState(), is(CcdStates.AWAITING_GENERAL_CONSIDERATION));
        verify(generalReferralWorkflow).run(ccdCallbackRequest.getCaseDetails());
    }

    @Test
    public void workflowExceptionIsMappedToCaseOrchestrationException()
        throws WorkflowException {
        Map<String, Object> caseData = buildCaseDataWithGeneralReferralFee(NO_VALUE);
        ccdCallbackRequest = buildCallbackRequest(caseData, CcdStates.AWAITING_GENERAL_REFERRAL_PAYMENT);

        when(generalReferralWorkflow.run(any())).thenThrow(WorkflowException.class);

        CaseOrchestrationServiceException exception = assertThrows(
            CaseOrchestrationServiceException.class,
            () -> generalReferralService.receiveReferral(ccdCallbackRequest)
        );

        assertCaseOrchestrationServiceExceptionIsSetProperly(exception);
    }

    @Test
    public void whenStateAwaitingGeneralConsideration_GeneralReferralFee_Yes_ThenState_Is_AwaitingGeneralReferralPayment()
        throws CaseOrchestrationServiceException, WorkflowException {
        Map<String, Object> caseData = buildCaseDataWithGeneralReferralFee(YES_VALUE);
        ccdCallbackRequest = buildCallbackRequest(caseData, CcdStates.AWAITING_GENERAL_CONSIDERATION);

        ccdCallbackResponse = generalReferralService.receiveReferral(ccdCallbackRequest);

        assertThat(ccdCallbackResponse.getState(), is(CcdStates.AWAITING_GENERAL_REFERRAL_PAYMENT));
        verify(generalReferralWorkflow).run(ccdCallbackRequest.getCaseDetails());
    }

    @Test
    public void whenAllOk_generalConsideration_shouldCallGeneralConsiderationWorkflow()
        throws CaseOrchestrationServiceException, WorkflowException {
        CaseDetails caseDetails = CaseDetails.builder().caseData(Collections.emptyMap()).build();
        when(generalConsiderationWorkflow.run(caseDetails)).thenReturn(new HashMap<>());

        generalReferralService.generalConsideration(caseDetails);

        verify(generalConsiderationWorkflow).run(caseDetails);
    }

    @Test(expected = CaseOrchestrationServiceException.class)
    public void whenSomethingWrong_generalConsideration_shouldThrowCaseOrchestrationServiceException()
        throws CaseOrchestrationServiceException, WorkflowException {
        CaseDetails caseDetails = CaseDetails.builder().caseData(Collections.emptyMap()).build();
        when(generalConsiderationWorkflow.run(caseDetails)).thenThrow(WorkflowException.class);

        generalReferralService.generalConsideration(caseDetails);
    }

    @Test
    public void givenCaseData_whenSetupConfirmServicePaymentWorkflow_thenReturnPayload() throws Exception {
        CaseDetails caseDetails = CaseDetails.builder()
            .caseData(new HashMap<>())
            .caseId(TEST_CASE_ID)
            .state(TEST_STATE)
            .build();

        when(setupGeneralReferralPaymentWorkflow.run(eq(caseDetails))).thenReturn(new HashMap<>());

        generalReferralService.setupGeneralReferralPaymentEvent(caseDetails);

        verify(setupGeneralReferralPaymentWorkflow).run(eq(caseDetails));
    }

    @Test
    public void shouldThrowException_whenSetupGeneralReferralPaymentWorkflow_throwsWorkflowException() throws Exception {
        when(setupGeneralReferralPaymentWorkflow.run(any())).thenThrow(WorkflowException.class);

        CaseOrchestrationServiceException exception = assertThrows(
            CaseOrchestrationServiceException.class,
            () -> generalReferralService.setupGeneralReferralPaymentEvent(
                CaseDetails.builder().caseId(TEST_CASE_ID).build()
            )
        );

        assertCaseOrchestrationServiceExceptionIsSetProperly(exception);
    }

    @Test
    public void givenCaseData_whenReturnToStateBeforeGeneralReferral_thenReturnPayloadAndNewCaseState() throws Exception {
        Map<String, Object> caseData = ImmutableMap.of(
            CcdFields.GENERAL_REFERRAL_PREVIOUS_CASE_STATE, "previousCaseState");
        CaseDetails caseDetails = CaseDetails.builder()
            .caseData(caseData)
            .caseId(TEST_CASE_ID)
            .state(TEST_STATE)
            .build();

        ccdCallbackResponse = generalReferralService.returnToStateBeforeGeneralReferral(caseDetails);

        assertThat(ccdCallbackResponse.getState(), is("previousCaseState"));
    }

    @Test
    public void givenMissingPreviousStateField_whenReturnToStateBeforeGeneralReferral_thenThrowException() {
        CaseOrchestrationServiceException exception = assertThrows(
            CaseOrchestrationServiceException.class,
            () -> generalReferralService.returnToStateBeforeGeneralReferral(
                CaseDetails.builder()
                    .caseId(TEST_CASE_ID)
                    .caseData(new HashMap<>())
                    .build()
            )
        );

        assertCaseOrchestrationServiceExceptionIsSetProperly(exception);
    }

    @Test
    public void givenCaseData_whenGeneralReferralPaymentEvent_thenReturnPayload() throws Exception {
        CaseDetails caseDetails = CaseDetails.builder()
            .caseId(TEST_CASE_ID)
            .state(TEST_STATE)
            .build();

        when(furtherPaymentWorkflow.run(eq(caseDetails), anyString())).thenReturn(new HashMap<>());

        generalReferralService.generalReferralPaymentEvent(caseDetails);

        verify(furtherPaymentWorkflow).run(eq(caseDetails), anyString());
    }
}
