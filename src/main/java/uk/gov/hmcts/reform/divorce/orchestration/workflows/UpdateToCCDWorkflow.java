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

@Component
@Scope("prototype")
public class UpdateToCCDWorkflow extends DefaultWorkflow<Map<String, Object>> {

    private static final String AUTH_TOKEN_KEY = "authToken";
    private static final String CASE_ID_KEY = "caseId";
    private static final String EVENT_ID_KEY = "eventId";

    @Autowired
    private FormatDivorceSessionToCaseData formatDivorceSessionToCaseData;

    @Autowired
    private UpdateCaseInCCD updateCaseInCCD;

    public Map<String, Object> run(Map<String, Object> payload,
                                   String authToken,
                                   String caseId,
                                   String eventId) throws WorkflowException {

        formatDivorceSessionToCaseData.setup(authToken);

        return this.execute(new Task[] {
            formatDivorceSessionToCaseData,
            updateCaseInCCD
        }, payload,
            new ImmutablePair<>(AUTH_TOKEN_KEY, authToken),
            new ImmutablePair<>(CASE_ID_KEY, caseId),
            new ImmutablePair<>(EVENT_ID_KEY, eventId)
        );
    }
}
