package uk.gov.hmcts.reform.divorce.orchestration.workflows;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.DefaultWorkflow;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.WorkflowException;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.Task;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.FormatDivorceSessionToCaseDataTask;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.PopulateExistingCollections;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.UpdateCaseInCCD;

import java.util.Map;

import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.AUTH_TOKEN_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CASE_EVENT_DATA_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CASE_EVENT_ID_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CASE_ID_JSON_KEY;

@Component
public class UpdateToCCDWorkflow extends DefaultWorkflow<Map<String, Object>> {

    @Autowired
    private PopulateExistingCollections populateExistingCollections;

    @Autowired
    private FormatDivorceSessionToCaseDataTask formatDivorceSessionToCaseDataTask;

    @Autowired
    private UpdateCaseInCCD updateCaseInCCD;

    @SuppressWarnings("unchecked")
    public Map<String, Object> run(Map<String, Object> divorceEvent,
                                   String authToken,
                                   String caseId) throws WorkflowException {

        Map<String, Object> payload = (Map<String, Object>) divorceEvent.get(CASE_EVENT_DATA_JSON_KEY);
        String eventId = divorceEvent.get(CASE_EVENT_ID_JSON_KEY).toString();

        return this.execute(
            new Task[] {
                populateExistingCollections,
                formatDivorceSessionToCaseDataTask,
                updateCaseInCCD
            },
            payload,
            ImmutablePair.of(AUTH_TOKEN_JSON_KEY, authToken),
            ImmutablePair.of(CASE_ID_JSON_KEY, caseId),
            ImmutablePair.of(CASE_EVENT_ID_JSON_KEY, eventId)
        );
    }
}
