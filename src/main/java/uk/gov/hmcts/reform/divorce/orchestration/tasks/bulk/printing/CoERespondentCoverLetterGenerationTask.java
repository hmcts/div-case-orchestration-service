package uk.gov.hmcts.reform.divorce.orchestration.tasks.bulk.printing;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.bulk.print.CoERespondentCoverLetter;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.document.template.docmosis.DocmosisTemplateVars;
import uk.gov.hmcts.reform.divorce.orchestration.exception.CourtDetailsNotFound;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.InvalidDataForTaskException;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.PdfDocumentGenerationService;
import uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor.AddresseeDataExtractor;
import uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor.CoECoverLetterDataExtractor;
import uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor.CtscContactDetailsDataProviderService;
import uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor.DatesDataExtractor;
import uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor.FullNamesDataExtractor;
import uk.gov.hmcts.reform.divorce.orchestration.service.impl.CourtLookupService;
import uk.gov.hmcts.reform.divorce.orchestration.util.CcdUtil;

import java.util.Map;

import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.LanguagePreference.ENGLISH;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.document.AOSPackOfflineConstants.COE_RESPONDENT_LETTER_DOCUMENT_TYPE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.document.template.DocumentType.COE_RESPONDENT_LETTER;
import static uk.gov.hmcts.reform.divorce.orchestration.tasks.util.TaskUtils.getCaseId;

@Component
public class CoERespondentCoverLetterGenerationTask extends BasePayloadSpecificDocumentGenerationTask {

    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static class FileMetadata {
        public static final String TEMPLATE_ID = COE_RESPONDENT_LETTER.getTemplateByLanguage(ENGLISH);
        public static final String DOCUMENT_TYPE = COE_RESPONDENT_LETTER_DOCUMENT_TYPE;
    }

    private final CourtLookupService courtLookupService;

    public CoERespondentCoverLetterGenerationTask(
        CtscContactDetailsDataProviderService ctscContactDetailsDataProviderService,
        PdfDocumentGenerationService pdfDocumentGenerationService,
        CcdUtil ccdUtil,
        CourtLookupService courtLookupService) {
        super(ctscContactDetailsDataProviderService, pdfDocumentGenerationService, ccdUtil);
        this.courtLookupService = courtLookupService;
    }

    @Override
    protected DocmosisTemplateVars prepareDataForPdf(TaskContext context, Map<String, Object> caseData) {
        return CoERespondentCoverLetter.coERespondentCoverLetterBuilder()
            .petitionerFullName(FullNamesDataExtractor.getPetitionerFullName(caseData))
            .respondentFullName(FullNamesDataExtractor.getRespondentFullName(caseData))
            .caseReference(getCaseId(context))
            .letterDate(DatesDataExtractor.getLetterDate())
            .ctscContactDetails(ctscContactDetailsDataProviderService.getCtscContactDetails())
            .hearingDate(DatesDataExtractor.getHearingDate(caseData))
            .costClaimGranted(CoECoverLetterDataExtractor.isCostsClaimGranted(caseData))
            .deadlineToContactCourtBy(DatesDataExtractor.getDeadlineToContactCourtBy(caseData))
            .addressee(AddresseeDataExtractor.getRespondent(caseData))
            .husbandOrWife(CoECoverLetterDataExtractor.getHusbandOrWife(caseData))
            .courtName(getCourtName(caseData))
            .build();
    }

    private String getCourtName(Map<String, Object> caseData) {
        try {
            return courtLookupService.getDnCourtByKey(CoECoverLetterDataExtractor.getCourtId(caseData)).getName();
        } catch (CourtDetailsNotFound e) {
            throw new InvalidDataForTaskException(e);
        }
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
