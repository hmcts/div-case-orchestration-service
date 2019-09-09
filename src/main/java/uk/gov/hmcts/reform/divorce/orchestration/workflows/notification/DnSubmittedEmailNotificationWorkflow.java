package uk.gov.hmcts.reform.divorce.orchestration.workflows.notification;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.DefaultWorkflow;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.WorkflowException;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.Task;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.DnSubmittedEmailNotificationTask;

import java.util.Map;

import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CASE_ID_JSON_KEY;

@Component
public class DnSubmittedEmailNotificationWorkflow extends DefaultWorkflow<Map<String, Object>> {

    @Autowired
    private DnSubmittedEmailNotificationTask dnSubmittedEmailNotificationTask;

    public Map<String, Object> run(String caseId, Map<String, Object> caseData) throws WorkflowException {
        return this.execute(
            new Task[] { dnSubmittedEmailNotificationTask },
            caseData,
            ImmutablePair.of(CASE_ID_JSON_KEY, caseId)
        );
    }
}