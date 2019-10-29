package uk.gov.hmcts.reform.divorce.orchestration.workflows;

import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.DefaultWorkflow;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.WorkflowException;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.DefaultTaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.Task;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.AddCourtsToPayloadTask;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.CaseDataDraftToDivorceFormatter;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.FormatDivorceSessionToCaseData;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.GetInconsistentPaymentInfo;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.RetrieveDraft;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.SetCaseIdAndStateOnSession;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.UpdatePaymentMadeCase;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.AUTH_TOKEN_JSON_KEY;

@Component
@RequiredArgsConstructor
public class RetrieveDraftWorkflow extends DefaultWorkflow<Map<String, Object>> {

    private final RetrieveDraft retrieveDraft;
    private final CaseDataDraftToDivorceFormatter caseDataToDivorceFormatter;
    private final SetCaseIdAndStateOnSession setCaseIdAndStateOnSession;
    private final AddCourtsToPayloadTask addCourtsToPayloadTask;

    private final GetInconsistentPaymentInfo getPaymentOnSession;
    private final UpdatePaymentMadeCase paymentMadeEvent;
    private final FormatDivorceSessionToCaseData formatDivorceSessionToCaseData;

    public Map<String, Object> run(String authToken) throws WorkflowException {
        Map<String, Object> caseData = this.execute(
            new Task[] {
                retrieveDraft
            },
            new HashMap<>(),
            ImmutablePair.of(AUTH_TOKEN_JSON_KEY, authToken)
        );
        DefaultTaskContext mainContext = getContext();
        boolean paymentDataUpdated = updatePaymentEvent(caseData);

        List<Task> pendingTasks = getPendingTasks(paymentDataUpdated);
        return this.execute(
            pendingTasks.toArray(new Task[0]),
            mainContext,
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
                new DefaultTaskContext(getContext()),
                caseData
            ));
    }

    private List<Task> getPendingTasks(boolean paymentDataUpdated) {
        List<Task> pendingTasks = new ArrayList<>();
        if (paymentDataUpdated) {
            pendingTasks.add(retrieveDraft);
        }
        pendingTasks.add(caseDataToDivorceFormatter);
        pendingTasks.add(setCaseIdAndStateOnSession);
        pendingTasks.add(addCourtsToPayloadTask);
        return pendingTasks;
    }

}