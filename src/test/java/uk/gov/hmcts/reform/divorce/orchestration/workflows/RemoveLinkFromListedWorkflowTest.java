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
import uk.gov.hmcts.reform.divorce.orchestration.tasks.RemoveCertificateOfEntitlementDocumentsTask;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.RemoveListingDataTask;

import java.util.Collections;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class RemoveLinkFromListedWorkflowTest {

    @Mock
    private RemoveListingDataTask removeListingDataTask;
    @Mock
    private RemoveCertificateOfEntitlementDocumentsTask removeCertificateOfEntitlementDocumentsTask;

    @InjectMocks
    private RemoveLinkFromListedWorkflow classToTest;

    private Map<String, Object> testData;
    private TaskContext context;

    @Before
    public void setup() {
        testData = Collections.emptyMap();
        context = new DefaultTaskContext();
    }

    @Test
    public void runShouldExecuteTasksAndReturnPayload() throws Exception {
        when(removeListingDataTask.execute(context, testData)).thenReturn(testData);
        when(removeCertificateOfEntitlementDocumentsTask.execute(context, testData)).thenReturn(testData);

        assertThat(classToTest.run(testData), Is.is(testData));

        final InOrder inOrder = inOrder(
            removeListingDataTask,
            removeCertificateOfEntitlementDocumentsTask
        );

        inOrder.verify(removeListingDataTask).execute(context, testData);
        inOrder.verify(removeCertificateOfEntitlementDocumentsTask).execute(context, testData);
    }
}
