package uk.gov.hmcts.reform.divorce.orchestration.workflows.generalorders;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CaseDetails;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.WorkflowException;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.generalorders.GeneralOrderDraftGenerationTask;
import uk.gov.hmcts.reform.divorce.orchestration.workflows.generalorder.GenerateGeneralOrderDraftWorkflow;

import java.util.Map;

import static java.util.Collections.emptyMap;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.divorce.orchestration.testutil.Verificators.mockTasksExecution;
import static uk.gov.hmcts.reform.divorce.orchestration.testutil.Verificators.verifyTaskWasCalled;

@RunWith(MockitoJUnitRunner.class)
public class GenerateGeneralOrderDraftWorkflowTest {

    @Mock
    private GeneralOrderDraftGenerationTask generalOrderGenerationTask;

    @InjectMocks
    private GenerateGeneralOrderDraftWorkflow generateGeneralOrderDraftWorkflow;

    @Test
    public void shouldCallTheOnlyTask() throws WorkflowException {
        Map<String, Object> caseData = emptyMap();
        mockTasksExecution(caseData, generalOrderGenerationTask);

        generateGeneralOrderDraftWorkflow.run(CaseDetails.builder().caseData(caseData).build(), AUTH_TOKEN);

        verifyTaskWasCalled(caseData, generalOrderGenerationTask);
    }
}
