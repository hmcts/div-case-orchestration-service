package uk.gov.hmcts.reform.divorce.orchestration.tasks.alternativeservice;

import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.CcdFields;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.Task;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskContext;

import java.util.Map;

import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.YES_VALUE;

@Service
public class MarkJourneyAsServedByProcessServerTask implements Task<Map<String, Object>> {

    @Override
    public Map<String, Object> execute(TaskContext context, Map<String, Object> payload)  {
        payload.put(CcdFields.SERVED_BY_PROCESS_SERVER, YES_VALUE);
        return payload;
    }
}
