package uk.gov.hmcts.reform.divorce.orchestration.tasks;

import com.google.common.base.Strings;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.Task;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskException;

import java.util.Map;

import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CASE_ID_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CASE_STATE_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.PERSONAL_SERVICE_VALUE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.SOL_SERVICE_METHOD_CCD_FIELD;
import static uk.gov.hmcts.reform.divorce.orchestration.tasks.util.TaskUtils.getOptionalPropertyValueAsString;

@Slf4j
@Component
public class ServiceMethodValidationTask implements Task<Map<String, Object>> {

    private static final String AWAITING_SERVICE_STATE = "AwaitingService";

    @Override
    public Map<String, Object> execute(TaskContext context, Map<String, Object> payload) throws TaskException {
        String solServiceMethod = getOptionalPropertyValueAsString(payload, SOL_SERVICE_METHOD_CCD_FIELD, null);
        if (!Strings.isNullOrEmpty(solServiceMethod) && PERSONAL_SERVICE_VALUE.equals(solServiceMethod)) {
            String currentCaseState = context.getTransientObject(CASE_STATE_JSON_KEY);
            if (!AWAITING_SERVICE_STATE.equals(currentCaseState)) {
                final String caseId = context.getTransientObject(CASE_ID_JSON_KEY);
                log.error("Unexpected service method {} - Case ID: {}, State: {}",
                        solServiceMethod,
                        caseId,
                        currentCaseState
                );
                throw new TaskException(
                        "This event cannot be used when service method is "
                                + "Personal Service and the case is not in Awaiting Service."
                );
            }
        }
        return payload;
    }
}
