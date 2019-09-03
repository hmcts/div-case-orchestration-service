package uk.gov.hmcts.reform.divorce.orchestration.workflows;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CaseDetails;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CcdCallbackRequest;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.WorkflowException;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskException;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.RemoveLegalAdvisorMakeDecisionFieldsTask;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.DUMMY_CASE_DATA;

@RunWith(MockitoJUnitRunner.class)
public class RemoveLegalAdvisorMakeDecisionFieldsWorkflowTest {

    @Mock
    private RemoveLegalAdvisorMakeDecisionFieldsTask removeLegalAdvisorMakeDecisionFieldsTask;

    @InjectMocks
    private RemoveLegalAdvisorMakeDecisionFieldsWorkflow classToTest;

    @Test
    public void givenWorkflow_thenExecuteTask() throws WorkflowException, TaskException {
        when(removeLegalAdvisorMakeDecisionFieldsTask.execute(any(TaskContext.class), eq(DUMMY_CASE_DATA))).thenReturn(DUMMY_CASE_DATA);

        CcdCallbackRequest ccdCallbackRequest = CcdCallbackRequest.builder()
                .caseDetails(CaseDetails.builder().caseData(DUMMY_CASE_DATA).build())
                .build();

        assertThat(classToTest.run(ccdCallbackRequest), is(DUMMY_CASE_DATA));

        verify(removeLegalAdvisorMakeDecisionFieldsTask).execute(any(TaskContext.class), eq(DUMMY_CASE_DATA));
    }


}
