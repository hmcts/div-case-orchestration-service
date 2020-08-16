package uk.gov.hmcts.reform.divorce.orchestration.tasks.servicejourney;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.document.ApplicationServiceTypes;

@RunWith(MockitoJUnitRunner.class)
public class DispensedServiceRefusalOrderDraftTaskTest extends ServiceRefusalOrderDraftTaskTest {

    @InjectMocks
    private DispensedServiceRefusalOrderDraftTask dispensedServiceRefusalOrderDraftTask;

    @Override
    protected ServiceRefusalOrderDraftTask getTask() {
        return dispensedServiceRefusalOrderDraftTask;
    }

    @Override
    protected String getTemplateId() {
        return DispensedServiceRefusalOrderDraftTask.FileMetadata.TEMPLATE_ID;
    }

    @Override
    protected String documentType() {
        return DispensedServiceRefusalOrderDraftTask.FileMetadata.DOCUMENT_TYPE;
    }

    @Override
    protected String getServiceApplicationType() {
        return ApplicationServiceTypes.DISPENSED;
    }

    @Test
    public void shouldGenerateServiceRefusalDraft() {
        shouldGenerateAndAddDraftDocument();
    }
}