package uk.gov.hmcts.reform.divorce.orchestration.workflows.notification;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CaseDetails;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.GetAmendPetitionFeeTask;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.notification.NotifyForRefusalOrderTask;

import java.util.Collections;
import java.util.Map;

import static uk.gov.hmcts.reform.divorce.orchestration.testutil.Verificators.mockTasksExecution;
import static uk.gov.hmcts.reform.divorce.orchestration.testutil.Verificators.verifyTasksCalledInOrder;

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

        mockTasksExecution(
            casePayload,
            getAmendPetitionFeeTask,
            notifyForRefusalOrderTask
        );

        notifyForRefusalOrderWorkflow.run(CaseDetails.builder().caseData(casePayload).build());

        verifyTasksCalledInOrder(
            casePayload,
            getAmendPetitionFeeTask,
            notifyForRefusalOrderTask
        );
    }
}
