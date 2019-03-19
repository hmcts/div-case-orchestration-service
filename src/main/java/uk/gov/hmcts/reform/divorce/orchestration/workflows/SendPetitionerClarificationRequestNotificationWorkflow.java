package uk.gov.hmcts.reform.divorce.orchestration.workflows;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CaseDetails;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CcdCallbackRequest;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.DefaultWorkflow;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.WorkflowException;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.Task;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.SendPetitionerClarificationRequestEmail;

import java.util.Map;

import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CASE_ID_JSON_KEY;

@Component
public class SendPetitionerClarificationRequestNotificationWorkflow extends DefaultWorkflow<Map<String, Object>> {

    private final SendPetitionerClarificationRequestEmail sendPetitionerClarificationRequestEmail;

    @Autowired
    public SendPetitionerClarificationRequestNotificationWorkflow(final SendPetitionerClarificationRequestEmail petitionerClarificationRequestEmail) {
        this.sendPetitionerClarificationRequestEmail = petitionerClarificationRequestEmail;
    }

    public Map<String, Object> run(final CcdCallbackRequest ccdCallbackRequest) throws WorkflowException {
        final CaseDetails caseDetails = ccdCallbackRequest.getCaseDetails();

        return execute(new Task[] {sendPetitionerClarificationRequestEmail}, caseDetails.getCaseData(),
            ImmutablePair.of(CASE_ID_JSON_KEY, caseDetails.getCaseId()));
    }
}
