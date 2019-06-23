package uk.gov.hmcts.reform.divorce.orchestration.tasks;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.Task;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskException;

import java.util.Map;

import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.BulkCaseConstants.BULK_CASE_DETAILS_CONTEXT_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CCD_CASE_DATA_FIELD;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DECREE_ABSOLUTE_ELIGIBLE_DATE_CCD_FIELD;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DECREE_NISI_GRANTED_DATE_CCD_FIELD;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.PRONOUNCEMENT_JUDGE_CCD_FIELD;

@Component
public class SetDnGrantedDetailsFromBulkCase implements Task<Map<String,Object>> {

    @Override
    public Map<String, Object> execute(TaskContext context, Map<String, Object> payload) throws TaskException {
        Map<String, Object> bulkCaseDetails = context.getTransientObject(BULK_CASE_DETAILS_CONTEXT_KEY);
        Map<String, Object> bulkCaseData = (Map<String, Object>) bulkCaseDetails.get(CCD_CASE_DATA_FIELD);

        payload.put(DECREE_NISI_GRANTED_DATE_CCD_FIELD, bulkCaseData.get(DECREE_NISI_GRANTED_DATE_CCD_FIELD));
        payload.put(DECREE_ABSOLUTE_ELIGIBLE_DATE_CCD_FIELD, bulkCaseData.get(DECREE_ABSOLUTE_ELIGIBLE_DATE_CCD_FIELD));
        payload.put(PRONOUNCEMENT_JUDGE_CCD_FIELD, bulkCaseData.get(PRONOUNCEMENT_JUDGE_CCD_FIELD));

        return payload;
    }
}
