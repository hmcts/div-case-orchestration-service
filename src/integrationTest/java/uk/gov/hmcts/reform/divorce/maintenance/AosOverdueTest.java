package uk.gov.hmcts.reform.divorce.maintenance;

import io.restassured.RestAssured;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpStatus;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.divorce.model.idam.UserDetails;
import uk.gov.hmcts.reform.divorce.support.cos.RetrieveCaseSupport;
import uk.gov.hmcts.reform.divorce.util.ElasticSearchTestHelper;

import static java.lang.String.format;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.awaitility.Awaitility.await;
import static org.awaitility.pollinterval.FibonacciPollInterval.fibonacci;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.CcdStates.AOS_AWAITING;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.CcdStates.AOS_OVERDUE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.CcdStates.AOS_STARTED;

@Slf4j
public class AosOverdueTest extends RetrieveCaseSupport {

    @Value("${case.orchestration.jobScheduler.make-case-overdue-for-aos.context-path}")
    private String jobSchedulerContextPath;

    private static final String SUBMIT_COMPLETE_CASE_JSON_FILE_PATH = "submit-complete-case.json";
    private static final String TEST_AOS_AWAITING_EVENT_ID = "testAosAwaiting";
    private static final String TEST_AOS_STARTED_EVENT_ID = "testAosStarted";

    private String aosAwaitingCaseId;
    private String aosStartedCaseId;

    private UserDetails citizenUser;

    @Autowired
    private ElasticSearchTestHelper elasticSearchTestHelper;

    @Before
    public void setUp() {
        citizenUser = createCitizenUser();

        aosAwaitingCaseId = createCaseAndTriggerGivenEvent(TEST_AOS_AWAITING_EVENT_ID);
        aosStartedCaseId = createCaseAndTriggerGivenEvent(TEST_AOS_STARTED_EVENT_ID);

        elasticSearchTestHelper.ensureCaseIsSearchable(aosAwaitingCaseId, citizenUser.getAuthToken(), AOS_AWAITING);
        elasticSearchTestHelper.ensureCaseIsSearchable(aosStartedCaseId, citizenUser.getAuthToken(), AOS_STARTED);
    }

    private String createCaseAndTriggerGivenEvent(String eventId) {
        final CaseDetails caseDetails = submitCase(SUBMIT_COMPLETE_CASE_JSON_FILE_PATH, citizenUser);
        String caseId = String.valueOf(caseDetails.getId());
        log.debug("Created case id {}", caseId);
        updateCaseForCitizen(caseId, null, eventId, citizenUser);

        return caseId;
    }

    @Test
    public void shouldMoveEligibleCasesToAosOverdue() {
        UserDetails caseworker = createCaseWorkerUser();
        RestAssured
            .given()
            .header(HttpHeaders.AUTHORIZATION, caseworker.getAuthToken())
            .when()
            .post(serverUrl + jobSchedulerContextPath)
            .then()
            .statusCode(HttpStatus.SC_OK);

        await().pollInterval(fibonacci(SECONDS)).atMost(120, SECONDS).untilAsserted(() -> {
            assertCaseIsInExpectedState(aosAwaitingCaseId, AOS_OVERDUE);
            assertCaseIsInExpectedState(aosStartedCaseId, AOS_STARTED);
        });
    }

    private void assertCaseIsInExpectedState(String caseId, String expectedState) {
        CaseDetails caseDetails = retrieveCase(citizenUser, caseId);
        String state = caseDetails.getState();
        assertThat(format("Case %s should be in \"%s\" state", caseId, expectedState), state, is(expectedState));
    }

}