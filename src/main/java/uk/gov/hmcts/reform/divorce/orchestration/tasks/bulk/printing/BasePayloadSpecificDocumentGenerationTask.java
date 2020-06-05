package uk.gov.hmcts.reform.divorce.orchestration.tasks.bulk.printing;

import lombok.AllArgsConstructor;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.bulk.print.DocmosisTemplateVars;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.documentgeneration.GeneratedDocumentInfo;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.Task;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskException;
import uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor.CtscContactDetailsDataProviderService;

import java.util.HashSet;
import java.util.Map;

import static com.google.common.collect.Sets.newHashSet;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DOCUMENT_COLLECTION;

/*
 * It should be used as a base class to prepare data models with set of data needed to generate pdfs
 * and store their metadata in context of flow (key DOCUMENT_COLLECTION)
 * so that it can be used by CaseFormatterAddDocuments and eventually by the BulkPrinterTask task.
 */
@AllArgsConstructor
public abstract class BasePayloadSpecificDocumentGenerationTask implements Task<Map<String, Object>> {

    protected final CtscContactDetailsDataProviderService ctscContactDetailsDataProviderService;

    @Override
    public Map<String, Object> execute(TaskContext context, Map<String, Object> caseData) throws TaskException {
        DocmosisTemplateVars templateModel = prepareDataForPdf(context, caseData);
        GeneratedDocumentInfo documentInfo = generatePdf(context, templateModel);
        GeneratedDocumentInfo documentInfoWithMetadata = populateMetadataForGeneratedDocument(documentInfo);

        context.computeTransientObjectIfAbsent(DOCUMENT_COLLECTION, new HashSet<>()).addAll(newHashSet(documentInfoWithMetadata));

        return caseData;
    }

    protected abstract GeneratedDocumentInfo generatePdf(TaskContext context, DocmosisTemplateVars templateModel);

    protected abstract DocmosisTemplateVars prepareDataForPdf(TaskContext context, Map<String, Object> caseData) throws TaskException;

    protected abstract GeneratedDocumentInfo populateMetadataForGeneratedDocument(GeneratedDocumentInfo documentInfo);

}