package uk.gov.hmcts.reform.divorce.orchestration.workflows;

import com.google.common.collect.ImmutableMap;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CaseDetails;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CcdCallbackRequest;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.WorkflowException;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.DefaultTaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskException;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.QueueCleanStateTask;

import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.AUTH_TOKEN;

@RunWith(MockitoJUnitRunner.class)
public class CleanStatusCallbackWorkflowTest {

    @Mock
    private QueueCleanStateTask cleanStateTask;

    @InjectMocks
    private CleanStatusCallbackWorkflow classToTest;

    @Test
    public void runShouldExecuteTasksAndReturnPayload() throws WorkflowException, TaskException {
        Map<String, Object> caseData = ImmutableMap.of("someKey", "someValue");
        CcdCallbackRequest ccdCallbackRequest = CcdCallbackRequest
            .builder()
            .caseDetails(CaseDetails.builder()
                        .caseData(caseData)
                        .build())
            .build();
        classToTest.run(ccdCallbackRequest, AUTH_TOKEN);
        verify(cleanStateTask).execute(any(DefaultTaskContext.class), eq(caseData));
    }
}