package uk.gov.hmcts.reform.divorce.maintenance;

import io.restassured.RestAssured;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpStatus;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Value;
import uk.gov.hmcts.reform.divorce.model.UserDetails;
import uk.gov.hmcts.reform.divorce.support.cos.RetrieveCaseSupport;

public class AosOverdueTest extends RetrieveCaseSupport {

    @Value("${case.orchestration.jobScheduler.make-case-eligible-for-da.context-path}")
    private String jobSchedulerContextPath;

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
    }

}