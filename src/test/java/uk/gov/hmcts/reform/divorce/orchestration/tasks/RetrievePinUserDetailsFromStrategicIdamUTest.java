package uk.gov.hmcts.reform.divorce.orchestration.tasks;

import feign.Request;
import feign.Response;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.HttpStatus;
import org.springframework.test.util.ReflectionTestUtils;
import uk.gov.hmcts.reform.divorce.orchestration.client.StrategicIdamClient;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CaseDetails;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.exception.AuthenticationError;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.idam.TokenExchangeResponse;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.idam.UserDetails;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.DefaultTaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskException;
import uk.gov.hmcts.reform.divorce.orchestration.util.AuthUtil;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpStatus.FOUND;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.HttpStatus.UNAUTHORIZED;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.BEARER_AUTH_TOKEN;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_LETTER_HOLDER_ID_CODE;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_PIN;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_PIN_CODE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.AUTHORIZATION_CODE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CASE_DETAILS_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.LOCATION_HEADER;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.RESPONDENT_LETTER_HOLDER_ID;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.RESPONDENT_PIN;

@RunWith(MockitoJUnitRunner.class)
public class RetrievePinUserDetailsFromStrategicIdamUTest {
    private static final String AUTH_CLIENT_ID = "authClientId";
    private static final String AUTH_CLIENT_SECRET = "authClientSecret";
    private static final String AUTH_REDIRECT_URL = "authRedirectUrl";
    private static final String AUTH_URL_WITH_REDIRECT = "http://www.redirect.url?code=" + TEST_PIN_CODE;

    @Mock
    private StrategicIdamClient idamClient;

    @Mock
    private AuthUtil authUtil;

    @InjectMocks
    private RetrievePinUserDetailsFromStrategicIdam classUnderTest;

    @Before
    public void setup() {
        when(authUtil.getBearToken(BEARER_AUTH_TOKEN)).thenCallRealMethod();
        ReflectionTestUtils.setField(classUnderTest, "authUtil", authUtil);
        ReflectionTestUtils.setField(classUnderTest, "authClientId", AUTH_CLIENT_ID);
        ReflectionTestUtils.setField(classUnderTest, "authClientSecret", AUTH_CLIENT_SECRET);
        ReflectionTestUtils.setField(classUnderTest, "authRedirectUrl", AUTH_REDIRECT_URL);
    }

    @Test
    public void givenHttpStatusIsNotFoundOrOK_whenExecute_thenThrowException() {
        final Response idamRedirectResponse = buildResponse(UNAUTHORIZED, Collections.emptyList());

        final UserDetails payload = UserDetails.builder().build();

        final TaskContext taskContext = new DefaultTaskContext();
        taskContext.setTransientObject(RESPONDENT_PIN, TEST_PIN);

        when(idamClient.authenticatePinUser(TEST_PIN, AUTH_CLIENT_ID, AUTH_REDIRECT_URL))
            .thenReturn(idamRedirectResponse);

        try {
            classUnderTest.execute(taskContext, payload);
        } catch (TaskException e) {
            assertTrue(e.getCause() instanceof AuthenticationError);
        }

        verify(idamClient).authenticatePinUser(TEST_PIN, AUTH_CLIENT_ID, AUTH_REDIRECT_URL);
    }

    @Test(expected = IllegalArgumentException.class)
    public void givenLocationKeyNotFound_whenExecute_thenThrowException() throws TaskException {
        final Response idamRedirectResponse = buildResponse(OK, Collections.emptyList());

        final UserDetails payload = UserDetails.builder().build();

        final TaskContext taskContext = new DefaultTaskContext();
        taskContext.setTransientObject(RESPONDENT_PIN, TEST_PIN);

        when(idamClient.authenticatePinUser(TEST_PIN, AUTH_CLIENT_ID, AUTH_REDIRECT_URL))
            .thenReturn(idamRedirectResponse);

        classUnderTest.execute(taskContext, payload);

        verify(idamClient).authenticatePinUser(TEST_PIN, AUTH_CLIENT_ID, AUTH_REDIRECT_URL);
    }

