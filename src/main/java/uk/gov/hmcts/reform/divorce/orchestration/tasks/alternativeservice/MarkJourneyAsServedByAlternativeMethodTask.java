package uk.gov.hmcts.reform.divorce.orchestration.tasks.alternativeservice;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.CcdFields;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.Task;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskContext;

import java.util.Map;

import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NO_VALUE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.YES_VALUE;

@Component
public class MarkJourneyAsServedByAlternativeMethodTask implements Task<Map<String, Object>> {

    @Override
    public Map<String, Object> execute(TaskContext context, Map<String, Object> payload)  {
        payload.put(CcdFields.SERVED_BY_ALTERNATIVE_METHOD, YES_VALUE);
        payload.put(CcdFields.SERVED_BY_PROCESS_SERVER, NO_VALUE);
        return payload;
    }
}
