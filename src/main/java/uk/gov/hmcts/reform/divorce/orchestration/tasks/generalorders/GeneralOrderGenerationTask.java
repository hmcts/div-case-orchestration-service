package uk.gov.hmcts.reform.divorce.orchestration.tasks.generalorders;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.bulk.print.DocmosisTemplateVars;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.document.GeneralOrder;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.documentgeneration.GeneratedDocumentInfo;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.PdfDocumentGenerationService;
import uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor.CaseDataExtractor;
import uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor.CtscContactDetailsDataProviderService;
import uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor.FullNamesDataExtractor;
import uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor.GeneralOrderDataExtractor;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.bulk.printing.BasePayloadSpecificDocumentGenerationTask;
import uk.gov.hmcts.reform.divorce.orchestration.util.CcdUtil;
import uk.gov.hmcts.reform.divorce.orchestration.util.PartyRepresentationChecker;
import uk.gov.hmcts.reform.divorce.utils.DateUtils;

import java.time.LocalDate;
import java.util.Map;

import static java.lang.String.format;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.CcdFields.GENERAL_ORDERS;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DOCUMENT_FILENAME_FMT;
import static uk.gov.hmcts.reform.divorce.orchestration.tasks.util.TaskUtils.getCaseId;

@Component
@Slf4j
public class GeneralOrderGenerationTask extends BasePayloadSpecificDocumentGenerationTask {

    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static class FileMetadata {
        public static final String TEMPLATE_ID = "FL-DIV-GOR-ENG-00572.docx";
        public static final String DOCUMENT_TYPE = "generalOrder";
    }

    public GeneralOrderGenerationTask(
        CtscContactDetailsDataProviderService ctscContactDetailsDataProviderService,
        PdfDocumentGenerationService pdfDocumentGenerationService,
        CcdUtil ccdUtil) {
        super(ctscContactDetailsDataProviderService, pdfDocumentGenerationService, ccdUtil);
    }

    @Override
    protected Map<String, Object> addToCaseData(
        TaskContext context, Map<String, Object> caseData, GeneratedDocumentInfo generatedDocumentInfo
    ) {
        log.info("CaseID: {} Adding general order to {} collection.", getCaseId(context), GENERAL_ORDERS);
        return ccdUtil.addNewDocumentToCollection(caseData, generatedDocumentInfo, GENERAL_ORDERS);
    }

    @Override
    protected GeneratedDocumentInfo populateMetadataForGeneratedDocument(GeneratedDocumentInfo documentInfo) {
        documentInfo.setDocumentType(getDocumentType());
        documentInfo.setFileName(nameWithCurrentDate());

        return documentInfo;
    }

    @Override
    protected DocmosisTemplateVars prepareDataForPdf(TaskContext context, Map<String, Object> caseData) {
        return GeneralOrder.generalOrderBuilder()
            .caseReference(CaseDataExtractor.getCaseReference(caseData))
            .ctscContactDetails(ctscContactDetailsDataProviderService.getCtscContactDetails())
            .petitionerFullName(FullNamesDataExtractor.getPetitionerFullName(caseData))
            .respondentFullName(FullNamesDataExtractor.getRespondentFullName(caseData))
            .hasCoRespondent(PartyRepresentationChecker.isCoRespondentLinkedToCase(caseData))
            .coRespondentFullName(FullNamesDataExtractor.getCoRespondentFullName(caseData))
            .judgeName(GeneralOrderDataExtractor.getJudgeName(caseData))
            .judgeType(GeneralOrderDataExtractor.getJudgeType(caseData))
            .generalOrderRecitals(GeneralOrderDataExtractor.getGeneralOrderRecitals(caseData))
            .generalOrderDetails(GeneralOrderDataExtractor.getGeneralOrderDetails(caseData))
            .generalOrderDate(GeneralOrderDataExtractor.getGeneralOrderDate(caseData))
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

    protected String nameWithCurrentDate() {
        return format(DOCUMENT_FILENAME_FMT, getDocumentType(), getFormattedNow());
    }

    private String getFormattedNow() {
        return DateUtils.formatDateFromLocalDate(LocalDate.now());
    }
}
