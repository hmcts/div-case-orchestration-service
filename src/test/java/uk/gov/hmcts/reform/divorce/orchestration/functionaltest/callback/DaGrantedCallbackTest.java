package uk.gov.hmcts.reform.divorce.orchestration.functionaltest.callback;

import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.matching.EqualToPattern;
import com.google.common.collect.ImmutableMap;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.Features;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CaseDetails;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CcdCallbackRequest;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CcdCallbackResponse;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CollectionMember;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.Document;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.documentgeneration.GeneratedDocumentInfo;
import uk.gov.hmcts.reform.divorce.orchestration.functionaltest.MockedFunctionalTest;
import uk.gov.hmcts.reform.divorce.orchestration.service.BulkPrintService;
import uk.gov.hmcts.reform.divorce.orchestration.service.EmailService;
import uk.gov.hmcts.reform.divorce.orchestration.service.FeatureToggleService;
import uk.gov.service.notify.NotificationClientException;

import java.util.List;
import java.util.Map;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.containing;
import static com.jayway.jsonpath.matchers.JsonPathMatchers.hasJsonPath;
import static java.util.Arrays.asList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.MediaType.ALL_VALUE;
import static org.springframework.http.MediaType.APPLICATION_JSON_UTF8_VALUE;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_DECREE_ABSOLUTE_GRANTED_DATE;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_PETITIONER_EMAIL;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_PETITIONER_FIRST_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_PETITIONER_LAST_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_RESPONDENT_EMAIL;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_RESPONDENT_FIRST_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_RESPONDENT_LAST_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_SERVICE_AUTH_TOKEN;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D8DOCUMENTS_GENERATED;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DECREE_ABSOLUTE_DOCUMENT_TYPE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DECREE_ABSOLUTE_FILENAME;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DECREE_ABSOLUTE_GRANTED_DATE_CCD_FIELD;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DECREE_ABSOLUTE_GRANTED_LETTER_DOCUMENT_TYPE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D_8_CASE_REFERENCE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D_8_PETITIONER_EMAIL;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D_8_PETITIONER_FIRST_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D_8_PETITIONER_LAST_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NO_VALUE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.RESPONDENT_DERIVED_CORRESPONDENCE_ADDRESS;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.RESPONDENT_EMAIL_ADDRESS;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.RESP_FIRST_NAME_CCD_FIELD;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.RESP_IS_USING_DIGITAL_CHANNEL;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.RESP_LAST_NAME_CCD_FIELD;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.YES_VALUE;
import static uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.DocumentContentFetcherService.Headers.SERVICE_AUTHORIZATION;
import static uk.gov.hmcts.reform.divorce.orchestration.testutil.CaseDataTestHelper.createCollectionMemberDocument;
import static uk.gov.hmcts.reform.divorce.orchestration.testutil.ObjectMapperTestUtil.convertObjectToJsonString;

public class DaGrantedCallbackTest extends MockedFunctionalTest {

    private static final String API_URL = "/handle-post-da-granted";

    private static final String DGS_MOCK_ENDPOINT = "http://localhost:4020/documents/";
    private static final String ADD_DOCUMENTS_CONTEXT_PATH = "/caseformatter/version/1/add-documents";
    private static final String GENERATE_DOCUMENT_CONTEXT_PATH = "/version/1/generatePDF";
    private static final String SERVICE_AUTH_CONTEXT_PATH = "/lease";

    private static final String DECREE_ABSOLUTE_LETTER_ID = "f1029b24-0a3f-4e74-82df-c7d2c33189e0";
    private static final String DECREE_ABSOLUTE_ID = "7d10126d-0e88-4f0e-b475-628b54a87ca6";

    private static final Map<String, Object> BASE_CASE_DATA = ImmutableMap.<String, Object>builder()
        .put(D_8_PETITIONER_FIRST_NAME, TEST_PETITIONER_FIRST_NAME)
        .put(D_8_PETITIONER_LAST_NAME, TEST_PETITIONER_LAST_NAME)
        .put(D_8_PETITIONER_EMAIL, TEST_PETITIONER_EMAIL)
        .put(RESP_FIRST_NAME_CCD_FIELD, TEST_RESPONDENT_FIRST_NAME)
        .put(RESP_LAST_NAME_CCD_FIELD, TEST_RESPONDENT_LAST_NAME)
        .put(RESPONDENT_EMAIL_ADDRESS, TEST_RESPONDENT_EMAIL)
        .put(RESPONDENT_DERIVED_CORRESPONDENCE_ADDRESS, "221B Baker Street")
        .put(D_8_CASE_REFERENCE, TEST_CASE_ID)
        .put(DECREE_ABSOLUTE_GRANTED_DATE_CCD_FIELD, TEST_DECREE_ABSOLUTE_GRANTED_DATE)
        .build();

