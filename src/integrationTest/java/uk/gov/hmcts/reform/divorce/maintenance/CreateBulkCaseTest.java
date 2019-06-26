package uk.gov.hmcts.reform.divorce.maintenance;

import io.restassured.response.Response;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.entity.ContentType;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import uk.gov.hmcts.reform.divorce.model.UserDetails;
import uk.gov.hmcts.reform.divorce.support.CcdSubmissionSupport;
import uk.gov.hmcts.reform.divorce.util.RestUtil;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.core.IsNot.not;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.BulkCaseConstants.BULK_CASE_LIST_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.BULK_LISTING_CASE_ID_FIELD;


@Slf4j
public class CreateBulkCaseTest extends CcdSubmissionSupport {

    private static final String  BULK_CASE_PATH = "/bulk/case";

    private static final int  MAX_WAITING_TIME_IN_SECONDS = 30;
    private static final int  POOL_INTERVAL_IN_MILLIS = 500;

    @Test
    //QA asked to disable this test, to able to test scheduler
    @Ignore
    public void whenCreateBulkCase_CaseIsCreated() throws Exception {
        final UserDetails user1 = createCitizenUser();
        final UserDetails user2 = createCitizenUser();

        createAwaitingPronouncementCase(user1);
        createAwaitingPronouncementCase(user2);

        Response response  = createBulkCase();
        response.then()
            .assertThat()
            .statusCode(is(HttpStatus.OK.value()))
            .body(BULK_CASE_LIST_KEY, is(not(empty())));

        List<String> casesList = response.jsonPath().get("BulkCases.case_data.CaseAcceptedList[0].value.CaseReference");
        final UserDetails caseworker = createCaseWorkerUser();
        casesList.forEach(caseId -> validateWithAwaitingTime(caseworker, caseId));
    }

    private void validateWithAwaitingTime(UserDetails user, String caseId) {
        await().pollInterval(POOL_INTERVAL_IN_MILLIS, MILLISECONDS)
            .atMost(MAX_WAITING_TIME_IN_SECONDS, SECONDS)
            .untilAsserted(() -> assertThat(retrieveCaseForCaseworker(user, caseId).getData().get(BULK_LISTING_CASE_ID_FIELD)).isNotNull());
    }

    private Response createBulkCase() throws Exception {
        final Map<String, Object> headers = new HashMap<>();
        headers.put(HttpHeaders.CONTENT_TYPE, ContentType.APPLICATION_JSON.toString());


        return RestUtil.postToRestService(
            serverUrl + BULK_CASE_PATH,
            headers,
            null
        );
    }
}