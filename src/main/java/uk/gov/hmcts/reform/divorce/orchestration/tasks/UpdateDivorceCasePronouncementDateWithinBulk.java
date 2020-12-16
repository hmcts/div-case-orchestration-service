package uk.gov.hmcts.reform.divorce.orchestration.tasks;

import org.springframework.context.ApplicationEvent;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.divorce.orchestration.event.bulk.BulkCaseUpdatePronouncementDateEvent;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.AutoPublishingAsyncTask;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskContext;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.BulkCaseConstants.BULK_CASE_DETAILS_CONTEXT_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CCD_CASE_DATA_FIELD;

@Component
public class UpdateDivorceCasePronouncementDateWithinBulk extends AutoPublishingAsyncTask<Map<String, Object>> {

    @Override
    public List<ApplicationEvent> getApplicationEventsToPublish(TaskContext context, Map<String, Object> bulkCaseData) {
        Map<String, Object> bulkCaseDetails = context.getTransientObject(BULK_CASE_DETAILS_CONTEXT_KEY);
        bulkCaseDetails.put(CCD_CASE_DATA_FIELD, bulkCaseData);

        return Collections.singletonList(new BulkCaseUpdatePronouncementDateEvent(context, bulkCaseDetails));
    }
}
