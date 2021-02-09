package uk.gov.hmcts.reform.divorce.orchestration.tasks.servicejourney;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.divorce.model.documentupdate.GeneratedDocumentInfo;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.document.ServiceApplicationRefusalOrder;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.document.template.docmosis.DocmosisTemplateVars;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.PdfDocumentGenerationService;
import uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor.CtscContactDetailsDataProviderService;
import uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor.DatesDataExtractor;
import uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor.FullNamesDataExtractor;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.bulk.printing.BasePayloadSpecificDocumentGenerationTask;
import uk.gov.hmcts.reform.divorce.orchestration.util.CcdUtil;

import java.time.LocalDateTime;
import java.util.Map;

import static java.lang.String.format;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.CcdFields.SERVICE_APPLICATION_DOCUMENTS;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DOCUMENT_FILENAME_FMT;
import static uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor.ServiceApplicationDataExtractor.getServiceApplicationRefusalReason;
import static uk.gov.hmcts.reform.divorce.orchestration.tasks.util.TaskUtils.getCaseId;
import static uk.gov.hmcts.reform.divorce.utils.DateUtils.formatDateTimeForDocument;

@Component
@Slf4j
public abstract class ServiceRefusalOrderGenerationTask extends BasePayloadSpecificDocumentGenerationTask {

    protected ServiceRefusalOrderGenerationTask(
        CtscContactDetailsDataProviderService ctscContactDetailsDataProviderService,
        PdfDocumentGenerationService pdfDocumentGenerationService,
        CcdUtil ccdUtil) {
        super(ctscContactDetailsDataProviderService, pdfDocumentGenerationService, ccdUtil);
    }

    @Override
    protected DocmosisTemplateVars prepareDataForPdf(TaskContext context, Map<String, Object> caseData) {
        return ServiceApplicationRefusalOrder.serviceApplicationRefusalOrderBuilder()
            .caseReference(getCaseId(context))
            .ctscContactDetails(ctscContactDetailsDataProviderService.getCtscContactDetails())
            .petitionerFullName(FullNamesDataExtractor.getPetitionerFullName(caseData))
            .respondentFullName(FullNamesDataExtractor.getRespondentFullName(caseData))
            .receivedServiceApplicationDate(DatesDataExtractor.getReceivedServiceApplicationDate(caseData))
            .serviceApplicationRefusalReason(getServiceApplicationRefusalReason(caseData))
            .documentIssuedOn(DatesDataExtractor.getLetterDate())
            .build();
    }

    @Override
    protected Map<String, Object> addToCaseData(TaskContext context, Map<String, Object> caseData, GeneratedDocumentInfo documentInfo) {
        log.info("CaseID: {} Adding document ({}) to serviceApplicationDocuments list", getCaseId(context), getDocumentType());
        return ccdUtil.addNewDocumentToCollection(caseData, documentInfo, SERVICE_APPLICATION_DOCUMENTS);
    }

    @Override
    protected String getFileName() {
        return format(DOCUMENT_FILENAME_FMT, getDocumentType(), formatDateTimeForDocument(LocalDateTime.now()));
    }
}
