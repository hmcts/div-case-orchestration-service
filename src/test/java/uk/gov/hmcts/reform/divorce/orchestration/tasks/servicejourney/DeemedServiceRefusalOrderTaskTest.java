package uk.gov.hmcts.reform.divorce.orchestration.tasks.servicejourney;

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

    @Override
    protected String getExpectedDocumentType() {
        return "deemedServiceRefused";
    }

    @Override
    protected String getExpectedTemplateId() {
        return "FL-DIV-GNO-ENG-00533.docx";
    }

    @Test
    public void testExecuteShouldGenerateAFile() throws TaskException {
        Map<String, Object> returnedCaseData = executeShouldGenerateAFile();
        assertNotNull(returnedCaseData);
    }
}
