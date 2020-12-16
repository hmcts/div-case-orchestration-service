package uk.gov.hmcts.reform.divorce.orchestration.tasks;

import com.google.common.collect.ImmutableMap;
import org.springframework.context.ApplicationEvent;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.divorce.orchestration.event.bulk.BulkCaseCancelPronouncementEvent;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.AutoPublishingAsyncTask;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskContext;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CASE_ID_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CCD_CASE_DATA_FIELD;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.ID;

@Component
public class CancelPronouncementDetailsWithinBulk extends AutoPublishingAsyncTask<Map<String, Object>> {

    @Override
    public List<ApplicationEvent> getApplicationEventsToPublish(TaskContext context, Map<String, Object> bulkCaseData) {
        String bulkCaseId = context.getTransientObject(CASE_ID_JSON_KEY);

        return Collections.singletonList(new BulkCaseCancelPronouncementEvent(context,
            ImmutableMap.of(ID, bulkCaseId,
                CCD_CASE_DATA_FIELD, bulkCaseData)));
    }
}
