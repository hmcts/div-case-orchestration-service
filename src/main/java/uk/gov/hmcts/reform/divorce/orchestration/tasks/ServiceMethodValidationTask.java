package uk.gov.hmcts.reform.divorce.orchestration.tasks;

import com.google.common.base.Strings;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.Task;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskException;

import java.util.Map;

import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CASE_ID_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.PERSONAL_SERVICE_VALUE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.SOL_SERVICE_METHOD_CCD_FIELD;
import static uk.gov.hmcts.reform.divorce.orchestration.tasks.util.TaskUtils.getOptionalPropertyValueAsString;

@Slf4j
@Component
public class ServiceMethodValidationTask implements Task<Map<String, Object>> {

    @Override
    public Map<String, Object> execute(TaskContext context, Map<String, Object> payload) throws TaskException {
        String solServiceMethod = getOptionalPropertyValueAsString(payload, SOL_SERVICE_METHOD_CCD_FIELD, null);
        if (!Strings.isNullOrEmpty(solServiceMethod) && PERSONAL_SERVICE_VALUE.equals(solServiceMethod)) {
            final String caseId = context.getTransientObject(CASE_ID_JSON_KEY);
            log.error("Unexpected service method {} - Case ID: {}", solServiceMethod, caseId);
            throw new TaskException(
                    "This event cannot be used when the service method is Personal Service. "
                            + "Please use the Personal Service event instead"
            );
        }
        return payload;
    }
}
