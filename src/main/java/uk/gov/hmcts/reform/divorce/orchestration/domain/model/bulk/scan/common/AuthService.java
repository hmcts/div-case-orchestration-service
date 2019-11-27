package uk.gov.hmcts.reform.divorce.orchestration.domain.model.bulk.scan.common;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.authorisation.validators.AuthTokenValidator;

import java.util.List;

import static org.apache.commons.lang3.StringUtils.isBlank;

@Service
public class AuthService {

    @Qualifier
    private final AuthTokenValidator authTokenValidator;
    private final List<String> allowedToValidate;
    private final List<String> allowedToUpdate;

    public AuthService(
        AuthTokenValidator authTokenValidator,
        @Value("${idam.s2s-auth.services-allowed-to-validate}") List<String> allowedToValidate,
        @Value("${idam.s2s-auth.services-allowed-to-update}") List<String> allowedToUpdate
    ) {
        this.authTokenValidator = authTokenValidator;
        this.allowedToValidate = allowedToValidate;
        this.allowedToUpdate = allowedToUpdate;
    }

    public void assertIsServiceAllowedToValidate(String token) {
        String serviceName = this.authenticate(token);

        if (!allowedToValidate.contains(serviceName)) {
            throw new ForbiddenException("Service is not authorised to OCR validation");
        }
    }

    public void assertIsServiceAllowedToUpdate(String token) {
        String serviceName = this.authenticate(token);

        if (!allowedToUpdate.contains(serviceName)) {
            throw new ForbiddenException("Service is not authorised to transform OCR data to case");
        }
    }

    private String authenticate(String authHeader) {
        if (isBlank(authHeader)) {
            throw new UnauthenticatedException("Provided S2S token is missing or invalid");
        }

        return authTokenValidator.getServiceName(authHeader);
    }
}
