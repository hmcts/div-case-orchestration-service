package uk.gov.hmcts.reform.divorce.callback;

import io.restassured.response.Response;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.divorce.support.CcdSubmissionSupport;

import java.util.List;
import java.util.Map;

import static java.util.stream.Collectors.toList;
import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.collection.IsMapContaining.hasEntry;
import static org.junit.Assert.assertThat;
import static org.springframework.http.HttpStatus.OK;
import static uk.gov.hmcts.reform.divorce.support.EvidenceManagementUtil.readDataFromEvidenceManagement;


public class PetitionIssueCallBackE2ETest extends CcdSubmissionSupport {
    private static final String SUBMIT_COMPLETE_RDC_CASE = "submit-complete-case.json";
    private static final String SUBMIT_COMPLETE_SERVICE_CENTRE_CASE = "submit-complete-service-centre-case.json";
    private static final String SUBMIT_COMPLETE_SERVICE_CENTRE_CO_RESPONDENT_CASE = "submit-complete-service-centre-coRespondent-case.json";
    private static final String PAYMENT_MADE = "payment-made.json";
    private static final String PAYMENT_EVENT_ID = "paymentMade";
    private static final String ISSUE_EVENT_ID = "issueFromSubmitted";

    private static final String DOC_TYPE_MINI_PETITION = "petition";
    private static final String DOC_TYPE_AOS_INVITATION = "aos";
    private static final String DOC_TYPE_CO_RESPONDENT_INVITATION = "aoscr";
    private static final String D8_MINI_PETITION_FILE_NAME_FORMAT = "d8petition%d.pdf";
    private static final String D8_AOS_INVITATION_FILE_NAME_FORMAT = "aosinvitation%s.pdf";
    private static final String D8_CO_RESPONDENT_INVITATION_FILE_NAME_FORMAT = "co-respondentaosinvitation%s.pdf";

    @Autowired
    @Qualifier("documentGeneratorTokenGenerator")
    private AuthTokenGenerator divDocAuthTokenGenerator;

    @Test
    public void givenRegionalDivorceCaseSubmitted_whenIssueEventFiredOnCCD_thenDocumentsAreGenerated() {
        final CaseDetails caseDetails = submitCaseAndFireIssueEvent(SUBMIT_COMPLETE_RDC_CASE);

        assertGeneratedDocumentsExists(caseDetails, false, false);

    }

    @Test
    public void givenServiceCentreCaseSubmitted_whenIssueEventFiredOnCCD_thenDocumentsAreGenerated() {
        final CaseDetails caseDetails = submitCaseAndFireIssueEvent(SUBMIT_COMPLETE_SERVICE_CENTRE_CASE);

        assertGeneratedDocumentsExists(caseDetails, true, false);
    }

    @Test
    @Ignore(value = "Temporarily ignore since this test wont run in the cnp pipeline. "
        + "The AAT config points to staging callback urls. Will reinstate this test after this PR has been merged.")
    public void givenServiceCentreCaseSubmittedWithCoRespondent_whenIssueEventFiredOnCCD_thenDocumentsAreGenerated() {
        final CaseDetails caseDetails = submitCaseAndFireIssueEvent(SUBMIT_COMPLETE_SERVICE_CENTRE_CO_RESPONDENT_CASE);

        assertGeneratedDocumentsExists(caseDetails, true, true);
    }

    private void assertGeneratedDocumentsExists(final CaseDetails caseDetails, final boolean expectAosInvitation,
                                                final boolean expectCoRespondentInvitation) {
        final List<Map<String, Object>> documentsCollection = getDocumentsGenerated(caseDetails);

        Map<String, Object> miniPetition = documentsCollection.stream()
            .filter(m -> m.get("DocumentType").equals(DOC_TYPE_MINI_PETITION))
            .findAny()
            .orElseThrow(() -> new AssertionError("Mini Petition not found"));

        assertDocumentWasGenerated(miniPetition, DOC_TYPE_MINI_PETITION, String.format(D8_MINI_PETITION_FILE_NAME_FORMAT, caseDetails.getId()));

        if (expectAosInvitation) {
            Map<String, Object> aosInvitation = documentsCollection.stream()
                .filter(m -> m.get("DocumentType").equals(DOC_TYPE_AOS_INVITATION))
                .findAny()
                .orElseThrow(() -> new AssertionError("AOS invitation not found"));

            assertDocumentWasGenerated(aosInvitation, DOC_TYPE_AOS_INVITATION,
                String.format(D8_AOS_INVITATION_FILE_NAME_FORMAT, caseDetails.getId()));
        }

        if (expectCoRespondentInvitation) {
            Map<String, Object> coRespondentInvitation = documentsCollection.stream()
                .filter(m -> m.get("DocumentType").equals(DOC_TYPE_CO_RESPONDENT_INVITATION))
                .findAny()
                .orElseThrow(() -> new AssertionError("Co-respondent invitation not found"));

            assertDocumentWasGenerated(coRespondentInvitation, DOC_TYPE_CO_RESPONDENT_INVITATION,
                String.format(D8_CO_RESPONDENT_INVITATION_FILE_NAME_FORMAT, caseDetails.getId()));
        }
    }

    private CaseDetails submitCaseAndFireIssueEvent(final String payloadFile) {
        // submit case
        final CaseDetails caseDetails = submitCase(payloadFile);
        // make payment
        updateCase(caseDetails.getId().toString(), PAYMENT_MADE, PAYMENT_EVENT_ID);
        // issue
        final CaseDetails caseDetailsAfterIssue = updateCase(caseDetails.getId().toString(), null, ISSUE_EVENT_ID);

        assertThat(caseDetails.getId(), is(caseDetailsAfterIssue.getId()));

        return caseDetailsAfterIssue;
    }

    private void assertDocumentWasGenerated(final Map<String, Object> documentData, final String expectedDocumentType,
                                            final String expectedFilename) {
        assertThat(documentData.get("DocumentType"), is(expectedDocumentType));

        final Map<String, String> documentLinkObject = getDocumentLinkObject(documentData);

        assertThat(documentLinkObject, allOf(hasEntry(equalTo("document_binary_url"), is(notNullValue())),
                                             hasEntry(equalTo("document_url"), is(notNullValue())),
                                             hasEntry(equalTo("document_filename"), is(expectedFilename))
        ));


        checkEvidenceManagement(documentLinkObject);
    }

    private void checkEvidenceManagement(final Map<String, String> documentLinkObject) {
        final String divDocAuthToken = divDocAuthTokenGenerator.generate();
        final String caseworkerAuthToken = createCaseWorkerUser().getAuthToken();

        final String document_binary_url = documentLinkObject.get("document_binary_url");
        final Response response = readDataFromEvidenceManagement(document_binary_url, divDocAuthToken, caseworkerAuthToken);

        assertThat("Unable to find " + document_binary_url + " in evidence management" , response.statusCode(), is(OK.value()));
    }

    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> getDocumentsGenerated(CaseDetails caseDetails) {
        List<Map<String, Object>> d8DocumentsGenerated = (List<Map<String, Object>>) caseDetails.getData().get("D8DocumentsGenerated");
        return d8DocumentsGenerated.stream().map(m -> (Map<String, Object>) m.get("value")).collect(toList());
    }

    @SuppressWarnings("unchecked")
    private Map<String, String> getDocumentLinkObject(Map<String, Object> documentGenerated) {
        return (Map<String, String>)documentGenerated.get("DocumentLink");
    }
}
