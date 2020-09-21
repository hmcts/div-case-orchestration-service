package uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.helper;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.InvalidDataForTaskException;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskException;

import java.util.List;
import java.util.Map;

import static uk.gov.hmcts.reform.divorce.orchestration.tasks.util.TaskUtils.getMandatoryPropertyValueAsObject;
import static uk.gov.hmcts.reform.divorce.orchestration.tasks.util.TaskUtils.getMandatoryPropertyValueAsString;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ExtractorHelper {

    public static String getMandatoryStringValue(Map<String, Object> caseData, String field) {
        try {
            return getMandatoryPropertyValueAsString(caseData, field);
        } catch (TaskException exception) {
            throw new InvalidDataForTaskException(exception);
        }
    }

    public static List<String> getMandatoryListOfStrings(Map<String, Object> caseData, String field) {
        try {
            return  (List<String>) getMandatoryPropertyValueAsObject(caseData, field);
        } catch (TaskException exception) {
            throw new InvalidDataForTaskException(exception);
        }
    }

}
