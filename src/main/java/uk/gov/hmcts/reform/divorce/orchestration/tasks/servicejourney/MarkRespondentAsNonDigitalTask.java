package uk.gov.hmcts.reform.divorce.orchestration.tasks.servicejourney;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.Task;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskException;

import java.util.Map;

@Component
@RequiredArgsConstructor
public class MarkRespondentAsNonDigitalTask implements Task<Map<String, Object>> {

    @Override
    public Map<String, Object> execute(TaskContext context, Map<String, Object> caseData) throws TaskException {
        String digitalChannel = (String) caseData.get(OrchestrationConstants.RESP_IS_USING_DIGITAL_CHANNEL);
        if (digitalChannel == null) {
            caseData.put(OrchestrationConstants.RESP_IS_USING_DIGITAL_CHANNEL, OrchestrationConstants.NO_VALUE);
        }
        return caseData;
    }
}
