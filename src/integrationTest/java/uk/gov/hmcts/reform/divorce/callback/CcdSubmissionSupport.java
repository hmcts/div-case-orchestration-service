package uk.gov.hmcts.reform.divorce.callback;

import io.restassured.response.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.divorce.context.IntegrationTest;
import uk.gov.hmcts.reform.divorce.support.ccd.CcdClientSupport;
import uk.gov.hmcts.reform.divorce.support.emclient.EvidenceManagementUtil;
import uk.gov.hmcts.reform.divorce.util.ResourceLoader;

import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class CcdSubmissionSupport extends IntegrationTest {
    private static final String PAYLOAD_CONTEXT_PATH = "fixtures/issue-petition/";

    private static final String PETITION = "petition";
    private static final String D8_MINI_PETITION_FILE_NAME_FORMAT = "d8petition%d.pdf";

    @Autowired
    private CcdClientSupport ccdClientSupport;

    @Autowired
    @Qualifier("documentGeneratorTokenGenerator")
    private AuthTokenGenerator divDocAuthTokenGenerator;

    CaseDetails submitCase(String fileName) {
        return ccdClientSupport.submitCase(
            ResourceLoader.loadJsonToObject(PAYLOAD_CONTEXT_PATH + fileName, Map.class),
            getUserDetails());
    }

    CaseDetails updateCase(String caseId, String fileName, String eventId) {
        return ccdClientSupport.update(caseId,
            ResourceLoader.loadJsonToObject(PAYLOAD_CONTEXT_PATH + fileName, Map.class),
            eventId, getUserDetails());
    }

    void assertGeneratedDocumentExists(CaseDetails caseDetails, Long caseId) {
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
                getUserDetails().getAuthToken());

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
