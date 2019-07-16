package uk.gov.hmcts.reform.divorce.orchestration.workflows.decreeabsolute;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.WorkflowException;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskException;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.CalculateDecreeAbsoluteDates;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.notification.NotifyApplicantCanFinaliseDivorceTask;

import java.util.Map;

import static java.util.Collections.singletonMap;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasEntry;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CASE_ID_JSON_KEY;

@RunWith(MockitoJUnitRunner.class)
public class ApplicantDecreeAbsoluteEligibilityWorkflowTest {

    private Map<String, Object> incomingPayload;

    @Mock
    private CalculateDecreeAbsoluteDates calculateDecreeAbsoluteDates;

    @Mock
    private NotifyApplicantCanFinaliseDivorceTask notifyApplicantCanFinaliseDivorceTask;

    @InjectMocks
    private ApplicantDecreeAbsoluteEligibilityWorkflow workflow;

    @Captor
    private ArgumentCaptor<TaskContext> taskContextArgumentCaptor;

    @Before
    public void setUp() {
        incomingPayload = singletonMap("dummyKey", "dummyValue");
    }

    @Test
    public void testTasksAreCalledAccordingly() throws TaskException, WorkflowException {
        Map<String, Object> outputMapFromFirstTask = singletonMap("outputKeyTask1", "outputValueTask1");
        when(calculateDecreeAbsoluteDates.execute(any(), eq(incomingPayload))).thenReturn(outputMapFromFirstTask);
        when(notifyApplicantCanFinaliseDivorceTask.execute(any(), eq(outputMapFromFirstTask)))
            .thenReturn(singletonMap("keyReturnedFromTask", "valueReturnedFromTask"));

        Map<String, Object> returnedPayload = workflow.run("testCaseId", incomingPayload);

        assertThat(returnedPayload, hasEntry("keyReturnedFromTask", "valueReturnedFromTask"));
        verify(calculateDecreeAbsoluteDates).execute(any(), eq(incomingPayload));
        verify(notifyApplicantCanFinaliseDivorceTask).execute(taskContextArgumentCaptor.capture(), eq(outputMapFromFirstTask));
        TaskContext taskContext = taskContextArgumentCaptor.getValue();
        assertThat(taskContext.getTransientObject(CASE_ID_JSON_KEY), equalTo("testCaseId"));
    }

}