package uk.gov.hmcts.reform.divorce.orchestration.tasks.servicejourney;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.junit.MockitoJUnitRunner;

import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.document.ApplicationServiceTypes.DISPENSED;

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
        return "FL-DIV-GNO-ENG-00535.docx";
    }

    @Override
    protected String documentType() {
        return DispensedServiceRefusalOrderDraftTask.FileMetadata.DOCUMENT_TYPE;
    }

    @Override
    protected String getApplicationType() {
        return DISPENSED;
    }

    @Test
    public void shouldGenerateServiceRefusalDraft() {
        shouldGenerateAndAddDraftDocument();
    }
}