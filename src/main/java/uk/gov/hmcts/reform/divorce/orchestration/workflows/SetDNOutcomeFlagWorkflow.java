package uk.gov.hmcts.reform.divorce.orchestration.workflows;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CaseDetails;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.DefaultWorkflow;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.WorkflowException;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.Task;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.AddDnOutcomeFlagFieldTask;

import java.util.Map;

@Component
@RequiredArgsConstructor
public class SetDNOutcomeFlagWorkflow extends DefaultWorkflow<Map<String, Object>> {

    @Autowired
    private final AddDnOutcomeFlagFieldTask addDnOutcomeFlagFieldTask;

    public Map<String, Object> run(CaseDetails caseDetails) throws WorkflowException {
        Map<String, Object> payload = caseDetails.getCaseData();

        return this.execute(new Task[] {
            addDnOutcomeFlagFieldTask,
        }, payload);
    }
}