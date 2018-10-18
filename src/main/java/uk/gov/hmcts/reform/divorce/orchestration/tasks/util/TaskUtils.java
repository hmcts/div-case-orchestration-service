package uk.gov.hmcts.reform.divorce.orchestration.tasks.util;

import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskException;

import java.util.Map;

import static com.google.common.base.Strings.isNullOrEmpty;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CASE_ID_JSON_KEY;

public class TaskUtils {

    public static String getMandatoryPropertyValueAsString(Map<String, Object> propertiesMap, String key)
            throws TaskException {
        if (!propertiesMap.containsKey(key)) {
            return throwTaskExceptionForMandatoryProperty(key);
        }

        String propertyValue = (String) propertiesMap.get(key);
        if (isNullOrEmpty(propertyValue)) {
            throwTaskExceptionForMandatoryProperty(key);
        }

        return propertyValue;
    }

    public static String getCaseId(TaskContext context) throws TaskException {
        String caseId = (String) context.getTransientObject(CASE_ID_JSON_KEY);
        if (isNullOrEmpty(caseId)) {
            throwTaskExceptionForMandatoryProperty(CASE_ID_JSON_KEY);
        }

        return caseId;
    }

    private static String throwTaskExceptionForMandatoryProperty(String key) throws TaskException {
        throw new TaskException(String.format("Could not evaluate value of mandatory property \"%s\"", key));
    }

}