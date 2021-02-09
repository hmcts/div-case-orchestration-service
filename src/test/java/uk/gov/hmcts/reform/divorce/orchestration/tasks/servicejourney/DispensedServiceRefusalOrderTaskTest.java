package uk.gov.hmcts.reform.divorce.orchestration.tasks.servicejourney;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskException;

import java.util.Map;

import static org.junit.Assert.assertNotNull;

@RunWith(MockitoJUnitRunner.class)
public class DispensedServiceRefusalOrderTaskTest extends ServiceRefusalOrderGenerationTaskTest {

    @InjectMocks
    private DispensedServiceRefusalOrderTask dispensedServiceRefusalOrderTask;

    @Override
    public ServiceRefusalOrderGenerationTask getTask() {
        return dispensedServiceRefusalOrderTask;
    }

    @Override
    protected String getExpectedDocumentType() {
        return "dispenseWithServiceRefused";
    }

    @Override
    protected String getExpectedTemplateId() {
        return "FL-DIV-GNO-ENG-00535.docx";
    }

    @Test
    public void testExecuteShouldGenerateAFile() throws TaskException {
        Map<String, Object> returnedCaseData = executeShouldGenerateAFile();
        assertNotNull(returnedCaseData);
    }
}