package uk.gov.hmcts.reform.divorce.orchestration.tasks.servicejourney;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import uk.gov.hmcts.reform.bsp.common.model.document.CtscContactDetails;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.bulk.print.DocmosisTemplateVars;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.DocumentLink;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.documentgeneration.GeneratedDocumentInfo;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.Task;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.PdfDocumentGenerationService;
import uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor.CtscContactDetailsDataProviderService;
import uk.gov.hmcts.reform.divorce.orchestration.util.mapper.CcdMappers;

import java.util.Map;

import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.CcdFields.SERVICE_REFUSAL_DRAFT;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.AUTH_TOKEN_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.tasks.util.TaskUtils.getCaseId;
import static uk.gov.hmcts.reform.divorce.orchestration.util.ServiceApplicationRefusalHelper.isServiceApplicationGranted;

@Slf4j
@RequiredArgsConstructor
public abstract class ServiceRefusalOrderDraftTask implements Task<Map<String, Object>> {

    private final CtscContactDetailsDataProviderService ctscContactDetailsDataProviderService;
    private final PdfDocumentGenerationService pdfDocumentGenerationService;

    @Override
    public Map<String, Object> execute(TaskContext context, Map<String, Object> caseData) {
        return generateAndAddDraftDocument(context, caseData);
    }

    protected abstract String getTemplateId();

    protected abstract DocmosisTemplateVars getDocumentTemplate(Map<String, Object> caseData);

    protected abstract String getApplicationType();

    protected CtscContactDetails getCtscContactDetails() {
        return ctscContactDetailsDataProviderService.getCtscContactDetails();
    }

    private GeneratedDocumentInfo generateServiceRefusalOrder(TaskContext context, Map<String, Object> caseData) {
        log.info("CaseID {}: Generating service application refusal order document", getCaseId(context));
        return pdfDocumentGenerationService.generatePdf(
            getDocumentTemplate(caseData),
            getTemplateId(),
            context.getTransientObject(AUTH_TOKEN_JSON_KEY)
        );
    }

    private DocumentLink generateRefusalDraftDocumentLink(GeneratedDocumentInfo generatedDocumentInfo) {
        return CcdMappers.mapDocumentInfoToCcdDocument(generatedDocumentInfo)
            .getValue()
            .getDocumentLink();
    }

    private Map<String, Object> generateAndAddDraftDocument(TaskContext context, Map<String, Object> caseData) {

        if (!isServiceApplicationGranted(caseData)) {
            GeneratedDocumentInfo generatedDocumentInfo = generateServiceRefusalOrder(context, caseData);
            caseData.put(SERVICE_REFUSAL_DRAFT, generateRefusalDraftDocumentLink(generatedDocumentInfo));
            log.info("CaseID: {} - Added Service Refusal Order draft document for {} service.", getCaseId(context), getApplicationType());
        }

        return caseData;
    }

}
