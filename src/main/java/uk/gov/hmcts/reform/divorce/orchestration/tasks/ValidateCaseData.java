package uk.gov.hmcts.reform.divorce.orchestration.tasks;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.divorce.orchestration.client.CaseValidationClient;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.validation.ValidationRequest;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.validation.ValidationResponse;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.Task;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskContext;

import java.util.Map;

import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.FORM_ID;

@Slf4j
@Component
public class ValidateCaseData implements Task<Map<String, Object>> {
    private final CaseValidationClient caseValidationClient;

    @Autowired
    public ValidateCaseData(CaseValidationClient caseValidationClient) {
        this.caseValidationClient = caseValidationClient;
    }

    @Override
    public Map<String, Object> execute(TaskContext context, Map<String, Object> caseData) {
        ValidationResponse validationResponse =
                caseValidationClient.validate(
                        ValidationRequest.builder()
                                .data(caseData)
                                .formId(FORM_ID)
                                .build());

        if (!validationResponse.isValid()) {
            context.setTaskFailed(true);
            log.info("VS RESPONSE {}", validationResponse);
            log.error("VS RESPONSE ERR {}", validationResponse);
            context.setTransientObject(this.getClass().getName() + "_Error", validationResponse);
        }

        return caseData;
    }
}
