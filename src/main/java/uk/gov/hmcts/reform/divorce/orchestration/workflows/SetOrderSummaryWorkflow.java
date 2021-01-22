package uk.gov.hmcts.reform.divorce.orchestration.workflows;

import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CaseDetails;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.StandardisedWorkflow;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.Task;
import uk.gov.hmcts.reform.divorce.orchestration.service.FeatureToggleService;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.GetAmendPetitionFeeTask;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.GetPetitionIssueFeeTask;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.SetOrderSummary;

import java.util.Map;

import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.Features.SOLICITOR_DN_REJECT_AND_AMEND;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.PREVIOUS_CASE_ID_CCD_KEY;

@Component
public class SetOrderSummaryWorkflow extends StandardisedWorkflow {

    private final Task<Map<String, Object>>[] issueTasks;
    private final Task<Map<String, Object>>[] amendmentTasks;

    private final FeatureToggleService featureToggleService;

    @Autowired
    public SetOrderSummaryWorkflow(GetPetitionIssueFeeTask getPetitionIssueFeeTask,
                                   GetAmendPetitionFeeTask getAmendPetitionFeeTask,
                                   SetOrderSummary setOrderSummary,
                                   FeatureToggleService featureToggleService) {
        issueTasks = new Task[] {getPetitionIssueFeeTask, setOrderSummary};
        amendmentTasks = new Task[] {getAmendPetitionFeeTask, setOrderSummary};
        this.featureToggleService = featureToggleService;
    }

    @Override
    protected Task<Map<String, Object>>[] getTasksToExecute(CaseDetails caseDetails) {
        Task<Map<String, Object>>[] tasks;

        Map<String, Object> caseData = caseDetails.getCaseData();
        if (featureToggleService.isFeatureEnabled(SOLICITOR_DN_REJECT_AND_AMEND) && caseData.containsKey(PREVIOUS_CASE_ID_CCD_KEY)) {
            tasks = amendmentTasks;
        } else {
            tasks = issueTasks;
        }

        return tasks;
    }

    @Override
    protected Pair<String, Object>[] prepareContextVariables(CaseDetails caseDetails, String authToken) {
        return new Pair[0];
    }

}