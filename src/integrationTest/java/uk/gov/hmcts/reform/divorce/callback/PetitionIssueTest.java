package uk.gov.hmcts.reform.divorce.callback;

import io.restassured.response.Response;
import org.apache.http.entity.ContentType;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import uk.gov.hmcts.reform.divorce.context.IntegrationTest;
import uk.gov.hmcts.reform.divorce.util.ResourceLoader;
import uk.gov.hmcts.reform.divorce.util.RestUtil;

import java.time.LocalDate;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static uk.gov.hmcts.reform.divorce.util.DateConstants.CCD_DATE_FORMATTER;

public class PetitionIssueTest extends IntegrationTest {
    private static final String PAYLOAD_CONTEXT_PATH = "fixtures/issue-petition/";

    private static final List<String> EXPECTED_ERROR =
        Collections.singletonList("D8StatementOfTruth must be 'YES'. Actual data is: null");

    private static final String D8_MINI_PETITION_DOCUMENT_URL_PATH =
        "data.D8DocumentsGenerated[0].value.DocumentLink.document_url";
    private static final String D8_MINI_PETITION_DOCUMENT_BINARY_URL_PATH =
        "data.D8DocumentsGenerated[0].value.DocumentLink.document_binary_url";
    private static final String D8_MINI_PETITION_DOCUMENT_TYPE_PATH =
        "data.D8DocumentsGenerated[0].value.DocumentType";
    private static final String D8_MINI_PETITION_DOCUMENT_FILENAME_PATH =
        "data.D8DocumentsGenerated[0].value.DocumentLink.document_filename";
    private static final String DOC_TYPE_PETITION = "petition";
    private static final String D8_MINI_PETITION_FILE_NAME_FORMAT = "d8petition%s.pdf";
    private static final String D8_AOS_INVITATION_DOCUMENT_URL_PATH =
            "data.D8DocumentsGenerated[1].value.DocumentLink.document_url";
    private static final String D8_AOS_INVITATION_DOCUMENT_BINARY_URL_PATH =
            "data.D8DocumentsGenerated[1].value.DocumentLink.document_binary_url";
    private static final String D8_AOS_INVITATION_DOCUMENT_TYPE_PATH =
            "data.D8DocumentsGenerated[1].value.DocumentType";
    private static final String D8_AOS_INVITATION_DOCUMENT_FILENAME_PATH =
            "data.D8DocumentsGenerated[1].value.DocumentLink.document_filename";
    private static final String DOC_TYPE_AOS_INVITATION = "aos";
    private static final String D8_AOS_INVITATION_FILE_NAME_FORMAT = "aosinvitation%s.pdf";
    private static final String AOS_LETTER_HOLDER_ID = "data.AosLetterHolderId";

    private static final String D8_CO_RESPONDENT_INVITATION_DOCUMENT_URL_PATH =
        "data.D8DocumentsGenerated[2].value.DocumentLink.document_url";
    private static final String D8_CO_RESPONDENT_INVITATION_DOCUMENT_BINARY_URL_PATH =
        "data.D8DocumentsGenerated[2].value.DocumentLink.document_binary_url";
    private static final String D8_CO_RESPONDENT_INVITATION_DOCUMENT_TYPE_PATH =
        "data.D8DocumentsGenerated[2].value.DocumentType";
    private static final String D8_CO_RESPONDENT_INVITATION_DOCUMENT_FILENAME_PATH =
        "data.D8DocumentsGenerated[2].value.DocumentLink.document_filename";
    private static final String DOC_TYPE_CO_RESPONDENT_INVITATION = "aoscr";
    private static final String D8_CO_RESPONDENT_INVITATION_FILE_NAME_FORMAT = "co-respondentaosinvitation%s.pdf";
    private static final String CO_RESPONDENT_LETTER_HOLDER_ID = "data.CoRespLetterHolderId";


    private static final String CASE_ERROR_KEY = "errors";
    private static final String CASE_ID = "1517833758870511";
    private static final String ISSUE_DATE = "data.IssueDate";

