package uk.gov.hmcts.reform.divorce.orchestration.workflows;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CaseDetails;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.idam.UserDetails;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.DefaultWorkflow;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.WorkflowException;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.Task;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.GetCaseWithId;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.LinkRespondent;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.RetrievePinUserDetails;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.UnlinkRespondent;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.UpdateRespondentDetails;

import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.AUTH_TOKEN_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CASE_ID_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.RESPONDENT_PIN;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.UPDATE_RESPONDENT_DATA_ERROR_KEY;

@Component
@Slf4j
public class RespondentSolicitorLinkCaseWorkflow extends DefaultWorkflow<UserDetails> {

    private static final String RESPONDENT_SOLICITOR_PIN = "RespondentSolicitorPin";
    public static final String RESPONDENT_SOLICITOR_CASE_NO = "RespondentSolicitorCaseNo";

    private final GetCaseWithId getCaseWithId;
    private final RetrievePinUserDetails retrievePinUserDetails;
    private final LinkRespondent linkRespondent;
    private final UpdateRespondentDetails updateRespondentDetails;
    private final UnlinkRespondent unlinkRespondent;

    @Autowired
    public RespondentSolicitorLinkCaseWorkflow(RetrievePinUserDetails retrievePinUserDetails,
                                  LinkRespondent linkRespondent,
                                  UpdateRespondentDetails updateRespondentDetails,
                                  UnlinkRespondent unlinkRespondent,
                                  GetCaseWithId getCaseWithId) {
        this.retrievePinUserDetails = retrievePinUserDetails;
        this.linkRespondent = linkRespondent;
        this.updateRespondentDetails = updateRespondentDetails;
        this.unlinkRespondent = unlinkRespondent;
        this.getCaseWithId = getCaseWithId;
    }

    public UserDetails run(String authToken, CaseDetails caseDetails) throws WorkflowException {
        final UserDetails userDetails = UserDetails.builder().authToken(authToken).build();
        final String caseId = (String) caseDetails.getCaseData().get(RESPONDENT_SOLICITOR_CASE_NO);
        final String pin = (String) caseDetails.getCaseData().get(RESPONDENT_SOLICITOR_PIN);

        try {
            return this.execute(
                    new Task[] {
                            getCaseWithId,
                            retrievePinUserDetails,
                            linkRespondent,
                            updateRespondentDetails
                    },
                    userDetails,
                    ImmutablePair.of(RESPONDENT_PIN, pin),
                    ImmutablePair.of(AUTH_TOKEN_JSON_KEY, authToken),
                    ImmutablePair.of(CASE_ID_JSON_KEY, caseId)
            );
        } catch (WorkflowException e) {
            if (this.errors().containsKey(UPDATE_RESPONDENT_DATA_ERROR_KEY)) {
                rollbackOperation(userDetails, caseId);
            }
            throw e;
        }
    }

    private void rollbackOperation(UserDetails userDetail, String caseId) throws WorkflowException {
        log.error("Cannot link respondent's solicitor for caseId {} and user {}", caseId, userDetail.getId());
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
