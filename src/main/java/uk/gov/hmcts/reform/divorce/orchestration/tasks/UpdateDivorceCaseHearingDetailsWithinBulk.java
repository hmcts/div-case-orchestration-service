package uk.gov.hmcts.reform.divorce.orchestration.tasks;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEvent;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.divorce.orchestration.event.bulk.BulkCaseUpdateCourtHearingEvent;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.AsyncTask;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskContext;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CASE_DETAILS_JSON_KEY;

@Component
@RequiredArgsConstructor
public class UpdateDivorceCaseHearingDetailsWithinBulk extends AsyncTask<Map<String, Object>> {

    private final ObjectMapper objectMapper;

    @Override
    public List<ApplicationEvent> getApplicationEvent(TaskContext context, Map<String, Object> bulkCase) {
        Map<String, Object> bulkCaseDetails = objectMapper.convertValue(context.getTransientObject(CASE_DETAILS_JSON_KEY), Map.class);
        return Collections.singletonList(new BulkCaseUpdateCourtHearingEvent(context, bulkCaseDetails));
    }
}
