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
import uk.gov.hmcts.reform.divorce.orchestration.client.EmailClient;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CaseDetails;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CcdCallbackRequest;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.courts.Court;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.documentgeneration.DocumentUpdateRequest;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.documentgeneration.GenerateDocumentRequest;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.documentgeneration.GeneratedDocumentInfo;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.TaskCommons;
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
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.MediaType.APPLICATION_JSON_UTF8_VALUE;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_COURT;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_EMAIL;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_EXPECTED_DUE_DATE;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_EXPECTED_DUE_DATE_FORMATTED;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CO_RESPONDENT_ANSWERS_TEMPLATE_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CO_RESPONDENT_DEFENDS_DIVORCE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CO_RESPONDENT_DUE_DATE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CO_RESP_EMAIL_ADDRESS;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D8_REASON_FOR_DIVORCE_ADULTERY_3RD_PARTY_FNAME;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D8_REASON_FOR_DIVORCE_ADULTERY_3RD_PARTY_LNAME;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DOCUMENT_CASE_DETAILS_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DOCUMENT_TYPE_CO_RESPONDENT_ANSWERS;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D_8_CASE_REFERENCE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D_8_DIVORCE_UNIT;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NOTIFICATION_ADDRESSEE_FIRST_NAME_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NOTIFICATION_ADDRESSEE_LAST_NAME_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NOTIFICATION_CASE_NUMBER_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NOTIFICATION_COURT_ADDRESS_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NOTIFICATION_FORM_SUBMISSION_DATE_LIMIT_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NOTIFICATION_RDC_NAME_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NO_VALUE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.YES_VALUE;
import static uk.gov.hmcts.reform.divorce.orchestration.testutil.ObjectMapperTestUtil.convertObjectToJsonString;

