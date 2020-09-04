package uk.gov.hmcts.reform.divorce.callback;

import io.restassured.response.Response;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.http.entity.ContentType;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.divorce.model.PinResponse;
import uk.gov.hmcts.reform.divorce.model.UserDetails;
import uk.gov.hmcts.reform.divorce.support.RetrieveAosCaseSupport;
import uk.gov.hmcts.reform.divorce.util.RestUtil;

import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D8_RESPONDENT_SOLICITOR_EMAIL;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D8_RESPONDENT_SOLICITOR_NAME;

public class SolicitorLinkCaseCallbackTest extends RetrieveAosCaseSupport {

    private static final String PIN_USER_FIRST_NAME = "pinuserfirstname";
    private static final String PIN_USER_LAST_NAME = "pinuserfirstname";
    private static final String TEST_AOS_STARTED_EVENT_ID = "testAosStarted";
    private static final String AOS_NOMINATE_SOL_EVENT_ID = "aosNominateSol";
    private static final String AOS_LETTER_HOLDER_ID = "AosLetterHolderId";
    private static final String RESPONDENT_SOLICITOR_CASE_NO = "RespondentSolicitorCaseNo";
    private static final String RESPONDENT_SOLICITOR_PIN = "RespondentSolicitorPin";
    private static final String CASE_REFERENCE = "CaseReference";
    private static final String AMEND_CASE = "amendCase";
    private static final String TEST_SOLICITOR_EMAIL = "testsolicitor@mailinator.com";
    private static final String TEST_SOLICITOR_NAME = "sol name";


    @Value("${case.orchestration.solicitor.solicitor-link-case.context-path}")
    private String contextPath;

    @Test
    public void givenAosAwaitingState_whenSolicitorLinksCase_thenCaseShouldBeLinked() {

        final UserDetails petitionerUserDetails = createCitizenUser();
        final PinResponse pinResponse = idamTestSupportUtil.generatePin(
                PIN_USER_FIRST_NAME,
                PIN_USER_LAST_NAME,
                petitionerUserDetails.getAuthToken()
        );

        CaseDetails caseDetails = submitCase(
                "submit-unlinked-case.json",
                petitionerUserDetails
        );

        String caseId = String.valueOf(caseDetails.getId());
        updateCaseForCitizen(caseId, null, TEST_AOS_STARTED_EVENT_ID, petitionerUserDetails);
        updateCaseForCitizen(caseId, null, AOS_NOMINATE_SOL_EVENT_ID, petitionerUserDetails,
                ImmutablePair.of(D8_RESPONDENT_SOLICITOR_NAME, TEST_SOLICITOR_NAME),
                ImmutablePair.of(D8_RESPONDENT_SOLICITOR_EMAIL, TEST_SOLICITOR_EMAIL)
        );
        updateCase(caseId, null, AMEND_CASE,
                ImmutablePair.of(AOS_LETTER_HOLDER_ID, pinResponse.getUserId())
        );

        UserDetails solicitorUser = createSolicitorUser();

        Response linkResponse = linkSolicitor(
                        solicitorUser.getAuthToken(),
                        caseDetails.getId(),
                        pinResponse.getPin()
        );

        String responseString = linkResponse.getBody().asString();
        assertThat(responseString, linkResponse.getStatusCode(), is(HttpStatus.OK.value()));
        assertThat(responseString,linkResponse.getBody().jsonPath().get(DATA), is(notNullValue()));
        caseDetails = ccdClientSupport.retrieveCaseForCaseworker(solicitorUser, caseId);
        assertThat(caseDetails.getData(), is(notNullValue()));

        //linking with a different user should fail
        solicitorUser = createSolicitorUser();

        linkResponse = linkSolicitor(
                solicitorUser.getAuthToken(),
                caseDetails.getId(),
                pinResponse.getPin()
        );
        responseString = linkResponse.getBody().asString();
        assertThat(responseString, linkResponse.getStatusCode(), is(HttpStatus.OK.value()));
        assertThat(responseString,linkResponse.getBody().jsonPath().getList(ERRORS), hasItem("Case is already linked to case with ID: " + caseId));
    }

    private Response linkSolicitor(String userToken, Long caseId, String pin) {
        final Map<String, Object> headers = new HashMap<>();
        headers.put(HttpHeaders.CONTENT_TYPE, ContentType.APPLICATION_JSON.toString());
        headers.put(HttpHeaders.AUTHORIZATION, userToken);

        HashMap<String, Object> caseData = new HashMap<>();
        HashMap<String, Object> respondentSolicitorCaseLink = new HashMap<>();
        respondentSolicitorCaseLink.put(CASE_REFERENCE, String.valueOf(caseId));
        caseData.put(RESPONDENT_SOLICITOR_CASE_NO, respondentSolicitorCaseLink);
        caseData.put(RESPONDENT_SOLICITOR_PIN, pin);

        HashMap<String, Object> caseDetails = new HashMap<>();
        caseDetails.put(CASE_DATA, caseData);

        HashMap<String, Object> payload = new HashMap<>();
        payload.put(CASE_DETAILS, caseDetails);

        return RestUtil.postToRestService(serverUrl + contextPath, headers, payload);
    }
}
