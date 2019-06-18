package uk.gov.hmcts.reform.divorce.orchestration.workflows;

import feign.FeignException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CaseDetails;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.idam.UserDetails;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.DefaultWorkflow;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.WorkflowException;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.Task;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.GetCaseWithId;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.LinkRespondent;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.RetrievePinUserDetails;

import java.util.Map;

import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.AUTH_TOKEN_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CASE_ID_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.RESPONDENT_PIN;

@Component
@Slf4j
public class RespondentSolicitorLinkCaseWorkflow extends DefaultWorkflow<UserDetails> {

    private static final String RESPONDENT_SOLICITOR_PIN = "RespondentSolicitorPin";
    private static final String RESPONDENT_SOLICITOR_CASE_NO = "RespondentSolicitorCaseNo";
    private static final String CASE_REFERENCE = "CaseReference";

    private final GetCaseWithId getCaseWithId;
    private final RetrievePinUserDetails retrievePinUserDetails;
    private final LinkRespondent linkRespondent;

    @Autowired
    public RespondentSolicitorLinkCaseWorkflow(GetCaseWithId getCaseWithId,
                                               RetrievePinUserDetails retrievePinUserDetails,
                                               LinkRespondent linkRespondent) {
        this.getCaseWithId = getCaseWithId;
        this.retrievePinUserDetails = retrievePinUserDetails;
        this.linkRespondent = linkRespondent;
    }

    public UserDetails run(CaseDetails caseDetails, String authToken) throws WorkflowException {
        final UserDetails userDetails = UserDetails.builder().authToken(authToken).build();
        final Map<String, String> respondentSolicitorCaseLink = (Map<String, String>) caseDetails.getCaseData().get(RESPONDENT_SOLICITOR_CASE_NO);
        final String caseId = respondentSolicitorCaseLink.get(CASE_REFERENCE);
        final String pin = (String) caseDetails.getCaseData().get(RESPONDENT_SOLICITOR_PIN);

        try {
            return this.execute(
                new Task[] {
                    getCaseWithId,
                    retrievePinUserDetails,
                    linkRespondent
                },
                userDetails,
                ImmutablePair.of(RESPONDENT_PIN, pin),
                ImmutablePair.of(AUTH_TOKEN_JSON_KEY, authToken),
                ImmutablePair.of(CASE_ID_JSON_KEY, caseId)
            );
        } catch (FeignException e) {
            if (e.status() == HttpStatus.NOT_FOUND.value()) {
                throw new WorkflowException("Case not found - please check the case ID", e);
            } else if (e.status() == HttpStatus.UNAUTHORIZED.value()) {
                throw new WorkflowException("Authentication error - please check the case ID/PIN code", e);
            }
            throw e;
        }
    }
}
