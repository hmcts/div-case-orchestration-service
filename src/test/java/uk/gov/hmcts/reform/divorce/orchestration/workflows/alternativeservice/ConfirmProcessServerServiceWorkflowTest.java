package uk.gov.hmcts.reform.divorce.orchestration.workflows.alternativeservice;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CaseDetails;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.alternativeservice.AlternativeServiceDueDateSetterTask;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.alternativeservice.MarkJourneyAsServedByProcessServerTask;

import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static uk.gov.hmcts.reform.divorce.orchestration.testutil.Verificators.mockTasksExecution;
import static uk.gov.hmcts.reform.divorce.orchestration.testutil.Verificators.verifyTasksCalledInOrder;

@RunWith(MockitoJUnitRunner.class)
public class ConfirmProcessServerServiceWorkflowTest {

    @Mock
    private AlternativeServiceDueDateSetterTask alternativeServiceDueDateSetterTask;

    @Mock
    private MarkJourneyAsServedByProcessServerTask markJourneyAsServedByProcessServerTask;

    @InjectMocks
    private ConfirmProcessServerServiceWorkflow confirmProcessServerServiceWorkflow;

    @Test
    public void whenConfirmAlternativeServiceWorkflowModifyDueDateTaskIsExecuted() throws Exception {
        HashMap<String, Object> caseData = new HashMap<>();
        mockTasksExecution(caseData, alternativeServiceDueDateSetterTask, markJourneyAsServedByProcessServerTask);

        Map<String, Object> returned = confirmProcessServerServiceWorkflow.run(
            CaseDetails.builder()
                .caseData(caseData)
                .build()
        );

        assertThat(returned, is(caseData));
        verifyTasksCalledInOrder(caseData, alternativeServiceDueDateSetterTask, markJourneyAsServedByProcessServerTask);
    }
}
