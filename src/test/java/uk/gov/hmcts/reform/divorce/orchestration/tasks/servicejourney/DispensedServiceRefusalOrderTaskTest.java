package uk.gov.hmcts.reform.divorce.orchestration.tasks.servicejourney;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskException;

@RunWith(MockitoJUnitRunner.class)
public class DispensedServiceRefusalOrderTaskTest extends ServiceRefusalOrderGenerationTaskTest {

    @InjectMocks
    private DispensedServiceRefusalOrderTask dispensedServiceRefusalOrderTask;

    @Override
    public ServiceRefusalOrderGenerationTask getTask() {
        return dispensedServiceRefusalOrderTask;
    }

    @Test
    public void testExecuteShouldGenerateAFile() throws TaskException {
       executeShouldGenerateAFile();
    }
}
