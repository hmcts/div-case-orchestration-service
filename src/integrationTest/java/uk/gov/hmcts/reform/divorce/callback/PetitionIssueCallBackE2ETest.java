package uk.gov.hmcts.reform.divorce.callback;

import io.restassured.response.Response;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.entity.ContentType;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.divorce.category.ExtendedTest;
import uk.gov.hmcts.reform.divorce.model.UserDetails;
import uk.gov.hmcts.reform.divorce.support.CcdSubmissionSupport;
import uk.gov.hmcts.reform.divorce.util.RestUtil;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.collection.IsMapContaining.hasEntry;
import static org.junit.Assert.assertThat;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CO_RESPONDENT_LETTER_HOLDER_ID;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CO_RESP_EMAIL_ADDRESS;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CO_RESP_LINKED_TO_CASE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CO_RESP_LINKED_TO_CASE_DATE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DOCUMENT_TYPE_CO_RESPONDENT_INVITATION;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DOCUMENT_TYPE_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.PAYMENT_MADE_EVENT;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.RECEIVED_AOS_FROM_CO_RESP;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.RECEIVED_AOS_FROM_CO_RESP_DATE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.RECEIVED_AOS_FROM_RESP;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.RECEIVED_AOS_FROM_RESP_DATE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.RESPONDENT_EMAIL_ADDRESS;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.RESPONDENT_LETTER_HOLDER_ID;
import static uk.gov.hmcts.reform.divorce.util.ResourceLoader.loadJson;

@Slf4j
public class PetitionIssueCallBackE2ETest extends CcdSubmissionSupport {
    private static final String SUBMIT_COMPLETE_SERVICE_CENTRE_CASE = "submit-complete-service-centre-case.json";
    private static final String SUBMIT_COMPLETE_SERVICE_CENTRE_CO_RESPONDENT_CASE = "submit-complete-service-centre-coRespondent-case.json";
    private static final String RESPONDENT_PAYLOAD_CONTEXT_PATH = "fixtures/maintenance/submit-aos/";
    private static final String CO_RESPONDENT_PAYLOAD_CONTEXT_PATH = "fixtures/maintenance/co-respondent/";
    private static final String AOS_DEFEND_CONSENT_JSON = "aos-defend-consent.json";

    private static final String PAYMENT_MADE_JSON = "payment-made.json";
    private static final String ISSUE_EVENT_ID = "issueFromSubmitted";
    private static final String ISSUE_AOS_EVENT_ID = "issueAos";
    private static final String REJECTED_EVENT_ID = "rejected";
    private static final String ISSUE_FROM_REJECTED_EVENT_ID = "issueFromRejected";

    private static final String DOC_TYPE_MINI_PETITION = "petition";
    private static final String DOC_TYPE_AOS_INVITATION = "aos";
    private static final String D8_MINI_PETITION_FILE_NAME_FORMAT = "d8petition%d.pdf";
    private static final String D8_AOS_INVITATION_FILE_NAME_FORMAT = "aosinvitation%s.pdf";
    private static final String D8_CO_RESPONDENT_INVITATION_FILE_NAME_FORMAT = "co-respondentaosinvitation%s.pdf";

    @Value("${case.orchestration.maintenance.link-respondent.context-path}")
    private String linkRespondentContextPath;

    @Test
    public void givenServiceCentreCaseSubmitted_whenIssueEventFiredOnCCD_thenDocumentsAreGenerated() {
        final UserDetails petitionerUserDetails = createCitizenUser();
        final CaseDetails caseDetails = submitCase(SUBMIT_COMPLETE_SERVICE_CENTRE_CASE, petitionerUserDetails);

        // make payment
        updateCase(caseDetails.getId().toString(), PAYMENT_MADE_JSON, PAYMENT_MADE_EVENT);
        final CaseDetails issuedCase = fireEvent(caseDetails.getId().toString(), ISSUE_EVENT_ID);

        assertGeneratedDocumentsExists(issuedCase, true, false);
    }

    @Test
    @Category(ExtendedTest.class)
    public void givenServiceCentreCaseSubmittedWithCoRespondent_whenIssueEventFiredOnCCD_thenDocumentsAreGenerated() {
        final UserDetails petitionerUserDetails = createCitizenUser();
        final CaseDetails caseDetails = submitCase(SUBMIT_COMPLETE_SERVICE_CENTRE_CO_RESPONDENT_CASE, petitionerUserDetails);

        // make payment
        updateCase(caseDetails.getId().toString(), PAYMENT_MADE_JSON, PAYMENT_MADE_EVENT);
        final CaseDetails issuedCase = fireEvent(caseDetails.getId().toString(), ISSUE_EVENT_ID);

        assertGeneratedDocumentsExists(issuedCase, true, true);
    }

