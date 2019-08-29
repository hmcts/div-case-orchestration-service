package uk.gov.hmcts.reform.divorce.orchestration.tasks;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.Task;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskException;

import java.util.HashMap;
import java.util.Map;

import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.COSTS_ORDER_ADDITIONAL_INFO_CCD_FIELD;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DECREE_NISI_GRANTED_CCD_FIELD;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DIVORCE_COSTS_CLAIM_GRANTED_CCD_FIELD;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.TYPE_COSTS_DECISION_CCD_FIELD;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.WHO_PAYS_COSTS_CCD_FIELD;

@Component
public class RemoveLegalAdvisorMakeDecisionFieldsTask implements Task<Map<String, Object>> {

    @Override
    public Map<String, Object> execute(TaskContext context, final Map<String, Object> payload) throws TaskException {
        Map<String, Object> response = new HashMap<>(payload);
        response.remove(DECREE_NISI_GRANTED_CCD_FIELD);
        response.remove(DIVORCE_COSTS_CLAIM_GRANTED_CCD_FIELD);
        response.remove(WHO_PAYS_COSTS_CCD_FIELD);
        response.remove(TYPE_COSTS_DECISION_CCD_FIELD);
        response.remove(COSTS_ORDER_ADDITIONAL_INFO_CCD_FIELD);
        return response;
    }
}
