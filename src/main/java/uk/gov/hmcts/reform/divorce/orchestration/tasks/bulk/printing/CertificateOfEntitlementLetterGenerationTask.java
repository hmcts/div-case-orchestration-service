package uk.gov.hmcts.reform.divorce.orchestration.tasks.bulk.printing;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.bulk.print.CertificateOfEntitlementCoverLetter;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.bulk.print.DocmosisTemplateVars;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.documentgeneration.GeneratedDocumentInfo;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskException;
import uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.PdfDocumentGenerationService;
import uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor.AddresseeDataExtractor;
import uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor.CertificateOfEntitlementLetterDataExtractor;
import uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor.CtscContactDetailsDataProviderService;
import uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor.DatesDataExtractor;
import uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor.FullNamesDataExtractor;
import uk.gov.hmcts.reform.divorce.orchestration.util.CcdUtil;

import java.util.Map;

import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.AOSPackOfflineConstants.CERTIFICATE_OF_ENTITLEMENT_LETTER_DOCUMENT_TYPE;
import static uk.gov.hmcts.reform.divorce.orchestration.tasks.util.TaskUtils.getCaseId;
import static uk.gov.hmcts.reform.divorce.orchestration.util.PartyRepresentationChecker.isRespondentRepresented;

@Component
public class CertificateOfEntitlementLetterGenerationTask extends BasePayloadSpecificDocumentGenerationTask {

    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static class FileMetadata {
        public static final String TEMPLATE_ID_RESPONDENT = "FL-DIV-LET-ENG-00360.docx";
        public static final String TEMPLATE_ID_SOLICITOR = "FL-DIV-GNO-ENG-00370.docx";
        public static final String DOCUMENT_TYPE = CERTIFICATE_OF_ENTITLEMENT_LETTER_DOCUMENT_TYPE;
    }

    public CertificateOfEntitlementLetterGenerationTask(
        CtscContactDetailsDataProviderService ctscContactDetailsDataProviderService,
        PdfDocumentGenerationService pdfDocumentGenerationService,
        CcdUtil ccdUtil) {
        super(ctscContactDetailsDataProviderService, pdfDocumentGenerationService, ccdUtil);
    }

    @Override
    protected DocmosisTemplateVars prepareDataForPdf(TaskContext context, Map<String, Object> caseData) throws TaskException {
        CertificateOfEntitlementCoverLetter.CertificateOfEntitlementCoverLetterBuilder coverLetter =
            CertificateOfEntitlementCoverLetter.builder()
                .petitionerFullName(FullNamesDataExtractor.getPetitionerFullName(caseData))
                .respondentFullName(FullNamesDataExtractor.getRespondentFullName(caseData))
                .caseReference(getCaseId(context))
                .letterDate(DatesDataExtractor.getLetterDate())
                .ctscContactDetails(ctscContactDetailsDataProviderService.getCtscContactDetails())
                .hearingDate(DatesDataExtractor.getHearingDate(caseData))
                .costClaimGranted(CertificateOfEntitlementLetterDataExtractor.isCostsClaimGranted(caseData))
                .deadlineToContactCourtBy(DatesDataExtractor.getDeadlineToContactCourtBy(caseData));

        if (isRespondentRepresented(caseData)) {
            coverLetter.addressee(AddresseeDataExtractor.getRespondentSolicitor(caseData));
            coverLetter.solicitorReference(CertificateOfEntitlementLetterDataExtractor.getSolicitorReference(caseData));
        } else {
            coverLetter.addressee(AddresseeDataExtractor.getRespondent(caseData));
            coverLetter.husbandOrWife(CertificateOfEntitlementLetterDataExtractor.getHusbandOrWife(caseData));
            coverLetter.courtName(CertificateOfEntitlementLetterDataExtractor.getCourtName(caseData));
        }

        return coverLetter.build();
    }

    @Override
    protected GeneratedDocumentInfo populateMetadataForGeneratedDocument(GeneratedDocumentInfo generatedDocumentInfo) {
        generatedDocumentInfo.setDocumentType(FileMetadata.DOCUMENT_TYPE);

        return generatedDocumentInfo;
    }

    @Override
    public String getTemplateId(Map<String, Object> caseData) {
        String templateId = isRespondentRepresented(caseData) ? FileMetadata.TEMPLATE_ID_SOLICITOR : FileMetadata.TEMPLATE_ID_RESPONDENT;
        return templateId;
    }

    @Override
    public String getDocumentType() {
        return FileMetadata.DOCUMENT_TYPE;
    }
}
