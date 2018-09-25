package uk.gov.hmcts.reform.divorce.context;

import net.serenitybdd.junit.runners.SerenityRunner;
import net.serenitybdd.junit.spring.integration.SpringIntegrationMethodRule;
import org.junit.Rule;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.test.context.ContextConfiguration;
import uk.gov.hmcts.reform.divorce.model.UserDetails;
import uk.gov.hmcts.reform.divorce.support.IdamUtils;

import java.util.UUID;

@RunWith(SerenityRunner.class)
@ContextConfiguration(classes = {ServiceContextConfiguration.class})
public abstract class IntegrationTest {
    private static final String CASE_WORKER_USERNAME = "CASE_WORKER_USER";
    private static final String CASE_WORKER_PASSWORD = "CASE_WORKER_PASSWORD";
    private static final String CITIZEN_ROLE = "citizen";
    private static final String CASEWORKER_DIVORCE_ROLE = "caseworker-divorce";
    private static final String CASEWORKER_DIVORCE_COURTADMIN_ROLE = "caseworker-divorce-courtadmin";
    private static final String CASEWORKER_ROLE = "caseworker";
    private static final String PASSWORD = "PassW0rd";

    private UserDetails caseWorkerUser;

    @Value("${case.orchestration.service.base.uri}")
    protected String serverUrl;

    @Autowired
    protected IdamUtils idamTestSupportUtil;

    @Rule
    public SpringIntegrationMethodRule springMethodIntegration;

    protected IntegrationTest() {
        this.springMethodIntegration = new SpringIntegrationMethodRule();
    }

    protected UserDetails createCaseWorkerUser() {
        synchronized (this) {
            if (caseWorkerUser == null) {
                caseWorkerUser = getUserDetails(CASE_WORKER_USERNAME, CASE_WORKER_PASSWORD,
                    CASEWORKER_DIVORCE_ROLE, CASEWORKER_DIVORCE_COURTADMIN_ROLE, CASEWORKER_ROLE, CITIZEN_ROLE);
            }

            return caseWorkerUser;
        }
    }

    protected UserDetails createCitizenUser() {
        final String username = "simulate-delivered" + UUID.randomUUID() + "@notifications.service.gov.uk";

        return getUserDetails(username, PASSWORD, CITIZEN_ROLE);
    }

    protected UserDetails createCitizenUser(String role) {
        final String username = "simulate-delivered" + UUID.randomUUID() + "@notifications.service.gov.uk";

        return getUserDetails(username, PASSWORD, role);
    }

    private UserDetails getUserDetails(String username, String password, String... role) {
        synchronized (this) {
            idamTestSupportUtil.createUser(username, password, role);

            final String authToken = idamTestSupportUtil.generateUserTokenWithNoRoles(username, password);

            final String userId = idamTestSupportUtil.getUserId(authToken);

            return UserDetails.builder()
                .username(username)
                .emailAddress(username)
                .password(password)
                .authToken(authToken)
                .id(userId)
                .build();
        }
    }
}
