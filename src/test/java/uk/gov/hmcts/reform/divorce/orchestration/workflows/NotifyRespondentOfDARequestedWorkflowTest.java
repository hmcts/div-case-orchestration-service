package uk.gov.hmcts.reform.divorce.orchestration.workflows;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CaseDetails;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CcdCallbackRequest;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.WorkflowException;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.DefaultTaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskException;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.SendDaRequestedNotifyRespondentEmailTask;

import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_STATE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.APPLY_FOR_DA;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CASE_DETAILS_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.YES_VALUE;

@RunWith(MockitoJUnitRunner.class)
public class NotifyRespondentOfDARequestedWorkflowTest {

    @Mock
    private SendDaRequestedNotifyRespondentEmailTask sendDaRequestedNotifyRespondentEmailTask;

    @InjectMocks
    private NotifyRespondentOfDARequestedWorkflow notifyRespondentOfDARequestedWorkflow;

    @Test
    public void callsTheRequiredTaskWithAllExpectedDataIsOk() throws WorkflowException, TaskException {
        final TaskContext context = new DefaultTaskContext();
        final Map<String, Object> payload = new HashMap<>();
        payload.put(APPLY_FOR_DA, YES_VALUE);

        when(sendDaRequestedNotifyRespondentEmailTask.execute(ArgumentMatchers.any(), ArgumentMatchers.any(Map.class)))
            .thenReturn(payload);

        final CaseDetails caseDetails = CaseDetails.builder()
            .caseId(TEST_CASE_ID)
            .state(TEST_STATE)
            .caseData(payload)
            .build();

        final CcdCallbackRequest ccdCallbackRequest = CcdCallbackRequest.builder().caseDetails(caseDetails).build();
        final Map<String, Object> result = notifyRespondentOfDARequestedWorkflow.run(ccdCallbackRequest);

        context.setTransientObject(CASE_DETAILS_JSON_KEY, caseDetails);

        assertThat(result, is(payload));

        verify(sendDaRequestedNotifyRespondentEmailTask).execute(context, payload);
    }

    @Test(expected = WorkflowException.class)
    public void callsTheRequiredTaskWithoutApplyForDaFlagThrowsWorkflowException() throws WorkflowException, TaskException {
        final TaskContext context = new DefaultTaskContext();
        final Map<String, Object> payload = new HashMap<>();

        final CaseDetails caseDetails = CaseDetails.builder()
            .caseId(TEST_CASE_ID)
            .state(TEST_STATE)
            .caseData(payload)
            .build();

        final CcdCallbackRequest ccdCallbackRequest = CcdCallbackRequest.builder().caseDetails(caseDetails).build();
        notifyRespondentOfDARequestedWorkflow.run(ccdCallbackRequest);
    }

}
