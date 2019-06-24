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
import uk.gov.hmcts.reform.divorce.util.ResourceLoader;
import uk.gov.hmcts.reform.divorce.util.RestUtil;

import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

public class SolicitorLinkCaseCallbackTest extends RetrieveAosCaseSupport {

    private static final String PIN_USER_FIRST_NAME = "pinuserfirstname";
    private static final String PIN_USER_LAST_NAME = "pinuserfirstname";
    private static final String PAYMENT_REFERENCE_EVENT = "paymentReferenceGenerated";
    private static final String AOS_LETTER_HOLDER_ID = "AosLetterHolderId";
    private static final String RESPONDENT_SOLICITOR_CASE_NO = "RespondentSolicitorCaseNo";
    private static final String RESPONDENT_SOLICITOR_PIN = "RespondentSolicitorPin";
    private static final String CASE_REFERENCE = "CaseReference";


    @Value("${case.orchestration.solicitor.solicitor-link-case.context-path}")
    private String contextPath;

    @Test
    public void givenAosAwaitingState_whenSolicitorLinksCase_thenCaseShouldBeLinked() throws Exception {
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

        updateCase(String.valueOf(caseDetails.getId()),
                null,
                PAYMENT_REFERENCE_EVENT,
                ImmutablePair.of(AOS_LETTER_HOLDER_ID, pinResponse.getUserId())
        );

        final UserDetails solicitorUser = createSolicitorUser();

        Response linkResponse = linkSolicitor(
                        solicitorUser.getAuthToken(),
                        caseDetails.getId(),
                        pinResponse.getPin()
        );

        assertThat(linkResponse.getBody().asString(), linkResponse.getStatusCode(), is(HttpStatus.OK.value()));
        assertThat(linkResponse.getBody().asString(),linkResponse.getBody().jsonPath().get("data"), is(notNullValue()));
        caseDetails = ccdClientSupport.retrieveCaseForCaseworker(solicitorUser, String.valueOf(caseDetails.getId()));
        assertThat(caseDetails.getData(), is(notNullValue()));
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

        return RestUtil.postToRestService(serverUrl + contextPath, headers, ResourceLoader.objectToJson(payload));
    }
}
