package uk.gov.hmcts.reform.divorce.orchestration.tasks;

import com.google.common.collect.ImmutableMap;
<<<<<<< HEAD
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.divorce.orchestration.client.DocumentGeneratorClient;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CaseDetails;
=======
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.divorce.orchestration.client.DocumentGeneratorClient;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants;
>>>>>>> 7f6ad23fd0b1ed8e947eb6dbf667ccf9dc811c22
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.documentgeneration.GenerateDocumentRequest;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.documentgeneration.GeneratedDocumentInfo;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.Task;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskContext;
<<<<<<< HEAD

import java.util.HashMap;
import java.util.LinkedHashSet;
=======
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskException;

import java.util.HashMap;
>>>>>>> 7f6ad23fd0b1ed8e947eb6dbf667ccf9dc811c22
import java.util.Map;

import static java.lang.String.format;
import static java.util.Collections.singletonMap;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.AUTH_TOKEN_JSON_KEY;
<<<<<<< HEAD
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CASE_DETAILS_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DOCUMENT_CASE_DETAILS_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DOCUMENT_COLLECTION;
=======
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DOCUMENT_CASE_DETAILS_JSON_KEY;
>>>>>>> 7f6ad23fd0b1ed8e947eb6dbf667ccf9dc811c22
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DOCUMENT_FILENAME;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DOCUMENT_FILENAME_FMT;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DOCUMENT_TEMPLATE_ID;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DOCUMENT_TYPE;
<<<<<<< HEAD

@Component
public class DocumentGenerationForPreparedDataTask implements Task<Map<String, Object>> {

    public static class ContextKeys {
        public static final String CASE_DETAILS_JSON_KEY = OrchestrationConstants.CASE_DETAILS_JSON_KEY;
        public static final String CASE_DATA_KEY = OrchestrationConstants.FORMATTER_CASE_DATA_KEY;
        public static final String PREPARED_DATA_FOR_DOCUMENT_GENERATION_KEY = "preparedDataForDocumentGeneration";
=======
import static uk.gov.hmcts.reform.divorce.orchestration.tasks.util.TaskUtils.getCaseId;

@Component
@AllArgsConstructor
public class DocumentGenerationForPreparedDataTask implements Task<Map<String, Object>> {

    public static class ContextKeys {
        public static final String CASE_DETAILS = OrchestrationConstants.CASE_DETAILS_JSON_KEY;
        public static final String CASE_DATA = OrchestrationConstants.FORMATTER_CASE_DATA_KEY;
        /*
         * This is a special field in context to store GeneratedDocumentInfo object with all metadata about generated document.
         *
         * <p>Should be used by
         */
        public static final String GENERATED_DOCUMENT = "documentGeneratedWithPreparedData";
>>>>>>> 7f6ad23fd0b1ed8e947eb6dbf667ccf9dc811c22
    }

    private final DocumentGeneratorClient documentGeneratorClient;

<<<<<<< HEAD
    @Autowired
    public DocumentGenerationForPreparedDataTask(final DocumentGeneratorClient documentGeneratorClient) {
        this.documentGeneratorClient = documentGeneratorClient;
    }

    @Override
    public Map<String, Object> execute(final TaskContext context, final Map<String, Object> caseData) {
=======
    @Override
    public Map<String, Object> execute(final TaskContext context, final Map<String, Object> caseData) throws TaskException {
>>>>>>> 7f6ad23fd0b1ed8e947eb6dbf667ccf9dc811c22
        GeneratedDocumentInfo generatedDocumentInfo =
            documentGeneratorClient.generatePDF(
                GenerateDocumentRequest.builder()
                    .template(context.getTransientObject(DOCUMENT_TEMPLATE_ID))
                    .values(getPreparedDataFromContext(context))
                    .build(),
                context.getTransientObject(AUTH_TOKEN_JSON_KEY)
            );

        addMetadataOfGeneratedDocumentToContext(context, generatedDocumentInfo);

        return caseData;
    }

<<<<<<< HEAD
    private void addMetadataOfGeneratedDocumentToContext(TaskContext context, GeneratedDocumentInfo generatedDocumentInfo) {
        CaseDetails caseDetails = context.getTransientObject(CASE_DETAILS_JSON_KEY);

        generatedDocumentInfo.setDocumentType(context.getTransientObject(DOCUMENT_TYPE));
        generatedDocumentInfo.setFileName(formatFilename(context, caseDetails));

        final LinkedHashSet<GeneratedDocumentInfo> documentCollection = context
            .computeTransientObjectIfAbsent(DOCUMENT_COLLECTION, new LinkedHashSet<>());

        documentCollection.add(generatedDocumentInfo);
    }

    private String formatFilename(TaskContext context, CaseDetails caseDetails) {
        return format(DOCUMENT_FILENAME_FMT, context.getTransientObject(DOCUMENT_FILENAME), caseDetails.getCaseId());
    }

    private Map<String, Object> getPreparedDataFromContext(TaskContext context) {
        Object preparedData = context.getTransientObject(ContextKeys.PREPARED_DATA_FOR_DOCUMENT_GENERATION_KEY);

        return singletonMap(DOCUMENT_CASE_DETAILS_JSON_KEY, new HashMap<>(ImmutableMap.of(ContextKeys.CASE_DATA_KEY, preparedData)));
=======
    private void addMetadataOfGeneratedDocumentToContext(TaskContext context, GeneratedDocumentInfo generatedDocumentInfo) throws TaskException {

        generatedDocumentInfo.setDocumentType(context.getTransientObject(DOCUMENT_TYPE));
        generatedDocumentInfo.setFileName(formatFilename(getCaseId(context), context.getTransientObject(DOCUMENT_FILENAME)));

        context.setTransientObject(ContextKeys.GENERATED_DOCUMENT, generatedDocumentInfo);
    }

    private String formatFilename(String caseId, String filename) {
        return format(DOCUMENT_FILENAME_FMT, filename, caseId);
    }

    private Map<String, Object> getPreparedDataFromContext(TaskContext context) {
        Object preparedData = context
            .getTransientObject(PrepareDataForDocumentGenerationTask.ContextKeys.PREPARED_DATA_FOR_DOCUMENT_GENERATION);

        return singletonMap(ContextKeys.CASE_DETAILS, new HashMap<>(ImmutableMap.of(ContextKeys.CASE_DATA, preparedData)));
>>>>>>> 7f6ad23fd0b1ed8e947eb6dbf667ccf9dc811c22
    }
}
