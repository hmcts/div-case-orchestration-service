package uk.gov.hmcts.reform.divorce.orchestration.tasks;

import com.google.common.collect.ImmutableMap;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.Task;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskException;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.BulkCaseConstants.CASE_REFERENCE_FIELD;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.BulkCaseConstants.VALUE_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.BULK_LISTING_CASE_ID_FIELD;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CASE_ID_JSON_KEY;


@Component
public class GetCaseIdFromCaseLink implements Task<Map<String,Object>> {

    @Override
    public Map<String, Object> execute(TaskContext context, Map<String, Object> caseLink) throws TaskException {

        Map<String, Object> caseLinkValue = (Map<String, Object>) caseLink.getOrDefault(VALUE_KEY, Collections.emptyMap());
        Map<String, Object> caseReferenceObject  = (Map<String, Object>) caseLinkValue.getOrDefault(CASE_REFERENCE_FIELD, Collections.emptyMap());
        String caseReference = Optional.ofNullable(caseReferenceObject.get(CASE_REFERENCE_FIELD))
            .map(String.class::cast)
            .orElseThrow(() -> new TaskException("Case reference not present."));

        context.setTransientObject(CASE_ID_JSON_KEY, caseReference);
        String bulkListCaseId = context.getTransientObject(BULK_LISTING_CASE_ID_FIELD);

        Map<String, Object> caseData = new HashMap<>();
        caseData.put(BULK_LISTING_CASE_ID_FIELD,  ImmutableMap.of(CASE_REFERENCE_FIELD, bulkListCaseId));
        return caseData;
    }
}
