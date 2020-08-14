package uk.gov.hmcts.reform.divorce.orchestration.workflows.servicejourney;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.CcdStates;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CaseDetails;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.DefaultWorkflow;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.WorkflowException;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.Task;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.servicejourney.ServiceRefusalOrderTask;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.AUTH_TOKEN_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CASE_ID_JSON_KEY;

@Component
@Slf4j
@RequiredArgsConstructor
public class ServiceRefusalOrderWorkflow extends DefaultWorkflow<Map<String, Object>> {

    public static final String SERVICE_DECISION = "serviceDecision";
    public static final String CASE_STATE = "caseState";

    private final ServiceRefusalOrderTask serviceRefusalOrderTask;

    public Map<String, Object> run(CaseDetails caseDetails, String decision, String authorisation) throws WorkflowException {

        String caseId = caseDetails.getCaseId();
        String awaitingServiceConsideration = caseDetails.getState();

        log.info("CaseID: {} Service decision made. ServiceDecisionMade workflow is going to be executed.", caseId);

        return this.execute(
            getTasks(awaitingServiceConsideration),
            caseDetails.getCaseData(),
            ImmutablePair.of(AUTH_TOKEN_JSON_KEY, authorisation),
            ImmutablePair.of(SERVICE_DECISION, decision),
            ImmutablePair.of(CASE_ID_JSON_KEY, caseId)
        );
    }

    private Task[] getTasks(String caseState) {
        List<Task> tasks = new ArrayList<>();

        if(isAwaitingServiceConsideration(caseState)){
            tasks.add(serviceRefusalOrderTask);
        }

        return tasks.toArray(new Task[] {});
    }

    private boolean isAwaitingServiceConsideration(String currentState) {
        String expectedState = CcdStates.AWAITING_SERVICE_CONSIDERATION;
        return expectedState.equals(currentState);
    }
}