    @Test
    public void givenPinUserNotFound_whenExecute_thenThrowException() {
        final Response idamRedirectResponse = buildResponse(FOUND, Collections.singletonList(AUTH_URL_WITH_REDIRECT));
        final TokenExchangeResponse tokenExchangeResponse =
            TokenExchangeResponse.builder()
                .accessToken(BEARER_AUTH_TOKEN)
                .build();

        final UserDetails payload = UserDetails.builder().build();

        final TaskContext taskContext = new DefaultTaskContext();
        taskContext.setTransientObject(RESPONDENT_PIN, TEST_PIN);

        when(idamClient.authenticatePinUser(TEST_PIN, AUTH_CLIENT_ID, AUTH_REDIRECT_URL))
            .thenReturn(idamRedirectResponse);
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

        verify(idamClient).authenticatePinUser(TEST_PIN, AUTH_CLIENT_ID, AUTH_REDIRECT_URL);
        verify(idamClient)
            .exchangeCode(TEST_PIN_CODE, AUTHORIZATION_CODE, AUTH_REDIRECT_URL, AUTH_CLIENT_ID, AUTH_CLIENT_SECRET);
        verify(idamClient).retrieveUserDetails(BEARER_AUTH_TOKEN);
    }

    @Test
    public void givenPinUserExists_whenExecute_thenProceedAsExpected() throws TaskException {
        final Response idamRedirectResponse = buildResponse(FOUND, Collections.singletonList(AUTH_URL_WITH_REDIRECT));
        final TokenExchangeResponse tokenExchangeResponse =
            TokenExchangeResponse.builder()
                .accessToken(BEARER_AUTH_TOKEN)
                .build();

        final UserDetails payload = UserDetails.builder().build();
        Map<String, Object> caseData = new HashMap<>();
        caseData.put(RESPONDENT_LETTER_HOLDER_ID, TEST_LETTER_HOLDER_ID_CODE);
        final CaseDetails caseDetails = CaseDetails.builder().caseData(caseData).build();

        final TaskContext taskContext = new DefaultTaskContext();
        taskContext.setTransientObject(CASE_DETAILS_JSON_KEY, caseDetails);
        taskContext.setTransientObject(RESPONDENT_PIN, TEST_PIN);

        final UserDetails pinUserDetails = UserDetails.builder().id(TEST_LETTER_HOLDER_ID_CODE).build();

        when(idamClient.authenticatePinUser(TEST_PIN, AUTH_CLIENT_ID, AUTH_REDIRECT_URL))
            .thenReturn(idamRedirectResponse);
        when(
            idamClient.exchangeCode(
                TEST_PIN_CODE, AUTHORIZATION_CODE, AUTH_REDIRECT_URL, AUTH_CLIENT_ID, AUTH_CLIENT_SECRET)
        ).thenReturn(tokenExchangeResponse);
        when(idamClient.retrieveUserDetails(BEARER_AUTH_TOKEN)).thenReturn(pinUserDetails);

        UserDetails actual = classUnderTest.execute(taskContext, payload);

        assertEquals(pinUserDetails, actual);
        assertEquals(TEST_LETTER_HOLDER_ID_CODE, taskContext.getTransientObject(RESPONDENT_LETTER_HOLDER_ID));

        verify(idamClient).authenticatePinUser(TEST_PIN, AUTH_CLIENT_ID, AUTH_REDIRECT_URL);
        verify(idamClient)
            .exchangeCode(TEST_PIN_CODE, AUTHORIZATION_CODE, AUTH_REDIRECT_URL, AUTH_CLIENT_ID, AUTH_CLIENT_SECRET);
        verify(idamClient).retrieveUserDetails(BEARER_AUTH_TOKEN);
    }

    private Response buildResponse(HttpStatus status, List<String> locationHeaders) {
        return Response.builder()
            .request(Request.create(Request.HttpMethod.GET, "http//example.com", Collections.emptyMap(), null))
            .status(status.value())
            .headers(Collections.singletonMap(LOCATION_HEADER, locationHeaders))
            .build();
    }
}
