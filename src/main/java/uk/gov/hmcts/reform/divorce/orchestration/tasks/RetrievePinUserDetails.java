package uk.gov.hmcts.reform.divorce.orchestration.tasks;

import feign.Response;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;
import uk.gov.hmcts.reform.divorce.orchestration.client.IdamClient;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CaseDetails;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.exception.AuthenticationError;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.idam.UserDetails;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.Task;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskException;
import uk.gov.hmcts.reform.divorce.orchestration.util.AuthUtil;

import java.util.Map;

import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.AUTHORIZATION_CODE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CASE_DETAILS_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CASE_ID_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CODE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CO_RESPONDENT_LETTER_HOLDER_ID;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.IS_RESPONDENT;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.LOCATION_HEADER;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.RESPONDENT_LETTER_HOLDER_ID;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.RESPONDENT_PIN;

@Slf4j
@Component
public class RetrievePinUserDetails implements Task<UserDetails> {
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

        String pinAuthToken = authUtil.getBearToken(
            getIdamClient().exchangeCode(
                pinCode,
                AUTHORIZATION_CODE,
                authRedirectUrl,
                authClientId,
                authClientSecret
            ).getAccessToken()
        );

        UserDetails pinUserDetails = getIdamClient().retrieveUserDetails(pinAuthToken);

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

    protected String authenticatePinUser(String pin, String authClientId, String authRedirectUrl) throws TaskException {
        Response authenticateResponse = getIdamClient().authenticatePinUser(pin, authClientId, authRedirectUrl);

        if (authenticateResponse.status() == HttpStatus.OK.value()
            || authenticateResponse.status() == HttpStatus.FOUND.value()) {
            return getCodeFromRedirect(authenticateResponse);
        }

        throw new TaskException(new AuthenticationError(String.format("Error authenticating RESPONDENT_PIN [%s]", pin)));
    }

    private String getCodeFromRedirect(Response response) {
        String location = response.headers().get(LOCATION_HEADER).stream().findFirst()
            .orElseThrow(IllegalArgumentException::new);

        UriComponents build = UriComponentsBuilder.fromUriString(location).build();
        return build.getQueryParams().getFirst(CODE);
    }

    protected IdamClient getIdamClient() {
        return idamClient;
    }
}