    @Captor
    private ArgumentCaptor<List<GeneratedDocumentInfo>> documentsToPrintCaptor;

    @Autowired
    private MockMvc webClient;

    @MockBean
    private EmailService mockEmailService;

    @MockBean
    private BulkPrintService bulkPrintService;

    @MockBean
    private FeatureToggleService featureToggleService;

    @Test
    public void givenOnlineRespondentDetails_ThenOkResponse() throws Exception {
        Map caseData = ImmutableMap.builder().putAll(BASE_CASE_DATA).put(RESP_IS_USING_DIGITAL_CHANNEL, YES_VALUE).build();

        CcdCallbackResponse expectedResponse = CcdCallbackResponse.builder().data(BASE_CASE_DATA).build();
        webClient.perform(post(API_URL)
            .header(AUTHORIZATION, AUTH_TOKEN)
            .contentType(MediaType.APPLICATION_JSON)
            .content(convertObjectToJsonString(
                CcdCallbackRequest.builder()
                    .caseDetails(CaseDetails.builder().caseId(TEST_CASE_ID).caseData(caseData).build())
                    .build()
            ))
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content().json(convertObjectToJsonString(expectedResponse)));
        verify(mockEmailService).sendEmailAndReturnExceptionIfFails(eq(TEST_PETITIONER_EMAIL), anyString(), anyMap(), anyString());
        verify(mockEmailService).sendEmailAndReturnExceptionIfFails(eq(TEST_RESPONDENT_EMAIL), anyString(), anyMap(), anyString());
    }

    @Test
    public void givenOfflineRespondentDetails_ThenOkResponse() throws Exception {
        //Given
        byte[] decreeAbsoluteLetterBytes = new byte[] {1, 2, 3};
        byte[] decreeAbsoluteBytes = new byte[] {4, 5, 6};
        stubDMStore("/documents/" + DECREE_ABSOLUTE_LETTER_ID + "/binary", decreeAbsoluteLetterBytes);
        stubDMStore("/documents/" + DECREE_ABSOLUTE_ID + "/binary", decreeAbsoluteBytes);

        when(featureToggleService.isFeatureEnabled(Features.PAPER_UPDATE)).thenReturn(true);
        stubServiceAuthProvider(TEST_SERVICE_AUTH_TOKEN);

        //Newly generated document
        String daGrantedLetterUrl = DGS_MOCK_ENDPOINT + DECREE_ABSOLUTE_LETTER_ID;
        CollectionMember<Document> daGrantedLetterDocument = createCollectionMemberDocument(daGrantedLetterUrl,
            DECREE_ABSOLUTE_GRANTED_LETTER_DOCUMENT_TYPE,
            "daLetter.pdf");
        GeneratedDocumentInfo daGrantedLetter = GeneratedDocumentInfo.builder()
            .documentType(DECREE_ABSOLUTE_GRANTED_LETTER_DOCUMENT_TYPE)
            .fileName("DA-granted-letter.pdf")
            .url(daGrantedLetterUrl)
            .build();
        stubDocumentGeneratorServerEndpoint(daGrantedLetter);

        //Existing document
        CollectionMember<Document> daGrantedDocument = createCollectionMemberDocument(DGS_MOCK_ENDPOINT + DECREE_ABSOLUTE_ID,
            DECREE_ABSOLUTE_DOCUMENT_TYPE,
            DECREE_ABSOLUTE_FILENAME);
        Map<String, Object> caseData = new ImmutableMap.Builder<String, Object>().putAll(BASE_CASE_DATA)
            .put(RESP_IS_USING_DIGITAL_CHANNEL, NO_VALUE)
            .put(D8DOCUMENTS_GENERATED, asList(daGrantedDocument))
            .build();

        //Mocking CFS returns formatted document
        mockCfsToRespondWithNewDocument(daGrantedLetter, asList(daGrantedDocument, daGrantedLetterDocument));

        //When
        CcdCallbackResponse expectedResponse = CcdCallbackResponse.builder().data(BASE_CASE_DATA).build();
        webClient.perform(post(API_URL)
            .header(AUTHORIZATION, AUTH_TOKEN)
            .contentType(MediaType.APPLICATION_JSON)
            .content(convertObjectToJsonString(
                CcdCallbackRequest.builder()
                    .caseDetails(CaseDetails.builder().caseId(TEST_CASE_ID).caseData(caseData).build())
                    .build()
            ))
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content().json(convertObjectToJsonString(expectedResponse)));

        //Then
        verify(bulkPrintService).send(eq(TEST_CASE_ID), anyString(), documentsToPrintCaptor.capture());
        List<GeneratedDocumentInfo> documentsSentToBulkPrint = documentsToPrintCaptor.getValue();
        assertThat(documentsSentToBulkPrint, hasSize(2));
        assertThat(documentsSentToBulkPrint.get(0).getBytes(), is(decreeAbsoluteLetterBytes));
        assertThat(documentsSentToBulkPrint.get(1).getBytes(), is(decreeAbsoluteBytes));
        verifyZeroInteractions(mockEmailService);
    }

