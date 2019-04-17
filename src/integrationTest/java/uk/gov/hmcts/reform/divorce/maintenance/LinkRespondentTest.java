package uk.gov.hmcts.reform.divorce.maintenance;

import io.restassured.response.Response;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.http.entity.ContentType;
import org.joda.time.LocalDate;
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

import static junit.framework.TestCase.assertNull;
import static org.junit.Assert.assertEquals;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CO_RESPONDENT_LETTER_HOLDER_ID;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CO_RESP_EMAIL_ADDRESS;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CO_RESP_LINKED_TO_CASE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CO_RESP_LINKED_TO_CASE_DATE;
import static uk.gov.hmcts.reform.divorce.util.DateConstants.CCD_DATE_FORMAT;

public class LinkRespondentTest extends RetrieveAosCaseSupport {
    private static final String PIN_USER_FIRST_NAME = "pinuserfirstname";
    private static final String PIN_USER_LAST_NAME = "pinuserfirstname";
    private static final String RESPONDENT_EMAIL_ADDRESS = "RespEmailAddress";
    private static final String RECEIVED_AOS_FROM_RESP = "ReceivedAOSfromResp";
    private static final String RECEIVED_AOS_FROM_RESP_DATE = "ReceivedAOSfromRespDate";
    private static final String YES_VALUE = "Yes";
    private static final String PAYMENT_REFERENCE_EVENT = "paymentReferenceGenerated";
    private static final String TEST_AOS_AWAITING_EVENT = "testAosAwaiting";
    private static final String AOS_LETTER_HOLDER_ID = "AosLetterHolderId";

    @Value("${case.orchestration.maintenance.link-respondent.context-path}")
    private String contextPath;

    @Test
    public void givenUserTokenIsNull_whenLinkRespondent_thenReturnBadRequest() {
        Response cosResponse = linkRespondent(null, 1L, "somepin");

        assertEquals(HttpStatus.BAD_REQUEST.value(), cosResponse.getStatusCode());
    }

    @Test
    public void givenInvalidPin_whenLinkRespondent_thenReturnUnAuthorised() {
        final UserDetails petitionerUserDetails = createCitizenUser();
        final CaseDetails caseDetails = submitCase(
            "submit-complete-case.json",
            petitionerUserDetails
        );

        Response cosResponse = linkRespondent(petitionerUserDetails.getAuthToken(),
            caseDetails.getId(), "abcd1234");

        assertEquals(HttpStatus.UNAUTHORIZED.value(), cosResponse.getStatusCode());
    }

    @Test
    public void givenCaseIdIsNotPresent_whenLinkRespondent_thenReturnUnauthorised() {
        final UserDetails petitionerUserDetails = createCitizenUser();

        final PinResponse pinResponse =
            idamTestSupportUtil.generatePin(PIN_USER_FIRST_NAME, PIN_USER_LAST_NAME,
                petitionerUserDetails.getAuthToken());

        Response cosResponse = linkRespondent(petitionerUserDetails.getAuthToken(), 1L, pinResponse.getPin());

        assertEquals(HttpStatus.UNAUTHORIZED.value(), cosResponse.getStatusCode());
    }

    @Test
    public void givenPinIdNotMatching_whenLinkRespondent_thenReturnUnauthorized() {
        final UserDetails petitionerUserDetails = createCitizenUser();

        final PinResponse pinResponse =
            idamTestSupportUtil.generatePin(PIN_USER_FIRST_NAME, PIN_USER_LAST_NAME,
                petitionerUserDetails.getAuthToken());

        final CaseDetails caseDetails = submitCase(
            "submit-complete-case.json",
            petitionerUserDetails
        );

        Response cosResponse =
            linkRespondent(petitionerUserDetails.getAuthToken(), caseDetails.getId(), pinResponse.getPin());

        assertEquals(HttpStatus.UNAUTHORIZED.value(), cosResponse.getStatusCode());
    }

    @Test
    public void givenAosAwaitingState_whenLinkRespondent_thenCaseShouldBeLinked() {
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

        final UserDetails respondentUserDetails = createCitizenUser();

        Response linkResponse =
            linkRespondent(
                respondentUserDetails.getAuthToken(),
                caseDetails.getId(),
                pinResponse.getPin()
            );

        assertEquals(HttpStatus.OK.value(), linkResponse.getStatusCode());

        Response caseResponse = retrieveAosCase(respondentUserDetails.getAuthToken());

        assertEquals(String.valueOf(caseDetails.getId()), caseResponse.path(CASE_ID_KEY));

        assertCaseDetailsRespondent(respondentUserDetails, String.valueOf(caseDetails.getId()));
    }

