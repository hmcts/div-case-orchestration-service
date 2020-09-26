package uk.gov.hmcts.reform.divorce.orchestration.tasks.servicejourney;

import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskException;

import java.util.Map;

import static org.junit.Assert.assertNotNull;

@RunWith(MockitoJUnitRunner.class)
public class DeemedServiceRefusalOrderTaskTest extends ServiceRefusalOrderGenerationTaskTest {

    @InjectMocks
    private DeemedServiceRefusalOrderTask deemedServiceRefusalOrderTask;

    @Override
    public ServiceRefusalOrderGenerationTask getTask() {
        return deemedServiceRefusalOrderTask;
    }

    @Test
    public void testExecuteShouldGenerateAFile() throws TaskException {
        Map<String, Object> returnedCaseData = executeShouldGenerateAFile();
        assertNotNull(returnedCaseData);
    }

    @Test
    @Ignore
    public void testExecuteShouldGenerateAndAddFileToCollection() throws TaskException {
        Map<String, Object> returnedCaseData = executeShouldGenerateAndAddToCollection();
        assertNotNull(returnedCaseData);
    }

    // TODO add negative scenario when document is not generated, error thrown
}
