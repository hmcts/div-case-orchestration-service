package uk.gov.hmcts.reform.divorce.orchestration.tasks.bulk.printing;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.divorce.orchestration.client.DocumentGeneratorClient;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.bulk.print.RequestTemplateVarsWrapper;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.documentgeneration.GenerateDocumentRequest;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.documentgeneration.GeneratedDocumentInfo;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.Task;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskException;

import java.util.Map;

import static java.lang.String.format;
import static java.util.Collections.singletonMap;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.AUTH_TOKEN_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DOCUMENT_FILENAME;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DOCUMENT_FILENAME_FMT;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DOCUMENT_TEMPLATE_ID;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DOCUMENT_TYPE;
import static uk.gov.hmcts.reform.divorce.orchestration.tasks.util.TaskUtils.getCaseId;

@Component
@AllArgsConstructor
public class DocumentGenerationForPreparedDataTask implements Task<Map<String, Object>> {

    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static class ContextKeys {
        public static final String CASE_DETAILS = OrchestrationConstants.CASE_DETAILS_JSON_KEY;
        public static final String CASE_DATA = OrchestrationConstants.FORMATTER_CASE_DATA_KEY;
        /*
         * This is a special field in context to store GeneratedDocumentInfo object with all metadata about generated document.
         */
        public static final String GENERATED_DOCUMENT = "documentGeneratedWithPreparedData";
    }

    private final DocumentGeneratorClient documentGeneratorClient;

    @Override
    public Map<String, Object> execute(TaskContext context, Map<String, Object> caseData) throws TaskException {
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

    private void addMetadataOfGeneratedDocumentToContext(TaskContext context, GeneratedDocumentInfo generatedDocumentInfo) throws TaskException {
        generatedDocumentInfo.setDocumentType(context.getTransientObject(DOCUMENT_TYPE));
        generatedDocumentInfo.setFileName(formatFilename(getCaseId(context), context.getTransientObject(DOCUMENT_FILENAME)));

        context.setTransientObject(ContextKeys.GENERATED_DOCUMENT, generatedDocumentInfo);
    }

    private Map<String, Object> getPreparedDataFromContext(TaskContext context) throws TaskException {
        Object preparedData = context
            .getTransientObject(PrepareDataForDocumentGenerationTask.ContextKeys.PREPARED_DATA_FOR_DOCUMENT_GENERATION);

        return singletonMap(ContextKeys.CASE_DETAILS, new RequestTemplateVarsWrapper(getCaseId(context), preparedData));
    }

    private String formatFilename(String caseId, String filename) {
        return format(DOCUMENT_FILENAME_FMT, filename, caseId);
    }
}
