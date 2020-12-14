package uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task;

import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.Template;

import java.util.Map;

import static uk.gov.hmcts.reform.divorce.orchestration.tasks.util.TaskUtils.getMandatoryPropertyValueAsString;

@FunctionalInterface
public interface Task<T> extends Template {

    default String getMandatoryStringValue(Map<String, Object> caseData, String field) {
        try {
            return getMandatoryPropertyValueAsString(caseData, field);
        } catch (TaskException exception) {
            throw new InvalidDataForTaskException(exception);
        }
    }

    T execute(TaskContext context, T payload) throws TaskException;

}
    