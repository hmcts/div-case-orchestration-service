package uk.gov.hmcts.reform.divorce.orchestration.tasks.bulk.printing;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.bulk.print.DocmosisTemplateVars;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.documentgeneration.GeneratedDocumentInfo;
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
@AllArgsConstructor
@Slf4j
public abstract class BasePayloadSpecificDocumentGenerationTask implements Task<Map<String, Object>> {

    final CtscContactDetailsDataProviderService ctscContactDetailsDataProviderService;
    private final PdfDocumentGenerationService pdfDocumentGenerationService;
    private final CcdUtil ccdUtil;

    @Override
    public Map<String, Object> execute(TaskContext context, Map<String, Object> caseData) throws TaskException {
        DocmosisTemplateVars templateModel = prepareDataForPdf(context, caseData);
        GeneratedDocumentInfo documentInfo = generatePdf(context, templateModel);
        documentInfo = populateMetadataForGeneratedDocument(documentInfo);

        log.info("Case {}: Document {} generated", getCaseId(context), documentInfo.getDocumentType());

        return ccdUtil.addNewDocumentsToCaseData(caseData, singletonList(documentInfo));
    }

    protected abstract DocmosisTemplateVars prepareDataForPdf(TaskContext context, Map<String, Object> caseData) throws TaskException;

    protected GeneratedDocumentInfo generatePdf(TaskContext context, DocmosisTemplateVars templateModel) throws TaskException {
        log.info("Case {}: Generating document from {}", getCaseId(context), getTemplateId());

        return pdfDocumentGenerationService.generatePdf(
            templateModel,
            getTemplateId(),
            context.getTransientObject(AUTH_TOKEN_JSON_KEY)
        );
    }

    protected GeneratedDocumentInfo populateMetadataForGeneratedDocument(GeneratedDocumentInfo documentInfo) {
        documentInfo.setDocumentType(getDocumentType());

        return documentInfo;
    }

    protected abstract String getTemplateId();

    public abstract String getDocumentType();

}