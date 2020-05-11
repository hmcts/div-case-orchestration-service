package uk.gov.hmcts.reform.divorce.orchestration.tasks.bulk.printing;

import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.bulk.print.DaGrantedLetter;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.bulk.print.DocmosisTemplateVars;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.documentgeneration.GeneratedDocumentInfo;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskException;
import uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.PdfDocumentGenerationService;
import uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor.CtscContactDetailsDataProviderService;
import uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor.DaGrantedLetterDataExtractor;

import java.util.Map;

import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.AUTH_TOKEN_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.tasks.util.TaskUtils.getCaseId;

@Component
public class DaGrantedLetterGenerationTask extends PrepareDataForDocumentGenerationTask {

    public static final String DA_GRANTED_LETTER_TEMPLATE_ID = "FL-FRM-APP-ENG-00009.docx";

    public DaGrantedLetterGenerationTask(CtscContactDetailsDataProviderService ctscContactDetailsDataProviderService, PdfDocumentGenerationService pdfDocumentGenerationService) {
        super(ctscContactDetailsDataProviderService);
        this.pdfDocumentGenerationService = pdfDocumentGenerationService;
    }

    private final PdfDocumentGenerationService pdfDocumentGenerationService;

    @Override
    protected DocmosisTemplateVars prepareDataForPdf(TaskContext context, Map<String, Object> caseData) throws TaskException {
        return DaGrantedLetter.builder()
            .caseReference(getCaseId(context))
            .ctscContactDetails(ctscContactDetailsDataProviderService.getCtscContactDetails())
            .addressee(DaGrantedLetterDataExtractor.getAddressee(caseData))
            .letterDate(DaGrantedLetterDataExtractor.getDaGrantedDate(caseData))
            .petitionerFullName(DaGrantedLetterDataExtractor.getPetitionerFullName(caseData))
            .respondentFullName(DaGrantedLetterDataExtractor.getRespondentFullName(caseData))
            .build();
    }

    @Override
    protected GeneratedDocumentInfo populateMetadataForGeneratedDocument(GeneratedDocumentInfo generatedDocumentInfo) {
        generatedDocumentInfo.setDocumentType("daGrantedLetter");
        generatedDocumentInfo.setFileName("da-granted-letter.pdf");

        return generatedDocumentInfo;
    }

    @Override
    protected GeneratedDocumentInfo generatePdf(TaskContext context, DocmosisTemplateVars templateModel) {
        return pdfDocumentGenerationService.generatePdf(
            templateModel,
            DA_GRANTED_LETTER_TEMPLATE_ID,
            context.getTransientObject(AUTH_TOKEN_JSON_KEY)
        );
    }
}
