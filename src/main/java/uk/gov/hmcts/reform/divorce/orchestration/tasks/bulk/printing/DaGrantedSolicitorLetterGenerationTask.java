package uk.gov.hmcts.reform.divorce.orchestration.tasks.bulk.printing;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.bulk.print.DaGrantedRespondentSolicitorCoverLetter;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.document.template.docmosis.DocmosisTemplateVars;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.PdfDocumentGenerationService;
import uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor.AddresseeDataExtractor;
import uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor.CtscContactDetailsDataProviderService;
import uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor.DatesDataExtractor;
import uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor.FullNamesDataExtractor;
import uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor.SolicitorDataExtractor;
import uk.gov.hmcts.reform.divorce.orchestration.util.CcdUtil;

import java.util.Map;

import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.LanguagePreference.ENGLISH;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DECREE_ABSOLUTE_GRANTED_SOLICITOR_LETTER_DOCUMENT_TYPE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.document.template.DocumentType.DECREE_ABSOLUTE_GRANTED_SOLICITOR_LETTER;
import static uk.gov.hmcts.reform.divorce.orchestration.tasks.util.TaskUtils.getCaseId;

@Component
public class DaGrantedSolicitorLetterGenerationTask extends BasePayloadSpecificDocumentGenerationTask {

    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static class FileMetadata {
        public static final String TEMPLATE_ID = DECREE_ABSOLUTE_GRANTED_SOLICITOR_LETTER.getTemplateByLanguage(ENGLISH);
        public static final String DOCUMENT_TYPE = DECREE_ABSOLUTE_GRANTED_SOLICITOR_LETTER_DOCUMENT_TYPE;
    }

    public DaGrantedSolicitorLetterGenerationTask(CtscContactDetailsDataProviderService ctscContactDetailsDataProviderService,
                                                  PdfDocumentGenerationService pdfDocumentGenerationService,
                                                  CcdUtil ccdUtil) {
        super(ctscContactDetailsDataProviderService, pdfDocumentGenerationService, ccdUtil);
    }

    @Override
    protected DocmosisTemplateVars prepareDataForPdf(TaskContext context, Map<String, Object> caseData) {
        return DaGrantedRespondentSolicitorCoverLetter.daGrantedRespondentSolicitorCoverLetterBuilder()
            .caseReference(getCaseId(context))
            .ctscContactDetails(ctscContactDetailsDataProviderService.getCtscContactDetails())
            .addressee(AddresseeDataExtractor.getRespondentSolicitor(caseData))
            .letterDate(DatesDataExtractor.getDaGrantedDate(caseData))
            .petitionerFullName(FullNamesDataExtractor.getPetitionerFullName(caseData))
            .respondentFullName(FullNamesDataExtractor.getRespondentFullName(caseData))
            .recipientReference(SolicitorDataExtractor.getSolicitorReference(caseData))
            .build();
    }

    @Override
    public String getTemplateId() {
        return FileMetadata.TEMPLATE_ID;
    }

    @Override
    public String getDocumentType() {
        return FileMetadata.DOCUMENT_TYPE;
    }

}