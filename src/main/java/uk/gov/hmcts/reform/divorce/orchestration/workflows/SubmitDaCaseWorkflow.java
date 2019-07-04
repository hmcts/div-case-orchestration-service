package uk.gov.hmcts.reform.divorce.orchestration.workflows;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.DefaultWorkflow;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.WorkflowException;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.Task;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.FormatDivorceSessionToDaCaseData;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.UpdateCaseInCCD;

import java.util.Map;

import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.AUTH_TOKEN_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CASE_EVENT_ID_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CASE_ID_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DECREE_ABSOLUTE_REQUESTED_EVENT_ID;

@Component
public class SubmitDaCaseWorkflow extends DefaultWorkflow<Map<String, Object>> {

    @Autowired
    private FormatDivorceSessionToDaCaseData formatDivorceSessionToDaCaseData;

    @Autowired
    private UpdateCaseInCCD updateCaseInCCD;

    public Map<String, Object> run(Map<String, Object> payload,
                                   String authToken,
                                   String caseId) throws WorkflowException {
        return this.execute(
            new Task[] {
                formatDivorceSessionToDaCaseData,
                updateCaseInCCD
            },
            payload,
            ImmutablePair.of(AUTH_TOKEN_JSON_KEY, authToken),
            ImmutablePair.of(CASE_ID_JSON_KEY, caseId),
            ImmutablePair.of(CASE_EVENT_ID_JSON_KEY, DECREE_ABSOLUTE_REQUESTED_EVENT_ID)
        );
    }
}
