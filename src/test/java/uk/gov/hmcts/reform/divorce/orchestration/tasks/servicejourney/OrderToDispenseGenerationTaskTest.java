package uk.gov.hmcts.reform.divorce.orchestration.tasks.servicejourney;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskException;

@RunWith(MockitoJUnitRunner.class)
public class OrderToDispenseGenerationTaskTest extends ServiceDecisionOrderGenerationTaskTest {

    @InjectMocks
    private OrderToDispenseGenerationTask orderToDispenseGenerationTask;

    @Override
    public ServiceDecisionOrderGenerationTask getTask() {
        return orderToDispenseGenerationTask;
    }

    @Test
    public void testExecuteShouldGenerateAFile() throws TaskException {
        executeShouldGenerateAFile();
    }
}
