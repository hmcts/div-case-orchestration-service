package uk.gov.hmcts.reform.divorce.orchestration.tasks;

import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.Task;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskException;

import java.util.Map;

/*
 * It should be used as a base class to generate pdf and store its metadata in context
 * so it can be either saved in case data or send to print.
 */
public abstract class PrepareDataForDocumentGenerationTask implements Task<Map<String, Object>> {

    public static class ContextKeys {
        public static final String PREPARED_DATA_FOR_DOCUMENT_GENERATION = "preparedDataForDocumentGeneration";
    }

    @Override
    public Map<String, Object> execute(TaskContext context, Map<String, Object> caseData) throws TaskException {

        addPreparedDataToContext(context, caseData);

        return caseData;
    }

    protected abstract void addPreparedDataToContext(TaskContext context, Map<String, Object> caseData) throws TaskException;
}
