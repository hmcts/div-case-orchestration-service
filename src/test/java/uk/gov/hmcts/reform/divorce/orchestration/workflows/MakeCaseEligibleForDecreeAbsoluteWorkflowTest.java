package uk.gov.hmcts.reform.divorce.orchestration.workflows;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.WorkflowException;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.UpdateCaseInCCD;

import java.util.Map;

import static java.util.Collections.emptyMap;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.AUTH_TOKEN_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CASE_EVENT_ID_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CASE_ID_JSON_KEY;

@RunWith(MockitoJUnitRunner.class)
public class MakeCaseEligibleForDecreeAbsoluteWorkflowTest {

    private static final String MAKE_CASE_ELIGIBLE_FOR_DA_PETITIONER_EVENT_ID = "MakeEligibleForDA_Petitioner";

    @Mock
    UpdateCaseInCCD updateCaseMock;

    @InjectMocks
    MakeCaseEligibleForDecreeAbsoluteWorkflow makeCaseEligibleForDecreeAbsoluteWorkFlow;

    @Captor
    ArgumentCaptor<TaskContext> taskContextCaptor;

    @Test
    public void testTasksAreCalledCorrectly() throws WorkflowException {
        Map<String, Object> returnedPayload = makeCaseEligibleForDecreeAbsoluteWorkFlow.run("testAuthorisationToken", "testCaseId");

        assertThat(returnedPayload, equalTo(emptyMap()));
        verify(updateCaseMock).execute(taskContextCaptor.capture(), eq(emptyMap()));
        TaskContext taskContext = taskContextCaptor.getValue();
        assertThat(taskContext.getTransientObject(AUTH_TOKEN_JSON_KEY), equalTo("testAuthorisationToken"));
        assertThat(taskContext.getTransientObject(CASE_ID_JSON_KEY), equalTo("testCaseId"));
        assertThat(taskContext.getTransientObject(CASE_EVENT_ID_JSON_KEY), equalTo(MAKE_CASE_ELIGIBLE_FOR_DA_PETITIONER_EVENT_ID));
    }

}