package uk.gov.hmcts.reform.divorce.orchestration.tasks;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.divorce.orchestration.client.CaseValidationClient;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.validation.ValidationRequest;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.validation.ValidationResponse;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.Task;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskException;

import java.util.Map;

@Component
public class ValidateCaseData implements Task<Map<String, Object>> {

    private static final String FORM_ID = "case-progression";

    @Autowired
    private CaseValidationClient caseValidationClient;

    @Override
    public Map<String, Object> execute(TaskContext context, Map<String, Object> caseData) throws TaskException {
        try {
            ValidationResponse validationResponse =
                    caseValidationClient.validate(
                            ValidationRequest.builder()
                                    .data(caseData)
                                    .formId(FORM_ID)
                                    .build());

            if (!validationResponse.isValid()) {
                context.setTaskFailed(true);
                context.setTransientObject(this.getClass().getName() + "_Error", validationResponse);
            }

            return caseData;
        } catch (Exception exception) {
            throw new TaskException(exception.getMessage());
        }
    }
}
