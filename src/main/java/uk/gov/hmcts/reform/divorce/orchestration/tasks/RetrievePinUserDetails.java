package uk.gov.hmcts.reform.divorce.orchestration.tasks;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import uk.gov.hmcts.reform.idam.client.IdamClient;
import uk.gov.hmcts.reform.idam.client.models.ExchangeCodeRequest;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CaseDetails;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.exception.AuthenticationError;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.Task;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskException;
import uk.gov.hmcts.reform.divorce.orchestration.util.AuthUtil;

import java.util.Map;

import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.GRANT_TYPE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CASE_DETAILS_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CASE_ID_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CO_RESPONDENT_LETTER_HOLDER_ID;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.IS_RESPONDENT;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.RESPONDENT_LETTER_HOLDER_ID;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.RESPONDENT_PIN;

@Slf4j
public abstract class RetrievePinUserDetails implements Task<UserDetails> {
    @Value("${auth2.client.id}")
    private String authClientId;

    @Value("${auth2.client.secret}")
    private String authClientSecret;

    @Value("${idam.api.redirect-url}")
    private String authRedirectUrl;

    @Autowired
    private AuthUtil authUtil;

    @Autowired
    private IdamClient idamClient;

    @Override
    public UserDetails execute(TaskContext context, UserDetails payLoad) throws TaskException {
        String pinCode = authenticatePinUser(
            context.getTransientObject(RESPONDENT_PIN),
            authClientId,
            authRedirectUrl);

        ExchangeCodeRequest exchangeCodeRequest =
            new ExchangeCodeRequest(pinCode, GRANT_TYPE, authRedirectUrl, authClientId, authClientSecret);

        String pinAuthToken = authUtil.getBearToken(
            idamClient.exchangeCode(exchangeCodeRequest).getAccessToken()
        );

        UserDetails pinUserDetails = idamClient.getUserDetails(pinAuthToken);

        if (pinUserDetails == null) {
            throw new TaskException(new AuthenticationError("Invalid pin"));
        }

        final String letterHolderId = pinUserDetails.getId();
        final Map<String, Object> caseData = ((CaseDetails) context
            .getTransientObject(CASE_DETAILS_JSON_KEY))
            .getCaseData();

        final String coRespondentLetterHolderId = (String) caseData.get(CO_RESPONDENT_LETTER_HOLDER_ID);
        final String respondentLetterHolderId = (String) caseData.get(RESPONDENT_LETTER_HOLDER_ID);
        final boolean isRespondent = letterHolderId.equals(respondentLetterHolderId);
        final boolean isCoRespondent = letterHolderId.equals(coRespondentLetterHolderId);
        final String caseId = context.getTransientObject(CASE_ID_JSON_KEY);

        if (isRespondent) {
            context.setTransientObject(RESPONDENT_LETTER_HOLDER_ID, letterHolderId);
            context.setTransientObject(IS_RESPONDENT, true);
            log.info("Letter holder ID [{}] is associated with respondent in case [{}]", letterHolderId, caseId);
        } else if (isCoRespondent) {
            context.setTransientObject(CO_RESPONDENT_LETTER_HOLDER_ID, letterHolderId);
            context.setTransientObject(IS_RESPONDENT, false);
            log.info("Letter holder ID [{}] is associated with co-respondent in case [{}]", letterHolderId, caseId);
        } else {
            throw new TaskException(new AuthenticationError(
                String.format("Letter holder ID [%s] not associated with case [%s]", letterHolderId, caseId)
            ));
        }

        return pinUserDetails;
    }

    protected abstract String authenticatePinUser(String pin, String authClientId, String authRedirectUrl)
        throws TaskException;

}
