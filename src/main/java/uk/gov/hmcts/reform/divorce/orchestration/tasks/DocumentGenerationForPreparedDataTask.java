package uk.gov.hmcts.reform.divorce.orchestration.tasks;

import com.google.common.collect.ImmutableMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.divorce.orchestration.client.DocumentGeneratorClient;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CaseDetails;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.documentgeneration.GenerateDocumentRequest;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.documentgeneration.GeneratedDocumentInfo;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.Task;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskContext;

import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;

import static java.lang.String.format;
import static java.util.Collections.singletonMap;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.AUTH_TOKEN_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CASE_DETAILS_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DOCUMENT_CASE_DETAILS_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DOCUMENT_COLLECTION;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DOCUMENT_FILENAME;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DOCUMENT_FILENAME_FMT;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DOCUMENT_TEMPLATE_ID;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DOCUMENT_TYPE;

@Component
public class DocumentGenerationForPreparedDataTask implements Task<Map<String, Object>> {

    public static class ContextKeys {
        public static final String CASE_DETAILS_JSON_KEY = OrchestrationConstants.CASE_DETAILS_JSON_KEY;
        public static final String CASE_DATA_KEY = OrchestrationConstants.FORMATTER_CASE_DATA_KEY;
        public static final String PREPARED_DATA_FOR_DOCUMENT_GENERATION_KEY = "preparedDataForDocumentGeneration";
    }

    private final DocumentGeneratorClient documentGeneratorClient;

    @Autowired
    public DocumentGenerationForPreparedDataTask(final DocumentGeneratorClient documentGeneratorClient) {
        this.documentGeneratorClient = documentGeneratorClient;
    }

    @Override
    public Map<String, Object> execute(final TaskContext context, final Map<String, Object> caseData) {
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
    }
}
