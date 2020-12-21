package uk.gov.hmcts.reform.divorce.orchestration.tasks.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskException;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.Map;
import java.util.Optional;

import static com.google.common.base.Strings.isNullOrEmpty;
import static java.lang.String.format;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.AUTH_TOKEN_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CASE_ID_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.YES_VALUE;
import static uk.gov.hmcts.reform.divorce.orchestration.util.CcdUtil.parseDateUsingCcdFormat;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
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

    public static LocalDate getMandatoryPropertyValueAsLocalDateFromCCD(Map<String, Object> propertiesMap, String key) throws TaskException {
        String mandatoryPropertyValueAsString = getMandatoryPropertyValueAsString(propertiesMap, key);
        try {
            return parseDateUsingCcdFormat(mandatoryPropertyValueAsString);
        } catch (DateTimeParseException exception) {
            throw new TaskException(format("Could not format date from \"%s\" field.", key));
        }
    }

    public static Object getMandatoryPropertyValueAsObject(Map<String, Object> propertiesMap, String key)
        throws TaskException {
        return Optional.ofNullable(propertiesMap.get(key))
            .orElseThrow(() -> buildTaskExceptionForMandatoryProperty(key));
    }

    public static String getOptionalPropertyValueAsString(Map<String, Object> propertiesMap, String key, String defaultValue) {
        return Optional.ofNullable(propertiesMap.get(key))
            .map(String.class::cast)
            .orElse(defaultValue);
    }

    public static String getCaseId(TaskContext context) throws TaskException {
        return getMandatoryContextValue(CASE_ID_JSON_KEY, context);
    }

    public static String getAuthToken(TaskContext context) throws TaskException {
        return getMandatoryContextValue(AUTH_TOKEN_JSON_KEY, context);
    }

    public static String getMandatoryContextValue(String key, TaskContext context) {
        Object transientObject = context.getTransientObject(key);
        if (!(transientObject instanceof String)) {
            throw buildTaskExceptionForMandatoryProperty(key);
        }

        String caseId = (String) transientObject;
        if (isNullOrEmpty(caseId)) {
            throw buildTaskExceptionForMandatoryProperty(key);
        }

        return caseId;
    }

    public static boolean isYes(String value) {
        return YES_VALUE.equalsIgnoreCase(value);
    }

    private static TaskException buildTaskExceptionForMandatoryProperty(String key) {
        return new TaskException(format("Could not evaluate value of mandatory property \"%s\"", key));
    }

}