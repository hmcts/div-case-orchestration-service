package uk.gov.hmcts.reform.divorce.orchestration.tasks;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.divorce.orchestration.client.FeesAndPaymentsClient;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.Task;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskContext;

import java.util.Map;

import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.AMEND_PETITION_FEE_JSON_KEY;

@Component
@RequiredArgsConstructor
public class GetAmendPetitionFeeTask implements Task<Map<String, Object>> {

    private final FeesAndPaymentsClient feesAndPaymentsClient;

    @Override
    public Map<String, Object> execute(TaskContext context, Map<String, Object> caseData) {
        context.setTransientObject(AMEND_PETITION_FEE_JSON_KEY, feesAndPaymentsClient.getAmendPetitioneFee());
        return caseData;
    }
}
