package uk.gov.hmcts.reform.divorce.orchestration.tasks.bulk.printing;

import lombok.extern.slf4j.Slf4j;
import uk.gov.hmcts.reform.divorce.model.documentupdate.GeneratedDocumentInfo;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.document.template.docmosis.DocmosisTemplateVars;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.Task;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskException;
import uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.PdfDocumentGenerationService;
import uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor.CtscContactDetailsDataProviderService;
import uk.gov.hmcts.reform.divorce.orchestration.util.CcdUtil;

import java.util.Map;

import static java.util.Collections.singletonList;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.AUTH_TOKEN_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.tasks.util.TaskUtils.getCaseId;

/*
 * It should be used as a base class to prepare data models with set of data needed to generate PDFs.
 * These documents will then be added to the case data.
 */
@Slf4j
public abstract class BasePayloadSpecificDocumentGenerationTask implements Task<Map<String, Object>> {

    protected final CtscContactDetailsDataProviderService ctscContactDetailsDataProviderService;
    protected final PdfDocumentGenerationService pdfDocumentGenerationService;
    protected final CcdUtil ccdUtil;

    protected BasePayloadSpecificDocumentGenerationTask(
        CtscContactDetailsDataProviderService ctscContactDetailsDataProviderService,
        PdfDocumentGenerationService pdfDocumentGenerationService,
        CcdUtil ccdUtil) {
        this.ctscContactDetailsDataProviderService = ctscContactDetailsDataProviderService;
        this.pdfDocumentGenerationService = pdfDocumentGenerationService;
        this.ccdUtil = ccdUtil;
    }

    @Override
    public Map<String, Object> execute(TaskContext context, Map<String, Object> caseData) throws TaskException {
        DocmosisTemplateVars templateModel = prepareDataForPdf(context, caseData);
        GeneratedDocumentInfo documentInfo = generatePdf(context, templateModel);
        documentInfo = populateMetadataForGeneratedDocument(documentInfo);

        log.info(
            "Case {}: Document {} generated. Name: {}",
            getCaseId(context),
            documentInfo.getDocumentType(),
            documentInfo.getFileName()
        );

        return addToCaseData(context, caseData, documentInfo);
    }

    protected abstract DocmosisTemplateVars prepareDataForPdf(TaskContext context, Map<String, Object> caseData);

    protected Map<String, Object> addToCaseData(TaskContext context, Map<String, Object> caseData, GeneratedDocumentInfo documentInfo) {
        log.info("CaseID: {} Adding document ({}) to d8document list", getCaseId(context), getDocumentType());
        return ccdUtil.addNewDocumentsToCaseData(caseData, singletonList(documentInfo));
    }

    protected GeneratedDocumentInfo generatePdf(TaskContext context, DocmosisTemplateVars templateModel) throws TaskException {
        log.info("CaseID: {} Generating document from {}", getCaseId(context), getTemplateId());

        return pdfDocumentGenerationService.generatePdf(
            templateModel,
            getTemplateId(),
            context.getTransientObject(AUTH_TOKEN_JSON_KEY)
        );
    }

    protected String getFileName() {
        return getDocumentType();
    }

    protected GeneratedDocumentInfo populateMetadataForGeneratedDocument(GeneratedDocumentInfo documentInfo) {
        documentInfo.setDocumentType(getDocumentType());
        documentInfo.setFileName(getFileName());
        return documentInfo;
    }

    public abstract String getTemplateId();

    public abstract String getDocumentType();
}
