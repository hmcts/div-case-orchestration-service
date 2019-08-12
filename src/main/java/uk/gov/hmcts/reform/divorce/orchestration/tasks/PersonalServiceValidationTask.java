package uk.gov.hmcts.reform.divorce.orchestration.tasks;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.Task;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskException;

import java.util.Map;

import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CASE_ID_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.tasks.util.TaskUtils.getMandatoryPropertyValueAsString;

@Slf4j
@Component
public class PersonalServiceValidationTask implements Task<Map<String, Object>> {

    private static final String SOL_SERVICE_METHOD = "SolServiceMethod";
    private static final String PERSONAL_SERVICE = "personalService";

    @Override
    public Map<String, Object> execute(TaskContext context, Map<String, Object> payload) throws TaskException {
        String solServiceMethod = getMandatoryPropertyValueAsString(payload, SOL_SERVICE_METHOD);
        if(!PERSONAL_SERVICE.equals(solServiceMethod)) {
            final String caseId = ;

            log.error("Unexpected service method for Personal Service event {}", context.getTransientObject(CASE_ID_JSON_KEY));
            throw new TaskException(
                    "This event can only be used with for a case with Personal Service as the service method"
            );
        }
        return payload;
    }
}
