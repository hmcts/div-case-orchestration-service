package uk.gov.hmcts.reform.divorce.orchestration.workflows.generalorders;

import junit.framework.TestCase;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CaseDetails;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.WorkflowException;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.generalorders.GeneralOrderDraftRemovalTask;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.generalorders.GeneralOrderGenerationTask;
import uk.gov.hmcts.reform.divorce.orchestration.workflows.generalorder.GenerateGeneralOrderWorkflow;

import java.util.Map;

import static java.util.Collections.EMPTY_MAP;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.divorce.orchestration.testutil.Verificators.mockTasksExecution;
import static uk.gov.hmcts.reform.divorce.orchestration.testutil.Verificators.verifyTasksCalledInOrder;

@RunWith(MockitoJUnitRunner.class)
public class GenerateGeneralOrderWorkflowTest extends TestCase {

    @Mock
    private GeneralOrderGenerationTask generalOrderGenerationTask;

    @Mock
    private GeneralOrderDraftRemovalTask generalOrderDraftRemovalTask;

    @InjectMocks
    private GenerateGeneralOrderWorkflow generateGeneralOrderWorkflow;

    @Test
    public void shouldCallBothTasks() throws WorkflowException {
        Map<String, Object> caseData = EMPTY_MAP;
        mockTasksExecution(caseData, generalOrderGenerationTask, generalOrderDraftRemovalTask);

        generateGeneralOrderWorkflow.run(CaseDetails.builder().caseData(caseData).build(), AUTH_TOKEN);

        verifyTasksCalledInOrder(caseData, generalOrderGenerationTask, generalOrderDraftRemovalTask);
    }
}
