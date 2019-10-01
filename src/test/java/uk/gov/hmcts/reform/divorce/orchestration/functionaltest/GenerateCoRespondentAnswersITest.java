package uk.gov.hmcts.reform.divorce.orchestration.functionaltest;

import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.matching.EqualToPattern;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import uk.gov.hmcts.reform.divorce.model.ccd.CollectionMember;
import uk.gov.hmcts.reform.divorce.model.ccd.Document;
import uk.gov.hmcts.reform.divorce.model.ccd.DocumentLink;
import uk.gov.hmcts.reform.divorce.model.documentupdate.GeneratedDocumentInfo;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CaseDetails;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CcdCallbackRequest;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CcdCallbackResponse;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.documentgeneration.GenerateDocumentRequest;
import uk.gov.hmcts.reform.divorce.orchestration.testutil.ObjectMapperTestUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Map;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalToJson;
import static java.util.Collections.singletonMap;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.MediaType.APPLICATION_JSON_UTF8_VALUE;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CO_RESPONDENT_ANSWERS_TEMPLATE_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DOCUMENT_CASE_DETAILS_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DOCUMENT_TYPE_CO_RESPONDENT_ANSWERS;
import static uk.gov.hmcts.reform.divorce.orchestration.testutil.ObjectMapperTestUtil.convertObjectToJsonString;

public class GenerateCoRespondentAnswersITest extends MockedFunctionalTest {
    private static final String API_URL = "/co-respondent-generate-answers";

    private static final String GENERATE_DOCUMENT_CONTEXT_PATH = "/version/1/generatePDF";

    private static final String USER_TOKEN = "anytoken";

    @Autowired
    private MockMvc webClient;

    @Test
    public void givenValidRequest_whenGenerateCoRespondentAnswers_thenReturnDocumentData() throws Exception {
        CcdCallbackRequest ccdCallbackRequest = CcdCallbackRequest.builder()
                .caseDetails(CaseDetails.builder()
                        .caseData(Collections.emptyMap())
                        .build())
                .build();

        final GenerateDocumentRequest generateCoRespondentAnswersRequest =
                GenerateDocumentRequest.builder()
                        .template(CO_RESPONDENT_ANSWERS_TEMPLATE_NAME)
                        .values(singletonMap(DOCUMENT_CASE_DETAILS_JSON_KEY,
                                ccdCallbackRequest.getCaseDetails()))
                        .build();

        final GeneratedDocumentInfo generatedCoRespondentAnswersResponse =
                GeneratedDocumentInfo.builder()
                        .documentType(DOCUMENT_TYPE_CO_RESPONDENT_ANSWERS)
                        .fileName(DOCUMENT_TYPE_CO_RESPONDENT_ANSWERS)
                        .build();

        stubDocumentGeneratorServerEndpoint(HttpStatus.OK.value(),
                generateCoRespondentAnswersRequest, generatedCoRespondentAnswersResponse);

        Map<String, Object> responseData = Collections
                .singletonMap("D8DocumentsGenerated", new ArrayList<CollectionMember<Document>>() {
                    {
                        Document document = new Document();
                        document.setDocumentFileName("coRespondentAnswers");
                        document.setDocumentType("coRespondentAnswers");
                        document.setDocumentLink(new DocumentLink() {
                            {
                                setDocumentBinaryUrl("null/binary");
                                setDocumentFilename("coRespondentAnswers.pdf");
                            }
                        });
                        CollectionMember<Document> collectionMember = new CollectionMember<>();
                        collectionMember.setValue(document);
                        add(collectionMember);
                    }
                });

        String expectedResponse = ObjectMapperTestUtil.convertObjectToJsonString(CcdCallbackResponse
                .builder()
                .data(responseData)
                .build()
        );

        webClient.perform(post(API_URL)
                .header(AUTHORIZATION, USER_TOKEN)
                .content(ObjectMapperTestUtil.convertObjectToJsonString(ccdCallbackRequest))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().string(expectedResponse));
    }

    @Test
    public void givenInvalidRequest_whenGenerateCoRespondentAnswers_thenReturnErrors() throws Exception {
        CcdCallbackRequest ccdCallbackRequest = CcdCallbackRequest.builder()
                .caseDetails(CaseDetails.builder()
                        .caseData(Collections.emptyMap())
                        .build())
                .build();

        final GenerateDocumentRequest generateCoRespondentAnswersRequest =
                GenerateDocumentRequest.builder()
                        .template(CO_RESPONDENT_ANSWERS_TEMPLATE_NAME)
                        .values(singletonMap(DOCUMENT_CASE_DETAILS_JSON_KEY,
                                ccdCallbackRequest.getCaseDetails()))
                        .build();

        final GeneratedDocumentInfo generatedCoRespondentAnswersResponse =
                GeneratedDocumentInfo.builder()
                        .documentType(DOCUMENT_TYPE_CO_RESPONDENT_ANSWERS)
                        .fileName(DOCUMENT_TYPE_CO_RESPONDENT_ANSWERS)
                        .build();

        stubDocumentGeneratorServerEndpoint(HttpStatus.BAD_REQUEST.value(),
                generateCoRespondentAnswersRequest, generatedCoRespondentAnswersResponse);

        CcdCallbackResponse ccdCallbackResponse = CcdCallbackResponse
                .builder()
                .errors(Collections.singletonList("Unable to generate or store Co-Respondent answers."))
                .build();
        String expectedResponse = ObjectMapperTestUtil.convertObjectToJsonString(ccdCallbackResponse);

        webClient.perform(post(API_URL)
                .header(AUTHORIZATION, USER_TOKEN)
                .content(ObjectMapperTestUtil.convertObjectToJsonString(ccdCallbackRequest))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().string(expectedResponse));
    }

    private void stubDocumentGeneratorServerEndpoint(int httpStatus, GenerateDocumentRequest generateDocumentRequest,
                                                     GeneratedDocumentInfo response) {
        documentGeneratorServiceServer.stubFor(WireMock.post(GENERATE_DOCUMENT_CONTEXT_PATH)
                .withRequestBody(equalToJson(convertObjectToJsonString(generateDocumentRequest)))
                .withHeader(AUTHORIZATION, new EqualToPattern(USER_TOKEN))
                .willReturn(aResponse()
                        .withHeader(CONTENT_TYPE, APPLICATION_JSON_UTF8_VALUE)
                        .withStatus(httpStatus)
                        .withBody(convertObjectToJsonString(response))));
    }
}
