package uk.gov.hmcts.reform.divorce.orchestration.workflows.generalorders;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CaseDetails;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.WorkflowException;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.generalorders.GeneralOrderFieldsRemovalTask;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.generalorders.GeneralOrderGenerationTask;
import uk.gov.hmcts.reform.divorce.orchestration.workflows.generalorder.GenerateGeneralOrderWorkflow;

import java.util.Map;

import static java.util.Collections.emptyMap;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.divorce.orchestration.testutil.Verificators.mockTasksExecution;
import static uk.gov.hmcts.reform.divorce.orchestration.testutil.Verificators.verifyTasksCalledInOrder;

@RunWith(MockitoJUnitRunner.class)
public class GenerateGeneralOrderWorkflowTest {

    @Mock
    private GeneralOrderGenerationTask generalOrderGenerationTask;

    @Mock
    private GeneralOrderFieldsRemovalTask generalOrderFieldsRemovalTask;

    @InjectMocks
    private GenerateGeneralOrderWorkflow generateGeneralOrderWorkflow;

    @Test
    public void shouldCallBothTasks() throws WorkflowException {
        Map<String, Object> caseData = emptyMap();
        mockTasksExecution(caseData, generalOrderGenerationTask, generalOrderFieldsRemovalTask);

        generateGeneralOrderWorkflow.run(CaseDetails.builder().caseData(caseData).build(), AUTH_TOKEN);

        verifyTasksCalledInOrder(caseData, generalOrderGenerationTask, generalOrderFieldsRemovalTask);
    }
}
