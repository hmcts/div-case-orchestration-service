package uk.gov.hmcts.reform.divorce.orchestration.tasks.servicejourney;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.junit.MockitoJUnitRunner;

import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.document.ApplicationServiceTypes.DEEMED;

@RunWith(MockitoJUnitRunner.class)
public class DeemedServiceRefusalOrderDraftTaskTest extends ServiceRefusalOrderDraftTaskTest {

    @InjectMocks
    protected DeemedServiceRefusalOrderDraftTask deemedServiceRefusalOrderDraftTask;

    @Override
    protected ServiceRefusalOrderDraftTask getTask() {
        return deemedServiceRefusalOrderDraftTask;
    }

    @Override
    protected String getTemplateId() {
        return DeemedServiceRefusalOrderDraftTask.FileMetadata.TEMPLATE_ID;
    }

    @Override
    protected String documentType() {
        return DeemedServiceRefusalOrderDraftTask.FileMetadata.DOCUMENT_TYPE;
    }

    @Override
    protected String getApplicationType() {
        return DEEMED;
    }

    @Test
    public void shouldGenerateServiceRefusalDraft() {
        shouldGenerateAndAddDraftDocument();
    }

}