package uk.gov.hmcts.reform.divorce.orchestration.workflows;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CaseDetails;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CcdCallbackRequest;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.WorkflowException;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.DefaultTaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskException;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.SendDnDecisionSolNotificationTask;

import java.util.HashMap;
import java.util.Map;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CASE_ID_JSON_KEY;

@RunWith(MockitoJUnitRunner.class)
public class DnDecisionMadeWorkflowTest {

    @Mock
    private SendDnDecisionSolNotificationTask sendDnDecisionSolNotificationTask;

    @InjectMocks
    private DnDecisionMadeWorkflow dnDecisionMadeWorkflow;

    private CcdCallbackRequest ccdCallbackRequestRequest;
    private Map<String, Object> payload;
    private TaskContext context;


    @Before
    public void setUp() {
        payload = new HashMap<>();
        CaseDetails caseDetails = CaseDetails.builder().caseData(payload).caseId(TEST_CASE_ID).build();
        ccdCallbackRequestRequest = CcdCallbackRequest.builder().caseDetails(caseDetails).build();
        context = new DefaultTaskContext();
        context.setTransientObject(CASE_ID_JSON_KEY, TEST_CASE_ID);
    }

    @Test
    public void runShouldExecuteTasksAndReturnPayload() throws WorkflowException, TaskException {
        when(sendDnDecisionSolNotificationTask.execute(context, payload)).thenReturn(payload);

        dnDecisionMadeWorkflow.run(ccdCallbackRequestRequest);
        verify(sendDnDecisionSolNotificationTask).execute(eq(context), eq(payload));
    }
}
