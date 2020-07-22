package uk.gov.hmcts.reform.divorce.maintenance;

import io.restassured.RestAssured;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpStatus;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Value;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.divorce.model.UserDetails;
import uk.gov.hmcts.reform.divorce.support.cos.RetrieveCaseSupport;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.awaitility.Awaitility.await;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.AOS_OVERDUE;

@Slf4j
public class AosOverdueTest extends RetrieveCaseSupport {

    @Value("${case.orchestration.jobScheduler.make-case-overdue-for-aos.context-path}")
    private String jobSchedulerContextPath;

    private static final String SUBMIT_COMPLETE_CASE_JSON_FILE_PATH = "submit-complete-case.json";
    private static final String TEST_AOS_AWAITING_EVENT_ID = "testAosAwaiting";

    private String caseId;
    private UserDetails citizenUser;

    @Before
    public void setUp() {
        citizenUser = createCitizenUser();

        final CaseDetails caseDetails = submitCase(SUBMIT_COMPLETE_CASE_JSON_FILE_PATH, citizenUser);
        caseId = String.valueOf(caseDetails.getId());
        log.warn("Test case id {}", caseId);//TODO - remove this before merging to master
        updateCaseForCitizen(caseId, null, TEST_AOS_AWAITING_EVENT_ID, citizenUser);//TODO - I might want to do that in the @after method as well
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

        await().atMost(30, SECONDS).untilAsserted(() -> { //TODO - check time I usually need to allow Elastic search
            CaseDetails caseDetails = retrieveCase(citizenUser, caseId);
            String state = caseDetails.getState();
            System.out.println("state is " + state);//TODO - remove
            assertThat(state, is(AOS_OVERDUE));
        });
    }

}