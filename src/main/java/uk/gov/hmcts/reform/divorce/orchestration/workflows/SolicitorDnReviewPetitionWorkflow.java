package uk.gov.hmcts.reform.divorce.orchestration.workflows;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CaseDetails;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.DefaultWorkflow;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.WorkflowException;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.Task;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.PopulateMiniPetitionUrl;

import java.util.Map;

@Component
public class SolicitorDnReviewPetitionWorkflow extends DefaultWorkflow<Map<String, Object>> {

    private final PopulateMiniPetitionUrl populateMiniPetitionUrl;

    @Autowired
    public SolicitorDnReviewPetitionWorkflow(PopulateMiniPetitionUrl populateMiniPetitionUrl) {
        this.populateMiniPetitionUrl = populateMiniPetitionUrl;
    }

    public Map<String, Object> run(CaseDetails caseDetails) throws WorkflowException {
        return this.execute(new Task[] {
            populateMiniPetitionUrl
        }, caseDetails.getCaseData());
    }
}
