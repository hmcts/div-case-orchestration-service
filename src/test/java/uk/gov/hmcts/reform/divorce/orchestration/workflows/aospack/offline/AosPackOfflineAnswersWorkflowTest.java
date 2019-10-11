package uk.gov.hmcts.reform.divorce.orchestration.workflows.aospack.offline;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.WorkflowException;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskException;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.aospack.offline.RespondentAosAnswersProcessor;

import java.util.Map;

import static java.util.Collections.singletonMap;
import static org.hamcrest.collection.IsMapContaining.hasEntry;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.parties.DivorceParty.CO_RESPONDENT;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.parties.DivorceParty.RESPONDENT;

@RunWith(MockitoJUnitRunner.class)
public class AosPackOfflineAnswersWorkflowTest {

    @Mock
    private RespondentAosAnswersProcessor respondentAosAnswersProcessor;

    @InjectMocks
    private AosPackOfflineAnswersWorkflow classUnderTest;

    @Test
    public void shouldCallRespondentTask_ForRespondent() throws WorkflowException, TaskException {
        Map<String, Object> payload = singletonMap("testKey", "testValue");
        when(respondentAosAnswersProcessor.execute(any(), eq(payload))).thenReturn(singletonMap("returnedKey", "returnedValue"));

        Map<String, Object> returnedPayload = classUnderTest.run(payload, RESPONDENT);

        verify(respondentAosAnswersProcessor).execute(any(), eq(payload));
        assertThat(returnedPayload, hasEntry("returnedKey", "returnedValue"));
    }

    @Test
    public void shouldCallNoTasks_ForCoRespondent() throws WorkflowException, TaskException {
        Map<String, Object> payload = singletonMap("testKey", "testValue");
        Map<String, Object> returnedPayload = classUnderTest.run(payload, CO_RESPONDENT);

        verify(respondentAosAnswersProcessor, never()).execute(any(), any());
        assertThat(returnedPayload, hasEntry("testKey", "testValue"));
    }

}