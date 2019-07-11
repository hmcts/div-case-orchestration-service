package uk.gov.hmcts.reform.divorce.orchestration.tasks;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.Task;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskException;

import java.util.Map;

import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DECREE_NISI_GRANTED_CCD_FIELD;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DIVORCE_COSTS_CLAIM_CCD_FIELD;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DIVORCE_COSTS_CLAIM_GRANTED_CCD_FIELD;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DN_COSTS_CLAIM_CCD_FIELD;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.EMPTY_STRING;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.YES_VALUE;
import static uk.gov.hmcts.reform.divorce.orchestration.tasks.util.TaskUtils.getOptionalPropertyValueAsString;

@Component
public class ValidateDNDecisionTask implements Task<Map<String, Object>> {

    private static final String DN_COST_END_CLAIM = "endClaim";

    @Override
    public Map<String, Object> execute(TaskContext context, Map<String, Object> payload) throws TaskException {

        String dnGranted = getOptionalPropertyValueAsString(payload, DECREE_NISI_GRANTED_CCD_FIELD, EMPTY_STRING);
        String claimCostGranted = getOptionalPropertyValueAsString(payload, DIVORCE_COSTS_CLAIM_GRANTED_CCD_FIELD, EMPTY_STRING);

        if (isClaimCostRequested(payload) && YES_VALUE.equalsIgnoreCase(dnGranted)) {
            if (StringUtils.isBlank(claimCostGranted)) {
                throw new TaskException("Cost decision expected");
            }
        } else if (StringUtils.isNotEmpty(claimCostGranted)) {
            throw new TaskException("Cost decision can only be made if cost has been requested");
        }

        return payload;
    }

    private boolean isClaimCostRequested(Map<String, Object> payload) {
        return YES_VALUE.equalsIgnoreCase(getOptionalPropertyValueAsString(payload, DIVORCE_COSTS_CLAIM_CCD_FIELD, EMPTY_STRING))
            && !DN_COST_END_CLAIM.equalsIgnoreCase(getOptionalPropertyValueAsString(payload, DN_COSTS_CLAIM_CCD_FIELD, EMPTY_STRING));
    }
}