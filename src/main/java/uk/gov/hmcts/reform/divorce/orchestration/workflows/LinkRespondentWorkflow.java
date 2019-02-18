package uk.gov.hmcts.reform.divorce.orchestration.workflows;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.idam.UserDetails;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.DefaultWorkflow;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.WorkflowException;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.Task;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.LinkRespondent;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.RetrievePinUserDetails;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.UnlinkRespondent;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.UpdateRespondentDetails;

import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.AUTH_TOKEN_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CASE_ID_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.IS_CO_RESPONDENT;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.PIN;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.UPDATE_REPONDENT_DATA_ERROR_KEY;

@Component
@Slf4j
public class LinkRespondentWorkflow extends DefaultWorkflow<UserDetails> {
    private final RetrievePinUserDetails retrievePinUserDetails;
    private final LinkRespondent linkRespondent;
    private final UpdateRespondentDetails updateRespondentDetails;
    private final UnlinkRespondent unlinkRespondent;

    @Autowired
    public LinkRespondentWorkflow(RetrievePinUserDetails retrievePinUserDetails,
                                  LinkRespondent linkRespondent,
                                  UpdateRespondentDetails updateRespondentDetails,
                                  UnlinkRespondent unlinkRespondent) {
        this.retrievePinUserDetails = retrievePinUserDetails;
        this.linkRespondent = linkRespondent;
        this.updateRespondentDetails = updateRespondentDetails;
        this.unlinkRespondent = unlinkRespondent;
    }

    public UserDetails run(String authToken, String caseId, String pin, boolean isCorespondent) throws WorkflowException {
        final UserDetails userDetail = UserDetails.builder().authToken(authToken).build();

        try {
            return this.execute(
                new Task[]{
                    retrievePinUserDetails,
                    linkRespondent,
                    updateRespondentDetails
                },
                userDetail,
                ImmutablePair.of(PIN, pin),
                ImmutablePair.of(AUTH_TOKEN_JSON_KEY, authToken),
                ImmutablePair.of(CASE_ID_JSON_KEY, caseId),
                ImmutablePair.of(IS_CO_RESPONDENT, isCorespondent)
            );
        } catch (WorkflowException e) {
            if (this.errors().containsKey(UPDATE_REPONDENT_DATA_ERROR_KEY)) {
                rollbackOperation(userDetail, caseId);
            }
            throw e;
        }
    }

    private void rollbackOperation(UserDetails userDetail, String caseId) throws WorkflowException {
        log.error("Cannot link respondent for caseId {} and user {}", caseId, userDetail.getId());
        this.execute(
            new Task[]{
                unlinkRespondent
            },
            userDetail,
            ImmutablePair.of(AUTH_TOKEN_JSON_KEY, userDetail.getAuthToken()),
            ImmutablePair.of(CASE_ID_JSON_KEY, caseId)
        );
    }
}
