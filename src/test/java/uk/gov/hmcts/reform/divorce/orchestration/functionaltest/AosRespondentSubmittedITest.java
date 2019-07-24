package uk.gov.hmcts.reform.divorce.orchestration.functionaltest;

import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.matching.EqualToPattern;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import uk.gov.hmcts.reform.divorce.orchestration.client.EmailClient;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CaseDetails;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CcdCallbackRequest;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CcdCallbackResponse;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.documentgeneration.DocumentUpdateRequest;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.documentgeneration.GenerateDocumentRequest;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.documentgeneration.GeneratedDocumentInfo;
import uk.gov.hmcts.reform.divorce.orchestration.testutil.ObjectMapperTestUtil;
import uk.gov.service.notify.NotificationClientException;

import java.util.ArrayList;
import java.util.Collections;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.MediaType.APPLICATION_JSON_UTF8_VALUE;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DOCUMENT_CASE_DETAILS_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DOCUMENT_TYPE_RESPONDENT_ANSWERS;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D_8_CASE_REFERENCE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D_8_INFERRED_RESPONDENT_GENDER;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D_8_PETITIONER_EMAIL;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D_8_PETITIONER_FIRST_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D_8_PETITIONER_LAST_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NOTIFICATION_ADDRESSEE_FIRST_NAME_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NOTIFICATION_ADDRESSEE_LAST_NAME_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NOTIFICATION_REFERENCE_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NOTIFICATION_RELATIONSHIP_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.RESP_WILL_DEFEND_DIVORCE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.YES_VALUE;
import static uk.gov.hmcts.reform.divorce.orchestration.testutil.ObjectMapperTestUtil.convertObjectToJsonString;

@RunWith(SpringRunner.class)
public class AosRespondentSubmittedITest extends MockedFunctionalTest {
    private static final String API_URL = "/aos-received";
    private static final String USER_TOKEN = "anytoken";
    private static final String GENERATE_DOCUMENT_CONTEXT_PATH = "/version/1/generatePDF";
    private static final String FORMAT_ADD_DOCUMENTS_CONTEXT_PATH = "/caseformatter/version/1/add-documents";

