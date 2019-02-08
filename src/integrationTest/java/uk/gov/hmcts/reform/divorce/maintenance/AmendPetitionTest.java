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
import uk.gov.hmcts.reform.divorce.orchestration.client.CaseMaintenanceClient;
import uk.gov.hmcts.reform.divorce.support.CcdClientSupport;
import uk.gov.hmcts.reform.divorce.support.CcdSubmissionSupport;
import uk.gov.hmcts.reform.divorce.util.ResourceLoader;
import uk.gov.hmcts.reform.divorce.util.RestUtil;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D_8_CASE_REFERENCE;

public class AmendPetitionTest extends CcdSubmissionSupport {

    private static final String PREVIOUS_CASE_REF_KEY = "previousCaseId";
    private static final String PAYLOAD_CONTEXT_PATH = "fixtures/amend-petition/";
    private static final String TEST_AOS_STARTED_EVENT_ID = "testAosStarted";
    private static final String AOS_RECEIVED_NO_ADMIT_EVENT_ID = "aosReceivedNoAdConStarted";
    private static final String AMEND_PETITION_STATE = "AmendPetition";

    @Autowired
    private CcdClientSupport ccdClientSupport;

    @Autowired
    private CaseMaintenanceClient cmsClient;

    @Value("${case.orchestration.amend-petition.context-path}")
    private String contextPath;

    @Test
    public void givenValidCase_whenAmendPetition_newDraftPetitionIsReturned() {
        UserDetails citizenUser = createCitizenUser();

        CaseDetails issuedCase = ccdClientSupport.submitCase(
                ResourceLoader.loadJsonToObject(PAYLOAD_CONTEXT_PATH + "issued-case.json", Map.class),
                citizenUser
        );
        String caseId = issuedCase.getId().toString();

        updateCaseForCitizen(caseId, null, TEST_AOS_STARTED_EVENT_ID, citizenUser);
        updateCaseForCitizen(caseId, null, AOS_RECEIVED_NO_ADMIT_EVENT_ID, citizenUser);
        Response cosResponse = amendPetition(citizenUser.getAuthToken(), caseId);
        uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CaseDetails oldCase;
        oldCase = cmsClient.retrievePetitionById(citizenUser.getAuthToken(), caseId);
        String oldCaseRef = oldCase.getCaseData().get(D_8_CASE_REFERENCE).toString();

        assertEquals(HttpStatus.OK.value(), cosResponse.getStatusCode());
        assertEquals(oldCaseRef, cosResponse.path(PREVIOUS_CASE_REF_KEY));
        assertEquals(oldCase.getState(), AMEND_PETITION_STATE);
    }

    private Response amendPetition(String userToken, String caseId) {
        final Map<String, Object> headers = new HashMap<>();
        headers.put(HttpHeaders.CONTENT_TYPE, ContentType.APPLICATION_JSON.toString());

        if (userToken != null) {
            headers.put(HttpHeaders.AUTHORIZATION, userToken);
        }

        return RestUtil.putToRestService(
            serverUrl + contextPath + "/" + caseId,
            headers,
            null,
            new HashMap<>()
        );
    }
}
