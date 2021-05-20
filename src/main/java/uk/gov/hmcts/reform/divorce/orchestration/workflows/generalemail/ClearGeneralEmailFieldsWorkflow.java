package uk.gov.hmcts.reform.divorce.orchestration.workflows.generalemail;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CaseDetails;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.DefaultWorkflow;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.WorkflowException;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.Task;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.generalemail.ClearGeneralEmailFieldsTask;

import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class ClearGeneralEmailFieldsWorkflow extends DefaultWorkflow<Map<String, Object>> {

    private final ClearGeneralEmailFieldsTask clearGeneralEmailFieldsTask;

    public Map<String, Object> run(CaseDetails caseDetails) throws WorkflowException {
        String caseId = caseDetails.getCaseId();
        Map<String, Object> caseData = caseDetails.getCaseData();

        log.info("CaseID: {} ClearGeneralEmailFieldsWorkflow workflow is going to be executed.", caseId);

        return this.execute(
            new Task[] { clearGeneralEmailFieldsTask },
            caseData
        );
    }
}
