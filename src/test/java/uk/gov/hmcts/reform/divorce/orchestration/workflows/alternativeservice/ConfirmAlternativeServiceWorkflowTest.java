package uk.gov.hmcts.reform.divorce.orchestration.workflows.alternativeservice;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CaseDetails;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.AlternativeServiceDueDateSetterTask;

import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static uk.gov.hmcts.reform.divorce.orchestration.testutil.Verificators.mockTasksExecution;
import static uk.gov.hmcts.reform.divorce.orchestration.testutil.Verificators.verifyTaskWasCalled;

@RunWith(MockitoJUnitRunner.class)
public class ConfirmAlternativeServiceWorkflowTest {

    @Mock
    private AlternativeServiceDueDateSetterTask alternativeServiceDueDateSetterTask;

    @InjectMocks
    private ConfirmAlternativeServiceWorkflow confirmAlternativeServiceWorkflow;

    @Test
    public void whenConfirmAlternativeServiceWorkflowModifyDueDateTaskIsExecuted() throws Exception {
        HashMap<String, Object> caseData = new HashMap<>();
        mockTasksExecution(caseData, alternativeServiceDueDateSetterTask);

        Map<String, Object> returned = confirmAlternativeServiceWorkflow.run(
            CaseDetails.builder()
                .caseData(caseData)
                .build()
        );

        assertThat(returned, is(caseData));
        verifyTaskWasCalled(caseData, alternativeServiceDueDateSetterTask);
    }
}
