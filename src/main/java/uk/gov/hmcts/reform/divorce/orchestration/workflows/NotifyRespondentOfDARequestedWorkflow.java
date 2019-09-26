package uk.gov.hmcts.reform.divorce.orchestration.workflows;

import org.apache.commons.lang3.tuple.ImmutablePair;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CaseDetails;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CcdCallbackRequest;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.DefaultWorkflow;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.WorkflowException;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.Task;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.SendDaRequestedNotifyRespondentEmail;

import java.util.Map;

import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.APPLY_FOR_DA;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CASE_DETAILS_JSON_KEY;

@Component
public class NotifyRespondentOfDARequestedWorkflow extends DefaultWorkflow<Map<String, Object>> {

    @Autowired
    private SendDaRequestedNotifyRespondentEmail sendDaRequestedNotifyRespondentEmail;

    public Map<String, Object> run(final CcdCallbackRequest ccdCallbackRequest) throws WorkflowException {
        CaseDetails caseDetails = ccdCallbackRequest.getCaseDetails();

        validateDaIsRequested(caseDetails);

        return this.execute(
            new Task[] {sendDaRequestedNotifyRespondentEmail},
            ccdCallbackRequest.getCaseDetails().getCaseData(),
            ImmutablePair.of(CASE_DETAILS_JSON_KEY, caseDetails)
        );
    }

    private void validateDaIsRequested(CaseDetails caseDetails) throws WorkflowException {
        String applyForDecreeAbsolute = (String) caseDetails.getCaseData().get(APPLY_FOR_DA);

        if (applyForDecreeAbsolute == null || !applyForDecreeAbsolute.equalsIgnoreCase("yes")) {
            throw new WorkflowException("You must select 'Yes' to apply for Decree Absolute");
        }
    }
}