    private static final String EXPECTED_ISSUE_DATE = LocalDate.now().format(CCD_DATE_FORMATTER);

    @Value("${case.orchestration.petition-issued.context-path}")
    private String contextPath;

    @Test
    public void givenUserTokenIsNull_whenRetrievePetition_thenReturnBadRequest() throws Exception {
        Response cosResponse = issuePetition(null, "ccd-callback-petition-issued.json", null);

        assertEquals(HttpStatus.BAD_REQUEST.value(), cosResponse.getStatusCode());
    }

    @Test
    public void givenInvalidCaseData_whenRetrievePetition_thenReturnValidationError() throws Exception {
        Response cosResponse = issuePetition(createCaseWorkerUser().getAuthToken(),
            "invalid-ccd-callback-petition-issued.json", null);

        assertEquals(HttpStatus.OK.value(), cosResponse.getStatusCode());
        assertEquals(EXPECTED_ERROR, cosResponse.path(CASE_ERROR_KEY));
    }

    @Test
    public void givenGenerateAosNull_whenRetrievePetition_thenReturnExpectedCaseData() throws Exception {
        Response cosResponse = issuePetition(createCaseWorkerUser().getAuthToken(),
            "ccd-callback-aos-invitation.json", null);

        assertEquals(HttpStatus.OK.value(), cosResponse.getStatusCode());
        assertGeneratedDocumentsExists(cosResponse, false, false);
        assertEquals(EXPECTED_ISSUE_DATE, cosResponse.path(ISSUE_DATE));
    }

    @Test
    public void givenGenerateAosFalse_whenRetrievePetition_thenReturnExpectedCaseData() throws Exception {
        Response cosResponse = issuePetition(createCaseWorkerUser().getAuthToken(),
            "ccd-callback-aos-invitation.json", false);

        assertEquals(HttpStatus.OK.value(), cosResponse.getStatusCode());
        assertGeneratedDocumentsExists(cosResponse, false, false);
        assertEquals(EXPECTED_ISSUE_DATE, cosResponse.path(ISSUE_DATE));
    }

    @Test
    public void givenGenerateAosTrue_whenRetrievePetition_thenReturnExpectedCaseData() throws Exception {
        Response cosResponse = issuePetition(createCaseWorkerUser().getAuthToken(),
            "ccd-callback-aos-invitation.json", true);

        assertEquals(HttpStatus.OK.value(), cosResponse.getStatusCode());
        assertGeneratedDocumentsExists(cosResponse, false, false);
        assertEquals(EXPECTED_ISSUE_DATE, cosResponse.path(ISSUE_DATE));
    }

    @Test
    public void givenGenerateAosTrueAndServiceCentre_whenRetrievePetition_thenReturnExpectedCaseData() throws Exception {
        Response cosResponse = issuePetition(createCaseWorkerUser().getAuthToken(),
            "ccd-callback-aos-invitation-service-centre.json", true);

        assertEquals(HttpStatus.OK.value(), cosResponse.getStatusCode());
        assertGeneratedDocumentsExists(cosResponse, true, false);
        assertEquals(EXPECTED_ISSUE_DATE, cosResponse.path(ISSUE_DATE));
    }


    @Test
    public void givenGenerateAosTrueAndEastMidlandsRdc_whenRetrievePetition_thenReturnExpectedCaseData() throws Exception {
        Response cosResponse = issuePetition(createCaseWorkerUser().getAuthToken(),
            "ccd-callback-aos-invitation-east-midlands.json", true);

        assertEquals(HttpStatus.OK.value(), cosResponse.getStatusCode());
        assertGeneratedDocumentsExists(cosResponse, true, true);
        assertEquals(EXPECTED_ISSUE_DATE, cosResponse.path(ISSUE_DATE));
    }

