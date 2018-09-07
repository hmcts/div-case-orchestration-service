package uk.gov.hmcts.reform.divorce.orchestration.workflows;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.DefaultWorkflow;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.WorkflowException;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.Task;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.FormatDivorceSessionToCaseData;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.UpdateCaseInCCD;

import java.util.Map;

import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.AUTH_TOKEN_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CASE_EVENT_DATA_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CASE_EVENT_ID_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CASE_ID_JSON_KEY;

@Component
public class UpdateToCCDWorkflow extends DefaultWorkflow<Map<String, Object>> {

    @Autowired
    private FormatDivorceSessionToCaseData formatDivorceSessionToCaseData;

    @Autowired
    private UpdateCaseInCCD updateCaseInCCD;

    public Map<String, Object> run(Map<String, Object> divorceEvent,
                                   String authToken,
                                   String caseId) throws WorkflowException {

        Map<String, Object> payload = (Map<String, Object>) divorceEvent.get(CASE_EVENT_DATA_JSON_KEY);
        String eventId = divorceEvent.get(CASE_EVENT_ID_JSON_KEY).toString();

        return this.execute(new Task[] {
            formatDivorceSessionToCaseData,
            updateCaseInCCD
        }, payload,
            new ImmutablePair<>(AUTH_TOKEN_JSON_KEY, authToken),
            new ImmutablePair<>(CASE_ID_JSON_KEY, caseId),
            new ImmutablePair<>(CASE_EVENT_ID_JSON_KEY, eventId)
        );
    }
}
