package uk.gov.hmcts.reform.divorce.orchestration.tasks.util;

import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskException;

import java.util.Map;
import java.util.Optional;

import static com.google.common.base.Strings.isNullOrEmpty;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CASE_ID_JSON_KEY;

public class TaskUtils {

    public static String getMandatoryPropertyValueAsString(Map<String, Object> propertiesMap, String key)
            throws TaskException {
        if (!propertiesMap.containsKey(key)) {
            throw buildTaskExceptionForMandatoryProperty(key);
        }

        String propertyValue = (String) propertiesMap.get(key);
        if (isNullOrEmpty(propertyValue)) {
            throw buildTaskExceptionForMandatoryProperty(key);
        }

        return propertyValue;
    }

    public static Object getMandatoryPropertyValueAsObject(Map<String, Object> propertiesMap, String key)
            throws TaskException {
        return Optional.ofNullable(propertiesMap.get(key))
                .orElseThrow(() -> buildTaskExceptionForMandatoryProperty(key));
    }

    public static String getCaseId(TaskContext context) throws TaskException {
        Object transientObject = context.getTransientObject(CASE_ID_JSON_KEY);
        if (!(transientObject instanceof String)) {
            throw buildTaskExceptionForMandatoryProperty(CASE_ID_JSON_KEY);
        }

        String caseId = (String) transientObject;
        if (isNullOrEmpty(caseId)) {
            throw buildTaskExceptionForMandatoryProperty(CASE_ID_JSON_KEY);
        }

        return caseId;
    }

    private static TaskException buildTaskExceptionForMandatoryProperty(String key) {
        return new TaskException(String.format("Could not evaluate value of mandatory property \"%s\"", key));
    }

}