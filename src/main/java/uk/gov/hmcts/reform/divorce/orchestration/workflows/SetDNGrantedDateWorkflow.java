package uk.gov.hmcts.reform.divorce.orchestration.workflows;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.DefaultWorkflow;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.WorkflowException;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.Task;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.SetDnPronouncementDetailsTask;

import java.util.Map;


@Component
public class SetDNGrantedDateWorkflow extends DefaultWorkflow<Map<String, Object>> {

    @Autowired
    private SetDnPronouncementDetailsTask setDnPronouncementDetailsTask;

    public Map<String, Object> run(Map<String, Object> payload) throws WorkflowException {
        return execute(
            new Task[] {
                setDnPronouncementDetailsTask
            },
            payload
        );
    }
}