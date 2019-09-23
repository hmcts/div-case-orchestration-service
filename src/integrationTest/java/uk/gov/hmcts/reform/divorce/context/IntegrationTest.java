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
import uk.gov.hmcts.reform.divorce.RetryRule;
import uk.gov.hmcts.reform.divorce.model.UserDetails;
import uk.gov.hmcts.reform.divorce.support.IdamUtils;

import java.io.IOException;
import java.net.InetAddress;
import java.net.URL;
import java.util.UUID;
import java.util.function.Supplier;
import javax.annotation.PostConstruct;

@Slf4j
@RunWith(SerenityRunner.class)
@ContextConfiguration(classes = {ServiceContextConfiguration.class})
public abstract class IntegrationTest {
    private static final String CASE_WORKER_USERNAME = "TEST_CASE_WORKER_USER";
    private static final String SOLICITOR_USER_NAME = "TEST_SOLICITOR";
    private static final String EMAIL_DOMAIN = "@mailinator.com";
    private static final String CITIZEN_ROLE = "citizen";
    private static final String PASSWORD = "genericPassword123";
    private static final String CITIZEN_USERGROUP = "citizens";

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

    @Rule
    public RetryRule retryRule;

    protected IntegrationTest() {
        this.springMethodIntegration = new SpringIntegrationMethodRule();
    }

    @PostConstruct
    public void init() {
        if (!Strings.isNullOrEmpty(httpProxy)) {
            try {
                URL proxy = new URL(httpProxy);
                if (!InetAddress.getByName(proxy.getHost()).isReachable(2000)) { // check proxy connectivity
                    throw new IOException("Could not reach proxy in timeout time");
                }
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
                caseWorkerUser = wrapInRetry(() -> getCreatedUserDetails(
                    CASE_WORKER_USERNAME +  EMAIL_DOMAIN
                ));
            }
            return caseWorkerUser;
        }
    }

    protected UserDetails createCitizenUser() {
        return createCitizenUser(CITIZEN_ROLE);
    }

    protected UserDetails createCitizenUser(String role) {
        return wrapInRetry(() -> {
            final String username = "simulate-delivered" + UUID.randomUUID() + EMAIL_DOMAIN;
            return getUserDetails(username, CITIZEN_USERGROUP, role);
        });
    }

    protected UserDetails createSolicitorUser() {
        return wrapInRetry(() -> {
            final String username = SOLICITOR_USER_NAME + EMAIL_DOMAIN;
            return getCreatedUserDetails(username);
        });
    }

    private UserDetails getUserDetails(String username, String userGroup, String... role) {
        synchronized (this) {
            idamTestSupportUtil.createUser(username, PASSWORD, userGroup, role);

            final String authToken = idamTestSupportUtil.generateUserTokenWithNoRoles(username, PASSWORD);

            final String userId = idamTestSupportUtil.getUserId(authToken);
            UserDetails userDetails = UserDetails.builder()
                .username(username)
                .emailAddress(username)
                .password(PASSWORD)
                .authToken(authToken)
                .id(userId)
                .build();
            return userDetails;
        }
    }

    private UserDetails getCreatedUserDetails(String username) {
        synchronized (this) {
            final String authToken = idamTestSupportUtil.generateUserTokenWithNoRoles(username, PASSWORD);

            final String userId = idamTestSupportUtil.getUserId(authToken);
            UserDetails userDetails = UserDetails.builder()
                .username(username)
                .emailAddress(username)
                .password(PASSWORD)
                .authToken(authToken)
                .id(userId)
                .build();
            return userDetails;
        }
    }

    private UserDetails wrapInRetry(Supplier<UserDetails> supplier) {
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
