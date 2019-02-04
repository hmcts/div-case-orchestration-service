package uk.gov.hmcts.reform.divorce.maintenance;

import io.restassured.response.Response;
import org.apache.http.entity.ContentType;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.divorce.model.UserDetails;
import uk.gov.hmcts.reform.divorce.support.CcdClientSupport;
import uk.gov.hmcts.reform.divorce.support.CcdSubmissionSupport;
import uk.gov.hmcts.reform.divorce.util.ResourceLoader;
import uk.gov.hmcts.reform.divorce.util.RestUtil;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;

public class AmendPetitionTest extends CcdSubmissionSupport {

    private static final String PREVIOUS_CASE_ID_KEY = "previousCaseId";
    private static final String PAYLOAD_CONTEXT_PATH = "fixtures/amend-petition/";
    private static final String TEST_AOS_STARTED_EVENT_ID = "testAosStarted";

    @Autowired
    private CcdClientSupport ccdClientSupport;

    @Value("${case.orchestration.maintenance.amend-petition.context-path}")
    private String contextPath;

    @Value("${case.orchestration.maintenance.submit-dn.context-path}")
    private String dnContextPath;

    @Test
    public void givenValidCase_whenAmendPetition_newDraftPetitionIsReturned() throws Exception {
        UserDetails citizenUser = createCitizenUser();

        CaseDetails issuedCase = ccdClientSupport.submitCase(
                ResourceLoader.loadJsonToObject(PAYLOAD_CONTEXT_PATH + "issued-case.json", Map.class),
                citizenUser
        );

        // testAwaitingDecreeNisi
        // dnReceived
        // refertoLegalAdvisor
        // dnRefused (AwaitingConsideration)
        String caseId = issuedCase.getId().toString();
        final CaseDetails caseDetails = submitCase("submit-complete-case.json", citizenUser);

        updateCaseForCitizen(caseId, null, TEST_AOS_STARTED_EVENT_ID, citizenUser);
        updateCaseForCitizen(caseId, null, "aosSubmittedUndefended", citizenUser);

        submitDnCase(citizenUser.getAuthToken(), issuedCase.getId(),
            "dn-submit.json", dnContextPath);

        updateCase(caseId, null, "refertoLegalAdvisor");
        updateCase(caseId, null, "dnRefused");

        Response cosResponse = amendPetition(citizenUser.getAuthToken(), issuedCase.getId());

        assertEquals(HttpStatus.OK.value(), cosResponse.getStatusCode());
        assertEquals(caseId, cosResponse.path(PREVIOUS_CASE_ID_KEY));
    }

    private Response amendPetition(String userToken, Long caseId) {
        final Map<String, Object> headers = new HashMap<>();
        headers.put(HttpHeaders.CONTENT_TYPE, ContentType.APPLICATION_JSON.toString());

        if (userToken != null) {
            headers.put(HttpHeaders.AUTHORIZATION, userToken);
        }

        return RestUtil.putToRestService(
            serverUrl + contextPath + "/" + caseId ,
            headers,
            null,
            null
        );
    }
}
