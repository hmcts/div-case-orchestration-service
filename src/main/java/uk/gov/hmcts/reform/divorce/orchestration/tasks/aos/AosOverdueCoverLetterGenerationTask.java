package uk.gov.hmcts.reform.divorce.orchestration.tasks.aos;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.document.template.docmosis.AosOverdueCoverLetter;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.document.template.docmosis.DocmosisTemplateVars;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.PdfDocumentGenerationService;
import uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor.CtscContactDetailsDataProviderService;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.bulk.printing.BasePayloadSpecificDocumentGenerationTask;
import uk.gov.hmcts.reform.divorce.orchestration.util.CcdUtil;

import java.util.Map;

import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.CcdFields.HELP_WITH_FEES_REF_NUMBER;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.LanguagePreference.ENGLISH;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.AOS_OVERDUE_COVER_LETTER_DOCUMENT_TYPE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CASE_ID_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.document.template.DocumentType.AOS_OVERDUE_COVER_LETTER;
import static uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor.AddresseeDataExtractor.getPetitioner;
import static uk.gov.hmcts.reform.divorce.orchestration.tasks.util.TaskUtils.getOptionalPropertyValueAsString;
import static uk.gov.hmcts.reform.divorce.orchestration.util.CaseDataUtils.formatCaseIdToReferenceNumber;

@Component
public class AosOverdueCoverLetterGenerationTask extends BasePayloadSpecificDocumentGenerationTask {

    public AosOverdueCoverLetterGenerationTask(CtscContactDetailsDataProviderService ctscContactDetailsDataProviderService,
                                               PdfDocumentGenerationService pdfDocumentGenerationService,
                                               CcdUtil ccdUtil) {
        super(ctscContactDetailsDataProviderService, pdfDocumentGenerationService, ccdUtil);
    }

    @Override
    protected DocmosisTemplateVars prepareDataForPdf(TaskContext context, Map<String, Object> caseData) {
        return AosOverdueCoverLetter.aosOverdueCoverLetterBuilder()
            .ctscContactDetails(ctscContactDetailsDataProviderService.getCtscContactDetails())
            .caseReference(formatCaseIdToReferenceNumber(context.getTransientObject(CASE_ID_JSON_KEY)))
            .addressee(getPetitioner(caseData))
            .helpWithFeesNumber(getOptionalPropertyValueAsString(caseData, HELP_WITH_FEES_REF_NUMBER, null))
            .build();
    }

    @Override
    public String getTemplateId() {
        return AOS_OVERDUE_COVER_LETTER.getTemplateByLanguage(ENGLISH);
    }

    @Override
    public String getDocumentType() {
        return AOS_OVERDUE_COVER_LETTER_DOCUMENT_TYPE;
    }

}