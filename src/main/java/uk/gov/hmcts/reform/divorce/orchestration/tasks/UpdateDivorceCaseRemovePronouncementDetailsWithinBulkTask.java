package uk.gov.hmcts.reform.divorce.orchestration.tasks;

import org.springframework.context.ApplicationEvent;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CaseDetails;
import uk.gov.hmcts.reform.divorce.orchestration.event.bulk.BulkCaseRemovePronouncementDetailsEvent;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.AutoPublishingAsyncTask;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskContext;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CASE_ID_JSON_KEY;

@Component
public class UpdateDivorceCaseRemovePronouncementDetailsWithinBulkTask extends AutoPublishingAsyncTask<Map<String, Object>> {

    @Override
    public List<ApplicationEvent> getApplicationEventsToPublish(TaskContext context, Map<String, Object> bulkCaseData) {
        String bulkCaseId = context.getTransientObject(CASE_ID_JSON_KEY);

        return Collections.singletonList(new BulkCaseRemovePronouncementDetailsEvent(context, CaseDetails.builder()
                .caseId(bulkCaseId)
                .caseData(bulkCaseData)
                .build()));
    }
}
