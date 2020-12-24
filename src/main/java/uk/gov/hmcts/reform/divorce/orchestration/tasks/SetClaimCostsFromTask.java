package uk.gov.hmcts.reform.divorce.orchestration.tasks;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.Task;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskContext;

import java.util.Arrays;
import java.util.Map;

import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DIVORCE_COSTS_CLAIM_FROM_CCD_CODE_FOR_RESPONDENT;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DIVORCE_COSTS_CLAIM_FROM_CCD_FIELD;

@Component
public class SetClaimCostsFromTask implements Task<Map<String, Object>> {

    @Override
    public Map<String, Object> execute(TaskContext context, Map<String, Object> payload) {
        payload.put(
            DIVORCE_COSTS_CLAIM_FROM_CCD_FIELD,
            Arrays.asList(DIVORCE_COSTS_CLAIM_FROM_CCD_CODE_FOR_RESPONDENT)
        );

        return payload;
    }
}
