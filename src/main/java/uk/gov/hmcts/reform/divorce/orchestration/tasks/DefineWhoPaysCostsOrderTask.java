package uk.gov.hmcts.reform.divorce.orchestration.tasks;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.Task;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskContext;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.WHO_PAYS_CCD_CODE_FOR_RESPONDENT;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.WHO_PAYS_COSTS_CCD_FIELD;

@Component
public class DefineWhoPaysCostsOrderTask implements Task<Map<String, Object>> {

    @Override
    public Map<String, Object> execute(TaskContext context, Map<String, Object> payload) {
        String whoShouldPay = Optional.ofNullable(payload.get(WHO_PAYS_COSTS_CCD_FIELD))
            .map(String.class::cast)
            .orElse(WHO_PAYS_CCD_CODE_FOR_RESPONDENT);

        Map<String, Object> payloadToReturn = new HashMap<>(payload);
        payloadToReturn.put(WHO_PAYS_COSTS_CCD_FIELD, whoShouldPay);

        return payloadToReturn;
    }

}