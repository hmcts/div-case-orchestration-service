package uk.gov.hmcts.reform.divorce.orchestration.tasks;

import org.springframework.context.ApplicationEvent;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CaseDetails;
import uk.gov.hmcts.reform.divorce.orchestration.event.bulk.BulkCaseAcceptedCasesEvent;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.AsyncTask;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskContext;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CASE_ID_JSON_KEY;

@Component
public class UpdateDivorceCasesWithinBulkTask extends AsyncTask<Map<String, Object>> {

    @Override
    public List<ApplicationEvent> getApplicationEvent(TaskContext context, Map<String, Object> bulkCaseData) {
        String bulkCaseId = context.getTransientObject(CASE_ID_JSON_KEY);

        return Collections.singletonList(new BulkCaseAcceptedCasesEvent(context, CaseDetails.builder()
            .caseId(bulkCaseId)
            .caseData(bulkCaseData)
            .build()));
    }
}
