package uk.gov.hmcts.reform.divorce.orchestration.util;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.divorce.orchestration.client.IdamClient;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.idam.AuthenticateUserResponse;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.idam.TokenExchangeResponse;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;
import static org.springframework.test.util.ReflectionTestUtils.setField;

@RunWith(MockitoJUnitRunner.class)
public class AuthUtilUTest {

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
        AuthenticateUserResponse authenticateResponse = AuthenticateUserResponse.builder().build();
        authenticateResponse.setCode("mycode");

        when(idamClient.authenticateUser(any(), any(), any(), any()))
            .thenReturn(authenticateResponse);

        TokenExchangeResponse tokenExchangeResponse = spy(TokenExchangeResponse.builder().build());
        when(idamClient.exchangeCode(eq("mycode"), anyString(), anyString(), anyString(), anyString()))
            .thenReturn(tokenExchangeResponse);
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
        assertEquals(AuthUtil.getBearToken(input), expected);
    }
}
