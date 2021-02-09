package uk.gov.hmcts.reform.divorce.orchestration.tasks;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.divorce.model.ccd.CoreCaseData;
import uk.gov.hmcts.reform.divorce.model.response.ValidationResponse;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.Task;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskContext;
import uk.gov.hmcts.reform.divorce.validation.service.ValidationService;

import java.util.Map;

@Component
@RequiredArgsConstructor
public class ValidateCaseDataTask implements Task<Map<String, Object>> {

    private final ValidationService validationService;
    private final ObjectMapper mapper;

    @Override
    public Map<String, Object> execute(TaskContext context, Map<String, Object> caseData) {
        CoreCaseData coreCaseData = mapper.convertValue(caseData, CoreCaseData.class);

        ValidationResponse validationResponse = validationService.validate(coreCaseData);

        if (!validationResponse.isValid()) {
            context.setTaskFailed(true);
            context.setTransientObject(this.getClass().getName() + "_Error", validationResponse);
        }

        return caseData;
    }
}
