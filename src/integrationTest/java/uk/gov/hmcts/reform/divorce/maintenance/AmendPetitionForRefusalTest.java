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
import uk.gov.hmcts.reform.divorce.support.cos.CosApiClient;
import uk.gov.hmcts.reform.divorce.util.ResourceLoader;
import uk.gov.hmcts.reform.divorce.util.RestUtil;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.startsWith;
import static org.junit.Assert.assertThat;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D_8_PETITIONER_EMAIL;

public class AmendPetitionForRefusalTest extends CcdSubmissionSupport {

    //Event that allows me to add the Issue Date to the case, since it's not updatable by the citizen user
    private static final String PAYMENT_REFERENCE_EVENT = "paymentReferenceGenerated";

    //Superuser event to move to correct state with expected data
    private static final String SUPERUSER_AWAITING_CLARIFICATION_EVENT = "SUAwaitingClarification";

    private static final String PREVIOUS_CASE_ID_KEY = "previousCaseId";
    private static final String PREVIOUS_ISSUE_DATE_KEY = "previousIssueDate";

    private static final String ISSUE_DATE = "IssueDate";
    private static final String REASONS_FOR_REFUSAL_REJECTION_KEY = "RefusalRejectionReason";
    private static final List<String> REASONS_FOR_REFUSAL_REJECTION_VALUES = Collections.singletonList("other");

    private static final String PAYLOAD_CONTEXT_PATH = "fixtures/amend-petition/";
    private static final String TEST_AOS_STARTED_EVENT_ID = "testAosStarted";
    private static final String AOS_RECEIVED_NO_ADMIT_EVENT_ID = "aosReceivedNoAdConStarted";
    private static final String AMEND_PETITION_STATE = "AmendPetition";

    @Autowired
    private CcdClientSupport ccdClientSupport;

    @Autowired
    private CaseMaintenanceClient cmsClient;

    @Autowired
    private CosApiClient cosApiClient;

    @Value("${case.orchestration.amend-petition-refusal.context-path}")
    private String amendPetitionContextPath;

    @Test
    public void givenValidCase_whenAmendPetitionForRefusalRejection_newDraftPetitionIsReturned() throws Exception {
        UserDetails citizenUser = createCitizenUser();

        Map<String, Object> caseToIssue = ResourceLoader.loadJsonToObject(PAYLOAD_CONTEXT_PATH + "issued-case.json", Map.class);
        caseToIssue.put(D_8_PETITIONER_EMAIL, citizenUser.getEmailAddress());
        CaseDetails issuedCase = ccdClientSupport.submitCase(
            caseToIssue,
            citizenUser
        );

        String caseId = issuedCase.getId().toString();

        String testIssueDate = "2018-06-08";
        updateCase(caseId, null, PAYMENT_REFERENCE_EVENT, ImmutablePair.of(ISSUE_DATE, testIssueDate));
        updateCaseForCitizen(caseId, null, TEST_AOS_STARTED_EVENT_ID, citizenUser);
        updateCaseForCitizen(caseId, null, AOS_RECEIVED_NO_ADMIT_EVENT_ID, citizenUser);
        submitDnCase(citizenUser.getAuthToken(), issuedCase.getId(), "dn-submit.json");
        updateCaseWithSuperuser(caseId, null, SUPERUSER_AWAITING_CLARIFICATION_EVENT,
            ImmutablePair.of(REASONS_FOR_REFUSAL_REJECTION_KEY, REASONS_FOR_REFUSAL_REJECTION_VALUES));

        Response cosResponse = amendPetition(citizenUser.getAuthToken(), caseId);
        assertThat(cosResponse.getStatusCode(), is(HttpStatus.OK.value()));
        Map<String, Object> newDraftDocument = cosResponse.getBody().as(Map.class);

        //Compare old case and new amended case
        uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CaseDetails oldCase;
        oldCase = cmsClient.retrievePetitionById(citizenUser.getAuthToken(), caseId);
        assertThat(oldCase.getCaseId(), equalTo(newDraftDocument.get(PREVIOUS_CASE_ID_KEY)));
        assertThat((String) newDraftDocument.get(PREVIOUS_ISSUE_DATE_KEY), allOf(
            is(notNullValue()),
            startsWith(testIssueDate)
        ));
        assertThat(oldCase.getState(), equalTo(AMEND_PETITION_STATE));
    }

    private Response amendPetition(String userToken, String caseId) {
        final Map<String, Object> headers = new HashMap<>();
        headers.put(HttpHeaders.CONTENT_TYPE, ContentType.APPLICATION_JSON.toString());

        if (userToken != null) {
            headers.put(HttpHeaders.AUTHORIZATION, userToken);
        }

        return RestUtil.putToRestService(
            serverUrl + amendPetitionContextPath + "/" + caseId,
            headers,
            null,
            new HashMap<>()
        );
    }

}