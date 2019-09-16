package uk.gov.hmcts.reform.divorce.orchestration.tasks;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.Task;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskException;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.BulkCaseConstants.SEARCH_RESULT_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CASES_OVERDUE_FOR_DA_PROCESSED_COUNT;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CASE_ID_JSON_KEY;

@Component
public class UpdateDAOverdueCase implements Task<Map<String, Object>> {

    @Autowired
    UpdateCaseInCCD updateCaseInCCD;

    @Override
    public Map<String, Object> execute(TaskContext context, Map<String, Object> payload) throws TaskException {

        AtomicInteger casesProcessed = new AtomicInteger();
        List<String> caseIds = context.<List<String>>getTransientObjectOptional(SEARCH_RESULT_KEY)
            .orElse(Collections.emptyList());

        caseIds.forEach(caseId -> {
            context.setTransientObject(CASE_ID_JSON_KEY, caseId);
            updateCaseInCCD.execute(context, Collections.emptyMap());
            casesProcessed.getAndIncrement();
        });

        context.setTransientObject(CASES_OVERDUE_FOR_DA_PROCESSED_COUNT, casesProcessed.get());

        return payload;
    }
}
