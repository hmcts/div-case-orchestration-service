package uk.gov.hmcts.reform.divorce.orchestration.functionaltest;

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
import static uk.gov.hmcts.reform.divorce.orchestration.functionaltest.DocumentGenerationITest.hasItemMatchingExpectedValues;
import static uk.gov.hmcts.reform.divorce.orchestration.testutil.ObjectMapperTestUtil.convertObjectToJsonString;

public class PrepareToPrintForPronouncementTest extends MockedFunctionalTest {

    private static final String API_URL = "/prepare-to-print-for-pronouncement";

    private static final Map<String, Object> CASE_DATA = new HashMap<>();

    private static final CaseDetails CASE_DETAILS = CaseDetails.builder()
        .caseData(CASE_DATA)
        .caseId(TEST_CASE_ID)
        .build();

    private static final CcdCallbackRequest CCD_CALLBACK_REQUEST = CcdCallbackRequest.builder()
        .caseDetails(CASE_DETAILS)
        .build();

    private static final String TEMPLATE_NAME = "FL-DIV-GNO-ENG-00059.docx";
    private static final String EXPECTED_DOCUMENT_TYPE = "caseListForPronouncement";
    public static final String EXPECTED_FILE_NAME_PREFIX = "caseListForPronouncement";

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
    public void happyPath() throws Exception {
        stubDocumentGeneratorService(TEMPLATE_NAME, singletonMap(DOCUMENT_CASE_DETAILS_JSON_KEY, CASE_DETAILS), EXPECTED_DOCUMENT_TYPE);

        webClient.perform(post(API_URL)
            .header(AUTHORIZATION, AUTH_TOKEN)
            .content(convertObjectToJsonString(CCD_CALLBACK_REQUEST))
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content().string(hasJsonPath("$.data.D8DocumentsGenerated", hasSize(1))))
            .andExpect(content().string(hasJsonPath("$.data.D8DocumentsGenerated", hasItemMatchingExpectedValues(
                EXPECTED_DOCUMENT_TYPE,
                EXPECTED_FILE_NAME_PREFIX
            ))))
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

        stubDocumentGeneratorService(TEMPLATE_NAME,
            singletonMap(DOCUMENT_CASE_DETAILS_JSON_KEY, expectedDocumentCaseDetails),
            EXPECTED_DOCUMENT_TYPE);

        CaseDetails caseDetails = CaseDetails.builder().caseId(TEST_CASE_ID).caseData(caseData).build();

        CcdCallbackRequest ccdCallbackRequest = CcdCallbackRequest.builder().caseDetails(caseDetails).build();

        webClient.perform(post(API_URL)
            .header(AUTHORIZATION, AUTH_TOKEN)
            .content(convertObjectToJsonString(ccdCallbackRequest))
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content().string(hasJsonPath("$.data.CourtName", is("liverpool"))))
            .andExpect(content().string(hasJsonPath("$.data.D8DocumentsGenerated", hasSize(1))))
            .andExpect(content().string(hasJsonPath("$.data.D8DocumentsGenerated", hasItemMatchingExpectedValues(
                EXPECTED_DOCUMENT_TYPE, EXPECTED_FILE_NAME_PREFIX
            ))))
            .andExpect(content().string(hasJsonPath("$.errors", is(nullValue()))));
    }

}