package uk.gov.hmcts.reform.divorce.orchestration.workflows;

import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.DefaultWorkflow;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.WorkflowException;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.Task;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.FormatDivorceSessionToCaseData;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.GetPaymentInfo;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.UpdatePaymentMadeCase;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.PopulateExistingCollections;

import java.util.Map;

import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.AUTH_TOKEN_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CASE_ID_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CASE_STATE_JSON_KEY;

@Component
@RequiredArgsConstructor
public class UpdatePaymentWorkflow extends DefaultWorkflow<Map<String, Object>> {
    private final GetPaymentInfo getPaymentOnSession;
    private final UpdatePaymentMadeCase paymentMadeEvent;
    private final FormatDivorceSessionToCaseData formatDivorceSessionToCaseData;
    private final PopulateExistingCollections populateExistingCollections;

    public Map<String, Object> run(String authToken, Map<String, Object> caseData) throws WorkflowException {
        return this.execute(
            new Task[] {
                getPaymentOnSession,
                populateExistingCollections,
                formatDivorceSessionToCaseData,
                paymentMadeEvent
            },
            caseData,
            ImmutablePair.of(AUTH_TOKEN_JSON_KEY, authToken),
            ImmutablePair.of(CASE_ID_JSON_KEY, caseData.get(CASE_ID_JSON_KEY)),
            ImmutablePair.of(CASE_STATE_JSON_KEY, caseData.get(CASE_STATE_JSON_KEY))
        );
    }
}
