package uk.gov.hmcts.reform.divorce.orchestration.tasks.util;

import uk.gov.hmcts.reform.divorce.model.ccd.CollectionMember;
import uk.gov.hmcts.reform.divorce.model.ccd.Document;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskException;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static com.google.common.base.Strings.isNullOrEmpty;
import static java.lang.String.format;
import static java.util.Optional.ofNullable;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CASE_ID_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D8DOCUMENTS_GENERATED;
import static uk.gov.hmcts.reform.divorce.orchestration.util.CcdUtil.parseDateUsingCcdFormat;

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

    @SuppressWarnings("unchecked")
    public static boolean isListOfMap(Map<String, Object> caseData) {
        List<Object> list = (List<Object>) caseData.get(D8DOCUMENTS_GENERATED);

        return list != null && !list.isEmpty() && list.get(0) instanceof LinkedHashMap;
    }

    public static List<Map> getGeneratedDocumentListOfMap(Map<String, Object> caseData) {
        return ofNullable(caseData.get(D8DOCUMENTS_GENERATED)).map(i -> (List<Map>) i).orElse(new ArrayList<>());
    }

    public static List<CollectionMember<Document>> getGeneratedDocumentListOfCm(Map<String, Object> caseData) {
        return ofNullable(caseData.get(D8DOCUMENTS_GENERATED))
            .map(i -> (List<CollectionMember<Document>>) i)
            .orElse(new ArrayList<>());
    }

    private static TaskException buildTaskExceptionForMandatoryProperty(String key) {
        return new TaskException(format("Could not evaluate value of mandatory property \"%s\"", key));
    }

}