package uk.gov.hmcts.reform.divorce.orchestration.functionaltest;

import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.junit.WireMockClassRule;
import com.github.tomakehurst.wiremock.matching.EqualToPattern;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.PropertySource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import uk.gov.hmcts.reform.divorce.orchestration.OrchestrationServiceApplication;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CaseDetails;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CcdCallbackRequest;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.documentgeneration.DocumentUpdateRequest;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.documentgeneration.GenerateDocumentRequest;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.documentgeneration.GeneratedDocumentInfo;

import java.util.HashMap;
import java.util.Map;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalToJson;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyMap;
import static java.util.Collections.singletonMap;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.MediaType.APPLICATION_JSON_UTF8_VALUE;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.BulkCaseConstants.COURT_NAME_CCD_FIELD;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.COURT_CONTACT_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DOCUMENT_CASE_DETAILS_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.testutil.ObjectMapperTestUtil.convertObjectToJsonString;

@RunWith(SpringRunner.class)
@ContextConfiguration(classes = OrchestrationServiceApplication.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@PropertySource(value = "classpath:application.yml")
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
public class DocumentGenerationITest {
    private static final String API_URL = "/generate-document";
    private static final String ADD_DOCUMENTS_CONTEXT_PATH = "/caseformatter/version/1/add-documents";
    private static final String GENERATE_DOCUMENT_CONTEXT_PATH = "/version/1/generatePDF";

    private static final Map<String, Object> CASE_DATA = new HashMap<>();

    private static final CaseDetails CASE_DETAILS = CaseDetails.builder()
        .caseData(CASE_DATA)
        .caseId(TEST_CASE_ID)
        .build();

    private static final CcdCallbackRequest CCD_CALLBACK_REQUEST = CcdCallbackRequest.builder()
        .caseDetails(CASE_DETAILS)
        .build();

    @Autowired
    private MockMvc webClient;

    @ClassRule
    public static WireMockClassRule documentGeneratorServiceServer = new WireMockClassRule(4007);

    @ClassRule
    public static WireMockClassRule formatterServiceServer = new WireMockClassRule(4011);

    @Test
    public void givenBodyIsNull_whenEndpointInvoked_thenReturnBadRequest()
        throws Exception {
        webClient.perform(post(API_URL)
            .header(AUTHORIZATION, AUTH_TOKEN)
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isBadRequest());
    }

    @Test
    public void givenAuthHeaderIsNull_whenEndpointInvoked_thenReturnBadRequest()
        throws Exception {
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
            .param("documentType", "a")
            .param("filename", "b"))
            .andExpect(status().isBadRequest());
    }


    @Test
    public void givenDocumentTypeIsNull_whenEndpointInvoked_thenReturnBadRequest() throws Exception {
        webClient.perform(post(API_URL)
            .content(convertObjectToJsonString(CCD_CALLBACK_REQUEST))
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON)
            .param("templateId", "a")
            .param("filename", "b"))
            .andExpect(status().isBadRequest());
    }


    @Test
    public void givenFilenameIsNull_whenEndpointInvoked_thenReturnBadRequest() throws Exception {
        webClient.perform(post(API_URL)
            .content(convertObjectToJsonString(CCD_CALLBACK_REQUEST))
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON)
            .param("templateId", "a")
            .param("documentType", "b"))
            .andExpect(status().isBadRequest());
    }

    @Test
    public void happyPath() throws Exception {

        final String templateId = "a";
        final String documentType = "b";
        final String filename = "c";

        final GenerateDocumentRequest documentGenerationRequest =
            GenerateDocumentRequest.builder()
                .template(templateId)
                .values(singletonMap(DOCUMENT_CASE_DETAILS_JSON_KEY, CASE_DETAILS))
                .build();

        final GeneratedDocumentInfo documentGenerationResponse =
            GeneratedDocumentInfo.builder()
                .documentType(documentType)
                .fileName(filename + TEST_CASE_ID)
                .build();

        final DocumentUpdateRequest documentUpdateRequest =
            DocumentUpdateRequest.builder()
                .documents(asList(documentGenerationResponse))
                .caseData(CASE_DATA)
                .build();

        final Map<String, Object> formattedCaseData = emptyMap();

        stubDocumentGeneratorServerEndpoint(documentGenerationRequest, documentGenerationResponse);
        stubFormatterServerEndpoint(documentUpdateRequest, formattedCaseData);

        webClient.perform(post(API_URL)
            .header(AUTHORIZATION, AUTH_TOKEN)
            .content(convertObjectToJsonString(CCD_CALLBACK_REQUEST))
            .param("templateId", "a")
            .param("documentType", "b")
            .param("filename", "c")
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content().json(convertObjectToJsonString(formattedCaseData)));
    }

    @Test
    public void happyPathWithDnCourt() throws Exception {

        final String templateId = "a";
        final String documentType = "b";
        final String filename = "c";

        // Data matching application properties.
        Map<String, Object> formattedDocumentCaseData = new HashMap<>();
        formattedDocumentCaseData.put(COURT_NAME_CCD_FIELD, "Liverpool Civil and Family Court Hearing Centre");
        formattedDocumentCaseData.put(COURT_CONTACT_JSON_KEY,
                "35 Vernon Street\nLiverpool\nL2 2BX\n\nEmail: divorcecase@justice.gov.uk\nPhone: 0300 303 0642");
        CaseDetails expectedDocumentCaseDetails = CaseDetails.builder().caseId(TEST_CASE_ID).caseData(formattedDocumentCaseData).build();

        final GenerateDocumentRequest documentGenerationRequest =
                GenerateDocumentRequest.builder()
                        .template(templateId)
                        .values(singletonMap(DOCUMENT_CASE_DETAILS_JSON_KEY, expectedDocumentCaseDetails))
                        .build();

        final GeneratedDocumentInfo documentGenerationResponse =
                GeneratedDocumentInfo.builder()
                        .documentType(documentType)
                        .fileName(filename + TEST_CASE_ID)
                        .build();

        Map<String, Object> caseData = new HashMap<>();
        caseData.put(COURT_NAME_CCD_FIELD, "liverpool");

        final DocumentUpdateRequest documentUpdateRequest =
                DocumentUpdateRequest.builder()
                        .documents(asList(documentGenerationResponse))
                        .caseData(caseData)
                        .build();

        final Map<String, Object> formattedCaseData = emptyMap();

        stubDocumentGeneratorServerEndpoint(documentGenerationRequest, documentGenerationResponse);
        stubFormatterServerEndpoint(documentUpdateRequest, formattedCaseData);

        CaseDetails caseDetails = CaseDetails.builder().caseId(TEST_CASE_ID).caseData(caseData).build();

        CcdCallbackRequest ccdCallbackRequest = CcdCallbackRequest.builder().caseDetails(caseDetails).build();

        webClient.perform(post(API_URL)
                .header(AUTHORIZATION, AUTH_TOKEN)
                .content(convertObjectToJsonString(ccdCallbackRequest))
                .param("templateId", "a")
                .param("documentType", "b")
                .param("filename", "c")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().json(convertObjectToJsonString(formattedCaseData)));
    }

    private void stubDocumentGeneratorServerEndpoint(GenerateDocumentRequest generateDocumentRequest,
                                                     GeneratedDocumentInfo response) {
        documentGeneratorServiceServer.stubFor(WireMock.post(GENERATE_DOCUMENT_CONTEXT_PATH)
            .withRequestBody(equalToJson(convertObjectToJsonString(generateDocumentRequest)))
            .withHeader(AUTHORIZATION, new EqualToPattern(AUTH_TOKEN))
            .willReturn(aResponse()
                .withHeader(CONTENT_TYPE, APPLICATION_JSON_UTF8_VALUE)
                .withStatus(HttpStatus.OK.value())
                .withBody(convertObjectToJsonString(response))));
    }

    private void stubFormatterServerEndpoint(DocumentUpdateRequest documentUpdateRequest,
                                             Map<String, Object> response) {
        formatterServiceServer.stubFor(WireMock.post(ADD_DOCUMENTS_CONTEXT_PATH)
            .withRequestBody(equalToJson(convertObjectToJsonString(documentUpdateRequest)))
            .willReturn(aResponse()
                .withHeader(CONTENT_TYPE, APPLICATION_JSON_UTF8_VALUE)
                .withStatus(HttpStatus.OK.value())
                .withBody(convertObjectToJsonString(response))));
    }

}
