package uk.gov.hmcts.reform.divorce.orchestration.tasks;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.bsp.common.model.document.Addressee;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.bulk.print.CoRespondentCostOrderCoverLetter;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.bulk.print.DocmosisTemplateVars;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.documentgeneration.GeneratedDocumentInfo;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskException;
import uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.PdfDocumentGenerationService;
import uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor.AddresseeDataExtractor;
import uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor.CtscContactDetailsDataProviderService;
import uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor.FullNamesDataExtractor;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.bulk.printing.BasePayloadSpecificDocumentGenerationTask;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.bulk.printing.DaGrantedLetterGenerationTask;
import uk.gov.hmcts.reform.divorce.orchestration.util.CcdUtil;

import java.util.Map;

import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.AUTH_TOKEN_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.COST_ORDER_CO_RESPONDENT_LETTER_DOCUMENT_TYPE;
import static uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor.CostOrderCoRespondentLetterDataExtractor.getHearingDate;
import static uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor.CostOrderCoRespondentLetterDataExtractor.getLetterDate;
import static uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor.FullNamesDataExtractor.getCoRespondentFullName;
import static uk.gov.hmcts.reform.divorce.orchestration.tasks.util.TaskUtils.getCaseId;
import static uk.gov.hmcts.reform.divorce.orchestration.util.PartyRepresentationChecker.isCoRespondentRepresented;

@Component
public class CostOrderLetterSolicitorGenerationTask extends BasePayloadSpecificDocumentGenerationTask {

    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static class FileMetadata {
        public static String TEMPLATE_ID = "FL-DIV-LET-ENG-00358.docx";
        public static String DOCUMENT_TYPE = COST_ORDER_CO_RESPONDENT_LETTER_DOCUMENT_TYPE;
    }

    public CostOrderLetterSolicitorGenerationTask(
        CtscContactDetailsDataProviderService ctscContactDetailsDataProviderService,
        PdfDocumentGenerationService pdfDocumentGenerationService,
        CcdUtil ccdUtil) {
        super(ctscContactDetailsDataProviderService, pdfDocumentGenerationService, ccdUtil);
    }

    @Override
    protected DocmosisTemplateVars prepareDataForPdf(TaskContext context, Map<String, Object> caseData) throws TaskException {
        return CoRespondentCostOrderCoverLetter.builder()
            .caseReference(getCaseId(context))
            .ctscContactDetails(ctscContactDetailsDataProviderService.getCtscContactDetails())
            .addressee(getAddresseeCoRespondentOrSolicitorIfRepresented(caseData))
            .coRespondentFullName(getCoRespondentFullName(caseData))
            .letterDate(getLetterDate())
            .hearingDate(getHearingDate(caseData))
            .petitionerFullName(FullNamesDataExtractor.getPetitionerFullName(caseData))
            .respondentFullName(FullNamesDataExtractor.getRespondentFullName(caseData))
            .build();
    }

    @Override
    protected GeneratedDocumentInfo populateMetadataForGeneratedDocument(GeneratedDocumentInfo generatedDocumentInfo) {
        generatedDocumentInfo.setDocumentType(FileMetadata.DOCUMENT_TYPE);

        return generatedDocumentInfo;
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

    @Override
    public String getTemplateId() {
        return FileMetadata.TEMPLATE_ID;
    }

}