    @Test
    public void responseShouldContainErrorsIfServiceFails() throws Exception {
        doThrow(new NotificationClientException("This has failed."))
            .when(mockEmailService).sendEmailAndReturnExceptionIfFails(anyString(), anyString(), anyMap(), anyString());

        Map caseData = ImmutableMap.builder().putAll(BASE_CASE_DATA).put(RESP_IS_USING_DIGITAL_CHANNEL, YES_VALUE).build();
        webClient.perform(post(API_URL)
            .header(AUTHORIZATION, AUTH_TOKEN)
            .contentType(MediaType.APPLICATION_JSON)
            .content(convertObjectToJsonString(
                CcdCallbackRequest.builder()
                    .caseDetails(CaseDetails.builder().caseId(TEST_CASE_ID).caseData(caseData).build())
                    .build()
            ))
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content().string(hasJsonPath("$.errors", hasItem("This has failed."))));
    }

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
            .content(convertObjectToJsonString(
                CcdCallbackRequest.builder()
                    .caseDetails(CaseDetails.builder().caseId(TEST_CASE_ID).caseData(BASE_CASE_DATA).build())
                    .build()
            ))
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isBadRequest());
    }

    private void stubDocumentGeneratorServerEndpoint(GeneratedDocumentInfo response) {
        documentGeneratorServiceServer.stubFor(WireMock.post(GENERATE_DOCUMENT_CONTEXT_PATH)
            .withHeader(AUTHORIZATION, new EqualToPattern(AUTH_TOKEN))
            .willReturn(aResponse()
                .withHeader(CONTENT_TYPE, APPLICATION_JSON_UTF8_VALUE)
                .withStatus(HttpStatus.OK.value())
                .withBody(convertObjectToJsonString(response))));
    }

    private void stubDMStore(String binaryUrl, byte[] fileBytes) {
        documentStore.stubFor(WireMock.get(binaryUrl)
            .withHeader(SERVICE_AUTHORIZATION, new EqualToPattern("Bearer " + TEST_SERVICE_AUTH_TOKEN))
            .withHeader("user-roles", new EqualToPattern("caseworker-divorce"))
            .willReturn(aResponse()
                .withStatus(HttpStatus.OK.value())
                .withHeader(CONTENT_TYPE, ALL_VALUE)
                .withBody(fileBytes)));
    }

    private void stubServiceAuthProvider(String response) {
        serviceAuthProviderServer.stubFor(WireMock.post(SERVICE_AUTH_CONTEXT_PATH)
            .willReturn(aResponse()
                .withStatus(HttpStatus.OK.value())
                .withBody(response)));
    }

    private void mockCfsToRespondWithNewDocument(GeneratedDocumentInfo newDocumentInfo, List<CollectionMember<Document>> listOfDocumentsToReturn) {
        Map<String, Object> cfsMockResponse = new ImmutableMap.Builder<String, Object>().putAll(BASE_CASE_DATA)
            .put(RESP_IS_USING_DIGITAL_CHANNEL, NO_VALUE)
            .put(D8DOCUMENTS_GENERATED, listOfDocumentsToReturn)
            .build();
        stubFormatterServerEndpoint(newDocumentInfo, cfsMockResponse);
    }

    private void stubFormatterServerEndpoint(GeneratedDocumentInfo generatedDocumentInfo,
                                             Map<String, Object> response) {
        formatterServiceServer.stubFor(WireMock.post(ADD_DOCUMENTS_CONTEXT_PATH)
            .withRequestBody(containing(convertObjectToJsonString(generatedDocumentInfo)))
            .willReturn(aResponse()
                .withHeader(CONTENT_TYPE, APPLICATION_JSON_UTF8_VALUE)
                .withStatus(HttpStatus.OK.value())
                .withBody(convertObjectToJsonString(response))));
    }

}