package uk.gov.hmcts.reform.divorce.orchestration.workflows;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CaseDetails;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.DefaultWorkflow;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.WorkflowException;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.Task;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.SetDnPronouncementDetailsTask;

import java.util.Map;

@Slf4j
@Component
public class SetDNGrantedDateWorkflow extends DefaultWorkflow<Map<String, Object>> {

    @Autowired
    private SetDnPronouncementDetailsTask setDnPronouncementDetailsTask;

    public Map<String, Object> run(CaseDetails caseDetails) throws WorkflowException {
        log.info("Setting DN Pronouncement details for case with ID: {}", caseDetails.getCaseId());
        return execute(
            new Task[] {
                setDnPronouncementDetailsTask
            },
            caseDetails.getCaseData()
        );
    }
}