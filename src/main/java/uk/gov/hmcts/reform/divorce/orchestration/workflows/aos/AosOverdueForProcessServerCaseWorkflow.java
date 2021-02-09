package uk.gov.hmcts.reform.divorce.orchestration.workflows.aos;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.DefaultWorkflow;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.WorkflowException;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.Task;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.UpdateCaseInCCD;

import java.util.Map;

import static java.util.Collections.emptyMap;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.CcdEvents.AOS_NOT_RECEIVED_FOR_PROCESS_SERVER_EVENT_ID;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.AUTH_TOKEN_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CASE_EVENT_ID_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CASE_ID_JSON_KEY;

@Slf4j
@Component
@RequiredArgsConstructor
public class AosOverdueForProcessServerCaseWorkflow extends DefaultWorkflow<Map<String, Object>> {

    private final UpdateCaseInCCD updateCaseInCCD;

    public void run(String authToken, String caseId) throws WorkflowException {
        execute(new Task[] {updateCaseInCCD},
            emptyMap(),
            ImmutablePair.of(AUTH_TOKEN_JSON_KEY, authToken),
            ImmutablePair.of(CASE_ID_JSON_KEY, caseId),
            ImmutablePair.of(CASE_EVENT_ID_JSON_KEY, AOS_NOT_RECEIVED_FOR_PROCESS_SERVER_EVENT_ID)
        );
        log.info("Executed workflow tasks for case id {}", caseId);
    }

}