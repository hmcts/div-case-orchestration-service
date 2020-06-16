package uk.gov.hmcts.reform.divorce.orchestration.tasks.bulk.printing;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.bulk.print.CoRespondentCostOrderCoverLetter;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.bulk.print.DocmosisTemplateVars;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskException;
import uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.PdfDocumentGenerationService;
import uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor.AddresseeDataExtractor;
import uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor.CtscContactDetailsDataProviderService;
import uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor.DatesDataExtractor;
import uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor.FullNamesDataExtractor;
import uk.gov.hmcts.reform.divorce.orchestration.util.CcdUtil;

import java.util.Map;

import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.COST_ORDER_CO_RESPONDENT_LETTER_DOCUMENT_TYPE;
import static uk.gov.hmcts.reform.divorce.orchestration.tasks.util.TaskUtils.getCaseId;

@Component
public class CostOrderCoRespondentCoverLetterGenerationTask extends BasePayloadSpecificDocumentGenerationTask {

    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static class FileMetadata {
        public static final String TEMPLATE_ID = "FL-DIV-LET-ENG-00358.docx";
        public static final String DOCUMENT_TYPE = COST_ORDER_CO_RESPONDENT_LETTER_DOCUMENT_TYPE;
    }

    public CostOrderCoRespondentCoverLetterGenerationTask(
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
            .addressee(AddresseeDataExtractor.getCoRespondent(caseData))
            .letterDate(DatesDataExtractor.getLetterDate())
            .hearingDate(DatesDataExtractor.getHearingDate(caseData))
            .petitionerFullName(FullNamesDataExtractor.getPetitionerFullName(caseData))
            .respondentFullName(FullNamesDataExtractor.getRespondentFullName(caseData))
            .build();
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
