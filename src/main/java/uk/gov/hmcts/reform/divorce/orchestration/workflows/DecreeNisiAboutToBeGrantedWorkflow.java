package uk.gov.hmcts.reform.divorce.orchestration.workflows;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CaseDetails;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.DefaultWorkflow;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.WorkflowException;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.Task;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.AddDecreeNisiApprovalDateTask;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.DefineWhoPaysCostsOrderTask;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.AWAITING_CLARIFICATION;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.AWAITING_PRONOUNCEMENT;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DECREE_NISI_GRANTED_CCD_FIELD;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DIVORCE_COSTS_CLAIM_GRANTED_CCD_FIELD;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.STATE_CCD_FIELD;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.YES_VALUE;

@Component
public class DecreeNisiAboutToBeGrantedWorkflow extends DefaultWorkflow<Map<String, Object>> {

    @Autowired
    private AddDecreeNisiApprovalDateTask addDecreeNisiApprovalDateTask;

    @Autowired
    private DefineWhoPaysCostsOrderTask defineWhoPaysCostsOrderTask;

    public Map<String, Object> run(CaseDetails caseDetails) throws WorkflowException {
        List<Task> tasksToRun = new ArrayList<>();

        String newCaseEndState = AWAITING_CLARIFICATION;
        Map<String, Object> caseData = caseDetails.getCaseData();
        Object decreeNisiGranted = caseData.get(DECREE_NISI_GRANTED_CCD_FIELD);
        if (YES_VALUE.equals(decreeNisiGranted)) {
            newCaseEndState = AWAITING_PRONOUNCEMENT;
            tasksToRun.add(addDecreeNisiApprovalDateTask);

            Object costsClaimGranted = caseData.get(DIVORCE_COSTS_CLAIM_GRANTED_CCD_FIELD);
            if (YES_VALUE.equals(costsClaimGranted)) {
                tasksToRun.add(defineWhoPaysCostsOrderTask);
            }
        }

        Map<String, Object> payloadToReturn = this.execute(tasksToRun.stream().toArray(Task[]::new), caseData);
        payloadToReturn.put(STATE_CCD_FIELD, newCaseEndState);

        return payloadToReturn;
    }

}