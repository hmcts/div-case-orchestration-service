package uk.gov.hmcts.reform.divorce.orchestration.tasks;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.divorce.model.ccd.CoreCaseData;
import uk.gov.hmcts.reform.divorce.model.response.ValidationResponse;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.Task;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskContext;
import uk.gov.hmcts.reform.divorce.validation.service.ValidationService;

import java.util.Map;

@Component
@Slf4j
@RequiredArgsConstructor
public class ValidateCaseDataTask implements Task<Map<String, Object>> {

    private final ValidationService validationService;
    private final ObjectMapper mapper;

    @Override
    public Map<String, Object> execute(TaskContext context, Map<String, Object> caseData) {
        log.info("Mapping: caseData, CoreCaseData.class");
        CoreCaseData coreCaseData = mapper.convertValue(caseData, CoreCaseData.class);
        log.info("Validating case data");
        ValidationResponse validationResponse = validationService.validate(coreCaseData);
        log.info("Finished Validation case data");

        if (!validationResponse.isValid()) {
            context.setTaskFailed(true);
            context.setTransientObject(this.getClass().getName() + "_Error", validationResponse);
        }

        return caseData;
    }
}
