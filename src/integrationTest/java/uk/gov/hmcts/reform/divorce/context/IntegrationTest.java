package uk.gov.hmcts.reform.divorce.context;

import lombok.extern.slf4j.Slf4j;
import net.serenitybdd.junit.runners.SerenityRunner;
import net.serenitybdd.junit.spring.integration.SpringIntegrationMethodRule;
import org.assertj.core.util.Strings;
import org.junit.Rule;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.test.context.ContextConfiguration;
import uk.gov.hmcts.reform.divorce.model.UserDetails;
import uk.gov.hmcts.reform.divorce.support.IdamUtils;

import java.io.IOException;
import java.net.InetAddress;
import java.net.URL;
import java.util.UUID;
import java.util.function.Supplier;
import javax.annotation.PostConstruct;

import javax.annotation.PostConstruct;

import javax.annotation.PostConstruct;

@Slf4j
@RunWith(SerenityRunner.class)
@ContextConfiguration(classes = {ServiceContextConfiguration.class})
public abstract class IntegrationTest {
    private static final String CASE_WORKER_USERNAME = "TEST_CASE_WORKER_USER";
    private static final String EMAIL_DOMAIN = "@notifications.service.gov.uk";
    private static final String CASE_WORKER_PASSWORD = "genericPassword123";
    private static final String CITIZEN_ROLE = "citizen";
    private static final String CASEWORKER_DIVORCE_ROLE = "caseworker-divorce";
    private static final String CASEWORKER_DIVORCE_COURTADMIN_ROLE = "caseworker-divorce-courtadmin";
    private static final String CASEWORKER_DIVORCE_COURTADMIN_BETA_ROLE = "caseworker-divorce-courtadmin_beta";
    private static final String CASEWORKER_ROLE = "caseworker";
    private static final String PASSWORD = "genericPassword123";
    private static final String CITIZEN_USERGROUP = "citizens";
    private static final String CASEWORKER_USERGROUP = "caseworker";

    protected static final String ERRORS = "errors";
    protected static final String DATA = "data";
    protected static final String CASE_DATA = "case_data";
    protected static final String CASE_DETAILS = "case_details";

    private UserDetails caseWorkerUser;

    @Value("${case.orchestration.service.base.uri}")
    protected String serverUrl;

    @Value("${http.proxy:#{null}}")
    protected String httpProxy;

    @Autowired
    protected IdamUtils idamTestSupportUtil;

    @Rule
    public SpringIntegrationMethodRule springMethodIntegration;

    protected IntegrationTest() {
        this.springMethodIntegration = new SpringIntegrationMethodRule();
    }

    @PostConstruct
    public void init() {
        if (!Strings.isNullOrEmpty(httpProxy)) {
            try {
                URL proxy = new URL(httpProxy);
                InetAddress.getByName(proxy.getHost()).isReachable(2000); // check proxy connectivity
                System.setProperty("http.proxyHost", proxy.getHost());
                System.setProperty("http.proxyPort", Integer.toString(proxy.getPort()));
                System.setProperty("https.proxyHost", proxy.getHost());
                System.setProperty("https.proxyPort", Integer.toString(proxy.getPort()));
            } catch (IOException e) {
                log.error("Error setting up proxy - are you connected to the VPN?", e);
                throw new RuntimeException("Error setting up proxy", e);
            }
        }
    }

    protected UserDetails createCaseWorkerUser() {
        synchronized (this) {
            if (caseWorkerUser == null) {
                caseWorkerUser = warpInRetry(() -> getUserDetails(
                    CASE_WORKER_USERNAME + UUID.randomUUID() + EMAIL_DOMAIN, CASE_WORKER_PASSWORD,
                    CASEWORKER_USERGROUP,
                    CASEWORKER_ROLE, CASEWORKER_DIVORCE_ROLE,
                    CASEWORKER_DIVORCE_COURTADMIN_ROLE, CASEWORKER_DIVORCE_COURTADMIN_BETA_ROLE
                ));
            }
            return caseWorkerUser;
        }
    }

    protected UserDetails createCitizenUser() {
        return warpInRetry(() -> {
            final String username = "simulate-delivered" + UUID.randomUUID() + "@notifications.service.gov.uk";
            return getUserDetails(username, PASSWORD, CITIZEN_USERGROUP, CITIZEN_ROLE);
        });
    }

    protected UserDetails createCitizenUser(String role) {
        return warpInRetry(() -> {
            final String username = "simulate-delivered" + UUID.randomUUID() + "@notifications.service.gov.uk";
            return getUserDetails(username, PASSWORD, CITIZEN_USERGROUP, role);
        });
    }

    private UserDetails getUserDetails(String username, String password, String userGroup, String... role) {
        synchronized (this) {
            idamTestSupportUtil.createUser(username, password, userGroup, role);

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

    private UserDetails warpInRetry(Supplier<UserDetails> supplier) {
        //tactical solution as sometimes the newly created user is somehow corrupted and won't generate a code..
        int count = 0;
        int maxTries = 5;
        while (true) {
            try {
                return supplier.get();
            } catch (Exception e) {
                if (++count == maxTries) {
                    log.error("Exhausted the number of maximum retry attempts..", e);
                    throw e;
                }
                try {
                    //some backoff time
                    Thread.sleep(200);
                } catch (InterruptedException ex) {
                    log.error("Error during sleep", ex);
                }
                log.trace("Encountered an error creating a user/token - retrying", e);
            }
        }
    }
}
