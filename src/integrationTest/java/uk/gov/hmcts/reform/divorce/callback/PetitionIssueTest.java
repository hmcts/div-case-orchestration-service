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
    private static final String D8_MINI_PETITION_FILE_NAME_FORMAT = "d8petition%s.pdf";
    private static final String D8_AOS_INVITATION_DOCUMENT_URL_PATH =
            "case_data.D8DocumentsGenerated[1].value.DocumentLink.document_url";
    private static final String D8_AOS_INVITATION_DOCUMENT_BINARY_URL_PATH =
            "case_data.D8DocumentsGenerated[1].value.DocumentLink.document_binary_url";
    private static final String D8_AOS_INVITATION_DOCUMENT_TYPE_PATH =
            "case_data.D8DocumentsGenerated[1].value.DocumentType";
    private static final String D8_AOS_INVITATION_DOCUMENT_FILENAME_PATH =
            "case_data.D8DocumentsGenerated[1].value.DocumentLink.document_filename";
    private static final String AOS_INVITATION = "aosinvitation";
    private static final String D8_AOS_INVITATION_FILE_NAME_FORMAT = "aosinvitation%s.pdf";

    private static final String CASE_ERROR_KEY = "errors";
    private static final String CASE_ID = "1517833758870511";

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
    public void givenInvalidCaseData_whenRetrievePetition_thenReturnValidationError() throws Exception {
        Response cosResponse = issuePetition(createCaseWorkerUser().getAuthToken(),
            "invalid-ccd-callback-petition-issued.json");

        assertEquals(HttpStatus.OK.value(), cosResponse.getStatusCode());
        assertEquals(EXPECTED_ERROR, cosResponse.path(CASE_ERROR_KEY));
    }

    @Test
    public void givenValidCaseData_whenRetrievePetition_thenReturnExpectedCaseData() throws Exception {
        Response cosResponse = issuePetition(createCaseWorkerUser().getAuthToken(),
            "ccd-callback-aos-invitation.json");

        assertEquals(HttpStatus.OK.value(), cosResponse.getStatusCode());
        assertGeneratedDocumentsExists(cosResponse, createCaseWorkerUser().getAuthToken());
    }

    private Response issuePetition(String userToken, String fileName) throws Exception {
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

    private void assertGeneratedDocumentsExists(Response cosResponse, String userToken) {
        String petitionUri = cosResponse.path(D8_MINI_PETITION_DOCUMENT_BINARY_URL_PATH);

        assertNotNull(petitionUri);
        assertNotNull(cosResponse.path(D8_MINI_PETITION_DOCUMENT_URL_PATH));
        assertEquals(PETITION, cosResponse.path(D8_MINI_PETITION_DOCUMENT_TYPE_PATH));
        assertEquals(String.format(D8_MINI_PETITION_FILE_NAME_FORMAT, CASE_ID),
            cosResponse.path(D8_MINI_PETITION_DOCUMENT_FILENAME_PATH));


        String aosInvitationUri = cosResponse.path(D8_AOS_INVITATION_DOCUMENT_BINARY_URL_PATH);

        assertNotNull(aosInvitationUri);
        assertNotNull(cosResponse.path(D8_AOS_INVITATION_DOCUMENT_URL_PATH));
        assertEquals(AOS_INVITATION, cosResponse.path(D8_AOS_INVITATION_DOCUMENT_TYPE_PATH));
        assertEquals(String.format(D8_AOS_INVITATION_FILE_NAME_FORMAT, CASE_ID),
                cosResponse.path(D8_AOS_INVITATION_DOCUMENT_FILENAME_PATH));
    }
}
