package uk.gov.hmcts.reform.divorce.orchestration.functionaltest;

import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.matching.EqualToPattern;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import uk.gov.hmcts.reform.divorce.orchestration.client.EmailClient;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CaseDetails;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CcdCallbackRequest;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.documentgeneration.DocumentUpdateRequest;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.documentgeneration.GenerateDocumentRequest;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.documentgeneration.GeneratedDocumentInfo;
import uk.gov.hmcts.reform.divorce.orchestration.testutil.ObjectMapperTestUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalToJson;
import static com.jayway.jsonpath.matchers.JsonPathMatchers.hasJsonPath;
import static com.jayway.jsonpath.matchers.JsonPathMatchers.isJson;
import static java.util.Collections.singletonMap;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.nullValue;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DOCUMENT_CASE_DETAILS_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DOCUMENT_TYPE_RESPONDENT_ANSWERS;
import static uk.gov.hmcts.reform.divorce.orchestration.testutil.ObjectMapperTestUtil.convertObjectToJsonString;

public class AosRespondentSubmittedITest extends MockedFunctionalTest {
    private static final String API_URL = "/aos-received";
    private static final String USER_TOKEN = "anytoken";
    private static final String GENERATE_DOCUMENT_CONTEXT_PATH = "/version/1/generatePDF";
    private static final String FORMAT_ADD_DOCUMENTS_CONTEXT_PATH = "/caseformatter/version/1/add-documents";

    private static final String EVENT_ID = "event-id";

    @Autowired
    private MockMvc webClient;

    @MockBean
    private EmailClient mockEmailClient;

    @Test
    public void givenEmptyBody_whenPerformAOSReceived_thenReturnBadRequestResponse() throws Exception {
        webClient.perform(post(API_URL)
            .header(AUTHORIZATION, USER_TOKEN)
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isBadRequest());
    }

    @Test
    public void givenCaseDetails_whenPerformAOSReceived_thenReturnDocumentsData()
        throws Exception {
        Map<String, Object> caseDetailMap = new HashMap<>();

        CaseDetails fullCase = CaseDetails.builder()
            .caseData(caseDetailMap)
            .build();

        final GenerateDocumentRequest documentRequest =
            GenerateDocumentRequest.builder()
                .template(DOCUMENT_TYPE_RESPONDENT_ANSWERS)
                .values(singletonMap(DOCUMENT_CASE_DETAILS_JSON_KEY, fullCase))
                .build();

        final GeneratedDocumentInfo documentInfo =
            GeneratedDocumentInfo.builder()
                .documentType(DOCUMENT_TYPE_RESPONDENT_ANSWERS)
                .fileName(DOCUMENT_TYPE_RESPONDENT_ANSWERS)
                .build();

        final Set<GeneratedDocumentInfo> documentsForFormatter = new HashSet<>();
        documentsForFormatter.add(documentInfo);

        DocumentUpdateRequest documentFormatRequest = DocumentUpdateRequest.builder()
            .caseData(caseDetailMap)
            .documents(new ArrayList<>(documentsForFormatter))
            .build();

        CcdCallbackRequest ccdCallbackRequest = CcdCallbackRequest.builder()
            .eventId(EVENT_ID)
            .caseDetails(fullCase)
            .build();

        stubDocumentGeneratorServerEndpoint(documentRequest, documentInfo);
        stubFormatterServerEndpoint(documentFormatRequest);

        webClient.perform(post(API_URL)
            .header(AUTHORIZATION, USER_TOKEN)
            .content(ObjectMapperTestUtil.convertObjectToJsonString(ccdCallbackRequest))
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content().string(allOf(
                isJson(),
                hasJsonPath("$.errors", nullValue()),
                hasJsonPath("$.data.documents", isJson())
            )));
    }

    private void stubDocumentGeneratorServerEndpoint(GenerateDocumentRequest generateDocumentRequest,
                                                     GeneratedDocumentInfo response) {
        documentGeneratorServiceServer.stubFor(WireMock.post(GENERATE_DOCUMENT_CONTEXT_PATH)
            .withRequestBody(equalToJson(convertObjectToJsonString(generateDocumentRequest)))
            .withHeader(AUTHORIZATION, new EqualToPattern(USER_TOKEN))
            .willReturn(aResponse()
                .withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
                .withStatus(HttpStatus.OK.value())
                .withBody(convertObjectToJsonString(response))));
    }

    private void stubFormatterServerEndpoint(DocumentUpdateRequest data) {
        formatterServiceServer.stubFor(WireMock.post(FORMAT_ADD_DOCUMENTS_CONTEXT_PATH)
            .withRequestBody(equalToJson(convertObjectToJsonString(data)))
            .willReturn(aResponse()
                .withStatus(HttpStatus.OK.value())
                .withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
                .withBody(convertObjectToJsonString(data))));
    }
}