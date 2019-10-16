package uk.gov.hmcts.reform.divorce.orchestration.workflows;

import org.hamcrest.core.Is;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CaseDetails;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.WorkflowException;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.SetDNDecisionStateTask;

import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.DUMMY_CASE_DATA;

@RunWith(MockitoJUnitRunner.class)
public class DecreeNisiDecisionStateWorkflowTest {

    @Mock
    private SetDNDecisionStateTask setDNDecisionStateTask;

    @InjectMocks
    private DecreeNisiDecisionStateWorkflow classToTest;

    @Test
    public void testExecuteWorkflow_thenAllTaskExecuted() throws WorkflowException {

        when(setDNDecisionStateTask.execute(any(TaskContext.class), eq(DUMMY_CASE_DATA))).thenReturn(DUMMY_CASE_DATA);
        CaseDetails caseDetails = CaseDetails.builder().caseData(DUMMY_CASE_DATA).build();
        assertThat(classToTest.run(caseDetails), Is.is(DUMMY_CASE_DATA));

    }

}
