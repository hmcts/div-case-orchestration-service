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
import uk.gov.hmcts.reform.divorce.orchestration.tasks.PersonalServiceValidationTask;

import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_EVENT_ID;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_STATE;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_TOKEN;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CASE_ID_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CASE_STATE_JSON_KEY;

@RunWith(MockitoJUnitRunner.class)
public class SolConfirmServiceWorkflowTest {

    @Mock
    private PersonalServiceValidationTask personalServiceValidationTask;

    @InjectMocks
    private SolConfirmServiceWorkflow solConfirmServiceWorkflow;

    private CcdCallbackRequest ccdCallbackRequestRequest;

    private Map<String, Object> payload;

    private TaskContext context;

    @Before
    public void setUp() {
        payload = new HashMap<>();

        CaseDetails caseDetails = CaseDetails.builder()
            .caseId(TEST_CASE_ID)
            .state(TEST_STATE)
            .caseData(payload)
            .build();

        ccdCallbackRequestRequest =
            CcdCallbackRequest.builder()
                .eventId(TEST_EVENT_ID)
                .token(TEST_TOKEN)
                .caseDetails(caseDetails)
                .build();

        context = new DefaultTaskContext();
        context.setTransientObject(CASE_ID_JSON_KEY, TEST_CASE_ID);
        context.setTransientObject(CASE_STATE_JSON_KEY, TEST_STATE);
    }

    @Test
    public void whenWorkflowRunsForAdulteryCase_WithNamedCoRespondent_allTasksRun_payloadReturned() throws WorkflowException, TaskException {
        when(personalServiceValidationTask.execute(context, payload)).thenReturn(payload);

        Map<String, Object> response = solConfirmServiceWorkflow.run(ccdCallbackRequestRequest);
        assertThat(response, is(payload));

        verify(personalServiceValidationTask).execute(context, payload);
    }

    @Test
    public void whenWorkflowRunsForNonAdulteryCase_allTasksRunExceptForCoRespondent_payloadReturned() throws WorkflowException, TaskException {
        when(personalServiceValidationTask.execute(context, payload)).thenReturn(payload);

        Map<String, Object> response = solConfirmServiceWorkflow.run(ccdCallbackRequestRequest);
        assertThat(response, is(payload));

        verify(personalServiceValidationTask).execute(context, payload);
    }

}
