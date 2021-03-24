package uk.gov.hmcts.reform.divorce.orchestration.tasks.aos;

import org.springframework.context.ApplicationEvent;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.divorce.orchestration.event.domain.AOSOfflineTriggerRequestEvent;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.AutoPublishingAsyncTask;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskException;

import java.util.List;

@Component
public class AosOfflineTriggerRequestTask extends AutoPublishingAsyncTask<String> {

    @Override
    public List<ApplicationEvent> getApplicationEventsToPublish(TaskContext context, String caseId) throws TaskException {
        return List.of(new AOSOfflineTriggerRequestEvent(this, caseId));
    }

}