@RunWith(SpringRunner.class)
@ContextConfiguration(classes = OrchestrationServiceApplication.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@PropertySource(value = "classpath:application.yml")
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
@AutoConfigureMockMvc
public class CoRespondentSubmittedITest {
    private static final String API_URL = "/co-respondent-received";

    private static final String USER_TOKEN = "anytoken";

    private static final String GENERATE_DOCUMENT_CONTEXT_PATH = "/version/1/generatePDF";
    private static final String FORMAT_ADD_DOCUMENTS_CONTEXT_PATH = "/caseformatter/version/1/add-documents";

    private static final String CO_RESP_FIRST_NAME = "any-name";
    private static final String CO_RESP_LAST_NAME = "any-last-name";

    private static final String EVENT_ID = "event-id";
    private static final String CASE_ID = "case-id";
    private static final String D8_ID = "d8-id";

    @Autowired
    private MockMvc webClient;

    @Autowired
    private TaskCommons taskCommons;

    @ClassRule
    public static WireMockClassRule documentGeneratorServer = new WireMockClassRule(4007);

    @ClassRule
    public static WireMockClassRule formatterServiceServer = new WireMockClassRule(4011);

    @MockBean
    private EmailClient mockClient;

    @Test
    public void givenEmptyBody_whenPerformAOSReceived_thenReturnBadRequestResponse() throws Exception {
        webClient.perform(post(API_URL)
                .header(AUTHORIZATION, USER_TOKEN)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void givenCaseData_whenPerformCoRespReceived_thenReturnCaseData() throws Exception {
        Map<String, Object> caseDetailMap = ImmutableMap.of(
            D_8_CASE_REFERENCE, D8_ID,
            D8_REASON_FOR_DIVORCE_ADULTERY_3RD_PARTY_FNAME, CO_RESP_FIRST_NAME,
            D8_REASON_FOR_DIVORCE_ADULTERY_3RD_PARTY_LNAME, CO_RESP_LAST_NAME,
            CO_RESP_EMAIL_ADDRESS, TEST_EMAIL,
            CO_RESPONDENT_DEFENDS_DIVORCE, NO_VALUE
        );

        CaseDetails caseDetails = CaseDetails.builder().caseData(caseDetailMap).build();

        final GenerateDocumentRequest documentRequest = GenerateDocumentRequest.builder()
                .template(CO_RESPONDENT_ANSWERS_TEMPLATE_NAME)
                .values(singletonMap(DOCUMENT_CASE_DETAILS_JSON_KEY, caseDetails))
                .build();

        final GeneratedDocumentInfo documentInfo = GeneratedDocumentInfo.builder()
                .documentType(DOCUMENT_TYPE_CO_RESPONDENT_ANSWERS)
                .fileName(DOCUMENT_TYPE_CO_RESPONDENT_ANSWERS)
                .build();

        final Set<GeneratedDocumentInfo> documentsForFormatter = new HashSet<>();
        documentsForFormatter.add(documentInfo);

        DocumentUpdateRequest documentFormatRequest = DocumentUpdateRequest.builder()
            .caseData(caseDetailMap)
            .documents(new ArrayList<>(documentsForFormatter))
            .build();

        stubDocumentGeneratorServerEndpoint(documentRequest, documentInfo);
        stubFormatterServerEndpoint(documentFormatRequest);

        CcdCallbackRequest ccdCallbackRequest = CcdCallbackRequest.builder().eventId(CASE_ID)
                .caseDetails(caseDetails)
                .build();

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

        verifyEmailSent(TEST_EMAIL, Collections.EMPTY_MAP);
    }

    @Test
    public void givenDefendedCoRespondCase_whenPerformCoRespReceived_thenReturnCaseData() throws Exception {
        Map<String, Object> caseDetailMap = new HashMap<>();
        caseDetailMap.put(D_8_CASE_REFERENCE, D8_ID);
        caseDetailMap.put(D8_REASON_FOR_DIVORCE_ADULTERY_3RD_PARTY_FNAME, CO_RESP_FIRST_NAME);
        caseDetailMap.put(D8_REASON_FOR_DIVORCE_ADULTERY_3RD_PARTY_LNAME, CO_RESP_LAST_NAME);
        caseDetailMap.put(CO_RESP_EMAIL_ADDRESS, TEST_EMAIL);
        caseDetailMap.put(CO_RESPONDENT_DEFENDS_DIVORCE, YES_VALUE);
        caseDetailMap.put(CO_RESPONDENT_DUE_DATE, TEST_EXPECTED_DUE_DATE);
        caseDetailMap.put(D_8_DIVORCE_UNIT, TEST_COURT);

        CaseDetails caseDetails = CaseDetails.builder().caseData(caseDetailMap).build();

        final GenerateDocumentRequest documentRequest = GenerateDocumentRequest.builder()
            .template(CO_RESPONDENT_ANSWERS_TEMPLATE_NAME)
            .values(singletonMap(DOCUMENT_CASE_DETAILS_JSON_KEY, caseDetails))
            .build();

        final GeneratedDocumentInfo documentInfo = GeneratedDocumentInfo.builder()
                .documentType(DOCUMENT_TYPE_CO_RESPONDENT_ANSWERS)
                .fileName(DOCUMENT_TYPE_CO_RESPONDENT_ANSWERS)
                .build();

        final Set<GeneratedDocumentInfo> documentsForFormatter = new HashSet<>();
        documentsForFormatter.add(documentInfo);

        DocumentUpdateRequest documentFormatRequest = DocumentUpdateRequest.builder()
            .caseData(caseDetailMap)
            .documents(new ArrayList<>(documentsForFormatter))
            .build();

        stubDocumentGeneratorServerEndpoint(documentRequest, documentInfo);
        stubFormatterServerEndpoint(documentFormatRequest);

        CcdCallbackRequest ccdCallbackRequest = CcdCallbackRequest.builder().eventId(EVENT_ID)
                .caseDetails(caseDetails)
                .build();

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
                
        Court court = taskCommons.getCourt(TEST_COURT);

        Map<String, String> expectedExtraFields = ImmutableMap.of(
            NOTIFICATION_COURT_ADDRESS_KEY, court.getFormattedAddress(),
            NOTIFICATION_RDC_NAME_KEY, court.getIdentifiableCentreName(),
            NOTIFICATION_FORM_SUBMISSION_DATE_LIMIT_KEY, TEST_EXPECTED_DUE_DATE_FORMATTED
        );

        verifyEmailSent(TEST_EMAIL, expectedExtraFields);
    }

    private void verifyEmailSent(String email, Map<String, String> additionalData) throws NotificationClientException {
        Map<String, String> notificationTemplateVars = new HashMap<>(additionalData);
        notificationTemplateVars.put(NOTIFICATION_ADDRESSEE_FIRST_NAME_KEY, CO_RESP_FIRST_NAME);
        notificationTemplateVars.put(NOTIFICATION_ADDRESSEE_LAST_NAME_KEY, CO_RESP_LAST_NAME);
        notificationTemplateVars.put(NOTIFICATION_CASE_NUMBER_KEY, D8_ID);
        verify(mockClient).sendEmail(any(), eq(email), eq(notificationTemplateVars), any());
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