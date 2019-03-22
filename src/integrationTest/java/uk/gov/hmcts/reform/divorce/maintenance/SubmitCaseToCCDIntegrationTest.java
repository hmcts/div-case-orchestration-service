package uk.gov.hmcts.reform.divorce.maintenance;

import io.restassured.response.Response;
import io.restassured.response.ResponseBody;
import org.apache.http.entity.ContentType;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import uk.gov.hmcts.reform.divorce.support.cos.RetrieveCaseSupport;
import uk.gov.hmcts.reform.divorce.util.ResourceLoader;
import uk.gov.hmcts.reform.divorce.util.RestUtil;

import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.core.AllOf.allOf;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNot.not;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.junit.Assert.assertThat;

public class SubmitCaseToCCDIntegrationTest extends RetrieveCaseSupport {

    private static final String CASE_ID_KEY = "caseId";
    private static final String PAYLOAD_CONTEXT_PATH = "fixtures/maintenance/submit/";
    private static final String ALLOCATED_COURT_ID_KEY = "allocatedCourt.courtId";

    @Value("${case.orchestration.maintenance.submit.context-path}")
    private String caseCreationContextPath;

    @Test
    public void givenDivorceSession_WithNoCourt_whenSubmitIsCalled_CaseIsCreated() throws Exception {
        String userToken = createCitizenUser().getAuthToken();
        Response submissionResponse = submitCase(userToken, "divorce-session-with-court-selected.json");

        ResponseBody caseCreationResponseBody = submissionResponse.getBody();
        assertThat(submissionResponse.getStatusCode(), is(HttpStatus.OK.value()));
        assertThat(caseCreationResponseBody.path(CASE_ID_KEY), is(not("0")));
        String allocatedCourt = caseCreationResponseBody.path(ALLOCATED_COURT_ID_KEY);
        assertThat(allocatedCourt, is(notNullValue()));

        ResponseBody retrieveCaseResponseBody = retrieveCase(userToken).body();
        assertThat(retrieveCaseResponseBody.path(RETRIEVED_DATA_COURT_ID_KEY), is(allocatedCourt));
    }

    @Test
    public void givenDivorceSession_WithCourt_whenSubmitIsCalled_CaseIsCreated_AndCourtIsIgnored() throws Exception {
        String userToken = createCitizenUser().getAuthToken();
        Response submissionResponse = submitCase(userToken, "basic-divorce-session.json");

        ResponseBody caseCreationResponseBody = submissionResponse.getBody();
        assertThat(submissionResponse.getStatusCode(), is(HttpStatus.OK.value()));
        assertThat(caseCreationResponseBody.path(CASE_ID_KEY), is(not("0")));
        String allocatedCourt = caseCreationResponseBody.path(ALLOCATED_COURT_ID_KEY);
        assertThat(allocatedCourt, allOf(
                is(notNullValue()),
                is(not("unknown-court"))
        ));

        ResponseBody retrieveCaseResponseBody = retrieveCase(userToken).body();
        assertThat(retrieveCaseResponseBody.path(RETRIEVED_DATA_COURT_ID_KEY), is(allocatedCourt));
    }

    private Response submitCase(String userToken, String fileName) throws Exception {
        final Map<String, Object> headers = new HashMap<>();
        headers.put(HttpHeaders.CONTENT_TYPE, ContentType.APPLICATION_JSON.toString());

        if (userToken != null) {
            headers.put(HttpHeaders.AUTHORIZATION, userToken);
        }

        return RestUtil.postToRestService(
                serverUrl + caseCreationContextPath,
                headers,
                fileName == null ? null : ResourceLoader.loadJson(PAYLOAD_CONTEXT_PATH + fileName)
        );
    }

}