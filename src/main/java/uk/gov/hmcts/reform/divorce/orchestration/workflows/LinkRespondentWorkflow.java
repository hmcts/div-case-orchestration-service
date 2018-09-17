package uk.gov.hmcts.reform.divorce.orchestration.workflows;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.idam.UserDetails;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.DefaultWorkflow;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.WorkflowException;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.Task;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.LinkRespondent;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.RetrievePinUserDetails;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.UpdateRespondentDetails;

import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.AUTH_TOKEN_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.PIN;

@Component
public class LinkRespondentWorkflow extends DefaultWorkflow<UserDetails> {
    private final RetrievePinUserDetails retrievePinUserDetails;
    private final LinkRespondent linkRespondent;
    private final UpdateRespondentDetails updateRespondentDetails;

    @Autowired
    public LinkRespondentWorkflow(RetrievePinUserDetails retrievePinUserDetails,
                                  LinkRespondent linkRespondent,
                                  UpdateRespondentDetails updateRespondentDetails) {
        this.retrievePinUserDetails = retrievePinUserDetails;
        this.linkRespondent = linkRespondent;
        this.updateRespondentDetails = updateRespondentDetails;
    }

    public UserDetails run(String authToken, String caseId, String pin) throws WorkflowException {
        return this.execute(
            new Task[] {
                retrievePinUserDetails,
                linkRespondent,
                updateRespondentDetails
            },
            UserDetails.builder().authToken(authToken).build(),
            ImmutablePair.of(PIN, pin),
            ImmutablePair.of(AUTH_TOKEN_JSON_KEY, authToken),
            ImmutablePair.of(OrchestrationConstants.CASE_ID_JSON_KEY, caseId)
        );
    }
}
