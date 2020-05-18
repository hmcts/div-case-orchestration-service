package uk.gov.hmcts.reform.divorce.orchestration.tasks.bulk.printing;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.bulk.print.DocmosisTemplateVars;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.documentgeneration.GeneratedDocumentInfo;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.Task;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskException;
import uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor.CtscContactDetailsDataProviderService;

import java.util.HashMap;
import java.util.Map;

/*
 * It should be used as a base class to prepare data models with set of data needed to generate pdfs
 * and store their metadata in context of flow (key GENERATED_DOCUMENTS)
 * so that it can be used by BulkPrinterTask task.
 */
@AllArgsConstructor
public abstract class PrepareDataForDocumentGenerationTask implements Task<Map<String, Object>> {

    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static class ContextKeys {
        public static final String CASE_DETAILS = OrchestrationConstants.CASE_DETAILS_JSON_KEY;
        public static final String CASE_DATA = OrchestrationConstants.FORMATTER_CASE_DATA_KEY;
        /*
         * This is a special field in context to store GeneratedDocumentInfo object with all metadata about generated document.
         */
        public static final String GENERATED_DOCUMENTS = OrchestrationConstants.DOCUMENTS_GENERATED;
    }

    protected final CtscContactDetailsDataProviderService ctscContactDetailsDataProviderService;

    @Override
    public Map<String, Object> execute(TaskContext context, Map<String, Object> caseData) throws TaskException {

        DocmosisTemplateVars templateModel = prepareDataForPdf(context, caseData);
        GeneratedDocumentInfo documentInfo = generatePdf(context, templateModel);
        GeneratedDocumentInfo documentInfoWithMetadata = populateMetadataForGeneratedDocument(documentInfo);
        appendAnotherDocumentToBulkPrint(context, populateContentOfDocument(documentInfoWithMetadata));

        return caseData;
    }

    protected abstract GeneratedDocumentInfo generatePdf(TaskContext context, DocmosisTemplateVars templateModel);

    protected abstract DocmosisTemplateVars prepareDataForPdf(TaskContext context, Map<String, Object> caseData) throws TaskException;

    protected abstract GeneratedDocumentInfo populateMetadataForGeneratedDocument(GeneratedDocumentInfo documentInfo);

    protected abstract GeneratedDocumentInfo populateContentOfDocument(GeneratedDocumentInfo documentInfo);

    static void appendAnotherDocumentToBulkPrint(TaskContext context, GeneratedDocumentInfo generatedDocumentInfo) {
        Map<String, GeneratedDocumentInfo> documentsToBulkPrint = getDocumentsToBulkPrint(context);

        documentsToBulkPrint.put(generatedDocumentInfo.getDocumentType(), generatedDocumentInfo);
    }

    static Map<String, GeneratedDocumentInfo> getDocumentsToBulkPrint(TaskContext context) {
        return context.computeTransientObjectIfAbsent(ContextKeys.GENERATED_DOCUMENTS, new HashMap<>());
    }
}
