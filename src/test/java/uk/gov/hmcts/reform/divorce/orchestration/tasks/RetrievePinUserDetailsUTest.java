package uk.gov.hmcts.reform.divorce.orchestration.tasks;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.test.util.ReflectionTestUtils;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CaseDetails;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.exception.AuthenticationError;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.DefaultTaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskException;
import uk.gov.hmcts.reform.divorce.orchestration.util.AuthUtil;
import uk.gov.hmcts.reform.idam.client.IdamClient;
import uk.gov.hmcts.reform.idam.client.models.AuthenticateUserResponse;
import uk.gov.hmcts.reform.idam.client.models.ExchangeCodeRequest;
import uk.gov.hmcts.reform.idam.client.models.TokenExchangeResponse;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.AUTH_CLIENT_ID;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.AUTH_CLIENT_SECRET;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.AUTH_REDIRECT_URL;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.BEARER_AUTH_TOKEN;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_LETTER_HOLDER_ID_CODE;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_PIN;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_PIN_CODE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CASE_DETAILS_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.GRANT_TYPE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.RESPONDENT_LETTER_HOLDER_ID;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.RESPONDENT_PIN;

@RunWith(MockitoJUnitRunner.class)
public class RetrievePinUserDetailsUTest {

    @Mock
    private IdamClient idamClient;

    @Mock
    private AuthUtil authUtil;

    @InjectMocks
    private RetrievePinUserDetails classUnderTest;

    @Before
    public void setup() {
        when(authUtil.getBearToken(BEARER_AUTH_TOKEN)).thenCallRealMethod();
        ReflectionTestUtils.setField(classUnderTest, "authUtil", authUtil);
        ReflectionTestUtils.setField(classUnderTest, "authClientId", AUTH_CLIENT_ID);
        ReflectionTestUtils.setField(classUnderTest, "authClientSecret", AUTH_CLIENT_SECRET);
        ReflectionTestUtils.setField(classUnderTest, "authRedirectUrl", AUTH_REDIRECT_URL, null);
    }

    @Test
    public void givenHttpStatusIsNotFoundOrOK_whenExecute_thenThrowException() {
        final UserDetails payload = UserDetails.builder().build();

        final TaskContext taskContext = new DefaultTaskContext();
        taskContext.setTransientObject(RESPONDENT_PIN, TEST_PIN);

        when(idamClient.authenticatePinUser(TEST_PIN, AUTH_CLIENT_ID, AUTH_REDIRECT_URL, null))
            .thenReturn(null);

        try {
            classUnderTest.execute(taskContext, payload);
        } catch (TaskException e) {
            assertTrue(e.getCause() instanceof AuthenticationError);
        }

        verify(idamClient).authenticatePinUser(TEST_PIN, AUTH_CLIENT_ID, AUTH_REDIRECT_URL, null);
    }

    @Test(expected = TaskException.class)
    public void givenPinUserAuth_whenDataIsNull_thenThrowException() throws TaskException {
        final UserDetails payload = UserDetails.builder().build();

        final TaskContext taskContext = new DefaultTaskContext();
        taskContext.setTransientObject(RESPONDENT_PIN, TEST_PIN);

        when(idamClient.authenticatePinUser(TEST_PIN, AUTH_CLIENT_ID, AUTH_REDIRECT_URL, null))
            .thenReturn(null);

        classUnderTest.execute(taskContext, payload);

        verify(idamClient).authenticatePinUser(TEST_PIN, AUTH_CLIENT_ID, AUTH_REDIRECT_URL, null);
    }

    @Test
    public void givenPinUserNotFound_whenExecute_thenThrowException() {
        final AuthenticateUserResponse authenticateUserResponse = new AuthenticateUserResponse(TEST_PIN_CODE);
        final TokenExchangeResponse tokenExchangeResponse = new TokenExchangeResponse(BEARER_AUTH_TOKEN);
        ExchangeCodeRequest exchangeCodeRequest =
            new ExchangeCodeRequest(TEST_PIN_CODE, GRANT_TYPE, AUTH_REDIRECT_URL, AUTH_CLIENT_ID, AUTH_CLIENT_SECRET);

        final UserDetails payload = UserDetails.builder().build();

        final TaskContext taskContext = new DefaultTaskContext();
        taskContext.setTransientObject(RESPONDENT_PIN, TEST_PIN);

        when(idamClient.authenticatePinUser(TEST_PIN, AUTH_CLIENT_ID, AUTH_REDIRECT_URL, null))
            .thenReturn(authenticateUserResponse);
        when(idamClient.exchangeCode(exchangeCodeRequest))
            .thenReturn(tokenExchangeResponse);
        when(idamClient.getUserDetails(BEARER_AUTH_TOKEN)).thenReturn(null);

        try {
            classUnderTest.execute(taskContext, payload);
        } catch (TaskException taskException) {
            assertTrue(taskException.getCause() instanceof AuthenticationError);
        }

        verify(idamClient).authenticatePinUser(TEST_PIN, AUTH_CLIENT_ID, AUTH_REDIRECT_URL, null);
        verify(idamClient).exchangeCode(exchangeCodeRequest);
        verify(idamClient).getUserDetails(BEARER_AUTH_TOKEN);
    }

    @Test
    public void givenPinUserExists_whenExecute_thenProceedAsExpected() throws TaskException {
        final AuthenticateUserResponse authenticateUserResponse = new AuthenticateUserResponse(TEST_PIN_CODE);
        final TokenExchangeResponse tokenExchangeResponse = new TokenExchangeResponse(BEARER_AUTH_TOKEN);

        final UserDetails payload = UserDetails.builder().build();
        Map<String, Object> caseData = new HashMap<>();
        caseData.put(RESPONDENT_LETTER_HOLDER_ID, TEST_LETTER_HOLDER_ID_CODE);
        final CaseDetails caseDetails = CaseDetails.builder().caseData(caseData).build();

        final TaskContext taskContext = new DefaultTaskContext();
        taskContext.setTransientObject(CASE_DETAILS_JSON_KEY, caseDetails);
        taskContext.setTransientObject(RESPONDENT_PIN, TEST_PIN);

        final UserDetails pinUserDetails = UserDetails.builder().id(TEST_LETTER_HOLDER_ID_CODE).build();
        ExchangeCodeRequest exchangeCodeRequest =
                new ExchangeCodeRequest(TEST_PIN_CODE, GRANT_TYPE, AUTH_REDIRECT_URL, AUTH_CLIENT_ID, AUTH_CLIENT_SECRET);

        when(idamClient.authenticatePinUser(TEST_PIN, AUTH_CLIENT_ID, AUTH_REDIRECT_URL, null))
            .thenReturn(authenticateUserResponse);
        when(idamClient.exchangeCode(exchangeCodeRequest)).thenReturn(tokenExchangeResponse);
        when(idamClient.getUserDetails(BEARER_AUTH_TOKEN)).thenReturn(pinUserDetails);

        UserDetails actual = classUnderTest.execute(taskContext, payload);

        assertEquals(pinUserDetails, actual);
        assertEquals(TEST_LETTER_HOLDER_ID_CODE, taskContext.getTransientObject(RESPONDENT_LETTER_HOLDER_ID));

        verify(idamClient).authenticatePinUser(TEST_PIN, AUTH_CLIENT_ID, AUTH_REDIRECT_URL, null);
        verify(idamClient).exchangeCode(exchangeCodeRequest);
        verify(idamClient).getUserDetails(BEARER_AUTH_TOKEN);
    }
}
