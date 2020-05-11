package uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.divorce.orchestration.client.DocumentGeneratorClient;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.bulk.print.DocmosisTemplateVars;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.documentgeneration.GeneratedDocumentInfo;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskException;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.AUTH_TOKEN;

@RunWith(MockitoJUnitRunner.class)
public class PdfDocumentGenerationServiceTest {

    @Mock
    private DocumentGeneratorClient documentGeneratorClient;

    @InjectMocks
    private PdfDocumentGenerationService pdfDocumentGenerationService;

    private GeneratedDocumentInfo newDocumentGeneratedByTask = GeneratedDocumentInfo.builder().build();

    @Before
    public void setup() {
        when(documentGeneratorClient.generatePDF(any(), eq(AUTH_TOKEN))).thenReturn(newDocumentGeneratedByTask);
    }

    @Test
    public void executeCallsDocumentGeneratorAndPopulatesContextWithTheFirstElement() throws TaskException {
        pdfDocumentGenerationService.generatePdf(new DocmosisTemplateVars(), "templateId", "let me in");

        // verify if it works
    }
}
