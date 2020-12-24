package uk.gov.hmcts.reform.divorce.orchestration.workflows;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.DefaultWorkflow;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.WorkflowException;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.Task;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.GetCaseWithIdTask;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.LinkRespondentTask;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.RetrievePinUserDetails;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.UnlinkRespondent;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.UpdateRespondentDetails;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;

import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.AUTH_TOKEN_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CASE_ID_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.RESPONDENT_PIN;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.UPDATE_RESPONDENT_DATA_ERROR_KEY;

@Component
@Slf4j
@RequiredArgsConstructor
public class LinkRespondentWorkflow extends DefaultWorkflow<UserDetails> {
    private final GetCaseWithIdTask getCaseWithId;
    private final RetrievePinUserDetails retrievePinUserDetails;
    private final LinkRespondentTask linkRespondentTask;
    private final UpdateRespondentDetails updateRespondentDetails;
    private final UnlinkRespondent unlinkRespondent;

    public UserDetails run(String authToken, String caseId, String pin) throws WorkflowException {
        final UserDetails userDetail = UserDetails.builder().build();

        try {
            return this.execute(
                new Task[] {
                    getCaseWithId,
                    retrievePinUserDetails,
                    linkRespondentTask,
                    updateRespondentDetails
                },
                userDetail,
                ImmutablePair.of(RESPONDENT_PIN, pin),
                ImmutablePair.of(AUTH_TOKEN_JSON_KEY, authToken),
                ImmutablePair.of(CASE_ID_JSON_KEY, caseId)
            );
        } catch (WorkflowException e) {
            if (this.errors().containsKey(UPDATE_RESPONDENT_DATA_ERROR_KEY)) {
                rollbackOperation(userDetail, caseId, authToken);
            }
            throw e;
        }
    }

    private void rollbackOperation(UserDetails userDetail, String caseId, String authToken) throws WorkflowException {
        log.error("Cannot link respondent for caseId {} and user {}", caseId, userDetail.getId());
        this.execute(
            new Task[] {
                unlinkRespondent
            },
            userDetail,
            ImmutablePair.of(AUTH_TOKEN_JSON_KEY, authToken),
            ImmutablePair.of(CASE_ID_JSON_KEY, caseId)
        );
    }
}
