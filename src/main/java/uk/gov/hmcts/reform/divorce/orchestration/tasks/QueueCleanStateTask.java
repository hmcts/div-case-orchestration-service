package uk.gov.hmcts.reform.divorce.orchestration.tasks;

import org.springframework.context.ApplicationEvent;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.divorce.orchestration.event.CleanStatusEvent;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.AsyncTask;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskContext;

import java.util.List;
import java.util.Map;

import static java.util.Collections.singletonList;

@Component
public class QueueCleanStateTask extends AsyncTask<Map<String, Object>> {

    @Override
    public List<ApplicationEvent> getApplicationEvent(TaskContext context, Map<String, Object> payload) {
        return  singletonList(new CleanStatusEvent(context));
    }
}