    private static final String PETITIONER_FIRST_NAME = "any-name";
    private static final String PETITIONER_LAST_NAME = "any-last-name";
    private static final String RESPONDENT_FEMALE_GENDER = "female";
    private static final String EVENT_ID = "event-id";
    private static final String CASE_ID = "case-id";
    private static final String D8_ID = "d8-id";
    private static final String RELATIONSHIP = "wife";

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
    public void givenWithoutPetitionerEmail_whenPerformAOSReceived_thenReturnDocumentsData()
        throws Exception {
        mockEmailClientError("null");
        Map<String, Object> caseDetailMap = new HashMap<>();

        caseDetailMap.put(D_8_PETITIONER_EMAIL, D_8_PETITIONER_EMAIL);
        caseDetailMap.put(D_8_PETITIONER_FIRST_NAME, PETITIONER_FIRST_NAME);
        caseDetailMap.put(D_8_PETITIONER_LAST_NAME, PETITIONER_LAST_NAME);
        caseDetailMap.put(D_8_INFERRED_RESPONDENT_GENDER, RESPONDENT_FEMALE_GENDER);
        caseDetailMap.put(D_8_CASE_REFERENCE, D8_ID);
        caseDetailMap.put(RESP_WILL_DEFEND_DIVORCE, YES_VALUE);

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
                hasJsonPath("$.data.documents", isJson()),
                hasJsonPath("$.data.caseData", isJson())
            )));
    }

    @Test
    public void givenCaseDataNotDefending_whenPerformAOSReceived_thenSendEmailAndAddDocument() throws Exception {
        Map<String, Object> caseDetailMap = new HashMap<>();

        caseDetailMap.put(D_8_PETITIONER_EMAIL, D_8_PETITIONER_EMAIL);
        caseDetailMap.put(D_8_PETITIONER_FIRST_NAME, PETITIONER_FIRST_NAME);
        caseDetailMap.put(D_8_PETITIONER_LAST_NAME, PETITIONER_LAST_NAME);
        caseDetailMap.put(D_8_INFERRED_RESPONDENT_GENDER, RESPONDENT_FEMALE_GENDER);
        caseDetailMap.put(D_8_CASE_REFERENCE, D8_ID);
        caseDetailMap.put(RESP_WILL_DEFEND_DIVORCE, "No");

        CaseDetails fullCase = CaseDetails.builder()
            .caseData(caseDetailMap)
            .build();

        final GenerateDocumentRequest respondentAnswersDocRequest =
            GenerateDocumentRequest.builder()
                .template(DOCUMENT_TYPE_RESPONDENT_ANSWERS)
                .values(singletonMap(DOCUMENT_CASE_DETAILS_JSON_KEY, fullCase))
                .build();

        final GeneratedDocumentInfo respondentAnswersDocResponse =
            GeneratedDocumentInfo.builder()
                .documentType(DOCUMENT_TYPE_RESPONDENT_ANSWERS)
                .fileName(DOCUMENT_TYPE_RESPONDENT_ANSWERS)
                .build();
        final Set<GeneratedDocumentInfo> documentsForFormatter = new HashSet<>();
        documentsForFormatter.add(respondentAnswersDocResponse);
        DocumentUpdateRequest docsReq = DocumentUpdateRequest.builder()
            .caseData(fullCase.getCaseData())
            .documents(new ArrayList<>(documentsForFormatter))
            .build();

        stubDocumentGeneratorServerEndpoint(respondentAnswersDocRequest, respondentAnswersDocResponse);
        stubFormatterServerEndpoint(docsReq);

        CcdCallbackRequest ccdCallbackRequest = CcdCallbackRequest.builder()
            .eventId(CASE_ID)
            .caseDetails(fullCase)
            .build();

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

        verify(mockEmailClient).sendEmail(any(),
            eq(D_8_PETITIONER_EMAIL),
            any(), any());
    }

    @Test
    public void givenCaseDataAosDefending_whenPerformAOSReceived_thenOnlyAddDocument() throws Exception {
        Map<String, Object> caseDetailMap = new HashMap<>();

        caseDetailMap.put(D_8_PETITIONER_EMAIL, D_8_PETITIONER_EMAIL);
        caseDetailMap.put(D_8_PETITIONER_FIRST_NAME, PETITIONER_FIRST_NAME);
        caseDetailMap.put(D_8_PETITIONER_LAST_NAME, PETITIONER_LAST_NAME);
        caseDetailMap.put(D_8_INFERRED_RESPONDENT_GENDER, RESPONDENT_FEMALE_GENDER);
        caseDetailMap.put(D_8_CASE_REFERENCE, D8_ID);
        caseDetailMap.put(RESP_WILL_DEFEND_DIVORCE, YES_VALUE);

        CaseDetails fullCase = CaseDetails.builder()
            .caseData(caseDetailMap)
            .build();

        final GenerateDocumentRequest respondentAnswersDocRequest =
            GenerateDocumentRequest.builder()
                .template(DOCUMENT_TYPE_RESPONDENT_ANSWERS)
                .values(singletonMap(DOCUMENT_CASE_DETAILS_JSON_KEY, fullCase))
                .build();

        final GeneratedDocumentInfo respondentAnswersDocResponse =
            GeneratedDocumentInfo.builder()
                .documentType(DOCUMENT_TYPE_RESPONDENT_ANSWERS)
                .fileName(DOCUMENT_TYPE_RESPONDENT_ANSWERS)
                .build();
        final Set<GeneratedDocumentInfo> documentsForFormatter = new HashSet<>();
        documentsForFormatter.add(respondentAnswersDocResponse);
        DocumentUpdateRequest docsReq = DocumentUpdateRequest.builder()
            .caseData(fullCase.getCaseData())
            .documents(new ArrayList<>(documentsForFormatter))
            .build();

        CcdCallbackRequest ccdCallbackRequest = CcdCallbackRequest.builder()
            .eventId(CASE_ID)
            .caseDetails(fullCase)
            .build();

        stubDocumentGeneratorServerEndpoint(respondentAnswersDocRequest, respondentAnswersDocResponse);
        stubFormatterServerEndpoint(docsReq);

        webClient.perform(post(API_URL)
            .header(AUTHORIZATION, USER_TOKEN)
            .content(ObjectMapperTestUtil.convertObjectToJsonString(ccdCallbackRequest))
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content().string(allOf(
                isJson(),
                hasJsonPath("$.errors", nullValue()),
                hasJsonPath("$.data.documents", isJson()),
                hasJsonPath("$.data.caseData", isJson())
            )));

        verifyZeroInteractions(mockEmailClient);
    }

    @Test
    public void givenCaseWithoutId_whenPerformAOSReceived_thenReturnBadRequestResponse() throws Exception {
        Map<String, Object> caseDetailMap = new HashMap<>();

        caseDetailMap.put(D_8_PETITIONER_EMAIL, D_8_PETITIONER_EMAIL);
        caseDetailMap.put(D_8_PETITIONER_FIRST_NAME, PETITIONER_FIRST_NAME);
        caseDetailMap.put(D_8_PETITIONER_LAST_NAME, PETITIONER_LAST_NAME);
        caseDetailMap.put(D_8_INFERRED_RESPONDENT_GENDER, RESPONDENT_FEMALE_GENDER);
        caseDetailMap.put(D_8_CASE_REFERENCE, D8_ID);
        caseDetailMap.put(RESP_WILL_DEFEND_DIVORCE, "No");

        CcdCallbackRequest ccdCallbackRequest = CcdCallbackRequest.builder().eventId(EVENT_ID)
            .caseDetails(CaseDetails.builder()
                .caseId(CASE_ID)
                .caseData(caseDetailMap)
                .build())
            .build();

        CcdCallbackResponse ccdCallbackResponse = CcdCallbackResponse
            .builder()
            .errors(Collections.singletonList("java.lang.Exception: error"))
            .build();

        mockEmailClientError(D_8_PETITIONER_EMAIL);

        String expectedResponse = ObjectMapperTestUtil.convertObjectToJsonString(ccdCallbackResponse);
        webClient.perform(post(API_URL)
            .header(AUTHORIZATION, USER_TOKEN)
            .content(ObjectMapperTestUtil.convertObjectToJsonString(ccdCallbackRequest))
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content().string(expectedResponse));
    }

    private void mockEmailClientError(String email)
        throws NotificationClientException {
        Map<String, String> notificationTemplateVars = new HashMap<>();
        notificationTemplateVars.put(NOTIFICATION_ADDRESSEE_FIRST_NAME_KEY, PETITIONER_FIRST_NAME);
        notificationTemplateVars.put(NOTIFICATION_ADDRESSEE_LAST_NAME_KEY, PETITIONER_LAST_NAME);
        notificationTemplateVars.put(NOTIFICATION_RELATIONSHIP_KEY, RELATIONSHIP);
        notificationTemplateVars.put(NOTIFICATION_REFERENCE_KEY, D8_ID);
        when(mockEmailClient.sendEmail(any(), eq(email), eq(notificationTemplateVars), any()))
            .thenThrow(new NotificationClientException(new Exception("error")));
    }

    private void stubDocumentGeneratorServerEndpoint(GenerateDocumentRequest generateDocumentRequest,
                                                     GeneratedDocumentInfo response) {
        documentGeneratorServiceServer.stubFor(WireMock.post(GENERATE_DOCUMENT_CONTEXT_PATH)
            .withRequestBody(equalToJson(convertObjectToJsonString(generateDocumentRequest)))
            .withHeader(AUTHORIZATION, new EqualToPattern(USER_TOKEN))
            .willReturn(aResponse()
                .withHeader(CONTENT_TYPE, APPLICATION_JSON_UTF8_VALUE)
                .withStatus(HttpStatus.OK.value())
                .withBody(convertObjectToJsonString(response))));
    }

    private void stubFormatterServerEndpoint(DocumentUpdateRequest data) {
        formatterServiceServer.stubFor(WireMock.post(FORMAT_ADD_DOCUMENTS_CONTEXT_PATH)
            .withRequestBody(equalToJson(convertObjectToJsonString(data)))
            .willReturn(aResponse()
                .withStatus(HttpStatus.OK.value())
                .withHeader(CONTENT_TYPE, APPLICATION_JSON_UTF8_VALUE)
                .withBody(convertObjectToJsonString(data))));
    }
}