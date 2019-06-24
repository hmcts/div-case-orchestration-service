package uk.gov.hmcts.reform.divorce.orchestration.tasks;

import com.google.common.collect.ImmutableMap;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.Task;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskException;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.BulkCaseConstants.CASE_REFERENCE_FIELD;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.BULK_LISTING_CASE_ID_FIELD;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.ID;


@Component
public class GetCaseIdFromCaseLink implements Task<Map<String,Object>> {

    @Override
    public Map<String, Object> execute(TaskContext context, Map<String, Object> caseLink) throws TaskException {

        String bulkCaseId = Optional.ofNullable(caseLink.get(ID))
            .map(String::valueOf)
            .orElseThrow(() -> new TaskException("Case reference not present."));
        Map<String, Object> caseData = new HashMap<>();
        caseData.put(BULK_LISTING_CASE_ID_FIELD,  ImmutableMap.of(CASE_REFERENCE_FIELD, bulkCaseId));
        return caseData;
    }
}
