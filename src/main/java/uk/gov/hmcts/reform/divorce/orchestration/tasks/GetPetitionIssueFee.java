package uk.gov.hmcts.reform.divorce.orchestration.tasks;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.divorce.orchestration.client.FeesAndPaymentsClient;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.Task;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskContext;

import java.util.Map;

import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.PETITION_FEE_JSON_KEY;

@Component
public class GetPetitionIssueFee implements Task<Map<String, Object>> {

    private final FeesAndPaymentsClient feesAndPaymentsClient;

    @Autowired
    public GetPetitionIssueFee(FeesAndPaymentsClient feesAndPaymentsClient) {
        this.feesAndPaymentsClient = feesAndPaymentsClient;
    }

    @Override
    public Map<String, Object> execute(TaskContext context, Map<String, Object> caseData) {
        context.setTransientObject(PETITION_FEE_JSON_KEY, feesAndPaymentsClient.getPetitionIssueFee());
        return caseData;
    }
}
