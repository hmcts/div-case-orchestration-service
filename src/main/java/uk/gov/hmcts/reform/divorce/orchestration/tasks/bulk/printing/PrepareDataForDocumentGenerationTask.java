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
import uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.PdfDocumentGenerationService;
import uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor.CtscContactDetailsDataProviderService;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DOCUMENT_FILENAME;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DOCUMENT_TYPE;
import static uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.helper.StringHelper.formatFilename;
import static uk.gov.hmcts.reform.divorce.orchestration.tasks.util.TaskUtils.getCaseId;

/*
 * It should be used as a base class to generate pdf and store its metadata in context
 * (key PREPARED_DATA_FOR_DOCUMENT_GENERATION) so it can be used by following tasks.
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

    protected CtscContactDetailsDataProviderService ctscContactDetailsDataProviderService;

    @Override
    public Map<String, Object> execute(TaskContext context, Map<String, Object> caseData) throws TaskException {

        DocmosisTemplateVars templateModel = prepareDataForPdf(context, caseData);
        GeneratedDocumentInfo documentInfo = generatePdf(context, templateModel);
        appendAnotherDocumentToBulkPrint(context, populateMetadataForGeneratedDocument(documentInfo));

        return caseData;
    }

    protected abstract GeneratedDocumentInfo generatePdf(TaskContext context, DocmosisTemplateVars templateModel);

    protected abstract DocmosisTemplateVars prepareDataForPdf(TaskContext context, Map<String, Object> caseData) throws TaskException;

    protected abstract GeneratedDocumentInfo populateMetadataForGeneratedDocument(GeneratedDocumentInfo documentInfo);

    public void appendAnotherDocumentToBulkPrint(TaskContext context, GeneratedDocumentInfo generatedDocumentInfo) {
        List<GeneratedDocumentInfo> documentsToBulkPrint = context.computeTransientObjectIfAbsent(
            ContextKeys.GENERATED_DOCUMENTS, new ArrayList<>()
        );

        documentsToBulkPrint.add(generatedDocumentInfo);
    }
}
