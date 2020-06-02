package uk.gov.hmcts.reform.divorce.orchestration.tasks.bulk.printing;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.bulk.print.BasicCoverLetter;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.bulk.print.CertificateOfEntitlementCoverLetter;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.bulk.print.DocmosisTemplateVars;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.documentgeneration.GeneratedDocumentInfo;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskException;
import uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.DocumentContentFetcherService;
import uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.PdfDocumentGenerationService;
import uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor.*;

import java.util.Map;

import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.AUTH_TOKEN_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.tasks.util.TaskUtils.getCaseId;
import static uk.gov.hmcts.reform.divorce.orchestration.util.DateUtils.*;

@Component
public class CertificateOfEntitlementLetterGenerationTask extends PrepareDataForDocumentGenerationTask {

    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static class FileMetadata {
        public static final String TEMPLATE_ID = "FL-DIV-GNO-ENG-00370.docx";
        public static final String DOCUMENT_TYPE = "coeLetter";
        public static final String FILE_NAME = "certificateOfEntitlementCoverLetterForRespondent.pdf";
    }

    private final PdfDocumentGenerationService pdfDocumentGenerationService;
    private final DocumentContentFetcherService documentContentFetcherService;

    public CertificateOfEntitlementLetterGenerationTask(
        CtscContactDetailsDataProviderService ctscContactDetailsDataProviderService,
        PdfDocumentGenerationService pdfDocumentGenerationService,
        DocumentContentFetcherService documentContentFetcherService) {
        super(ctscContactDetailsDataProviderService);
        this.pdfDocumentGenerationService = pdfDocumentGenerationService;
        this.documentContentFetcherService = documentContentFetcherService;
    }

    @Override
    protected DocmosisTemplateVars prepareDataForPdf(TaskContext context, Map<String, Object> caseData) throws TaskException {
        CertificateOfEntitlementCoverLetter.CertificateOfEntitlementCoverLetterBuilder coverLetter =
            CertificateOfEntitlementCoverLetter.builder()
                .petitionerFullName(FullNamesDataExtractor.getPetitionerFullName(caseData))
                .respondentFullName(FullNamesDataExtractor.getRespondentFullName(caseData))
                .caseReference(getCaseId(context))
                .letterDate(formatDate(todaysDate(), TEMPLATE_FORMAT))
                .ctscContactDetails(ctscContactDetailsDataProviderService.getCtscContactDetails())
                .hearingDate(CertificateOfEntitlementLetterDataExtractor.getHearingDate(caseData))
                .costClaimGranted(CertificateOfEntitlementLetterDataExtractor.isCostsClaimGranted(caseData))
                .deadlineToContactCourtBy(CertificateOfEntitlementLetterDataExtractor.getLimitDateToContactCourt(caseData));

        if (addresseeIsSolicitor())

        return BasicCoverLetter.builder()
            .caseReference(getCaseId(context))
            .ctscContactDetails(ctscContactDetailsDataProviderService.getCtscContactDetails())
            .addressee(AddresseeDataExtractor.getRespondent(caseData))
            .letterDate(DaGrantedLetterDataExtractor.getDaGrantedDate(caseData))
            .petitionerFullName(FullNamesDataExtractor.getPetitionerFullName(caseData))
            .respondentFullName(FullNamesDataExtractor.getRespondentFullName(caseData))
            .build();
    }

    @Override
    protected GeneratedDocumentInfo populateMetadataForGeneratedDocument(GeneratedDocumentInfo generatedDocumentInfo) {
        generatedDocumentInfo.setDocumentType(FileMetadata.DOCUMENT_TYPE);
        generatedDocumentInfo.setFileName(FileMetadata.FILE_NAME);

        return generatedDocumentInfo;
    }

    @Override
    protected GeneratedDocumentInfo populateContentOfDocument(GeneratedDocumentInfo generatedDocumentInfo) {
        return documentContentFetcherService.fetchPrintContent(generatedDocumentInfo);
    }

    @Override
    protected GeneratedDocumentInfo generatePdf(TaskContext context, DocmosisTemplateVars templateModel) {
        return pdfDocumentGenerationService.generatePdf(
            templateModel,
            FileMetadata.TEMPLATE_ID,
            context.getTransientObject(AUTH_TOKEN_JSON_KEY)
        );
    }

    private boolean addresseeIsSolicitor(Map<String, Object> caseData) {
        return caseD
    }

}
