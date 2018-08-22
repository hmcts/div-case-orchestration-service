package uk.gov.hmcts.reform.divorce.context;

import net.serenitybdd.junit.spring.integration.SpringIntegrationMethodRule;
import org.junit.Rule;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.hmcts.reform.divorce.model.UserDetails;
import uk.gov.hmcts.reform.divorce.util.IdamUtils;

@RunWith(SpringRunner.class)
@ContextConfiguration(classes = {ServiceContextConfiguration.class})
public abstract class IntegrationTest {
    private static final String CASE_WORKER_USERNAME = "robreallywantsccdaccess@mailinator.com";
    private static final String CASE_WORKER_PASSWORD = "Passw0rd";

    private UserDetails userDetails;

    @Value("${case.orchestration.service.base.uri}")
    protected String serverUrl;

    @Autowired
    private IdamUtils idamTestSupportUtil;

    @Rule
    public SpringIntegrationMethodRule springMethodIntegration;

    protected IntegrationTest() {
        this.springMethodIntegration = new SpringIntegrationMethodRule();
    }

    protected synchronized UserDetails getUserDetails() {
        if (userDetails == null) {
            final String username = CASE_WORKER_USERNAME;
            final String password = CASE_WORKER_PASSWORD;

            idamTestSupportUtil.createDivorceCaseworkerUserInIdam(username, password);
            final String authToken = idamTestSupportUtil.generateUserTokenWithNoRoles(username, password);

            final String userId = idamTestSupportUtil.getUserId(authToken);

            userDetails = UserDetails.builder()
                .username(username)
                .emailAddress(username)
                .password(password)
                .authToken(authToken)
                .id(userId)
                .build();
        }

        return userDetails;
    }
}
