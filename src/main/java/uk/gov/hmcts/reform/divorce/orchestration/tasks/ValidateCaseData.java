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

import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.FORM_ID;

@Component
public class ValidateCaseData implements Task<Map<String, Object>> {
    private final CaseValidationClient caseValidationClient;

    @Autowired
    public ValidateCaseData(CaseValidationClient caseValidationClient) {
        this.caseValidationClient = caseValidationClient;
    }

    @Override
    public Map<String, Object> execute(TaskContext context, Map<String, Object> caseData) throws TaskException {
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
    }
}
