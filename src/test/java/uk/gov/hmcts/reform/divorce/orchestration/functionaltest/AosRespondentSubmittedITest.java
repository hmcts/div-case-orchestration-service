package uk.gov.hmcts.reform.divorce.orchestration.functionaltest;

import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.junit.WireMockClassRule;
import com.github.tomakehurst.wiremock.matching.EqualToPattern;
import com.google.common.collect.ImmutableMap;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.PropertySource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import uk.gov.hmcts.reform.divorce.orchestration.OrchestrationServiceApplication;
import uk.gov.hmcts.reform.divorce.orchestration.client.DocumentGeneratorClient;
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
import static java.util.Collections.singletonMap;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.MediaType.APPLICATION_JSON_UTF8_VALUE;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DOCUMENT_CASE_DETAILS_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DOCUMENT_TYPE_PETITION;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DOCUMENT_TYPE_RESPONDENT_INVITATION;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D_8_CASE_REFERENCE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D_8_INFERRED_RESPONDENT_GENDER;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D_8_PETITIONER_EMAIL;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D_8_PETITIONER_FIRST_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D_8_PETITIONER_LAST_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.MINI_PETITION_FILE_NAME_FORMAT;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.MINI_PETITION_TEMPLATE_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NOTIFICATION_ADDRESSEE_FIRST_NAME_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NOTIFICATION_ADDRESSEE_LAST_NAME_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NOTIFICATION_REFERENCE_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NOTIFICATION_RELATIONSHIP_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.RESPONDENT_ANSWERS_TEMPLATE_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.testutil.ObjectMapperTestUtil.convertObjectToJsonString;

