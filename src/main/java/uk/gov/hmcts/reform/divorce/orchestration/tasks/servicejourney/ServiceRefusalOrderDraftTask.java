package uk.gov.hmcts.reform.divorce.orchestration.tasks.servicejourney;

import lombok.extern.slf4j.Slf4j;
import uk.gov.hmcts.reform.bsp.common.model.document.CtscContactDetails;
import uk.gov.hmcts.reform.divorce.model.ccd.DocumentLink;
import uk.gov.hmcts.reform.divorce.model.documentupdate.GeneratedDocumentInfo;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.PdfDocumentGenerationService;
import uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor.CtscContactDetailsDataProviderService;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.bulk.printing.BasePayloadSpecificDocumentGenerationTask;
import uk.gov.hmcts.reform.divorce.orchestration.util.CcdUtil;
import uk.gov.hmcts.reform.divorce.orchestration.util.mapper.CcdMappers;

import java.util.Map;

import static java.lang.String.format;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.CcdFields.SERVICE_REFUSAL_DRAFT;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DOCUMENT_FILENAME_FMT;
import static uk.gov.hmcts.reform.divorce.orchestration.service.common.Conditions.isServiceApplicationGranted;
import static uk.gov.hmcts.reform.divorce.orchestration.tasks.util.TaskUtils.getCaseId;

@Slf4j
public abstract class ServiceRefusalOrderDraftTask extends BasePayloadSpecificDocumentGenerationTask {

    protected ServiceRefusalOrderDraftTask(
        CtscContactDetailsDataProviderService ctscContactDetailsDataProviderService,
        PdfDocumentGenerationService pdfDocumentGenerationService,
        CcdUtil ccdUtil) {
        super(ctscContactDetailsDataProviderService, pdfDocumentGenerationService, ccdUtil);
    }

    protected abstract String getApplicationType();

    protected CtscContactDetails getCtscContactDetails() {
        return ctscContactDetailsDataProviderService.getCtscContactDetails();
    }

    @Override
    protected Map<String, Object> addToCaseData(TaskContext context, Map<String, Object> caseData, GeneratedDocumentInfo generatedDocumentInfo) {

        if (!isServiceApplicationGranted(caseData)) {
            caseData.put(SERVICE_REFUSAL_DRAFT, generateRefusalDraftDocumentLink(generatedDocumentInfo, getCaseId(context)));
            log.info("CaseID: {} Added Service Refusal Order draft document for {} service.", getCaseId(context), getApplicationType());
        }

        return caseData;
    }

    private DocumentLink generateRefusalDraftDocumentLink(GeneratedDocumentInfo generatedDocumentInfo, String caseId) {
        generatedDocumentInfo.setFileName(format(DOCUMENT_FILENAME_FMT, generatedDocumentInfo.getFileName(), caseId));
        return CcdMappers.mapDocumentInfoToCcdDocument(generatedDocumentInfo)
            .getValue()
            .getDocumentLink();
    }
}
