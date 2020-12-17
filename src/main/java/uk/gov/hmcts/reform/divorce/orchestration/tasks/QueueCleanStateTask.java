package uk.gov.hmcts.reform.divorce.orchestration.tasks;

import org.springframework.context.ApplicationEvent;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.divorce.orchestration.event.domain.CleanStatusEvent;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.AutoPublishingAsyncTask;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskContext;

import java.util.List;
import java.util.Map;

import static java.util.Collections.singletonList;

@Component
public class QueueCleanStateTask extends AutoPublishingAsyncTask<Map<String, Object>> {

    @Override
    public List<ApplicationEvent> getApplicationEventsToPublish(TaskContext context, Map<String, Object> payload) {
        return  singletonList(new CleanStatusEvent(context));
    }
}
