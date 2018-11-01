package uk.gov.hmcts.reform.divorce.maintenance;

import io.restassured.response.Response;
import org.apache.http.entity.ContentType;
import org.joda.time.LocalDate;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.divorce.model.UserDetails;
import uk.gov.hmcts.reform.divorce.support.CcdSubmissionSupport;
import uk.gov.hmcts.reform.divorce.util.RestUtil;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.OK;
import static uk.gov.hmcts.reform.divorce.util.ResourceLoader.loadJson;

public class SubmitAosCaseTest extends CcdSubmissionSupport {
    private static final String PAYLOAD_CONTEXT_PATH = "fixtures/maintenance/submit-aos/";
    private static final String TEST_AOS_STARTED_EVENT_ID = "testAosStarted";
    private static final String AOS_RECEIVED_DATE = "2018-10-22";
    private static final String CCD_DATE_FORMAT = "yyyy-MM-dd";
    private static final String CCD_DUE_DATE = "dueDate";

    @Value("${case.orchestration.maintenance.submit-aos.context-path}")
    private String contextPath;

    @Value("${aos.responded.awaiting-answer.days-to-respond}")
    private int daysToRespond;

    @Test
    public void givenUserTokenIsNull_whenSubmitAos_thenReturnBadRequest() throws Exception {
        Response cosResponse = submitAosCase(null, 1L, "aos-defend-consent.json");

        assertEquals(BAD_REQUEST.value(), cosResponse.getStatusCode());
    }

    @Test
    public void givenNoCaseData_whenSubmitAos_thenReturnBadRequest() throws Exception {
        final UserDetails userDetails = createCitizenUser();

        Response cosResponse = submitAosCase(userDetails.getAuthToken(), 1L, null);

        assertEquals(BAD_REQUEST.value(), cosResponse.getStatusCode());
    }

    @Test
    public void givenConsentAndDefend_whenSubmitAos_thenProceedAsExpected() throws Exception {
        final UserDetails userDetails = createCitizenUser();

        CaseDetails caseDetails = submitCase("submit-complete-case.json", userDetails);

        updateCaseForCitizen(String.valueOf(caseDetails.getId()), null, TEST_AOS_STARTED_EVENT_ID, userDetails);

        Response cosResponse = submitAosCase(userDetails.getAuthToken(), caseDetails.getId(),
            "aos-defend-consent.json");

        assertEquals(OK.value(), cosResponse.getStatusCode());
        assertEquals(caseDetails.getId(), cosResponse.path("id"));
        assertEquals("AosSubmittedAwaitingAnswer", cosResponse.path("state"));
        assertDueDate(userDetails, String.valueOf(caseDetails.getId()), true);
    }

    @Test
    public void givenNoConsentAndDefend_whenSubmitAos_thenProceedAsExpected() throws Exception {
        final UserDetails userDetails = createCitizenUser();

        final CaseDetails caseDetails = submitCase("submit-complete-case.json", userDetails);

        updateCaseForCitizen(String.valueOf(caseDetails.getId()), null, TEST_AOS_STARTED_EVENT_ID, userDetails);

        Response cosResponse = submitAosCase(userDetails.getAuthToken(), caseDetails.getId(),
            "aos-defend-no-consent.json");

        assertEquals(OK.value(), cosResponse.getStatusCode());
        assertEquals(caseDetails.getId(), cosResponse.path("id"));
        assertEquals("AosSubmittedAwaitingAnswer", cosResponse.path("state"));
        assertDueDate(userDetails, String.valueOf(caseDetails.getId()), true);
    }

    @Test
    public void givenConsentAndNoDefend_whenSubmitAos_thenProceedAsExpected() throws Exception {
        final UserDetails userDetails = createCitizenUser();

        final CaseDetails caseDetails = submitCase("submit-complete-case.json", userDetails);

        updateCaseForCitizen(String.valueOf(caseDetails.getId()), null, TEST_AOS_STARTED_EVENT_ID, userDetails);

        Response cosResponse = submitAosCase(userDetails.getAuthToken(), caseDetails.getId(),
            "aos-no-defend-consent.json");

        assertEquals(OK.value(), cosResponse.getStatusCode());
        assertEquals(caseDetails.getId(), cosResponse.path("id"));
        assertEquals("AwaitingDecreeNisi", cosResponse.path("state"));
        assertDueDate(userDetails, String.valueOf(caseDetails.getId()), false);
    }

    @Test
    public void givenNoConsentAndNoDefend_whenSubmitAos_thenProceedAsExpected() throws Exception {
        final UserDetails userDetails = createCitizenUser();

        final CaseDetails caseDetails = submitCase("submit-complete-case.json", userDetails);

        updateCaseForCitizen(String.valueOf(caseDetails.getId()), null, TEST_AOS_STARTED_EVENT_ID, userDetails);

        Response cosResponse = submitAosCase(userDetails.getAuthToken(), caseDetails.getId(),
            "aos-no-defend-no-consent.json");

        assertEquals(OK.value(), cosResponse.getStatusCode());
        assertEquals(caseDetails.getId(), cosResponse.path("id"));
        assertEquals("AosCompleted", cosResponse.path("state"));
        assertDueDate(userDetails, String.valueOf(caseDetails.getId()), false);
    }

    private Response submitAosCase(String userToken, Long caseId, String filePath) throws Exception {
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

    private void assertDueDate(UserDetails userDetails, String caseId, boolean defended) {
        CaseDetails caseDetails = ccdClientSupport.retrieveCase(userDetails, caseId);

        if (defended) {
            String dueDate = new LocalDate(AOS_RECEIVED_DATE).plusDays(daysToRespond).toString(CCD_DATE_FORMAT);
            assertEquals(dueDate, caseDetails.getData().get(CCD_DUE_DATE));
        } else {
            assertNull(caseDetails.getData().get(CCD_DUE_DATE));
        }
    }
}
