package uk.gov.hmcts.reform.divorce.orchestration.tasks;

import com.google.common.base.Strings;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.Task;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskException;

import java.util.Map;

import static uk.gov.hmcts.reform.divorce.orchestration.tasks.util.TaskUtils.getOptionalPropertyValueAsString;

@Slf4j
@Component
public class ValidateServiceMethodTask implements Task<Map<String, Object>> {

    private static final String SOL_SERVICE_METHOD = "SolServiceMethod";
    private static final String PERSONAL_SERVICE = "personalService";

    @Override
    public Map<String, Object> execute(TaskContext context, Map<String, Object> payload) throws TaskException {
        String solServiceMethod = getOptionalPropertyValueAsString(payload, SOL_SERVICE_METHOD, null);
        if(!Strings.isNullOrEmpty(solServiceMethod)) {
            if(PERSONAL_SERVICE.equals(solServiceMethod)) {
                throw new TaskException(
                        "This event cannot be used when the service method is Personal Service. " +
                                "Please use the Personal Service event instead"
                );
            }
        }
        return payload;
    }
}
