package uk.gov.hmcts.reform.divorce.orchestration.tasks;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.Task;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskException;

import java.util.HashMap;
import java.util.Map;

import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.BulkCaseConstants.CASE_REFERENCE_FIELD;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.BulkCaseConstants.VALUE_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.BULK_LISTING_CASE_ID_FIELD;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CASE_ID_JSON_KEY;


@Component
public class GetCaseIdFromCaseLink implements Task<Map<String,Object>> {

    @Override
    public Map<String, Object> execute(TaskContext context, Map<String, Object> caseLink) throws TaskException {

        Map<String, Object> caseLinkValue = (Map<String, Object>) caseLink.get(VALUE_KEY);
        Map<String, Object> caseReferenceObject  = (Map<String, Object>) caseLinkValue.get(CASE_REFERENCE_FIELD);
        String caseReference = (String) caseReferenceObject.get(CASE_REFERENCE_FIELD);
        context.setTransientObject(CASE_ID_JSON_KEY, caseReference);
        String bulkListCaseId = String.valueOf(context.getTransientObject(BULK_LISTING_CASE_ID_FIELD));

        HashMap<String, Object> caseData = new HashMap<>();
        caseData.put(BULK_LISTING_CASE_ID_FIELD, bulkListCaseId);
        return caseData;
    }
}
