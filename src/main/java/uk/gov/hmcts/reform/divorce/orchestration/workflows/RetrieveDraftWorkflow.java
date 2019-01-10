package uk.gov.hmcts.reform.divorce.orchestration.workflows;

import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.DefaultWorkflow;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.WorkflowException;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.Task;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.CaseDataDraftToDivorceFormatter;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.FormatDivorceSessionToCaseData;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.GetPaymentInfo;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.UpdatePaymentMadeCase;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.RetrieveDraft;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.SetCaseIdAndStateOnSession;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.AUTH_TOKEN_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CHECK_CCD;

@Component
@RequiredArgsConstructor
public class RetrieveDraftWorkflow extends DefaultWorkflow<Map<String, Object>> {

    private final RetrieveDraft retrieveDraft;
    private final CaseDataDraftToDivorceFormatter caseDataToDivorceFormatter;
    private final SetCaseIdAndStateOnSession setCaseIdAndStateOnSession;

    private final GetPaymentInfo getPaymentOnSession;
    private final UpdatePaymentMadeCase paymentMadeEvent;
    private final FormatDivorceSessionToCaseData formatDivorceSessionToCaseData;

    public Map<String, Object> run(String authToken, Boolean checkCcd) throws WorkflowException {
        Map<String, Object> caseData = this.execute(
            new Task[] {
                retrieveDraft
            },
            new HashMap<>(),
            ImmutablePair.of(AUTH_TOKEN_JSON_KEY, authToken),
            ImmutablePair.of(CHECK_CCD, checkCcd)
        );
        boolean paymentDataUpdated = updatePaymentEvent(caseData);
        Task[] taskPending = paymentDataUpdated ? new Task[] {
            retrieveDraft,
            caseDataToDivorceFormatter,
            setCaseIdAndStateOnSession
        } : new Task[] {
            caseDataToDivorceFormatter,
            setCaseIdAndStateOnSession
        };
        return this.execute(
            taskPending,
            getContext(),
            caseData
        );
    }

    private boolean updatePaymentEvent(Map<String, Object> caseData) throws WorkflowException {

        return Objects.nonNull(
            this.execute(
                new Task[] {
                    getPaymentOnSession,
                    formatDivorceSessionToCaseData,
                    paymentMadeEvent
                },
                getContext(),
                caseData
            ));
    }
}
