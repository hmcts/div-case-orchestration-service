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

import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CASE_EVENT_ID_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.ID;

@Component
@Slf4j
@RequiredArgsConstructor
public class ValidateCaseDataTask implements Task<Map<String, Object>> {

    private final ValidationService validationService;
    private final ObjectMapper mapper;

    @Override
    public Map<String, Object> execute(TaskContext context, Map<String, Object> caseData) {
        String caseId = caseData.getOrDefault(ID, "").toString();

        String caseEventId = context.getTransientObject(CASE_EVENT_ID_JSON_KEY);

        log.info("Mapping: caseData, CoreCaseData.class");
        CoreCaseData coreCaseData = mapper.convertValue(caseData, CoreCaseData.class);
        log.info("Validating case data for case Id {}", caseId);
        ValidationResponse validationResponse = validationService.validate(coreCaseData, caseEventId);
        log.info("Finished Validation case data for case Id {}, valid = {}", caseId, validationResponse.getValidationStatus());

        if (!validationResponse.isValid()) {
            for (String s : validationResponse.getErrors()) {
                log.info("Validation Error: " + s);
            }
            context.setTaskFailed(true);
            context.setTransientObject(this.getClass().getName() + "_Error", validationResponse);
        }

        return caseData;
    }
}
