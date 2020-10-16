package uk.gov.hmcts.reform.divorce.orchestration.service.impl;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.CcdStates;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CaseDetails;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CcdCallbackRequest;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CcdCallbackResponse;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.WorkflowException;
import uk.gov.hmcts.reform.divorce.orchestration.service.CaseOrchestrationServiceException;
import uk.gov.hmcts.reform.divorce.orchestration.workflows.generalreferral.GeneralReferralWorkflow;

import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_TOKEN;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NO_VALUE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.YES_VALUE;
import static uk.gov.hmcts.reform.divorce.orchestration.testutil.GeneralReferralTestUtil.buildCallbackRequest;
import static uk.gov.hmcts.reform.divorce.orchestration.testutil.GeneralReferralTestUtil.buildCaseDataWithGeneralReferralFee;

@RunWith(MockitoJUnitRunner.class)
public class GeneralReferralServiceImplTest {

    @Mock
    private GeneralReferralWorkflow generalReferralWorkflow;

    @InjectMocks
    private GeneralReferralServiceImpl generalReferralService;

    private CcdCallbackResponse ccdCallbackResponse;
    private CcdCallbackRequest ccdCallbackRequest;

    @Test
    public void whenGeneralReferralFee_Yes_ThenState_Is_AwaitingGeneralReferralPayment()
        throws CaseOrchestrationServiceException, WorkflowException {

        Map<String, Object> caseData = buildCaseDataWithGeneralReferralFee(YES_VALUE);
        ccdCallbackRequest = buildCallbackRequest(caseData, null);

        when(generalReferralWorkflow.run(any(CaseDetails.class), anyString())).thenReturn(caseData);

        ccdCallbackResponse = generalReferralService.receiveReferral(ccdCallbackRequest, TEST_TOKEN);

        assertThat(ccdCallbackResponse.getState(), is(CcdStates.AWAITING_GENERAL_REFERRAL_PAYMENT));
        verify(generalReferralWorkflow).run(any(CaseDetails.class), anyString());
    }

    @Test
    public void whenGeneralReferralFee_No_ThenState_Is_AwaitingGeneralConsideration()
        throws CaseOrchestrationServiceException, WorkflowException {

        Map<String, Object> caseData = buildCaseDataWithGeneralReferralFee(NO_VALUE);
        ccdCallbackRequest = buildCallbackRequest(caseData, null);

        when(generalReferralWorkflow.run(any(CaseDetails.class), anyString())).thenReturn(caseData);

        ccdCallbackResponse = generalReferralService.receiveReferral(ccdCallbackRequest, TEST_TOKEN);

        assertThat(ccdCallbackResponse.getState(), is(CcdStates.AWAITING_GENERAL_CONSIDERATION));
        verify(generalReferralWorkflow).run(any(CaseDetails.class), anyString());
    }

    @Test
    public void whenStateAwaitingGeneralReferralPayment_GeneralReferralFee_No_ThenState_Is_AwaitingGeneralConsideration()
        throws CaseOrchestrationServiceException, WorkflowException {

        Map<String, Object> caseData = buildCaseDataWithGeneralReferralFee(NO_VALUE);
        ccdCallbackRequest = buildCallbackRequest(caseData, CcdStates.AWAITING_GENERAL_REFERRAL_PAYMENT);

        when(generalReferralWorkflow.run(any(CaseDetails.class), anyString())).thenReturn(caseData);

        ccdCallbackResponse = generalReferralService.receiveReferral(ccdCallbackRequest, TEST_TOKEN);

        assertThat(ccdCallbackResponse.getState(), is(CcdStates.AWAITING_GENERAL_CONSIDERATION));
        verify(generalReferralWorkflow).run(any(CaseDetails.class), anyString());
    }

    @Test
    public void whenStateAwaitingGeneralConsideration_GeneralReferralFee_Yes_ThenState_Is_AwaitingGeneralReferralPayment()
        throws CaseOrchestrationServiceException, WorkflowException {

        Map<String, Object> caseData = buildCaseDataWithGeneralReferralFee(YES_VALUE);
        ccdCallbackRequest = buildCallbackRequest(caseData, CcdStates.AWAITING_GENERAL_CONSIDERATION);

        when(generalReferralWorkflow.run(any(CaseDetails.class), anyString())).thenReturn(caseData);

        ccdCallbackResponse = generalReferralService.receiveReferral(ccdCallbackRequest, TEST_TOKEN);

        assertThat(ccdCallbackResponse.getState(), is(CcdStates.AWAITING_GENERAL_REFERRAL_PAYMENT));
        verify(generalReferralWorkflow).run(any(CaseDetails.class), anyString());
    }

    @Test
    public void whenNo_GeneralReferralFeeValueThenReturnErrors()
        throws WorkflowException {

        Map<String, Object> caseData = new HashMap<>();
        ccdCallbackRequest = buildCallbackRequest(caseData, null);

        when(generalReferralWorkflow.run(any(CaseDetails.class), anyString())).thenThrow(WorkflowException.class);

        assertThrows(CaseOrchestrationServiceException.class, () -> generalReferralService.receiveReferral(ccdCallbackRequest, TEST_TOKEN));
    }

}