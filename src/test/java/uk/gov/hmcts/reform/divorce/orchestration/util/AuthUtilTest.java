package uk.gov.hmcts.reform.divorce.orchestration.util;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.idam.client.IdamClient;
import uk.gov.hmcts.reform.idam.client.models.AuthenticateUserResponse;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.util.ReflectionTestUtils.setField;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_PIN_CODE;
import static uk.gov.hmcts.reform.idam.client.IdamClient.BEARER_AUTH_TYPE;

@RunWith(MockitoJUnitRunner.class)
public class AuthUtilTest {

    @Mock
    IdamClient idamClient;

    @InjectMocks
    private AuthUtil authUtil;

    @Before
    public void setup() {
        setField(authUtil, "citizenUserName", "citizenUsername");
        setField(authUtil, "citizenPassword", "password");
        setField(authUtil, "authRedirectUrl", "redirectUrl");
        setField(authUtil, "authClientId", "authClientId");
        setField(authUtil, "authClientSecret", "authClientSecret");
    }

    @Test
    public void testGetCitizenToken() {
        AuthenticateUserResponse authenticateResponse = new AuthenticateUserResponse(TEST_PIN_CODE);
        when(idamClient.authenticateUser(anyString(), anyString()))
            .thenReturn(BEARER_AUTH_TYPE + " " + authenticateResponse.getCode());

        String token = authUtil.getCitizenToken();
        assertTrue(token.startsWith("Bearer"));
    }

    @Test
    public void givenTokenIsNull_whenGetBearToken_thenReturnNull() {
        testGetBearToken(null, null);
    }

    @Test
    public void givenTokenIsBlank_whenGetBearToken_thenReturnBlank() {
        testGetBearToken(" ", " ");
    }

    @Test
    public void givenTokenDoesNotHaveBearer_whenGetBearToken_thenReturnWithBearer() {
        testGetBearToken("SomeToken", "Bearer SomeToken");
    }

    @Test
    public void givenTokenDoesHaveBearer_whenGetBearToken_thenReturnWithBearer() {
        testGetBearToken("Bearer SomeToken", "Bearer SomeToken");
    }

    private void testGetBearToken(String input, String expected) {
        assertEquals(authUtil.getBearerToken(input), expected);
    }
}
