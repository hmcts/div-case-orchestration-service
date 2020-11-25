package uk.gov.hmcts.reform.divorce.orchestration.workflows.alternativeservice;

import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CaseDetails;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.alternativeservice.MarkServedByAlternativeMethodAsYesTask;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.alternativeservice.MarkServedByProcessServerAsNoTask;

import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static uk.gov.hmcts.reform.divorce.orchestration.testutil.Verificators.mockTasksExecution;
import static uk.gov.hmcts.reform.divorce.orchestration.testutil.Verificators.verifyTasksCalledInOrder;

public class ConfirmServiceByAlternativeMethodWorkflowTest {

    @Mock
    private MarkServedByProcessServerAsNoTask markServedByProcessServerAsNoTask;

    @Mock
    private MarkServedByAlternativeMethodAsYesTask markServedByAlternativeMethodAsYesTask;

    @InjectMocks
    private ConfirmServiceByAlternativeMethodWorkflow confirmServiceByAlternativeMethodWorkflow;

    @Test
    public void whenConfirmServiceByAlternativeMethodWorkflow_markingProcessServerAndAltMethodTasksAreExecuted() throws Exception {
        HashMap<String, Object> caseData = new HashMap<>();
        mockTasksExecution(
            caseData,
            markServedByProcessServerAsNoTask,
            markServedByAlternativeMethodAsYesTask
        );

        Map<String, Object> returned = confirmServiceByAlternativeMethodWorkflow.run(
            CaseDetails.builder()
                .caseData(caseData)
                .build()
        );

        assertThat(returned, is(caseData));
        verifyTasksCalledInOrder(
            caseData,
            markServedByProcessServerAsNoTask,
            markServedByAlternativeMethodAsYesTask
        );
    }
}
