package uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.divorce.orchestration.client.DocumentGeneratorClient;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.bulk.print.DaGrantedLetter;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.bulk.print.RequestTemplateVarsWrapper;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.documentgeneration.GenerateDocumentRequest;

import java.time.LocalDate;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.AUTH_TOKEN;

@RunWith(MockitoJUnitRunner.class)
public class PdfDocumentGenerationServiceTest {

    private final ArgumentCaptor<GenerateDocumentRequest> documentGenerationRequest = ArgumentCaptor.forClass(GenerateDocumentRequest.class);
    private final ArgumentCaptor<String> authTokenArg = ArgumentCaptor.forClass(String.class);
    private final String letterDate = LocalDate.now().toString();
    private final String caseId = "123-987";
    private final String templateId = "template";
    private final DaGrantedLetter model = DaGrantedLetter.builder().caseReference(caseId).letterDate(letterDate).build();

    @Mock
    private DocumentGeneratorClient documentGeneratorClient;

    @InjectMocks
    private PdfDocumentGenerationService pdfDocumentGenerationService;

    @Test
    public void executeCallsDocumentGeneratorAndPopulatesContextWithTheFirstElement() {
        pdfDocumentGenerationService.generatePdf(model, templateId, AUTH_TOKEN);

        verify(documentGeneratorClient).generatePDF(documentGenerationRequest.capture(), authTokenArg.capture());

        GenerateDocumentRequest capturedRequest = documentGenerationRequest.getValue();
        RequestTemplateVarsWrapper modelSentToPdfGenerator = (RequestTemplateVarsWrapper) capturedRequest.getValues()
            .get(OrchestrationConstants.CASE_DETAILS_JSON_KEY);
        DaGrantedLetter templateVars = (DaGrantedLetter) modelSentToPdfGenerator.getCaseData();

        assertThat(capturedRequest.getTemplate(), is(templateId));
        assertThat(modelSentToPdfGenerator.getId(), is(caseId));
        assertThat(modelSentToPdfGenerator.getId(), is(model.getCaseReference()));
        assertThat(templateVars.getCaseReference(), is(model.getCaseReference()));
        assertThat(templateVars.getLetterDate(), is(letterDate));
    }
}