    @Test
    public void givenAosOverdueState_whenLinkRespondent_thenCaseShouldBeLinked() {
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

        updateCase(String.valueOf(caseDetails.getId()), null, "aosNotReceived");

        final UserDetails respondentUserDetails = createCitizenUser();
        Response linkResponse =
            linkRespondent(
                respondentUserDetails.getAuthToken(),
                caseDetails.getId(),
                pinResponse.getPin()
            );

        assertEquals(HttpStatus.OK.value(), linkResponse.getStatusCode());

        Response caseResponse = retrieveAosCase(respondentUserDetails.getAuthToken());

        assertEquals(String.valueOf(caseDetails.getId()), caseResponse.path(CASE_ID_KEY));

        assertCaseDetailsRespondent(respondentUserDetails, String.valueOf(caseDetails.getId()));
    }

    @Test
    public void givenValidCaseDetails_whenLinkCoRespondent_thenCaseShouldBeLinked() {
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
            ImmutablePair.of(CO_RESPONDENT_LETTER_HOLDER_ID, pinResponse.getUserId()));

        updateCaseForCitizen(String.valueOf(caseDetails.getId()),
            null,
            TEST_AOS_AWAITING_EVENT,
            petitionerUserDetails);

        final UserDetails coRespondentUserDetails = createCitizenUser();

        Response linkResponse =
            linkRespondent(
                coRespondentUserDetails.getAuthToken(),
                caseDetails.getId(),
                pinResponse.getPin()
            );

        assertEquals(HttpStatus.OK.value(), linkResponse.getStatusCode());

        Response caseResponse = retrieveAosCase(coRespondentUserDetails.getAuthToken());

        assertEquals(String.valueOf(caseDetails.getId()), caseResponse.path(CASE_ID_KEY));

        assertCaseDetailsCoRespondent(coRespondentUserDetails, String.valueOf(caseDetails.getId()));
    }

    @Test
    public void givenLinkedCase_whenLinkCoRespondent_thenCaseShouldBeLinked() {
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
            ImmutablePair.of(CO_RESPONDENT_LETTER_HOLDER_ID, pinResponse.getUserId()));

        updateCaseForCitizen(String.valueOf(caseDetails.getId()),
            null,
            TEST_AOS_AWAITING_EVENT,
            petitionerUserDetails);

        final UserDetails coRespondentUserDetails = createCitizenUser();

        Response linkResponse =
            linkRespondent(
                coRespondentUserDetails.getAuthToken(),
                caseDetails.getId(),
                pinResponse.getPin()
            );

        assertEquals(HttpStatus.OK.value(), linkResponse.getStatusCode());

        Response caseResponse = retrieveAosCase(coRespondentUserDetails.getAuthToken());

        assertEquals(String.valueOf(caseDetails.getId()), caseResponse.path(CASE_ID_KEY));

        assertCaseDetailsCoRespondent(coRespondentUserDetails, String.valueOf(caseDetails.getId()));

        linkResponse =
            linkRespondent(
                coRespondentUserDetails.getAuthToken(),
                caseDetails.getId(),
                pinResponse.getPin()
            );

        assertEquals(HttpStatus.OK.value(), linkResponse.getStatusCode());

    }

    private void assertCaseDetailsRespondent(UserDetails userDetails, String caseId) {
        CaseDetails caseDetails = ccdClientSupport.retrieveCase(userDetails, caseId);

        assertEquals(userDetails.getEmailAddress(), caseDetails.getData().get(RESPONDENT_EMAIL_ADDRESS));
        assertNull(caseDetails.getData().get(RECEIVED_AOS_FROM_RESP));
        assertNull(caseDetails.getData().get(RECEIVED_AOS_FROM_RESP_DATE));
    }

    private void assertCaseDetailsCoRespondent(UserDetails userDetails, String caseId) {
        CaseDetails caseDetails = ccdClientSupport.retrieveCase(userDetails, caseId);

        assertEquals(userDetails.getEmailAddress(), caseDetails.getData().get(CO_RESP_EMAIL_ADDRESS));
        assertNull(caseDetails.getData().get(RECEIVED_AOS_FROM_RESP));
        assertNull(caseDetails.getData().get(RECEIVED_AOS_FROM_RESP_DATE));
        assertEquals(YES_VALUE, caseDetails.getData().get(CO_RESP_LINKED_TO_CASE));
        assertEquals(LocalDate.now().toString(CCD_DATE_FORMAT), caseDetails.getData().get(CO_RESP_LINKED_TO_CASE_DATE));
    }

    private Response linkRespondent(String userToken, Long caseId, String pin) {
        final Map<String, Object> headers = new HashMap<>();
        headers.put(HttpHeaders.CONTENT_TYPE, ContentType.APPLICATION_JSON.toString());

        if (userToken != null) {
            headers.put(HttpHeaders.AUTHORIZATION, userToken);
        }

        return RestUtil.postToRestService(
            serverUrl + contextPath + "/" + caseId + "/" + pin,
            headers,
            null
        );
    }
}
