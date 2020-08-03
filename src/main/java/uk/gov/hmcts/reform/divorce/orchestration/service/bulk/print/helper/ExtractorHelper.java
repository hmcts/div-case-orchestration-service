package uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.helper;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.InvalidDataForTaskException;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskException;

import java.util.Map;

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

}
