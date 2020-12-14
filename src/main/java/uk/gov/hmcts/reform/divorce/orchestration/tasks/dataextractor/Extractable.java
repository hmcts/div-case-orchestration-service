package uk.gov.hmcts.reform.divorce.orchestration.tasks.dataextractor;

import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.InvalidDataForTaskException;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskException;

import java.util.List;
import java.util.Map;

import static uk.gov.hmcts.reform.divorce.orchestration.tasks.util.TaskUtils.getMandatoryPropertyValueAsObject;
import static uk.gov.hmcts.reform.divorce.orchestration.tasks.util.TaskUtils.getMandatoryPropertyValueAsString;

interface Extractable {

    default String getMandatoryStringValue(Map<String, Object> caseData, String field) {
        try {
            return getMandatoryPropertyValueAsString(caseData, field);
        } catch (TaskException exception) {
            throw new InvalidDataForTaskException(exception);
        }
    }

    default List<String> getMandatoryListOfStrings(Map<String, Object> caseData, String field) {
        try {
            return (List<String>) getMandatoryPropertyValueAsObject(caseData, field);
        } catch (TaskException exception) {
            throw new InvalidDataForTaskException(exception);
        }
    }

}
