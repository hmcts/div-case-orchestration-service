package uk.gov.hmcts.reform.divorce.callback;

import io.restassured.response.Response;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.divorce.support.emclient.EvidenceManagementUtil;

import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;


public class PetitionIssueCallBackE2ETest extends CcdSubmissionSupport {
    private static final String SUBMIT_COMPLETE_CASE = "submit-complete-case.json";
    private static final String PAYMENT_MADE = "payment-made.json";
    private static final String PAYMENT_EVENT_ID = "paymentMade";
    private static final String ISSUE_EVENT_ID = "issueFromSubmitted";

    private static final String PETITION = "petition";
    private static final String D8_MINI_PETITION_FILE_NAME_FORMAT = "d8petition%d.pdf";

    @Autowired
    @Qualifier("documentGeneratorTokenGenerator")
    private AuthTokenGenerator divDocAuthTokenGenerator;

    @Test
    public void submittingCaseAndIssuePetitionOnCcdShouldGeneratePDF() {
        //submit case
        CaseDetails caseDetails = submitCase(SUBMIT_COMPLETE_CASE);
        //make payment
        updateCase(caseDetails.getId().toString(), PAYMENT_MADE, PAYMENT_EVENT_ID);
        //issue
        CaseDetails caseDetailsAfterIssue =
            updateCase(caseDetails.getId().toString(), PAYMENT_MADE, ISSUE_EVENT_ID);

        assertGeneratedDocumentExists(caseDetailsAfterIssue, caseDetails.getId());
    }

    private void assertGeneratedDocumentExists(CaseDetails caseDetails, Long caseId) {
        final Map<String, Object> documentGeneratedObject = getDocumentGeneratedObject(caseDetails);
        final Map<String, String> documentLinkObject = getDocumentLinkObject(documentGeneratedObject);

        String documentUri = documentLinkObject.get("document_binary_url");

        assertEquals(caseId, caseDetails.getId());
        assertNotNull(documentUri);
        assertNotNull(documentLinkObject.get("document_url"));
        assertEquals(PETITION, documentGeneratedObject.get("DocumentType"));
        assertEquals(String.format(D8_MINI_PETITION_FILE_NAME_FORMAT, caseId),
            documentLinkObject.get("document_filename"));

        Response documentManagementResponse =
            EvidenceManagementUtil.readDataFromEvidenceManagement(documentUri,
                divDocAuthTokenGenerator.generate(),
                createCaseWorkerUser().getAuthToken());

        assertEquals(HttpStatus.OK.value(), documentManagementResponse.statusCode());
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> getDocumentGeneratedObject(CaseDetails caseDetails) {
        Map<String, Object> caseData = caseDetails.getData();
        return  (Map<String, Object>)((Map<String, Object>)
            ((List)caseData.get("D8DocumentsGenerated")).get(0)).get("value");
    }

    @SuppressWarnings("unchecked")
    private Map<String, String> getDocumentLinkObject(Map<String, Object> documentGenerated) {
        return (Map<String, String>)documentGenerated.get("DocumentLink");
    }
}
