package uk.gov.hmcts.reform.divorce.orchestration.workflows;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.DefaultWorkflow;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.WorkflowException;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.Task;
import uk.gov.hmcts.reform.divorce.orchestration.service.FeatureToggleService;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.GetAmendPetitionFeeTask;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.GetPetitionIssueFeeTask;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.SetOrderSummary;

import java.util.Map;

import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.Features.SOLICITOR_DN_REJECT_AND_AMEND;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.PREVIOUS_CASE_ID_CCD_KEY;

@Component
public class SetOrderSummaryWorkflow extends DefaultWorkflow<Map<String, Object>> {

    private final Task<?>[] issueTasks;
    private final Task<?>[] amendmentTasks;

    private final FeatureToggleService featureToggleService;

    @Autowired
    public SetOrderSummaryWorkflow(GetPetitionIssueFeeTask getPetitionIssueFeeTask,
                                   GetAmendPetitionFeeTask getAmendPetitionFeeTask,
                                   SetOrderSummary setOrderSummary,
                                   FeatureToggleService featureToggleService) {
        issueTasks = new Task<?>[] {getPetitionIssueFeeTask, setOrderSummary};
        amendmentTasks = new Task<?>[] {getAmendPetitionFeeTask, setOrderSummary};
        this.featureToggleService = featureToggleService;
    }

    public Map<String, Object> run(Map<String, Object> payload) throws WorkflowException {
        Task<?>[] tasks;

        if (featureToggleService.isFeatureEnabled(SOLICITOR_DN_REJECT_AND_AMEND) && payload.containsKey(PREVIOUS_CASE_ID_CCD_KEY)) {
            tasks = amendmentTasks;
        } else {
            tasks = issueTasks;
        }

        return this.execute(tasks, payload);
    }

}