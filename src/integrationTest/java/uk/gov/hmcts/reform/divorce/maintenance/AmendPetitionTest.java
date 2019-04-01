package uk.gov.hmcts.reform.divorce.maintenance;

import io.restassured.response.Response;
import org.apache.commons.lang3.tuple.ImmutablePair;
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
import java.util.List;
import java.util.Map;

import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D_8_PETITIONER_EMAIL;

public class AmendPetitionTest extends CcdSubmissionSupport {

    //Event that allows me to add the Issue Date to the case, since it's not updatable by the citizen user
    private static final String PAYMENT_REFERENCE_EVENT = "paymentReferenceGenerated";

    private static final String PREVIOUS_CASE_ID_KEY = "previousCaseId";
    private static final String PREVIOUS_ISSUE_DATE_KEY = "previousIssueDate";
    private static final String PREVIOUS_REASONS_KEY = "previousReasonsForDivorce";

    private static final String ISSUE_DATE = "IssueDate";
    private static final String D8_REASON_DIVORCE = "D8ReasonForDivorce";
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

        Map<String, Object> caseToIssue = ResourceLoader.loadJsonToObject(PAYLOAD_CONTEXT_PATH + "issued-case.json", Map.class);
        caseToIssue.put(D_8_PETITIONER_EMAIL, citizenUser.getEmailAddress());
        CaseDetails issuedCase = ccdClientSupport.submitCase(
            caseToIssue,
            citizenUser
        );
        String caseId = issuedCase.getId().toString();

        updateCase(caseId,
            null,
            PAYMENT_REFERENCE_EVENT,
            ImmutablePair.of(ISSUE_DATE, "2018-06-08"));

        updateCaseForCitizen(caseId, null, TEST_AOS_STARTED_EVENT_ID, citizenUser);
        updateCaseForCitizen(caseId, null, AOS_RECEIVED_NO_ADMIT_EVENT_ID, citizenUser);

        Response cosResponse = amendPetition(citizenUser.getAuthToken(), caseId);
        uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CaseDetails oldCase;
        oldCase = cmsClient.retrievePetitionById(citizenUser.getAuthToken(), caseId);

        assertEquals(HttpStatus.OK.value(), cosResponse.getStatusCode());

        Map<String, Object> newDraftDocument = cosResponse.getBody().as(Map.class);
        assertThat(newDraftDocument.get(PREVIOUS_ISSUE_DATE_KEY), allOf(
            is(notNullValue()),
            equalTo("2018-06-08T00:00:00.000+0000")
        ));

        List<String> previousReasons = (List<String>) newDraftDocument.get(PREVIOUS_REASONS_KEY);
        assertTrue(previousReasons.contains(oldCase.getCaseData().get(D8_REASON_DIVORCE)));
        assertEquals(oldCase.getCaseId(), cosResponse.path(PREVIOUS_CASE_ID_KEY).toString());
        assertEquals(oldCase.getState(), AMEND_PETITION_STATE);
    }

    @Test
    public void givenNoCaseForUser_whenAmendPetition_thenReturn404() {
        UserDetails citizenUser = createCitizenUser();

        String caseId = "111111";

        Response cosResponse = amendPetition(citizenUser.getAuthToken(), caseId);
        assertEquals(HttpStatus.NOT_FOUND.value(), cosResponse.getStatusCode());
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
