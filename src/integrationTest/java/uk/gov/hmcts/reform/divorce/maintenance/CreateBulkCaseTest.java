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
import java.util.Map;

import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.core.IsNot.not;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.springframework.http.HttpStatus.OK;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.BulkCaseConstants.BULK_CASE_LIST_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.BULK_LISTING_CASE_ID_FIELD;
import static uk.gov.hmcts.reform.divorce.util.ResourceLoader.loadJson;

@Slf4j
public class CreateBulkCaseTest extends CcdSubmissionSupport {


    private static final String PAYLOAD_CONTEXT_PATH = "fixtures/maintenance/submit-dn/";
    private static final String TEST_AOS_STARTED_EVENT_ID = "testAosStarted";
    private static final String  BULK_CASE_PATH = "/bulk/case";

    private static final int  WAITING_TIME_IN_MILLIS = 2000;

    @Value("${case.orchestration.maintenance.submit.context-path}")
    private String caseCreationContextPath;

    @Value("${case.orchestration.maintenance.submit-dn.context-path}")
    private String contextPath;

    @Test
    public void whenCreateBulkCase_CaseIsCreated() throws Exception {
        final UserDetails user1 = createCitizenUser();
        final UserDetails user2 = createCitizenUser();

        CaseDetails case1 = createAwaitingPronouncementCase(user1);
        CaseDetails case2 = createAwaitingPronouncementCase(user2);
        waitToProcess();

        createBulkCase()
            .then()
            .assertThat()
            .statusCode(is(HttpStatus.OK.value()))
            .body(BULK_CASE_LIST_KEY, is(not(empty())));

        waitToProcess();

        CaseDetails case1updated = retrieveCase(user1, case1.getId().toString());
        CaseDetails case2updated = retrieveCase(user2, case2.getId().toString());

        assertThat(case1updated.getData().get(BULK_LISTING_CASE_ID_FIELD), is(notNullValue()));
        assertThat(case2updated.getData().get(BULK_LISTING_CASE_ID_FIELD), is(notNullValue()));

    }

    private void waitToProcess() {
        try {
            Thread.sleep(WAITING_TIME_IN_MILLIS);
        } catch (InterruptedException e) {
            log.info("Error  waiting", e);
        }
    }

    private CaseDetails createAwaitingPronouncementCase(UserDetails userDetails) throws Exception {

        final CaseDetails caseDetails = submitCase("submit-complete-case.json", userDetails);

        updateCaseForCitizen(String.valueOf(caseDetails.getId()), null, TEST_AOS_STARTED_EVENT_ID, userDetails);
        updateCaseForCitizen(String.valueOf(caseDetails.getId()), null, "aosSubmittedUndefended", userDetails);


        Response cosResponse = submitDnCase(userDetails.getAuthToken(), caseDetails.getId(),
            "dn-submit.json");
        assertEquals(OK.value(), cosResponse.getStatusCode());

        assertEquals(caseDetails.getId(), cosResponse.path("id"));
        updateCase(String.valueOf(caseDetails.getId()), null, "refertoLegalAdvisor");
        CaseDetails ccdCase = updateCase(String.valueOf(caseDetails.getId()), null, "entitlementGranted");

        System.out.println(caseDetails.getId()) ;
        return ccdCase;
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