@RunWith(SpringRunner.class)
@ContextConfiguration(classes = OrchestrationServiceApplication.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@PropertySource(value = "classpath:application.yml")
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
@AutoConfigureMockMvc
public class AosRespondentSubmittedITest {
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

    @ClassRule
    public static WireMockClassRule documentGeneratorServer = new WireMockClassRule(4007);

    @ClassRule
    public static WireMockClassRule formatterServiceServer = new WireMockClassRule(4011);

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
    public void givenWithoutPetitionerEmail_whenPerformAOSReceived_thenReturnBadRequestResponse()
        throws Exception {
        mockEmailClient("null");
        Map<String, Object> caseDetailMap = ImmutableMap.of(
            D_8_CASE_REFERENCE, D8_ID,
            D_8_PETITIONER_FIRST_NAME, PETITIONER_FIRST_NAME,
            D_8_PETITIONER_LAST_NAME, PETITIONER_LAST_NAME,
            D_8_INFERRED_RESPONDENT_GENDER, RESPONDENT_FEMALE_GENDER
        );

        CcdCallbackResponse ccdCallbackResponse = CcdCallbackResponse
            .builder()
            .errors(Collections.singletonList("No destination email given"))
            .build();

        String expectedResponse = ObjectMapperTestUtil.convertObjectToJsonString(ccdCallbackResponse);
        CaseDetails fullCase = CaseDetails.builder()
            .caseData(caseDetailMap)
            .build();

        CcdCallbackRequest ccdCallbackRequest = CcdCallbackRequest.builder()
            .eventId(EVENT_ID)
            .caseDetails(fullCase)
            .build();

        final GenerateDocumentRequest respondentAnswersDocRequest =
            GenerateDocumentRequest.builder()
                .template(RESPONDENT_ANSWERS_TEMPLATE_NAME)
                .values(singletonMap(DOCUMENT_CASE_DETAILS_JSON_KEY, fullCase))
                .build();

        final GeneratedDocumentInfo respondentAnswersDocResponse =
            GeneratedDocumentInfo.builder()
                .documentType(DOCUMENT_TYPE_RESPONDENT_INVITATION)
                .fileName(RESPONDENT_ANSWERS_TEMPLATE_NAME)
                .build();
        final Set<GeneratedDocumentInfo> documentsForFormatter = new HashSet<>();
        documentsForFormatter.add(respondentAnswersDocResponse);
        DocumentUpdateRequest docsReq = DocumentUpdateRequest.builder()
            .caseData(fullCase.getCaseData())
            .documents(new ArrayList<>(documentsForFormatter))
            .build();

        stubDocumentGeneratorServerEndpoint(respondentAnswersDocRequest, respondentAnswersDocResponse);
        stubFormatterServerEndpoint(docsReq);

        webClient.perform(post(API_URL)
            .header(AUTHORIZATION, USER_TOKEN)
            .content(ObjectMapperTestUtil.convertObjectToJsonString(ccdCallbackRequest))
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content().string(expectedResponse));
    }

    @Test
    public void givenCaseData_whenPerformAOSReceived_thenReturnCaseData() throws Exception {
        Map<String, Object> caseDetailMap = ImmutableMap.of(
            D_8_PETITIONER_EMAIL, D_8_PETITIONER_EMAIL,
            D_8_PETITIONER_FIRST_NAME, PETITIONER_FIRST_NAME,
            D_8_PETITIONER_LAST_NAME, PETITIONER_LAST_NAME,
            D_8_INFERRED_RESPONDENT_GENDER, RESPONDENT_FEMALE_GENDER,
            D_8_CASE_REFERENCE, D8_ID
        );

        CaseDetails fullCase = CaseDetails.builder()
            .caseData(caseDetailMap)
            .build();

        CcdCallbackRequest ccdCallbackRequest = CcdCallbackRequest.builder().eventId(CASE_ID)
            .caseDetails(fullCase)
            .build();

        final GenerateDocumentRequest respondentAnswersDocRequest =
            GenerateDocumentRequest.builder()
                .template(RESPONDENT_ANSWERS_TEMPLATE_NAME)
                .values(singletonMap(DOCUMENT_CASE_DETAILS_JSON_KEY, fullCase))
                .build();

        final GeneratedDocumentInfo respondentAnswersDocResponse =
            GeneratedDocumentInfo.builder()
                .documentType(DOCUMENT_TYPE_RESPONDENT_INVITATION)
                .fileName(RESPONDENT_ANSWERS_TEMPLATE_NAME)
                .build();
        final Set<GeneratedDocumentInfo> documentsForFormatter = new HashSet<>();
        documentsForFormatter.add(respondentAnswersDocResponse);
        DocumentUpdateRequest docsReq = DocumentUpdateRequest.builder()
            .caseData(fullCase.getCaseData())
            .documents(new ArrayList<>(documentsForFormatter))
            .build();

        stubDocumentGeneratorServerEndpoint(respondentAnswersDocRequest, respondentAnswersDocResponse);
        stubFormatterServerEndpoint(docsReq);

        Map<String, Object> responseData = new HashMap<>();
        responseData.put("caseData", caseDetailMap);
        responseData.put("documents", new ArrayList<>(documentsForFormatter));

        CcdCallbackResponse ccdCallbackResponse = CcdCallbackResponse.builder()
            .data(responseData)
            .build();

        String expectedResponse = ObjectMapperTestUtil.convertObjectToJsonString(ccdCallbackResponse);

        webClient.perform(post(API_URL)
            .header(AUTHORIZATION, USER_TOKEN)
            .content(ObjectMapperTestUtil.convertObjectToJsonString(ccdCallbackRequest))
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content().string(expectedResponse));
    }

    @Test
    public void givenCaseWithoutId_whenPerformAOSReceived_thenReturnBadRequestResponse() throws Exception {
        Map<String, Object> caseDetailMap = ImmutableMap.of(
            D_8_PETITIONER_EMAIL, D_8_PETITIONER_EMAIL,
            D_8_PETITIONER_FIRST_NAME, PETITIONER_FIRST_NAME,
            D_8_PETITIONER_LAST_NAME, PETITIONER_LAST_NAME,
            D_8_INFERRED_RESPONDENT_GENDER, RESPONDENT_FEMALE_GENDER,
            D_8_CASE_REFERENCE, D8_ID
        );

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

        mockEmailClient(D_8_PETITIONER_EMAIL);

        String expectedResponse = ObjectMapperTestUtil.convertObjectToJsonString(ccdCallbackResponse);
        webClient.perform(post(API_URL)
            .header(AUTHORIZATION, USER_TOKEN)
            .content(ObjectMapperTestUtil.convertObjectToJsonString(ccdCallbackRequest))
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content().string(expectedResponse));
    }

    private void mockEmailClient(String email)
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
        documentGeneratorServer.stubFor(WireMock.post(GENERATE_DOCUMENT_CONTEXT_PATH)
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
