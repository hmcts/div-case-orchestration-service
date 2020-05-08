package uk.gov.hmcts.reform.divorce.orchestration.tasks.bulk.printing;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.Task;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskException;
import uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor.CtscContactDetailsDataProviderService;

import java.util.Map;

/*
 * It should be used as a base class to generate pdf and store its metadata in context
 * (key PREPARED_DATA_FOR_DOCUMENT_GENERATION) so it can be used by following tasks.
 */
@AllArgsConstructor
public abstract class PrepareDataForDocumentGenerationTask implements Task<Map<String, Object>> {

    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static class ContextKeys {
        public static final String PREPARED_DATA_FOR_DOCUMENT_GENERATION = "preparedDataForDocumentGeneration";
    }

    protected CtscContactDetailsDataProviderService ctscContactDetailsDataProviderService;

    @Override
    public Map<String, Object> execute(TaskContext context, Map<String, Object> caseData) throws TaskException {

        addPreparedDataToContext(context, caseData);

        return caseData;
    }

    protected abstract void addPreparedDataToContext(TaskContext context, Map<String, Object> caseData) throws TaskException;
}
