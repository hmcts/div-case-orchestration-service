package uk.gov.hmcts.reform.divorce.orchestration.tasks;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.documentgeneration.DocumentGenerationRequest;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.Task;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskException;

import java.util.List;
import java.util.Map;

import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DOCUMENT_FILENAME;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DOCUMENT_GENERATION_REQUESTS_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DOCUMENT_TEMPLATE_ID;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DOCUMENT_TYPE;

@Component
public class MultipleDocumentGenerationTask implements Task<Map<String, Object>> {

    private final DocumentGenerationTask documentGenerationTask;

    @Autowired
    public MultipleDocumentGenerationTask(final DocumentGenerationTask documentGenerationTask) {
        this.documentGenerationTask = documentGenerationTask;
    }

    @Override
    public Map<String, Object> execute(final TaskContext context, final Map<String, Object> caseData) throws TaskException {
        List<DocumentGenerationRequest> documentGenerationRequests = context.getTransientObject(DOCUMENT_GENERATION_REQUESTS_KEY);
        if (documentGenerationRequests == null || documentGenerationRequests.isEmpty()) {
            throw new TaskException("Could not find a list of document generation requests");
        }

        Map<String, Object> payloadForNextTask = caseData;
        for (DocumentGenerationRequest documentGenerationRequest : documentGenerationRequests) {
            context.setTransientObject(DOCUMENT_TEMPLATE_ID, documentGenerationRequest.getDocumentTemplateId());
            context.setTransientObject(DOCUMENT_TYPE, documentGenerationRequest.getDocumentType());
            context.setTransientObject(DOCUMENT_FILENAME, documentGenerationRequest.getDocumentFileName());
            payloadForNextTask = documentGenerationTask.execute(context, payloadForNextTask);
        }

        return payloadForNextTask;
    }

}