    @Test
    public void givenGenerateAosTrueAndServiceCentreWithCoRespondent_whenRetrievePetition_thenReturnExpectedCaseData() throws Exception {
        Response cosResponse = issuePetition(createCaseWorkerUser().getAuthToken(),
            "ccd-callback-aos-invitation-service-centre-with-coRespondent.json", true);

        assertEquals(HttpStatus.OK.value(), cosResponse.getStatusCode());
        assertGeneratedDocumentsExists(cosResponse, true, true);
        assertEquals(EXPECTED_ISSUE_DATE, cosResponse.path(ISSUE_DATE));
    }

    private Response issuePetition(String userToken, String fileName, Boolean generateAosInvitation) throws Exception {
        final Map<String, Object> headers = new HashMap<>();
        headers.put(HttpHeaders.CONTENT_TYPE, ContentType.APPLICATION_JSON.toString());

        if (userToken != null) {
            headers.put(HttpHeaders.AUTHORIZATION, userToken);
        }

        final Map<String, Object> params = new HashMap<>();

        if (generateAosInvitation != null) {
            params.put("generateAosInvitation", generateAosInvitation);
        }

        String json = ResourceLoader.loadJson(PAYLOAD_CONTEXT_PATH + fileName);
        return
            RestUtil.postToRestService(
                serverUrl + contextPath,
                headers,
                fileName == null ? null : json,
                params
            );
    }

    @SuppressWarnings("Duplicates")
    private void assertGeneratedDocumentsExists(Response cosResponse, boolean aosInvitationExists, boolean coRespondentInvitationExists) {
        assertThat(cosResponse.path(D8_MINI_PETITION_DOCUMENT_BINARY_URL_PATH), is(notNullValue()));
        assertThat(cosResponse.path(D8_MINI_PETITION_DOCUMENT_URL_PATH), is(notNullValue()));
        assertThat(cosResponse.path(D8_MINI_PETITION_DOCUMENT_TYPE_PATH), is(DOC_TYPE_PETITION));
        assertThat(cosResponse.path(D8_MINI_PETITION_DOCUMENT_FILENAME_PATH), is(String.format(D8_MINI_PETITION_FILE_NAME_FORMAT, CASE_ID)));

        if (aosInvitationExists) {
            assertThat(cosResponse.path(D8_AOS_INVITATION_DOCUMENT_BINARY_URL_PATH), is(notNullValue()));
            assertThat(cosResponse.path(D8_AOS_INVITATION_DOCUMENT_URL_PATH), is(notNullValue()));
            assertThat(cosResponse.path(D8_AOS_INVITATION_DOCUMENT_TYPE_PATH), is(DOC_TYPE_AOS_INVITATION));
            assertThat(cosResponse.path(D8_AOS_INVITATION_DOCUMENT_FILENAME_PATH),
                is(String.format(D8_AOS_INVITATION_FILE_NAME_FORMAT, CASE_ID)));

            assertThat(cosResponse.path(AOS_LETTER_HOLDER_ID), is(notNullValue()));


        } else {
            assertThat(cosResponse.path(D8_AOS_INVITATION_DOCUMENT_BINARY_URL_PATH), is(nullValue()));
        }

        if (coRespondentInvitationExists) {
            assertThat(cosResponse.path(D8_CO_RESPONDENT_INVITATION_DOCUMENT_BINARY_URL_PATH), is(notNullValue()));
            assertThat(cosResponse.path(D8_CO_RESPONDENT_INVITATION_DOCUMENT_URL_PATH), is(notNullValue()));
            assertThat(cosResponse.path(D8_CO_RESPONDENT_INVITATION_DOCUMENT_TYPE_PATH), is(DOC_TYPE_CO_RESPONDENT_INVITATION));
            assertThat(cosResponse.path(D8_CO_RESPONDENT_INVITATION_DOCUMENT_FILENAME_PATH),
                is(String.format(D8_CO_RESPONDENT_INVITATION_FILE_NAME_FORMAT, CASE_ID)));

            assertThat(cosResponse.path(CO_RESPONDENT_LETTER_HOLDER_ID), is(notNullValue()));
        } else {
            assertThat(cosResponse.path(D8_CO_RESPONDENT_INVITATION_DOCUMENT_BINARY_URL_PATH), is(nullValue()));
        }
    }
}
