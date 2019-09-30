package uk.gov.hmcts.reform.divorce.orchestration.workflows;

import org.hamcrest.core.Is;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.DefaultTaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.RemoveCostOrderDocumentTask;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.RemoveDecreeNisiDocumentTask;

import java.util.Collections;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class RemoveDNDocumentsWorkflowTest {

    @Mock
    private RemoveDecreeNisiDocumentTask removeDecreeNisiDocumentTask;
    @Mock
    private RemoveCostOrderDocumentTask removeCostOrderDocumentTask;

    @InjectMocks
    private RemoveDNDocumentsWorkflow  classToTest;

    private Map<String, Object> testData;
    private TaskContext context;

    @Before
    public void setup() {
        testData = Collections.emptyMap();
        context = new DefaultTaskContext();
    }

    @Test
    public void runShouldExecuteTasksAndReturnPayload() throws Exception {
        when(removeDecreeNisiDocumentTask.execute(context, testData)).thenReturn(testData);
        when(removeCostOrderDocumentTask.execute(context, testData)).thenReturn(testData);

        assertThat(classToTest.run(testData), Is.is(testData));

        final InOrder inOrder = inOrder(
            removeDecreeNisiDocumentTask,
            removeCostOrderDocumentTask
        );

        inOrder.verify(removeDecreeNisiDocumentTask).execute(context, testData);
        inOrder.verify(removeCostOrderDocumentTask).execute(context, testData);
    }
}
