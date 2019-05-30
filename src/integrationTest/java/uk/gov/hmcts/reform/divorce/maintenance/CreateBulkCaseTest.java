package uk.gov.hmcts.reform.divorce.maintenance;

import io.restassured.response.Response;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.entity.ContentType;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
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
import static org.junit.Assert.assertEquals;
import static org.springframework.http.HttpStatus.OK;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.BulkCaseConstants.BULK_CASE_LIST_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.AWAITING_DN_AOS_EVENT_ID;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.BULK_LISTING_CASE_ID_FIELD;
import static uk.gov.hmcts.reform.divorce.util.ResourceLoader.loadJson;

@Slf4j
public class CreateBulkCaseTest extends CcdSubmissionSupport {


    private static final String PAYLOAD_CONTEXT_PATH = "fixtures/maintenance/submit-dn/";
    private static final String TEST_AOS_STARTED_EVENT_ID = "testAosStarted";
    private static final String  BULK_CASE_PATH = "/bulk/case";

    private static final int  MAX_WAITING_TIME_IN_SECONDS = 30;
    private static final int  POOL_INTERVAL_IN_MILLIS = 500;

    @Value("${case.orchestration.maintenance.submit.context-path}")
    private String caseCreationContextPath;

    @Value("${case.orchestration.maintenance.submit-dn.context-path}")
    private String contextPath;

    @Test
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
        casesList.forEach( caseId ->{
            validateWithAwaitingTime(caseworker, caseId);
        });
    }

    private void validateWithAwaitingTime(UserDetails user, String caseId) {
        await().pollInterval(POOL_INTERVAL_IN_MILLIS, MILLISECONDS)
                .atMost(MAX_WAITING_TIME_IN_SECONDS, SECONDS)
                .untilAsserted(() -> assertThat(retrieveCaseForCaseworker(user, caseId).getData().get(BULK_LISTING_CASE_ID_FIELD)).isNotNull());
    }

    private CaseDetails createAwaitingPronouncementCase(UserDetails userDetails) throws Exception {

        final CaseDetails caseDetails = submitCase("submit-complete-case.json", userDetails);

        updateCaseForCitizen(String.valueOf(caseDetails.getId()), null, TEST_AOS_STARTED_EVENT_ID, userDetails);
        updateCaseForCitizen(String.valueOf(caseDetails.getId()), null, AWAITING_DN_AOS_EVENT_ID, userDetails);

        Response cosResponse = submitDnCase(userDetails.getAuthToken(), caseDetails.getId(),
                "dn-submit.json");
        assertEquals(OK.value(), cosResponse.getStatusCode());

        assertEquals(caseDetails.getId(), cosResponse.path("id"));
        updateCase(String.valueOf(caseDetails.getId()), null, "refertoLegalAdvisor");
        return updateCase(String.valueOf(caseDetails.getId()), null, "entitlementGranted");
    }

    private Response submitDnCase(String userToken, Long caseId, String filePath) throws Exception {
        final Map<String, Object> headers = new HashMap<>();
        headers.put(HttpHeaders.CONTENT_TYPE, ContentType.APPLICATION_JSON.toString());

        if (userToken != null) {
            headers.put(HttpHeaders.AUTHORIZATION, userToken);
        }

        return RestUtil.postToRestService(
                serverUrl + contextPath + "/" + caseId,
                headers,
                filePath == null ? null : loadJson(PAYLOAD_CONTEXT_PATH + filePath)
        );
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
