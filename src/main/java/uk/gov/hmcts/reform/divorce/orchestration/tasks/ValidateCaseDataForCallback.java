package uk.gov.hmcts.reform.divorce.orchestration.tasks;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.Task;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskException;

import java.util.Map;

@Component
public class ValidateCaseDataForCallback implements Task<Map<String, Object>> {

    @Autowired
    private ValidateCaseData validateCaseData;

    @SuppressWarnings("unchecked")
    @Override
    public Map<String, Object> execute(TaskContext context, Map<String, Object> caseDetails) throws TaskException {
        try {
            validateCaseData.execute(context, (Map<String, Object>) caseDetails.get("case_data"));
            return caseDetails;
        } catch (Exception exception) {
            throw new TaskException(exception.getMessage());
        }
    }
}
