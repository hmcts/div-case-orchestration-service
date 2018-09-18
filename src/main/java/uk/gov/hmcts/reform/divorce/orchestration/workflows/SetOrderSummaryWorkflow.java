package uk.gov.hmcts.reform.divorce.orchestration.workflows;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.DefaultWorkflow;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.WorkflowException;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.Task;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.GetPetitionIssueFee;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.SetOrderSummary;

import java.util.Map;

@Component
public class SetOrderSummaryWorkflow extends DefaultWorkflow<Map<String, Object>> {

    private final GetPetitionIssueFee getPetitionIssueFee;
    private final SetOrderSummary setOrderSummary;

    @Autowired
    public SetOrderSummaryWorkflow(GetPetitionIssueFee getPetitionIssueFee,
                                   SetOrderSummary setOrderSummary) {
        this.getPetitionIssueFee = getPetitionIssueFee;
        this.setOrderSummary = setOrderSummary;
    }

    public Map<String, Object> run(Map<String, Object> payload) throws WorkflowException {

        return this.execute(new Task[] {
            getPetitionIssueFee,
            setOrderSummary
        }, payload);
    }
}
