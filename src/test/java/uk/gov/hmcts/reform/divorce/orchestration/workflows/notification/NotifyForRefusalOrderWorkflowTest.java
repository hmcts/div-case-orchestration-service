package uk.gov.hmcts.reform.divorce.orchestration.workflows.notification;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.GetAmendPetitionFeeTask;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.notification.NotifyForRefusalOrderTask;

import java.util.Collections;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNotNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class NotifyForRefusalOrderWorkflowTest {

    @Mock
    private NotifyForRefusalOrderTask notifyForRefusalOrderTask;

    @Mock
    private GetAmendPetitionFeeTask getAmendPetitionFeeTask;

    @InjectMocks
    private NotifyForRefusalOrderWorkflow notifyForRefusalOrderWorkflow;

    @Test
    public void notifyPetitionerForRefusalOrderClarificationTaskIsExecuted() throws Exception {
        Map<String, Object> casePayload = Collections.emptyMap();
        when(getAmendPetitionFeeTask.execute(isNotNull(), eq(casePayload))).thenReturn(casePayload);
        when(notifyForRefusalOrderTask.execute(isNotNull(), eq(casePayload))).thenReturn(casePayload);

        notifyForRefusalOrderWorkflow.run(casePayload);

        verify(getAmendPetitionFeeTask).execute(any(TaskContext.class), eq(casePayload));
        verify(notifyForRefusalOrderTask).execute(any(TaskContext.class), eq(casePayload));
    }
}
