package uk.gov.hmcts.reform.divorce.orchestration.tasks;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.test.util.ReflectionTestUtils;
import uk.gov.hmcts.reform.divorce.orchestration.client.TacticalIdamClient;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CaseDetails;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.exception.AuthenticationError;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.idam.AuthenticateUserResponse;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.idam.TokenExchangeResponse;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.idam.UserDetails;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.DefaultTaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskException;
import uk.gov.hmcts.reform.divorce.orchestration.util.AuthUtil;

import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.BEARER_AUTH_TOKEN;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_LETTER_HOLDER_ID_CODE;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_PIN;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_PIN_CODE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.AUTHORIZATION_CODE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CCD_CASE_DATA;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CODE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.PIN;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.PIN_PREFIX;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.RESPONDENT_LETTER_HOLDER_ID;

@RunWith(MockitoJUnitRunner.class)
public class RetrievePinUserDetailsFromTacticalIdamUTest {
    private static final String AUTH_CLIENT_ID = "authClientId";
    private static final String AUTH_CLIENT_SECRET = "authClientSecret";
    private static final String AUTH_REDIRECT_URL = "authRedirectUrl";

    @Mock
    private TacticalIdamClient idamClient;

    @Mock
    private AuthUtil authUtil;

    @InjectMocks
    private RetrievePinUserDetailsFromTacticalIdam classUnderTest;

    @Before
    public void setup() {
        when(authUtil.getBearToken(BEARER_AUTH_TOKEN)).thenCallRealMethod();
        ReflectionTestUtils.setField(classUnderTest, "authUtil", authUtil);
        ReflectionTestUtils.setField(classUnderTest, "authClientId", AUTH_CLIENT_ID);
        ReflectionTestUtils.setField(classUnderTest, "authClientSecret", AUTH_CLIENT_SECRET);
        ReflectionTestUtils.setField(classUnderTest, "authRedirectUrl", AUTH_REDIRECT_URL);
    }

    @Test
    public void givenPinUserNotFound_whenExecute_thenThrowException() {
        final String pinAuthToken = getPinAuthToken();

        final AuthenticateUserResponse authenticateUserResponse =
            AuthenticateUserResponse.builder()
                .code(TEST_PIN_CODE)
                .build();

        final TokenExchangeResponse tokenExchangeResponse =
            TokenExchangeResponse.builder()
                .accessToken(BEARER_AUTH_TOKEN)
                .build();

        final UserDetails payload = UserDetails.builder().build();

        final TaskContext taskContext = new DefaultTaskContext();
        taskContext.setTransientObject(PIN, TEST_PIN);

        when(idamClient.authenticatePinUser(pinAuthToken, CODE, AUTH_CLIENT_ID, AUTH_REDIRECT_URL))
            .thenReturn(authenticateUserResponse);
        when(
            idamClient.exchangeCode(
                TEST_PIN_CODE, AUTHORIZATION_CODE, AUTH_REDIRECT_URL, AUTH_CLIENT_ID, AUTH_CLIENT_SECRET)
        ).thenReturn(tokenExchangeResponse);
        when(idamClient.retrieveUserDetails(BEARER_AUTH_TOKEN)).thenReturn(null);

        try {
            classUnderTest.execute(taskContext, payload);
        } catch (TaskException taskException) {
            assertTrue(taskException.getCause() instanceof AuthenticationError);
        }

        verify(idamClient).authenticatePinUser(pinAuthToken, CODE, AUTH_CLIENT_ID, AUTH_REDIRECT_URL);
        verify(idamClient)
            .exchangeCode(TEST_PIN_CODE, AUTHORIZATION_CODE, AUTH_REDIRECT_URL, AUTH_CLIENT_ID, AUTH_CLIENT_SECRET);
        verify(idamClient).retrieveUserDetails(BEARER_AUTH_TOKEN);
    }

    @Test
    public void givenPinUserExists_whenExecute_thenProceedAsExpected() throws TaskException {
        final String pinAuthToken = getPinAuthToken();

        final AuthenticateUserResponse authenticateUserResponse =
            AuthenticateUserResponse.builder()
                .code(TEST_PIN_CODE)
                .build();

        final TokenExchangeResponse tokenExchangeResponse =
            TokenExchangeResponse.builder()
                .accessToken(BEARER_AUTH_TOKEN)
                .build();

        Map<String, Object> caseData = new HashMap<>();
        caseData.put(RESPONDENT_LETTER_HOLDER_ID, TEST_LETTER_HOLDER_ID_CODE);
        final UserDetails payload = UserDetails.builder().build();
        final CaseDetails caseDetails = CaseDetails.builder().caseData(caseData).build();

        final TaskContext taskContext = new DefaultTaskContext();
        taskContext.setTransientObject(PIN, TEST_PIN);
        taskContext.setTransientObject(CCD_CASE_DATA, caseDetails.getCaseData());

        final UserDetails pinUserDetails = UserDetails.builder().id(TEST_LETTER_HOLDER_ID_CODE).build();

        when(idamClient.authenticatePinUser(pinAuthToken, CODE, AUTH_CLIENT_ID, AUTH_REDIRECT_URL))
            .thenReturn(authenticateUserResponse);
        when(
            idamClient.exchangeCode(
                TEST_PIN_CODE, AUTHORIZATION_CODE, AUTH_REDIRECT_URL, AUTH_CLIENT_ID, AUTH_CLIENT_SECRET)
        ).thenReturn(tokenExchangeResponse);
        when(idamClient.retrieveUserDetails(BEARER_AUTH_TOKEN)).thenReturn(pinUserDetails);

        UserDetails actual = classUnderTest.execute(taskContext, payload);

        assertEquals(pinUserDetails, actual);
        assertEquals(TEST_LETTER_HOLDER_ID_CODE, taskContext.getTransientObject(RESPONDENT_LETTER_HOLDER_ID));

        verify(idamClient).authenticatePinUser(pinAuthToken, CODE, AUTH_CLIENT_ID, AUTH_REDIRECT_URL);
        verify(idamClient)
            .exchangeCode(TEST_PIN_CODE, AUTHORIZATION_CODE, AUTH_REDIRECT_URL, AUTH_CLIENT_ID, AUTH_CLIENT_SECRET);
        verify(idamClient).retrieveUserDetails(BEARER_AUTH_TOKEN);
    }

    private String getPinAuthToken() {
        return PIN_PREFIX + new String(Base64.getEncoder().encode(TEST_PIN.getBytes()));
    }
}