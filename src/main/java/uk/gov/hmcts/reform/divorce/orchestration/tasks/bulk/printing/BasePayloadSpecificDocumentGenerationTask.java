package uk.gov.hmcts.reform.divorce.orchestration.tasks.bulk.printing;

import lombok.AllArgsConstructor;
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

/*
 * It should be used as a base class to prepare data models with set of data needed to generate PDFs.
 * These documents will then be added to the case data.
 */
@AllArgsConstructor
public abstract class BasePayloadSpecificDocumentGenerationTask implements Task<Map<String, Object>> {

    final CtscContactDetailsDataProviderService ctscContactDetailsDataProviderService;
    private final PdfDocumentGenerationService pdfDocumentGenerationService;
    private final CcdUtil ccdUtil;

    @Override
    public Map<String, Object> execute(TaskContext context, Map<String, Object> caseData) throws TaskException {
        DocmosisTemplateVars templateModel = prepareDataForPdf(context, caseData);
        GeneratedDocumentInfo documentInfo = generatePdf(context, templateModel);
        documentInfo = populateMetadataForGeneratedDocument(documentInfo);

        return ccdUtil.addNewDocumentsToCaseData(caseData, singletonList(documentInfo));
    }

    protected abstract DocmosisTemplateVars prepareDataForPdf(TaskContext context, Map<String, Object> caseData) throws TaskException;

    protected GeneratedDocumentInfo generatePdf(TaskContext context, DocmosisTemplateVars templateModel) {
        return pdfDocumentGenerationService.generatePdf(
            templateModel,
            getTemplateId(),
            context.getTransientObject(AUTH_TOKEN_JSON_KEY)
        );
    }

    protected abstract GeneratedDocumentInfo populateMetadataForGeneratedDocument(GeneratedDocumentInfo documentInfo);

    protected abstract String getTemplateId();

    protected abstract String getDocumentType();

}