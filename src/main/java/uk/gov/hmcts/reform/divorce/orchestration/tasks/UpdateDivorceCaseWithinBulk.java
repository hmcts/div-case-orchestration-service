package uk.gov.hmcts.reform.divorce.orchestration.tasks;

import org.springframework.context.ApplicationEvent;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.divorce.orchestration.event.bulk.BulkCaseCreateEvent;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.AsyncTask;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskContext;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static java.util.stream.Collectors.toList;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.BulkCaseConstants.BULK_CASE_LIST_KEY;

@Component
public class UpdateDivorceCaseWithinBulk extends AsyncTask<Map<String, Object>> {

    @Override
    public List<ApplicationEvent> getApplicationEvent(TaskContext context, Map<String, Object> payload) {
        List<Map<String,Object>> bulkCases = (List<Map<String, Object>>) payload.getOrDefault(BULK_CASE_LIST_KEY, Collections.emptyList());
        return  bulkCases.stream()
            .map(bulkCase -> new BulkCaseCreateEvent(context, bulkCase))
            .collect(toList());
    }
}
