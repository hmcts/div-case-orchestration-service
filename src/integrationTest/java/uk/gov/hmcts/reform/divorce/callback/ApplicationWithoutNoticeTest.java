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
public class ApplicationWithoutNoticeTest extends CcdSubmissionSupport {

    private static final String SERVICE_PAYMENT = "servicePayment";

    @Test
    public void givenServiceCentreCaseSubmitted_whenIssueEventFiredOnCCD_thenDocumentsAreGenerated() {
        final UserDetails caseWorkerUserDetails = createCaseWorkerUser();
        final CaseDetails caseDetails = submitCase(SUBMIT_COMPLETE_SERVICE_CENTRE_CASE, caseWorkerUserDetails);

        // 1. create caseworker
        // 2. create a case
        // 3. refuse a case, trigger awaiting payment on this case
        // 4. trigger servicePayment event

        // assert:
        // 200
        // case state is: AwaitingServiceConsideration
        // case data contains fee, see: uk/gov/hmcts/reform/divorce/orchestration/functionaltest/FeeLookupTest.java:45

        updateCase(caseDetails.getId().toString(), PAYMENT_MADE_JSON, PAYMENT_MADE_EVENT);
        final CaseDetails issuedCase = fireEvent(caseDetails.getId().toString(), SERVICE_PAYMENT);

        assertGeneratedDocumentsExists(issuedCase, true, false);
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
