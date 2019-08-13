package uk.gov.hmcts.reform.divorce.orchestration.service.impl;

import com.google.common.collect.ImmutableMap;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CaseDetails;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CcdCallbackRequest;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.WorkflowException;
import uk.gov.hmcts.reform.divorce.orchestration.workflows.NotifyRespondentOfDARequestedWorkflow;
import uk.gov.hmcts.reform.divorce.orchestration.workflows.UpdateDNPronouncedCasesWorkflow;

import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_DECREE_ABSOLUTE_GRANTED_DATE;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_PETITIONER_FIRST_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_PETITIONER_LAST_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_PRONOUNCEMENT_JUDGE;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_RESPONDENT_FIRST_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_RESPONDENT_LAST_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DECREE_ABSOLUTE_GRANTED_DATE_CCD_FIELD;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D_8_CASE_REFERENCE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D_8_PETITIONER_FIRST_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D_8_PETITIONER_LAST_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.PRONOUNCEMENT_JUDGE_CCD_FIELD;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.RESP_FIRST_NAME_CCD_FIELD;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.RESP_LAST_NAME_CCD_FIELD;

@RunWith(MockitoJUnitRunner.class)
public class DecreeAbsoluteServiceImplTest {

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @InjectMocks
    private DecreeAbsoluteServiceImpl classUnderTest;

    @Mock
    private UpdateDNPronouncedCasesWorkflow updateDNPronouncedCasesWorkflow;

    @Mock
    private NotifyRespondentOfDARequestedWorkflow notifyRespondentOfDARequestedWorkflow;

    @Test
    public void run_10CasesEligibleForDA_10CasesProcessed() throws WorkflowException {
        when(updateDNPronouncedCasesWorkflow.run(AUTH_TOKEN)).thenReturn(10);

        int casesProcessed = classUnderTest.enableCaseEligibleForDecreeAbsolute(AUTH_TOKEN);

        assertEquals(10, casesProcessed);
        verify(updateDNPronouncedCasesWorkflow).run(AUTH_TOKEN);
    }

    @Test
    public void run_throwsWorkflowException_workflowExceptionThrown() throws WorkflowException {
        expectedException.expect(WorkflowException.class);
        expectedException.expectMessage("a WorkflowException message");
        when(updateDNPronouncedCasesWorkflow.run(AUTH_TOKEN)).thenThrow(new WorkflowException(" a WorkflowException message"));

        classUnderTest.enableCaseEligibleForDecreeAbsolute(AUTH_TOKEN);
    }


    @Test
    public void shouldCallTheRightWorkflow_forNotifyRespondentOfDARequested() throws WorkflowException {
        CcdCallbackRequest ccdCallbackRequest = notifyRespondentOfDaCallbackRequest();

        classUnderTest.notifyRespondentOfDARequested(ccdCallbackRequest, AUTH_TOKEN);

        verify(notifyRespondentOfDARequestedWorkflow).run(ccdCallbackRequest);

    }

    @Test
    public void shouldThrowWorkflowException_forNotifyRespondentOfDARequested() throws WorkflowException {
        expectedException.expect(WorkflowException.class);
        expectedException.expectMessage("a WorkflowException message");

        CcdCallbackRequest ccdCallbackRequest = notifyRespondentOfDaCallbackRequest();

        when(notifyRespondentOfDARequestedWorkflow.run(ccdCallbackRequest)).thenThrow(new WorkflowException(" a WorkflowException message"));

        classUnderTest.notifyRespondentOfDARequested(ccdCallbackRequest, AUTH_TOKEN);
    }

    private CcdCallbackRequest notifyRespondentOfDaCallbackRequest() {
        Map<String, Object> caseData = ImmutableMap.<String, Object>builder()
            .put(PRONOUNCEMENT_JUDGE_CCD_FIELD, TEST_PRONOUNCEMENT_JUDGE)
            .put(D_8_PETITIONER_FIRST_NAME, TEST_PETITIONER_FIRST_NAME)
            .put(D_8_PETITIONER_LAST_NAME, TEST_PETITIONER_LAST_NAME)
            .put(RESP_FIRST_NAME_CCD_FIELD, TEST_RESPONDENT_FIRST_NAME)
            .put(RESP_LAST_NAME_CCD_FIELD, TEST_RESPONDENT_LAST_NAME)
            .put(D_8_CASE_REFERENCE, TEST_CASE_ID)
            .put(DECREE_ABSOLUTE_GRANTED_DATE_CCD_FIELD, TEST_DECREE_ABSOLUTE_GRANTED_DATE)
            .build();

        return CcdCallbackRequest.builder().caseDetails(
            CaseDetails.builder().caseData(caseData).build())
            .build();
    }

}
