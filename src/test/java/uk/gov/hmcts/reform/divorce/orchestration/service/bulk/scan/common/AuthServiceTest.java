package uk.gov.hmcts.reform.divorce.orchestration.service.bulk.scan.common;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.authorisation.validators.ServiceAuthTokenValidator;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.bulk.scan.common.AuthService;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.bulk.scan.common.ForbiddenException;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.bulk.scan.common.UnauthenticatedException;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class AuthServiceTest {

    private static final String VALID_S2S_TOKEN = "this is valid token";
    private static final String WRONG_SERVICE_TOKEN = "this will return service that is not whitelisted";

    private static final String WHITELISTED_SERVICE = "service_name";
    private static final String WHITELISTED_SERVICE_2 = "service_name2";

    @Mock
    private ServiceAuthTokenValidator authTokenValidator;

    @Before
    public void setup() {
        when(authTokenValidator.getServiceName(VALID_S2S_TOKEN)).thenReturn(WHITELISTED_SERVICE);
        when(authTokenValidator.getServiceName(WRONG_SERVICE_TOKEN)).thenReturn("this service is not whitelisted");
    }

    @Test(expected = ForbiddenException.class)
    public void assertIsServiceAllowedToValidateShouldThrowForbiddenExceptionWhenServiceIsNotAllowed() {
        AuthService authService = new AuthService(authTokenValidator, asList(WHITELISTED_SERVICE), emptyList());
        authService.assertIsServiceAllowedToValidate(WRONG_SERVICE_TOKEN);
    }

    @Test(expected = ForbiddenException.class)
    public void assertIsServiceAllowedToValidateShouldThrowForbiddenExceptionWhenThereIsNoServiceAllowed() {
        AuthService authService = new AuthService(authTokenValidator, emptyList(), emptyList());
        authService.assertIsServiceAllowedToValidate(VALID_S2S_TOKEN);
    }

    @Test(expected = UnauthenticatedException.class)
    public void assertIsServiceAllowedToValidateShouldThrowUnauthenticatedExceptionWhenNoToken() {
        AuthService authService = new AuthService(authTokenValidator, emptyList(), emptyList());
        authService.assertIsServiceAllowedToValidate("");
    }

    @Test(expected = ForbiddenException.class)
    public void assertIsServiceAllowedToUpdateShouldThrowForbiddenExceptionWhenServiceIsNotAllowed() {
        AuthService authService = new AuthService(authTokenValidator, emptyList(), asList(WHITELISTED_SERVICE));
        authService.assertIsServiceAllowedToValidate(WRONG_SERVICE_TOKEN);
    }

    @Test
    public void assertIsServiceAllowedToValidateShouldBeOk() {
        AuthService authService = new AuthService(
            authTokenValidator, asList(WHITELISTED_SERVICE, WHITELISTED_SERVICE_2), emptyList()
        );
        authService.assertIsServiceAllowedToValidate(VALID_S2S_TOKEN);
    }

    @Test(expected = ForbiddenException.class)
    public void assertIsServiceAllowedToUpdateShouldThrowForbiddenExceptionWhenThereIsNoServiceAllowed() {
        AuthService authService = new AuthService(authTokenValidator, emptyList(), emptyList());
        authService.assertIsServiceAllowedToUpdate(VALID_S2S_TOKEN);
    }

    @Test(expected = UnauthenticatedException.class)
    public void assertIsServiceAllowedToUpdateShouldThrowUnauthenticatedExceptionWhenNoToken() {
        AuthService authService = new AuthService(authTokenValidator, emptyList(), emptyList());
        authService.assertIsServiceAllowedToUpdate("");
    }

    @Test
    public void assertIsServiceAllowedToUpdateShouldBeOk() {
        AuthService authService = new AuthService(
            authTokenValidator,
            asList(WHITELISTED_SERVICE_2),
            asList(WHITELISTED_SERVICE)
        );
        authService.assertIsServiceAllowedToUpdate(VALID_S2S_TOKEN);
    }
}