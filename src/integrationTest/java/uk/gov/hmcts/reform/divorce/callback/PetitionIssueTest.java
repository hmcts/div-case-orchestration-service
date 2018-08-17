package uk.gov.hmcts.reform.divorce.callback;

import io.restassured.response.Response;
import org.apache.http.entity.ContentType;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.divorce.context.IntegrationTest;
import uk.gov.hmcts.reform.divorce.support.emclient.EvidenceManagementUtil;
import uk.gov.hmcts.reform.divorce.util.ResourceLoader;
import uk.gov.hmcts.reform.divorce.util.RestUtil;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class PetitionIssueTest extends IntegrationTest {
    private static final String PAYLOAD_CONTEXT_PATH = "fixtures/issue-petition/";

    private static final List<String> EXPECTED_ERROR = 
        Collections.singletonList("D8StatementOfTruth must be 'YES'. Actual data is: null");
    
    private static final String D8_MINI_PETITION_DOCUMENT_URL_PATH =
        "case_data.D8DocumentsGenerated[0].value.DocumentLink.document_url";
    private static final String D8_MINI_PETITION_DOCUMENT_BINARY_URL_PATH =
        "case_data.D8DocumentsGenerated[0].value.DocumentLink.document_binary_url";
    private static final String D8_MINI_PETITION_DOCUMENT_TYPE_PATH =
        "case_data.D8DocumentsGenerated[0].value.DocumentType";
    private static final String D8_MINI_PETITION_DOCUMENT_FILENAME_PATH =
        "case_data.D8DocumentsGenerated[0].value.DocumentLink.document_filename";
    private static final String PETITION = "petition";
    private static final String D8_MINI_PETITION_FILE_NAME_FORMAT = "d8petition%d.pdf";
    private static final String CASE_ERROR_KEY = "errors";
    private static final Long CASE_ID = 999999999L;

    @Value("${case.orchestration.petition-issued.context-path}")
    private String contextPath;

    @Autowired
    @Qualifier("documentGeneratorTokenGenerator")
    private AuthTokenGenerator divDocAuthTokenGenerator;

    @Test
    public void givenUserTokenIsNull_whenRetrievePetition_thenReturnBadRequest() throws Exception {
        Response cosResponse = issuePetition(null, "ccd-callback-petition-issued.json");

        assertEquals(HttpStatus.BAD_REQUEST.value(), cosResponse.getStatusCode());
    }

    @Test
    public void givenEventIsNull_whenRetrievePetition_thenReturnBadRequest() throws Exception {
        Response cosResponse = issuePetition(getUserDetails().getAuthToken(), null);

        assertEquals(HttpStatus.BAD_REQUEST.value(), cosResponse.getStatusCode());
    }

    @Test
    public void givenInvalidCaseData_whenRetrievePetition_thenReturnValidationError() throws Exception {
        Response cosResponse = issuePetition(getUserDetails().getAuthToken(),
            "invalid-ccd-callback-petition-issued.json");

        assertEquals(HttpStatus.OK.value(), cosResponse.getStatusCode());
        assertEquals(EXPECTED_ERROR, cosResponse.path(CASE_ERROR_KEY));
    }

    @Test
    public void givenValidCaseData_whenRetrievePetition_thenReturnExpectedCaseData() throws Exception {
        Response cosResponse = issuePetition(getUserDetails().getAuthToken(),
            "ccd-callback-aos-invitation.json");

        assertEquals(HttpStatus.OK.value(), cosResponse.getStatusCode());
        assertGeneratedDocumentExists(cosResponse, getUserDetails().getAuthToken());
    }

    private Response issuePetition(String userToken, String fileName) throws Exception {
        System.setProperty("http.proxyHost", "proxyout.reform.hmcts.net");
        System.setProperty("http.proxyPort", "8080");
        System.setProperty("https.proxyHost", "proxyout.reform.hmcts.net");
        System.setProperty("https.proxyPort", "8080");
        final Map<String, Object> headers = new HashMap<>();
        headers.put(HttpHeaders.CONTENT_TYPE, ContentType.APPLICATION_JSON.toString());

        if (userToken != null) {
            headers.put(HttpHeaders.AUTHORIZATION, userToken);
        }

        String json = ResourceLoader.loadJson(PAYLOAD_CONTEXT_PATH + fileName);
        return
            RestUtil.postToRestService(
                serverUrl + contextPath,
                headers,
                fileName == null ? null : json
            );
    }

    private void assertGeneratedDocumentExists(Response cosResponse, String userToken) {
        String documentUri = cosResponse.path(D8_MINI_PETITION_DOCUMENT_BINARY_URL_PATH);

        assertNotNull(documentUri);
        assertNotNull(cosResponse.path(D8_MINI_PETITION_DOCUMENT_URL_PATH));
        assertEquals(PETITION, cosResponse.path(D8_MINI_PETITION_DOCUMENT_TYPE_PATH));
        assertEquals(String.format(D8_MINI_PETITION_FILE_NAME_FORMAT, CASE_ID),
            cosResponse.path(D8_MINI_PETITION_DOCUMENT_FILENAME_PATH));

        Response documentManagementResponse =
            EvidenceManagementUtil.readDataFromEvidenceManagement(documentUri,
                divDocAuthTokenGenerator.generate(),
                userToken);

        assertEquals(HttpStatus.OK.value(), documentManagementResponse.statusCode());
    }
}
