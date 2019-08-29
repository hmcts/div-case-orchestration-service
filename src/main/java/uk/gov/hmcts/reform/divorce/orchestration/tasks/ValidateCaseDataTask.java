package uk.gov.hmcts.reform.divorce.orchestration.tasks;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.divorce.models.request.CoreCaseData;
import uk.gov.hmcts.reform.divorce.models.response.ValidationResponse;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.Task;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskContext;
import uk.gov.hmcts.reform.divorce.validation.service.ValidationService;

import java.util.Map;

@Component
public class ValidateCaseDataTask implements Task<Map<String, Object>> {
    private final ValidationService validationService;

    @Autowired
    public ValidateCaseDataTask(ValidationService validationService) {
        this.validationService = validationService;
    }

    @Override
    public Map<String, Object> execute(TaskContext context, Map<String, Object> caseData) {
        ObjectMapper mapper = new ObjectMapper();
        CoreCaseData coreCaseData = mapper.convertValue(caseData, CoreCaseData.class);

        ValidationResponse validationResponse = validationService.validate(coreCaseData);

        if (!validationResponse.isValid()) {
            context.setTaskFailed(true);
            context.setTransientObject(this.getClass().getName() + "_Error", validationResponse);
        }

        return caseData;
    }
}
