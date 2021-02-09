package uk.gov.hmcts.reform.divorce.orchestration.tasks.servicejourney;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskException;

import java.util.Map;

import static org.junit.Assert.assertNotNull;

@RunWith(MockitoJUnitRunner.class)
public class OrderToDispenseGenerationTaskTest extends ServiceDecisionOrderGenerationTaskTest {

    @InjectMocks
    private OrderToDispenseGenerationTask orderToDispenseGenerationTask;

    @Override
    public ServiceDecisionOrderGenerationTask getTask() {
        return orderToDispenseGenerationTask;
    }

    @Override
    protected String getExpectedDocumentType() {
        return "dispenseWithServiceGranted";
    }

    @Override
    protected String getExpectedTemplateId() {
        return "FL-DIV-DEC-ENG-00531.docx";
    }

    @Test
    public void testExecuteShouldGenerateAFile() throws TaskException {
        Map<String, Object> returnedCaseData = executeShouldGenerateAFile();
        assertNotNull(returnedCaseData);
    }
}