    @Test
    @Category(ExtendedTest.class)
    public void givenAosSubmitted_whenReissuing_thenAosLinkingFieldsAreReset() throws Exception {
        final UserDetails petitionerUserDetails = createCitizenUser();
        final CaseDetails caseDetails = submitCase(SUBMIT_COMPLETE_SERVICE_CENTRE_CO_RESPONDENT_CASE, petitionerUserDetails);

        // make payment
        updateCase(caseDetails.getId().toString(), null, PAYMENT_MADE_EVENT);
        fireEvent(caseDetails.getId().toString(), ISSUE_EVENT_ID);

        log.info("case {}", caseDetails.getId().toString());

        // put case in aos awaiting
        CaseDetails updatedCaseDetails = fireEvent(caseDetails.getId().toString(), ISSUE_AOS_EVENT_ID);

        // link the respondent
        final UserDetails respondentUser = createCitizenUser();
        final String respondentPin = idamTestSupportUtil.getPin((String) updatedCaseDetails.getData().get(RESPONDENT_LETTER_HOLDER_ID));

        linkRespondent(respondentUser.getAuthToken(), caseDetails.getId(), respondentPin);

        // submit respondent response
        submitRespondentAosCase(respondentUser.getAuthToken(), caseDetails.getId(),
            loadJson(RESPONDENT_PAYLOAD_CONTEXT_PATH + AOS_DEFEND_CONSENT_JSON));

        // link the co-respondent
        final UserDetails coRespondentUser = createCitizenUser();
        final String coRespondentPin = idamTestSupportUtil.getPin((String) updatedCaseDetails.getData().get(CO_RESPONDENT_LETTER_HOLDER_ID));

        linkRespondent(coRespondentUser.getAuthToken(), caseDetails.getId(), coRespondentPin);

        // submit co-respondent response
        final String coRespondentAnswersJson = loadJson(CO_RESPONDENT_PAYLOAD_CONTEXT_PATH + "co-respondent-answers.json");
        submitCoRespondentAosCase(coRespondentUser, coRespondentAnswersJson);

        // reject the case
        final CaseDetails beforeReIssue = fireEvent(caseDetails.getId().toString(), REJECTED_EVENT_ID);

        assertThat(beforeReIssue.getData(), allOf(
            hasEntry(equalTo(CO_RESP_LINKED_TO_CASE), notNullValue()),
            hasEntry(equalTo(CO_RESP_LINKED_TO_CASE_DATE), notNullValue()),
            hasEntry(equalTo(CO_RESP_EMAIL_ADDRESS), notNullValue()),
            hasEntry(equalTo(RECEIVED_AOS_FROM_CO_RESP), notNullValue()),
            hasEntry(equalTo(RECEIVED_AOS_FROM_CO_RESP_DATE), notNullValue()),
            hasEntry(equalTo(RECEIVED_AOS_FROM_RESP), notNullValue()),
            hasEntry(equalTo(RECEIVED_AOS_FROM_RESP_DATE), notNullValue()),
            hasEntry(equalTo(RESPONDENT_EMAIL_ADDRESS), notNullValue())
        ));

        // issue the case
        final CaseDetails caseAfterReIssue = fireEvent(caseDetails.getId().toString(), ISSUE_FROM_REJECTED_EVENT_ID);
        final Map<String, Object> result = caseAfterReIssue.getData();

        // assert fields have been nullified
        assertThat(result, allOf(
            hasEntry(CO_RESP_LINKED_TO_CASE, null),
            hasEntry(CO_RESP_LINKED_TO_CASE_DATE, null),
            hasEntry(CO_RESP_EMAIL_ADDRESS, null),
            hasEntry(RECEIVED_AOS_FROM_CO_RESP, null),
            hasEntry(RECEIVED_AOS_FROM_CO_RESP_DATE, null),
            hasEntry(RECEIVED_AOS_FROM_RESP, null),
            hasEntry(RECEIVED_AOS_FROM_RESP_DATE, null),
            hasEntry(RESPONDENT_EMAIL_ADDRESS, null)
        ));
    }

    private void assertGeneratedDocumentsExists(final CaseDetails caseDetails, final boolean expectAosInvitation,
                                                final boolean expectCoRespondentInvitation) {
        final List<Map<String, Object>> documentsCollection = getDocumentsGenerated(caseDetails);

        Map<String, Object> miniPetition = documentsCollection.stream()
            .filter(m -> m.get(DOCUMENT_TYPE_JSON_KEY).equals(DOC_TYPE_MINI_PETITION))
            .findAny()
            .orElseThrow(() -> new AssertionError("Mini Petition not found"));

        assertDocumentWasGenerated(miniPetition, DOC_TYPE_MINI_PETITION, String.format(D8_MINI_PETITION_FILE_NAME_FORMAT, caseDetails.getId()));

        if (expectAosInvitation) {
            Map<String, Object> aosInvitation = documentsCollection.stream()
                .filter(m -> m.get(DOCUMENT_TYPE_JSON_KEY).equals(DOC_TYPE_AOS_INVITATION))
                .findAny()
                .orElseThrow(() -> new AssertionError("AOS invitation not found"));

            assertDocumentWasGenerated(aosInvitation, DOC_TYPE_AOS_INVITATION,
                String.format(D8_AOS_INVITATION_FILE_NAME_FORMAT, caseDetails.getId()));
        }

        if (expectCoRespondentInvitation) {
            Map<String, Object> coRespondentInvitation = documentsCollection.stream()
                .filter(m -> m.get(DOCUMENT_TYPE_JSON_KEY).equals(DOCUMENT_TYPE_CO_RESPONDENT_INVITATION))
                .findAny()
                .orElseThrow(() -> new AssertionError("Co-respondent invitation not found"));

            assertDocumentWasGenerated(coRespondentInvitation, DOCUMENT_TYPE_CO_RESPONDENT_INVITATION,
                String.format(D8_CO_RESPONDENT_INVITATION_FILE_NAME_FORMAT, caseDetails.getId()));
        }
    }

    private CaseDetails fireEvent(final String caseId, final String eventId) {
        return updateCase(caseId, null, eventId);
    }

    private Response linkRespondent(String userToken, Long caseId, String pin) {
        final Map<String, Object> headers = new HashMap<>();
        headers.put(HttpHeaders.CONTENT_TYPE, ContentType.APPLICATION_JSON.toString());

        if (userToken != null) {
            headers.put(HttpHeaders.AUTHORIZATION, userToken);
        }

        return RestUtil.postToRestService(
            serverUrl + linkRespondentContextPath + "/" + caseId + "/" + pin,
            headers,
            null
        );
    }
}
