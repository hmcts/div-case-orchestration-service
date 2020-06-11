package uk.gov.hmcts.reform.divorce.orchestration.tasks;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.bsp.common.model.document.Addressee;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.bulk.print.CoRespondentCostOrderNotificationCoverLetter;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.bulk.print.DocmosisTemplateVars;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.documentgeneration.GeneratedDocumentInfo;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskException;
import uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.PdfDocumentGenerationService;
import uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor.AddresseeDataExtractor;
import uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor.CtscContactDetailsDataProviderService;
import uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor.FullNamesDataExtractor;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.bulk.printing.BasePayloadSpecificDocumentGenerationTask;

import java.util.Map;

import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.AUTH_TOKEN_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.COST_ORDER_CO_RESPONDENT_SOLICITOR_LETTER_DOCUMENT_TYPE;
import static uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor.CostOrderCoRespondentLetterDataExtractor.getHearingDate;
import static uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor.CostOrderCoRespondentLetterDataExtractor.getLetterDate;
import static uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor.CostOrderCoRespondentLetterDataExtractor.getSolicitorReference;
import static uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor.FullNamesDataExtractor.getCoRespondentFullName;
import static uk.gov.hmcts.reform.divorce.orchestration.tasks.util.TaskUtils.getCaseId;
import static uk.gov.hmcts.reform.divorce.orchestration.util.PartyRepresentationChecker.isCoRespondentRepresented;

@Component
public class CostOrderNotificationLetterGenerationTask extends BasePayloadSpecificDocumentGenerationTask {

    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static class FileMetadata {
        public static final String TEMPLATE_ID = "FL-DIV-GNO-ENG-00423.docx";
        public static final String DOCUMENT_TYPE = COST_ORDER_CO_RESPONDENT_SOLICITOR_LETTER_DOCUMENT_TYPE;
    }

    private final PdfDocumentGenerationService pdfDocumentGenerationService;

    public CostOrderNotificationLetterGenerationTask(
        CtscContactDetailsDataProviderService ctscContactDetailsDataProviderService,
        PdfDocumentGenerationService pdfDocumentGenerationService) {
        super(ctscContactDetailsDataProviderService);
        this.pdfDocumentGenerationService = pdfDocumentGenerationService;
    }

    @Override
    protected DocmosisTemplateVars prepareDataForPdf(TaskContext context, Map<String, Object> caseData) throws TaskException {
        return getCoRespondentCostOrderNotificationCoverLetter(context, caseData);
    }

    @Override
    protected GeneratedDocumentInfo populateMetadataForGeneratedDocument(GeneratedDocumentInfo generatedDocumentInfo) {
        generatedDocumentInfo.setDocumentType(FileMetadata.DOCUMENT_TYPE);

        return generatedDocumentInfo;
    }

    @Override
    protected GeneratedDocumentInfo generatePdf(TaskContext context, DocmosisTemplateVars templateModel) {
        return pdfDocumentGenerationService.generatePdf(
            templateModel,
            FileMetadata.TEMPLATE_ID,
            context.getTransientObject(AUTH_TOKEN_JSON_KEY)
        );
    }

    private Addressee getAddresseeCoRespondentOrSolicitorIfRepresented(Map<String, Object> caseData) {
        if (isCoRespondentRepresented(caseData)) {
            return AddresseeDataExtractor.getCoRespondentSolicitor(caseData);
        }

        return AddresseeDataExtractor.getCoRespondent(caseData);
    }

    @Override
    public String getDocumentType() {
        return FileMetadata.DOCUMENT_TYPE;
    }

    private DocmosisTemplateVars getCoRespondentCostOrderNotificationCoverLetter(TaskContext context, Map<String, Object> caseData)
        throws TaskException {
        return CoRespondentCostOrderNotificationCoverLetter.builder()
            .caseReference(getCaseId(context))
            .solicitorReference(getSolicitorReference(caseData))
            .ctscContactDetails(ctscContactDetailsDataProviderService.getCtscContactDetails())
            .addressee(getAddresseeCoRespondentOrSolicitorIfRepresented(caseData))
            .coRespondentFullName(getCoRespondentFullName(caseData))
            .letterDate(getLetterDate())
            .hearingDate(getHearingDate(caseData))
            .petitionerFullName(FullNamesDataExtractor.getPetitionerFullName(caseData))
            .respondentFullName(FullNamesDataExtractor.getRespondentFullName(caseData))
            .build();
    }

}
