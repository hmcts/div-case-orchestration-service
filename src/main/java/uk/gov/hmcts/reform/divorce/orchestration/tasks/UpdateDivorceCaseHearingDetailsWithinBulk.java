package uk.gov.hmcts.reform.divorce.orchestration.tasks;

import org.springframework.context.ApplicationEvent;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.divorce.orchestration.event.bulk.BulkCaseUpdateCourtHearingEvent;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.AutoPublishingAsyncTask;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskContext;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@Component
public class UpdateDivorceCaseHearingDetailsWithinBulk extends AutoPublishingAsyncTask<Map<String, Object>> {

    @Override
    public List<ApplicationEvent> getApplicationEventsToPublish(TaskContext context, Map<String, Object> bulkCase) {
        return Collections.singletonList(new BulkCaseUpdateCourtHearingEvent(context, bulkCase));
    }
}
