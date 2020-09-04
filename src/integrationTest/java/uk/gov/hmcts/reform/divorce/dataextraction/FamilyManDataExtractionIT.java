package uk.gov.hmcts.reform.divorce.dataextraction;

import com.dumbster.smtp.SimpleSmtpServer;
import io.restassured.response.Response;
import lombok.extern.slf4j.Slf4j;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Value;
import uk.gov.hmcts.reform.divorce.support.cos.RetrieveCaseSupport;
import uk.gov.hmcts.reform.divorce.util.RestUtil;

import java.io.IOException;

import static java.util.Collections.emptyMap;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.http.HttpStatus.OK;

/**
 * This is going to be used to assert that the listener side of the data extraction job is called and runs successfully.
 */
@Slf4j
public class FamilyManDataExtractionIT extends RetrieveCaseSupport {

    private static final int LOCAL_SMTP_SERVER_PORT = 32773;
    private static final String LOCAL_ENVIRONMENT_NAME = "local";

    private static SimpleSmtpServer simpleSmtpServer;

    @Value("${env}")
    private String environment;

    @Value("${case.orchestration.jobScheduler.extract-data-to-family-man.context-path}")
    private String testDataExtractionEndPoint;

    @Before
    public void setUp() throws IOException {
        /*
         * Set up a local SMTP server when running this test locally. In the pipeline, we'll use the actual SMTP server.
         * The reason for this is because a firewall prevents us from connecting to the actual SMTP server from our local machines.
         */
        if (isTestRunningLocally()) {
            setUpLocalEmailServer();
            log.info("Starting local SMTP server");
        } else {
            log.info("Using actual SMTP server");
        }
    }

    /**
     * Please look into class-level comment if this test is not passing locally.
     */
    @Test
    public void shouldEmailCsvFiles() {
        Response response = RestUtil.postToRestService(
            serverUrl + testDataExtractionEndPoint,
            emptyMap(),
            null
        );

        assertThat(response.getStatusCode(), is(OK.value()));
        checkEmailHasBeenSent();
    }

    /**
     * Please look into class-level comment if this test is not passing locally.
     */
    @Test
    public void shouldEmailCsvFiles_ForGivenStatusAndDate() {
        Response response = RestUtil.postToRestService(
            serverUrl + testDataExtractionEndPoint + "/status/DA/lastModifiedDate/2019-10-01",
            emptyMap(),
            null
        );

        assertThat(response.getStatusCode(), is(OK.value()));
    }

    @AfterClass
    public static void cleanUp() {
        if (simpleSmtpServer != null) {
            simpleSmtpServer.close();
        }
    }

    private boolean isTestRunningLocally() {
        return LOCAL_ENVIRONMENT_NAME.equalsIgnoreCase(environment);
    }

    private static void setUpLocalEmailServer() throws IOException {
        if (simpleSmtpServer == null) {
            simpleSmtpServer = SimpleSmtpServer.start(LOCAL_SMTP_SERVER_PORT);
        }
    }

    private void checkEmailHasBeenSent() {
        if (isTestRunningLocally()) {
            assertThat(simpleSmtpServer.getReceivedEmails(), hasSize(greaterThan(0)));
            simpleSmtpServer.reset();
        }
    }

}