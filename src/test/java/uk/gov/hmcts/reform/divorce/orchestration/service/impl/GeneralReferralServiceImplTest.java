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
import uk.gov.hmcts.reform.divorce.orchestration.workflows.generalreferral.GeneralConsiderationWorkflow;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NO_VALUE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.YES_VALUE;
import static uk.gov.hmcts.reform.divorce.orchestration.testutil.GeneralReferralUtil.buildCallbackRequest;
import static uk.gov.hmcts.reform.divorce.orchestration.testutil.GeneralReferralUtil.buildCaseDataWithGeneralReferralFee;

@RunWith(MockitoJUnitRunner.class)
public class GeneralReferralServiceImplTest {

    @Mock
    private GeneralConsiderationWorkflow generalConsiderationWorkflow;

    @InjectMocks
    private GeneralReferralServiceImpl generalReferralService;

    private CcdCallbackResponse ccdCallbackResponse;
    private CcdCallbackRequest ccdCallbackRequest;

    @Test
    public void whenGeneralReferralFee_Yes_ThenState_Is_AwaitingGeneralReferralPayment() {
        Map<String, Object> caseData = buildCaseDataWithGeneralReferralFee(YES_VALUE);
        ccdCallbackRequest = buildCallbackRequest(caseData, null);

        ccdCallbackResponse = generalReferralService.receiveReferral(ccdCallbackRequest);

        assertThat(ccdCallbackResponse.getState(), is(CcdStates.AWAITING_GENERAL_REFERRAL_PAYMENT));
    }

    @Test
    public void whenGeneralReferralFee_No_ThenState_Is_AwaitingGeneralConsideration() {
        Map<String, Object> caseData = buildCaseDataWithGeneralReferralFee(NO_VALUE);
        ccdCallbackRequest = buildCallbackRequest(caseData, null);

        ccdCallbackResponse = generalReferralService.receiveReferral(ccdCallbackRequest);

        assertThat(ccdCallbackResponse.getState(), is(CcdStates.AWAITING_GENERAL_CONSIDERATION));
    }

    @Test
    public void whenStateAwaitingGeneralReferralPayment_GeneralReferralFee_No_ThenState_Is_AwaitingGeneralConsideration() {
        Map<String, Object> caseData = buildCaseDataWithGeneralReferralFee(NO_VALUE);
        ccdCallbackRequest = buildCallbackRequest(caseData, CcdStates.AWAITING_GENERAL_REFERRAL_PAYMENT);

        ccdCallbackResponse = generalReferralService.receiveReferral(ccdCallbackRequest);

        assertThat(ccdCallbackResponse.getState(), is(CcdStates.AWAITING_GENERAL_CONSIDERATION));
    }

    @Test
    public void whenStateAwaitingGeneralConsideration_GeneralReferralFee_Yes_ThenState_Is_AwaitingGeneralReferralPayment() {
        Map<String, Object> caseData = buildCaseDataWithGeneralReferralFee(YES_VALUE);
        ccdCallbackRequest = buildCallbackRequest(caseData, CcdStates.AWAITING_GENERAL_CONSIDERATION);

        ccdCallbackResponse = generalReferralService.receiveReferral(ccdCallbackRequest);

        assertThat(ccdCallbackResponse.getState(), is(CcdStates.AWAITING_GENERAL_REFERRAL_PAYMENT));
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
}
