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

import static junit.framework.TestCase.assertNull;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.RECEIVED_AOS_FROM_RESP;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.RECEIVED_AOS_FROM_RESP_DATE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.RESPONDENT_EMAIL_ADDRESS;

public class SolicitorLinkCaseCallbackTest extends RetrieveAosCaseSupport {

    private static final String PIN_USER_FIRST_NAME = "pinuserfirstname";
    private static final String PIN_USER_LAST_NAME = "pinuserfirstname";
    private static final String PAYMENT_REFERENCE_EVENT = "paymentReferenceGenerated";
    private static final String TEST_AOS_AWAITING_EVENT = "testAosAwaiting";
    private static final String AOS_LETTER_HOLDER_ID = "AosLetterHolderId";
    private static final String RESPONDENT_SOLICITOR_CASE_NO = "RespondentSolicitorCaseNo";
    private static final String RESPONDENT_SOLICITOR_PIN = "RespondentSolicitorPin";


    @Value("${case.orchestration.solicitor.solicitor-link-case.context-path}")
    private String contextPath;

    @Test
    public void givenAosAwaitingState_whenSolicitorLinksCase_thenCaseShouldBeLinked() throws Exception {
        final UserDetails petitionerUserDetails = createCitizenUser();

        final PinResponse pinResponse =
                idamTestSupportUtil.generatePin(PIN_USER_FIRST_NAME, PIN_USER_LAST_NAME,
                        petitionerUserDetails.getAuthToken());

        final CaseDetails caseDetails = submitCase(
                "submit-unlinked-case.json",
                petitionerUserDetails);

        updateCase(String.valueOf(caseDetails.getId()),
                null,
                PAYMENT_REFERENCE_EVENT,
                ImmutablePair.of(AOS_LETTER_HOLDER_ID, pinResponse.getUserId()));

        updateCaseForCitizen(String.valueOf(caseDetails.getId()),
                null,
                TEST_AOS_AWAITING_EVENT,
                petitionerUserDetails);

        final UserDetails solicitorUser = createSolicitorUser();

        Response linkResponse = linkSolicitor(
                        solicitorUser.getAuthToken(),
                        caseDetails.getId(),
                        pinResponse.getPin()
        );

        assertThat(linkResponse.getStatusCode(), is(HttpStatus.OK.value()));
        assertThat(linkResponse.getStatusCode(), is(HttpStatus.OK.value()));
        assertCaseDetailsRespondent(solicitorUser, String.valueOf(caseDetails.getId()));
    }

    private Response linkSolicitor(String userToken, Long caseId, String pin) {
        final Map<String, Object> headers = new HashMap<>();
        headers.put(HttpHeaders.CONTENT_TYPE, ContentType.APPLICATION_JSON.toString());

        if (userToken != null) {
            headers.put(HttpHeaders.AUTHORIZATION, userToken);
        }

        HashMap<String, Object> caseData = new HashMap<>();
        caseData.put(RESPONDENT_SOLICITOR_CASE_NO, caseId);
        caseData.put(RESPONDENT_SOLICITOR_PIN, pin);

        HashMap<String, Object> caseDetails = new HashMap<>();
        caseDetails.put(CASE_DATA, caseData);

        HashMap<String, Object> payload = new HashMap<>();
        payload.put(CASE_DETAILS, caseDetails);

        return RestUtil.postToRestService(
                serverUrl + contextPath,
                headers,
                ResourceLoader.objectToJson(payload)
        );
    }

    private void assertCaseDetailsRespondent(UserDetails userDetails, String caseId) {
        CaseDetails caseDetails = ccdClientSupport.retrieveCaseForCaseworker(userDetails, caseId);

        assertThat(caseDetails.getData().get(RESPONDENT_EMAIL_ADDRESS), is(userDetails.getEmailAddress()));
        assertNull(caseDetails.getData().get(RECEIVED_AOS_FROM_RESP));
        assertNull(caseDetails.getData().get(RECEIVED_AOS_FROM_RESP_DATE));
    }
}
