package uk.gov.hmcts.reform.divorce.orchestration.functionaltest;

import org.hamcrest.Matcher;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CaseDetails;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CcdCallbackRequest;

import java.util.HashMap;
import java.util.Map;

import static com.jayway.jsonpath.matchers.JsonPathMatchers.hasJsonPath;
import static java.util.Collections.singletonMap;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.BulkCaseConstants.COURT_NAME_CCD_FIELD;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.COURT_CONTACT_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DOCUMENT_CASE_DETAILS_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.testutil.ObjectMapperTestUtil.convertObjectToJsonString;

public class DocumentGenerationITest extends MockedFunctionalTest {

    private static final String API_URL = "/generate-document";

    private static final Map<String, Object> CASE_DATA = new HashMap<>();

    private static final CaseDetails CASE_DETAILS = CaseDetails.builder()
        .caseData(CASE_DATA)
        .caseId(TEST_CASE_ID)
        .build();

    private static final CcdCallbackRequest CCD_CALLBACK_REQUEST = CcdCallbackRequest.builder()
        .caseDetails(CASE_DETAILS)
        .build();

    private static final String TEST_TEMPLATE_ID = "a";
    private static final String TEST_DOCUMENT_TYPE = "b";
    private static final String TEST_FILENAME = "c";

    @Autowired
    private MockMvc webClient;

    @Test
    public void givenBodyIsNull_whenEndpointInvoked_thenReturnBadRequest() throws Exception {
        webClient.perform(post(API_URL)
            .header(AUTHORIZATION, AUTH_TOKEN)
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isBadRequest());
    }

    @Test
    public void givenAuthHeaderIsNull_whenEndpointInvoked_thenReturnBadRequest() throws Exception {
        webClient.perform(post(API_URL)
            .content(convertObjectToJsonString(CCD_CALLBACK_REQUEST))
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isBadRequest());
    }

    @Test
    public void givenTemplateIdIsNull_whenEndpointInvoked_thenReturnBadRequest() throws Exception {
        webClient.perform(post(API_URL)
            .content(convertObjectToJsonString(CCD_CALLBACK_REQUEST))
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON)
            .param("documentType", TEST_DOCUMENT_TYPE)
            .param("filename", TEST_FILENAME))
            .andExpect(status().isBadRequest());
    }

    @Test
    public void givenDocumentTypeIsNull_whenEndpointInvoked_thenReturnBadRequest() throws Exception {
        webClient.perform(post(API_URL)
            .content(convertObjectToJsonString(CCD_CALLBACK_REQUEST))
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON)
            .param("templateId", TEST_TEMPLATE_ID)
            .param("filename", TEST_FILENAME))
            .andExpect(status().isBadRequest());
    }

    @Test
    public void givenFilenameIsNull_whenEndpointInvoked_thenReturnBadRequest() throws Exception {
        webClient.perform(post(API_URL)
            .content(convertObjectToJsonString(CCD_CALLBACK_REQUEST))
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON)
            .param("templateId", TEST_TEMPLATE_ID)
            .param("documentType", TEST_DOCUMENT_TYPE))
            .andExpect(status().isBadRequest());
    }

    @Test
    public void happyPath() throws Exception {
        stubDocumentGeneratorService(TEST_TEMPLATE_ID, singletonMap(DOCUMENT_CASE_DETAILS_JSON_KEY, CASE_DETAILS), TEST_DOCUMENT_TYPE);

        webClient.perform(post(API_URL)
            .header(AUTHORIZATION, AUTH_TOKEN)
            .content(convertObjectToJsonString(CCD_CALLBACK_REQUEST))
            .param("templateId", TEST_TEMPLATE_ID)
            .param("documentType", TEST_DOCUMENT_TYPE)
            .param("filename", TEST_FILENAME)
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content().string(hasJsonPath("$.data.D8DocumentsGenerated", hasSize(1))))
            .andExpect(content().string(hasJsonPath("$.data.D8DocumentsGenerated", hasItemMatchingExpectedValues(TEST_DOCUMENT_TYPE, TEST_FILENAME))))
            .andExpect(content().string(hasJsonPath("$.errors", is(nullValue()))));
    }

    @Test
    public void happyPathWithDnCourt() throws Exception {
        // Data matching application properties.
        Map<String, Object> formattedDocumentCaseData = new HashMap<>();
        formattedDocumentCaseData.put(COURT_NAME_CCD_FIELD, "Liverpool Civil and Family Court Hearing Centre");
        formattedDocumentCaseData.put(COURT_CONTACT_JSON_KEY, "c/o Liverpool Civil and Family Court Hearing Centre"
            + "\n35 Vernon Street\nLiverpool\nL2 2BX\n\nEmail: divorcecase@justice.gov.uk\nPhone: 0300 303 0642");
        CaseDetails expectedDocumentCaseDetails = CaseDetails.builder().caseId(TEST_CASE_ID).caseData(formattedDocumentCaseData).build();

        Map<String, Object> caseData = new HashMap<>();
        caseData.put(COURT_NAME_CCD_FIELD, "liverpool");

        stubDocumentGeneratorService(TEST_TEMPLATE_ID, singletonMap(DOCUMENT_CASE_DETAILS_JSON_KEY, expectedDocumentCaseDetails), TEST_DOCUMENT_TYPE);

        CaseDetails caseDetails = CaseDetails.builder().caseId(TEST_CASE_ID).caseData(caseData).build();

        CcdCallbackRequest ccdCallbackRequest = CcdCallbackRequest.builder().caseDetails(caseDetails).build();

        webClient.perform(post(API_URL)
            .header(AUTHORIZATION, AUTH_TOKEN)
            .content(convertObjectToJsonString(ccdCallbackRequest))
            .param("templateId", TEST_TEMPLATE_ID)
            .param("documentType", TEST_DOCUMENT_TYPE)
            .param("filename", TEST_FILENAME)
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content().string(hasJsonPath("$.data.CourtName", is("liverpool"))))
            .andExpect(content().string(hasJsonPath("$.data.D8DocumentsGenerated", hasSize(1))))
            .andExpect(content().string(hasJsonPath("$.data.D8DocumentsGenerated", hasItemMatchingExpectedValues(TEST_DOCUMENT_TYPE, TEST_FILENAME))))
            .andExpect(content().string(hasJsonPath("$.errors", is(nullValue()))));
    }

    public static Matcher<Iterable<? super Object>> hasItemMatchingExpectedValues(String expectedDocumentType, String expectedFileNamePrefix) {
        return hasItem(
            hasJsonPath("value", allOf(
                hasJsonPath("DocumentType", is(expectedDocumentType)),
                hasJsonPath("DocumentLink.document_filename", is(expectedFileNamePrefix + TEST_CASE_ID + ".pdf"))
            ))
        );
    }

}