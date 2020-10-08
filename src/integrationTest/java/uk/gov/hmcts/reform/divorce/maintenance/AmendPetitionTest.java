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
import uk.gov.hmcts.reform.divorce.model.idam.UserDetails;
import uk.gov.hmcts.reform.divorce.orchestration.client.CaseMaintenanceClient;
import uk.gov.hmcts.reform.divorce.support.CcdClientSupport;
import uk.gov.hmcts.reform.divorce.support.CcdSubmissionSupport;
import uk.gov.hmcts.reform.divorce.support.cos.CosApiClient;
import uk.gov.hmcts.reform.divorce.util.ResourceLoader;
import uk.gov.hmcts.reform.divorce.util.RestUtil;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.Collections.singletonList;
import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.startsWith;
import static org.hamcrest.MatcherAssert.assertThat;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CASE_ID_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D_8_PETITIONER_EMAIL;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D_8_REASON_FOR_DIVORCE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.ISSUE_DATE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.YES_VALUE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.courts.CourtConstants.REASON_FOR_DIVORCE_KEY;

public class AmendPetitionTest extends CcdSubmissionSupport {

    //Event that allows me to add the Issue Date to the case, since it's not updatable by the citizen user
    private static final String PAYMENT_REFERENCE_EVENT = "paymentReferenceGenerated";

    private static final String PREVIOUS_CASE_ID_KEY = "previousCaseId";
    private static final String PREVIOUS_ISSUE_DATE_KEY = "previousIssueDate";
    private static final String PREVIOUS_REASONS_KEY = "previousReasonsForDivorce";

    private static final String PAYLOAD_CONTEXT_PATH = "fixtures/amend-petition/";
    private static final String TEST_AOS_STARTED_EVENT_ID = "testAosStarted";
    private static final String AOS_RECEIVED_NO_ADMIT_EVENT_ID = "aosReceivedNoAdConStarted";
    private static final String AMEND_PETITION_STATE = "AmendPetition";
    private static final String REASON_FOR_DIVORCE_BEHAVIOUR_DETAILS = "reasonForDivorceBehaviourDetails";
    private static final String CLAIMS_COSTS = "claimsCosts";
    private static final String CONFIRM_PRAYER = "confirmPrayer";

    @Autowired
    private CcdClientSupport ccdClientSupport;

    @Autowired
    private CaseMaintenanceClient cmsClient;

    @Autowired
    private CosApiClient cosApiClient;

    @Value("${case.orchestration.amend-petition.context-path}")
    private String amendPetitionContextPath;

    @Test
    public void givenValidCase_whenAmendPetition_newDraftPetitionIsReturned() {
        UserDetails citizenUser = createCitizenUser();

        Map caseToIssue = ResourceLoader.loadJsonToObject(PAYLOAD_CONTEXT_PATH + "issued-case.json", Map.class);
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

        Response cosResponse = amendPetition(citizenUser.getAuthToken(), caseId);
        assertThat(cosResponse.getStatusCode(), is(HttpStatus.OK.value()));
        Map<String, Object> newDraftDocument = cosResponse.getBody().as(Map.class);

        //Assert fields added in transformation
        assertThat(newDraftDocument.get("court"), is(instanceOf(Map.class)));

        //Compare old case and new amended case
        uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CaseDetails oldCase;
        oldCase = cmsClient.retrievePetitionById(citizenUser.getAuthToken(), caseId);
        List<Object> previousReasons = (List<Object>) newDraftDocument.get(PREVIOUS_REASONS_KEY);
        assertThat(previousReasons, hasItem(oldCase.getCaseData().get(D_8_REASON_FOR_DIVORCE)));
        assertThat(oldCase.getCaseId(), equalTo(newDraftDocument.get(PREVIOUS_CASE_ID_KEY)));
        assertThat((String) newDraftDocument.get(PREVIOUS_ISSUE_DATE_KEY), allOf(
            is(notNullValue()),
            startsWith(testIssueDate)
        ));
        assertThat(oldCase.getState(), equalTo(AMEND_PETITION_STATE));

        //Fill in mandatory data that's removed from original case
        newDraftDocument.put(REASON_FOR_DIVORCE_KEY, "unreasonable-behaviour");
        newDraftDocument.put(REASON_FOR_DIVORCE_BEHAVIOUR_DETAILS, singletonList("my partner did unreasonable things"));
        newDraftDocument.put(CLAIMS_COSTS, YES_VALUE);
        newDraftDocument.put(CONFIRM_PRAYER, YES_VALUE);

        //Submit amended case
        Map<String, Object> submittedCase = cosApiClient.submitCase(citizenUser.getAuthToken(), newDraftDocument);
        assertThat(submittedCase.get(CASE_ID_JSON_KEY), is(notNullValue()));
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