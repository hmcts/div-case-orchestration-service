package uk.gov.hmcts.reform.divorce.orchestration.tasks;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.divorce.orchestration.client.DocumentGeneratorClient;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CaseDetails;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.documentgeneration.GenerateDocumentRequest;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.documentgeneration.GeneratedDocumentInfo;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.Task;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskContext;

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
public class DocumentGenerationTask implements Task<Map<String, Object>> {

    private final DocumentGeneratorClient documentGeneratorClient;

    @Autowired
    public DocumentGenerationTask(final DocumentGeneratorClient documentGeneratorClient) {
        this.documentGeneratorClient = documentGeneratorClient;
    }

    @Override
    public Map<String, Object> execute(final TaskContext context, final Map<String, Object> caseData) {
        final CaseDetails caseDetails = context.getTransientObject(CASE_DETAILS_JSON_KEY);

        final GeneratedDocumentInfo generatedDocumentInfo =
            documentGeneratorClient.generatePDF(
                GenerateDocumentRequest.builder()
                    .template(context.getTransientObject(DOCUMENT_TEMPLATE_ID))
                    .values(singletonMap(DOCUMENT_CASE_DETAILS_JSON_KEY, caseDetails))
                    .build(),
                context.getTransientObject(AUTH_TOKEN_JSON_KEY)
            );

        generatedDocumentInfo.setDocumentType(context.getTransientObject(DOCUMENT_TYPE));
        generatedDocumentInfo.setFileName(format(DOCUMENT_FILENAME_FMT, context.getTransientObject(DOCUMENT_FILENAME), caseDetails.getCaseId()));

        final LinkedHashSet<GeneratedDocumentInfo> documentCollection = context.computeTransientObjectIfAbsent(DOCUMENT_COLLECTION,
            new LinkedHashSet<>());

        documentCollection.add(generatedDocumentInfo);

        return caseData;
    }
}
