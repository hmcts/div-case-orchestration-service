package uk.gov.hmcts.reform.divorce.orchestration.tasks;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.divorce.models.request.ValidationRequest;
import uk.gov.hmcts.reform.divorce.models.response.ValidationResponse;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.Task;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskContext;
import uk.gov.hmcts.reform.divorce.validation.service.ValidationService;

import java.util.Map;

import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.FORM_ID;

@Component
public class ValidateCaseData implements Task<Map<String, Object>> {
    private final ValidationService validationService;

    @Autowired
    public ValidateCaseData(ValidationService validationService) {
        this.validationService = validationService;
    }

    @Override
    public Map<String, Object> execute(TaskContext context, Map<String, Object> caseData) {
        ValidationResponse validationResponse =
                validationService.validate(
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
