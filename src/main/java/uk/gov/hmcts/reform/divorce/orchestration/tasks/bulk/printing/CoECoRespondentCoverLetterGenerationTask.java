package uk.gov.hmcts.reform.divorce.orchestration.tasks.bulk.printing;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.bsp.common.model.document.Addressee;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.LanguagePreference;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.bulk.print.CoECoverLetter;
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
import uk.gov.hmcts.reform.divorce.utils.DateUtils;

import java.time.LocalDate;
import java.util.Map;

import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.document.template.DocumentType.COE_CO_RESPONDENT_LETTER;
import static uk.gov.hmcts.reform.divorce.orchestration.tasks.util.TaskUtils.getCaseId;
import static uk.gov.hmcts.reform.divorce.orchestration.util.PartyRepresentationChecker.isCoRespondentRepresented;

@Component
public class CoECoRespondentCoverLetterGenerationTask extends BasePayloadSpecificDocumentGenerationTask {

    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static class FileMetadata {
        public static final String TEMPLATE_ID = COE_CO_RESPONDENT_LETTER.getTemplateByLanguage(LanguagePreference.ENGLISH);
        public static final String DOCUMENT_TYPE = "coeCoRespondentLetter";
    }

    private final CourtLookupService courtLookupService;

    public CoECoRespondentCoverLetterGenerationTask(
        CtscContactDetailsDataProviderService ctscContactDetailsDataProviderService,
        PdfDocumentGenerationService pdfDocumentGenerationService,
        CcdUtil ccdUtil,
        CourtLookupService courtLookupService) {
        super(ctscContactDetailsDataProviderService, pdfDocumentGenerationService, ccdUtil);
        this.courtLookupService = courtLookupService;
    }

    @Override
    protected DocmosisTemplateVars prepareDataForPdf(TaskContext context, Map<String, Object> caseData) {
        return CoECoverLetter.coECoverLetterBuilder()
            .caseReference(getCaseId(context))
            .ctscContactDetails(ctscContactDetailsDataProviderService.getCtscContactDetails())
            .addressee(getAddresseeCoRespondentOrSolicitorIfRepresented(caseData))
            .letterDate(DateUtils.formatDateWithCustomerFacingFormat(LocalDate.now()))
            .petitionerFullName(FullNamesDataExtractor.getPetitionerFullName(caseData))
            .respondentFullName(FullNamesDataExtractor.getRespondentFullName(caseData))
            .costClaimGranted(CoECoverLetterDataExtractor.isCostsClaimGranted(caseData))
            .courtName(getCourtName(caseData))
            .deadlineToContactCourtBy(DatesDataExtractor.getDeadlineToContactCourtBy(caseData))
            .hearingDate(DatesDataExtractor.getHearingDate(caseData))
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

    private Addressee getAddresseeCoRespondentOrSolicitorIfRepresented(Map<String, Object> caseData) {
        if (isCoRespondentRepresented(caseData)) {
            return AddresseeDataExtractor.getCoRespondentSolicitor(caseData);
        }

        return AddresseeDataExtractor.getCoRespondent(caseData);
    }
}
