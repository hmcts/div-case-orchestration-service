package uk.gov.hmcts.reform.divorce.orchestration.tasks;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.divorce.orchestration.client.IdamClient;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.idam.AuthenticateUserResponse;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.idam.Pin;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.idam.PinRequest;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.idam.TokenExchangeResponse;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.Task;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskContext;

import java.util.Base64;
import java.util.Map;

import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D_8_PETITIONER_FIRST_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D_8_PETITIONER_LAST_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.PIN;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.RESPONDENT_LETTER_HOLDER_ID;

@Component
public class IdamPinGenerator implements Task<Map<String, Object>> {
    private static final String BEARER = "Bearer ";
    private static final String AUTHORIZATION_CODE = "authorization_code";
    private static final String CODE = "code";
    private static final String BASIC = "Basic ";

    @Value("${idam.api.redirect-url}")
    private String authRedirectUrl;

    @Value("${auth2.client.id}")
    private String authClientId;

    @Value("${auth2.client.secret}")
    private String authClientSecret;

    @Value("${idam.citizen.username}")
    private String citizenUserName;

    @Value("${idam.citizen.password}")
    private String citizenPassword;

    private final IdamClient idamClient;

    public IdamPinGenerator(IdamClient idamClient) {
        this.idamClient = idamClient;
    }

    @Override
    public Map<String, Object> execute(TaskContext context,
                                       Map<String, Object> caseData) {
        Pin pin = idamClient.createPin(PinRequest.builder()
                        .firstName(String.valueOf(caseData.getOrDefault(D_8_PETITIONER_FIRST_NAME, "")))
                        .lastName(String.valueOf(caseData.getOrDefault(D_8_PETITIONER_LAST_NAME, "")))
                        .build(),
                getIdamOauth2Token(citizenUserName, citizenPassword));

        caseData.put(PIN, pin.getPin());
        caseData.put(RESPONDENT_LETTER_HOLDER_ID, pin.getUserId());

        return caseData;
    }

    private String getIdamOauth2Token(String username, String password) {
        String basicAuthHeader = getBasicAuthHeader(username, password);
        AuthenticateUserResponse authenticateUserResponse = idamClient.authenticateUser(
                basicAuthHeader,
                CODE,
                authClientId,
                authRedirectUrl
        );

        TokenExchangeResponse tokenExchangeResponse = idamClient.exchangeCode(
                authenticateUserResponse.getCode(),
                AUTHORIZATION_CODE,
                authRedirectUrl,
                authClientId,
                authClientSecret
        );

        return BEARER + tokenExchangeResponse.getAccessToken();
    }

    private String getBasicAuthHeader(String username, String password) {
        String authorisation = username + ":" + password;
        return BASIC + Base64.getEncoder().encodeToString(authorisation.getBytes());
    }
}
