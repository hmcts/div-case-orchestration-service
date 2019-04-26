package uk.gov.hmcts.reform.divorce.orchestration.workflows;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CaseDetails;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.DefaultWorkflow;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.WorkflowException;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.Task;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.PopulateDivorceCostOptions;

import java.util.Map;

@Component
public class SolicitorDnCostsWorkflow extends DefaultWorkflow<Map<String, Object>> {

    private final PopulateDivorceCostOptions populateDivorceCostOptions;

    @Autowired
    public SolicitorDnCostsWorkflow(PopulateDivorceCostOptions populateDivorceCostOptions) {
        this.populateDivorceCostOptions = populateDivorceCostOptions;
    }

    public Map<String, Object> run(CaseDetails caseDetails) throws WorkflowException {
        return this.execute(new Task[]{
            populateDivorceCostOptions
        }, caseDetails.getCaseData());
    }
}
