package uk.gov.hmcts.reform.divorce.orchestration.functionaltest.callback;

import com.google.common.collect.ImmutableMap;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import uk.gov.hmcts.reform.divorce.model.ccd.CollectionMember;
import uk.gov.hmcts.reform.divorce.model.ccd.Document;
import uk.gov.hmcts.reform.divorce.model.documentupdate.GeneratedDocumentInfo;
import uk.gov.hmcts.reform.divorce.orchestration.client.EmailClient;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.Features;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CaseDetails;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CcdCallbackRequest;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CcdCallbackResponse;
import uk.gov.hmcts.reform.divorce.orchestration.functionaltest.MockedFunctionalTest;
import uk.gov.hmcts.reform.divorce.orchestration.service.BulkPrintService;
import uk.gov.hmcts.reform.divorce.orchestration.service.FeatureToggleService;
import uk.gov.service.notify.NotificationClientException;

import java.util.List;
import java.util.Map;

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
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_CUSTOMER_FACING_NOTIFICATION_LIMIT_DATE_TO_DOWNLOAD_CERTIFICATE;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_DECREE_ABSOLUTE_GRANTED_DATE;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_PETITIONER_EMAIL;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_PETITIONER_FIRST_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_PETITIONER_FULL_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_PETITIONER_LAST_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_RESPONDENT_EMAIL;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_RESPONDENT_FIRST_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_RESPONDENT_FULL_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_RESPONDENT_LAST_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_RESP_SOLICITOR_EMAIL;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_RESP_SOLICITOR_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_SERVICE_AUTH_TOKEN;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_SOLICITOR_EMAIL;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_SOLICITOR_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_WELSH_CUSTOMER_FACING_NOTIFICATION_LIMIT_DATE_TO_DOWNLOAD_CERTIFICATE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.CaseFieldConstants.RESPONDENT_SOLICITOR_DERIVED_CORRESPONDENCE_ADDRESS;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.LanguagePreference.ENGLISH;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D8DOCUMENTS_GENERATED;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D8_RESPONDENT_SOLICITOR_EMAIL;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D8_RESPONDENT_SOLICITOR_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D8_RESPONDENT_SOLICITOR_REFERENCE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DECREE_ABSOLUTE_DOCUMENT_TYPE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DECREE_ABSOLUTE_FILENAME;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DECREE_ABSOLUTE_GRANTED_CITIZEN_LETTER_DOCUMENT_TYPE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DECREE_ABSOLUTE_GRANTED_DATE_CCD_FIELD;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DECREE_ABSOLUTE_GRANTED_SOLICITOR_LETTER_DOCUMENT_TYPE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D_8_CASE_REFERENCE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D_8_PETITIONER_EMAIL;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D_8_PETITIONER_FIRST_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D_8_PETITIONER_LAST_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NOTIFICATION_ADDRESSEE_FIRST_NAME_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NOTIFICATION_ADDRESSEE_LAST_NAME_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NOTIFICATION_CASE_NUMBER_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NOTIFICATION_CCD_REFERENCE_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NOTIFICATION_EMAIL;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NOTIFICATION_LIMIT_DATE_TO_DOWNLOAD_CERTIFICATE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NOTIFICATION_PET_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NOTIFICATION_RESP_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NOTIFICATION_SOLICITOR_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NOTIFICATION_WELSH_LIMIT_DATE_TO_DOWNLOAD_CERTIFICATE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NO_VALUE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.PETITIONER_SOLICITOR_EMAIL;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.PETITIONER_SOLICITOR_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.RESPONDENT_DERIVED_CORRESPONDENCE_ADDRESS;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.RESPONDENT_EMAIL_ADDRESS;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.RESP_FIRST_NAME_CCD_FIELD;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.RESP_IS_USING_DIGITAL_CHANNEL;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.RESP_LAST_NAME_CCD_FIELD;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.RESP_SOL_REPRESENTED;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.YES_VALUE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.document.template.DocumentType.DECREE_ABSOLUTE_GRANTED_CITIZEN_LETTER;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.document.template.DocumentType.DECREE_ABSOLUTE_GRANTED_SOLICITOR_LETTER;
import static uk.gov.hmcts.reform.divorce.orchestration.testutil.CaseDataTestHelper.createCollectionMemberDocument;
import static uk.gov.hmcts.reform.divorce.orchestration.testutil.ObjectMapperTestUtil.convertObjectToJsonString;

public class DaGrantedCallbackTest extends MockedFunctionalTest {

    private static final String API_URL = "/handle-post-da-granted";

    private static final String DECREE_ABSOLUTE_ID = "812f2709-7891-4f7e-835e-11a84a1fa008";

    private static final String DA_GRANTED_NOTIFICATION_TEMPLATE_ID = "b25d9f31-a67e-42f6-a606-0083d273f149";
    private static final String SOL_DA_GRANTED_NOTIFICATION_TEMPLATE_ID = "fc8f7343-8f07-42b7-a3c5-52543a21015d";

    private static final Map<String, Object> BASE_CASE_DATA = ImmutableMap.<String, Object>builder()
        .put(D_8_PETITIONER_FIRST_NAME, TEST_PETITIONER_FIRST_NAME)
        .put(D_8_PETITIONER_LAST_NAME, TEST_PETITIONER_LAST_NAME)
        .put(D_8_PETITIONER_EMAIL, TEST_PETITIONER_EMAIL)
        .put(RESP_FIRST_NAME_CCD_FIELD, TEST_RESPONDENT_FIRST_NAME)
        .put(RESP_LAST_NAME_CCD_FIELD, TEST_RESPONDENT_LAST_NAME)
        .put(RESPONDENT_EMAIL_ADDRESS, TEST_RESPONDENT_EMAIL)
        .put(RESPONDENT_DERIVED_CORRESPONDENCE_ADDRESS, "221B Baker Street")
        .put(RESPONDENT_SOLICITOR_DERIVED_CORRESPONDENCE_ADDRESS, "10 Solicitor Avenue")
        .put(D8_RESPONDENT_SOLICITOR_REFERENCE, "123Sol")
        .put(D_8_CASE_REFERENCE, TEST_CASE_ID)
        .put(DECREE_ABSOLUTE_GRANTED_DATE_CCD_FIELD, TEST_DECREE_ABSOLUTE_GRANTED_DATE)
        .build();

    @Captor
    private ArgumentCaptor<List<GeneratedDocumentInfo>> documentsToPrintCaptor;

    @Autowired
    private MockMvc webClient;

    @MockBean
    private EmailClient emailClient;

    @MockBean
    private BulkPrintService bulkPrintService;

    @MockBean
    private FeatureToggleService featureToggleService;

    @Test
    public void givenOnlineRespondentDetails_ThenOkResponse() throws Exception {
        Map<String, Object> caseData = ImmutableMap.<String, Object>builder()
            .putAll(BASE_CASE_DATA)
            .put(RESP_IS_USING_DIGITAL_CHANNEL, YES_VALUE)
            .build();

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

        verifyPetitionerEmailWasSent();
        verifyRespondentEmailWasSent();
        verifyNoMoreInteractions(emailClient);
    }

    @Test
    public void givenOfflineRespondentDetails_ThenOkResponse() throws Exception {
        //Given
        when(featureToggleService.isFeatureEnabled(Features.PAPER_UPDATE)).thenReturn(true);
        stubServiceAuthProvider(HttpStatus.OK, TEST_SERVICE_AUTH_TOKEN);

        //Newly generated document
        byte[] decreeAbsoluteLetterBytes = new byte[] {1, 2, 3};
        String daGrantedLetterDocumentId =
            stubDocumentGeneratorService(DECREE_ABSOLUTE_GRANTED_CITIZEN_LETTER.getTemplateByLanguage(ENGLISH),
                DECREE_ABSOLUTE_GRANTED_CITIZEN_LETTER_DOCUMENT_TYPE);
        stubDMStore(daGrantedLetterDocumentId, decreeAbsoluteLetterBytes);

        //Existing document
        byte[] decreeAbsoluteBytes = new byte[] {4, 5, 6};
        stubDMStore(DECREE_ABSOLUTE_ID, decreeAbsoluteBytes);
        CollectionMember<Document> daGrantedDocument = createCollectionMemberDocument(getDocumentStoreTestUrl(DECREE_ABSOLUTE_ID),
            DECREE_ABSOLUTE_DOCUMENT_TYPE,
            DECREE_ABSOLUTE_FILENAME);
        Map<String, Object> caseData = new ImmutableMap.Builder<String, Object>().putAll(BASE_CASE_DATA)
            .put(RESP_IS_USING_DIGITAL_CHANNEL, NO_VALUE)
            .put(D8DOCUMENTS_GENERATED, asList(daGrantedDocument))
            .build();

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
        verifyNoInteractions(emailClient);
    }

    @Test
    public void givenOfflineRepresentedRespondentDetails_ThenOkResponse() throws Exception {
        //Given
        when(featureToggleService.isFeatureEnabled(Features.PAPER_UPDATE)).thenReturn(true);
        stubServiceAuthProvider(HttpStatus.OK, TEST_SERVICE_AUTH_TOKEN);

        //Newly generated document
        byte[] decreeAbsoluteLetterBytes = new byte[] {1, 2, 3};
        String daGrantedLetterDocumentId = stubDocumentGeneratorService(
            DECREE_ABSOLUTE_GRANTED_SOLICITOR_LETTER.getTemplateByLanguage(ENGLISH),
            DECREE_ABSOLUTE_GRANTED_SOLICITOR_LETTER_DOCUMENT_TYPE
        );
        stubDMStore(daGrantedLetterDocumentId, decreeAbsoluteLetterBytes);

        //Existing document
        byte[] decreeAbsoluteBytes = new byte[] {4, 5, 6};
        stubDMStore(DECREE_ABSOLUTE_ID, decreeAbsoluteBytes);
        CollectionMember<Document> daGrantedDocument = createCollectionMemberDocument(getDocumentStoreTestUrl(DECREE_ABSOLUTE_ID),
            DECREE_ABSOLUTE_DOCUMENT_TYPE,
            DECREE_ABSOLUTE_FILENAME);
        Map<String, Object> caseData = new ImmutableMap.Builder<String, Object>().putAll(BASE_CASE_DATA)
            .put(RESP_IS_USING_DIGITAL_CHANNEL, NO_VALUE)
            .put(RESP_SOL_REPRESENTED, YES_VALUE)
            .put(D8DOCUMENTS_GENERATED, asList(daGrantedDocument))
            .build();

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
        verifyNoInteractions(emailClient);
    }

    @Test
    public void givenOnlineRepresentedRespondentDetails_ThenOkResponse() throws Exception {
        Map<String, Object> caseData = ImmutableMap.<String, Object>builder()
            .putAll(BASE_CASE_DATA)
            .put(RESP_IS_USING_DIGITAL_CHANNEL, YES_VALUE)
            .put(RESP_SOL_REPRESENTED, YES_VALUE)
            .put(D8_RESPONDENT_SOLICITOR_EMAIL, TEST_RESP_SOLICITOR_EMAIL)
            .put(D8_RESPONDENT_SOLICITOR_NAME, TEST_RESP_SOLICITOR_NAME)
            .build();

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

        verifyPetitionerEmailWasSent();
        verifyRespondentSolicitorWasSentEmail();
        verifyNoMoreInteractions(emailClient);
    }

    @Test
    public void givenOnlineRepresentedPetitionerDetails_ThenOkResponse() throws Exception {
        Map<String, Object> caseData = ImmutableMap.<String, Object>builder()
            .putAll(BASE_CASE_DATA)
            .put(RESP_IS_USING_DIGITAL_CHANNEL, YES_VALUE)
            .put(PETITIONER_SOLICITOR_EMAIL, TEST_SOLICITOR_EMAIL)
            .put(PETITIONER_SOLICITOR_NAME, TEST_SOLICITOR_NAME)
            .build();

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

        verifyPetitionerSolicitorWasSentEmail();
        verifyRespondentEmailWasSent();
        verifyNoMoreInteractions(emailClient);
    }

    @Test
    public void givenOnlineRepresentedRespondent_andOnlineRepresentedPetitioner_ThenOkResponse() throws Exception {
        Map<String, Object> caseData = ImmutableMap.<String, Object>builder()
            .putAll(BASE_CASE_DATA)
            .put(RESP_IS_USING_DIGITAL_CHANNEL, YES_VALUE)
            .put(PETITIONER_SOLICITOR_EMAIL, TEST_SOLICITOR_EMAIL)
            .put(PETITIONER_SOLICITOR_NAME, TEST_SOLICITOR_NAME)
            .put(RESP_SOL_REPRESENTED, YES_VALUE)
            .put(D8_RESPONDENT_SOLICITOR_EMAIL, TEST_RESP_SOLICITOR_EMAIL)
            .put(D8_RESPONDENT_SOLICITOR_NAME, TEST_RESP_SOLICITOR_NAME)
            .build();

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

        verifyPetitionerSolicitorWasSentEmail();
        verifyRespondentSolicitorWasSentEmail();
        verifyNoMoreInteractions(emailClient);
    }

    @Test
    public void responseShouldContainErrorsIfServiceFails() throws Exception {
        doThrow(new NotificationClientException("This has failed."))
            .when(emailClient).sendEmail(anyString(), anyString(), anyMap(), anyString());

        Map<String, Object> caseData = ImmutableMap.<String, Object>builder()
            .putAll(BASE_CASE_DATA)
            .put(RESP_IS_USING_DIGITAL_CHANNEL, YES_VALUE)
            .build();
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

    private void verifyPetitionerEmailWasSent() throws NotificationClientException {
        verify(emailClient).sendEmail(
            eq(DA_GRANTED_NOTIFICATION_TEMPLATE_ID),
            eq(TEST_PETITIONER_EMAIL),
            eq(ImmutableMap.<String, Object>builder()
                .put(NOTIFICATION_CASE_NUMBER_KEY, TEST_CASE_ID)
                .put(NOTIFICATION_ADDRESSEE_LAST_NAME_KEY, TEST_PETITIONER_LAST_NAME)
                .put(NOTIFICATION_ADDRESSEE_FIRST_NAME_KEY, TEST_PETITIONER_FIRST_NAME)
                .put(NOTIFICATION_EMAIL, TEST_PETITIONER_EMAIL)
                .put(NOTIFICATION_WELSH_LIMIT_DATE_TO_DOWNLOAD_CERTIFICATE,
                    TEST_WELSH_CUSTOMER_FACING_NOTIFICATION_LIMIT_DATE_TO_DOWNLOAD_CERTIFICATE)
                .put(NOTIFICATION_LIMIT_DATE_TO_DOWNLOAD_CERTIFICATE,
                    TEST_CUSTOMER_FACING_NOTIFICATION_LIMIT_DATE_TO_DOWNLOAD_CERTIFICATE)
                .build()),
            anyString()
        );
    }

    private void verifyRespondentEmailWasSent() throws NotificationClientException {
        verify(emailClient).sendEmail(
            eq(DA_GRANTED_NOTIFICATION_TEMPLATE_ID),
            eq(TEST_RESPONDENT_EMAIL),
            eq(ImmutableMap.<String, Object>builder()
                .put(NOTIFICATION_CASE_NUMBER_KEY, TEST_CASE_ID)
                .put(NOTIFICATION_ADDRESSEE_LAST_NAME_KEY, TEST_RESPONDENT_LAST_NAME)
                .put(NOTIFICATION_ADDRESSEE_FIRST_NAME_KEY, TEST_RESPONDENT_FIRST_NAME)
                .put(NOTIFICATION_EMAIL, TEST_RESPONDENT_EMAIL)
                .put(NOTIFICATION_WELSH_LIMIT_DATE_TO_DOWNLOAD_CERTIFICATE,
                    TEST_WELSH_CUSTOMER_FACING_NOTIFICATION_LIMIT_DATE_TO_DOWNLOAD_CERTIFICATE)
                .put(NOTIFICATION_LIMIT_DATE_TO_DOWNLOAD_CERTIFICATE,
                    TEST_CUSTOMER_FACING_NOTIFICATION_LIMIT_DATE_TO_DOWNLOAD_CERTIFICATE)
                .build()),
            anyString()
        );
    }

    private void verifyRespondentSolicitorWasSentEmail() throws NotificationClientException {
        verify(emailClient).sendEmail(
            eq(SOL_DA_GRANTED_NOTIFICATION_TEMPLATE_ID),
            eq(TEST_RESP_SOLICITOR_EMAIL),
            eq(ImmutableMap.<String, Object>builder()
                .put(NOTIFICATION_SOLICITOR_NAME, TEST_RESP_SOLICITOR_NAME)
                .put(NOTIFICATION_EMAIL, TEST_RESP_SOLICITOR_EMAIL)
                .put(NOTIFICATION_PET_NAME, TEST_PETITIONER_FULL_NAME)
                .put(NOTIFICATION_CCD_REFERENCE_KEY, TEST_CASE_ID)
                .put(NOTIFICATION_RESP_NAME, TEST_RESPONDENT_FULL_NAME)
                .build()),
            anyString()
        );
    }

    private void verifyPetitionerSolicitorWasSentEmail() throws NotificationClientException {
        verify(emailClient).sendEmail(
            eq(SOL_DA_GRANTED_NOTIFICATION_TEMPLATE_ID),
            eq(TEST_SOLICITOR_EMAIL),
            eq(ImmutableMap.<String, Object>builder()
                .put(NOTIFICATION_SOLICITOR_NAME, TEST_SOLICITOR_NAME)
                .put(NOTIFICATION_EMAIL, TEST_SOLICITOR_EMAIL)
                .put(NOTIFICATION_PET_NAME, TEST_PETITIONER_FULL_NAME)
                .put(NOTIFICATION_CCD_REFERENCE_KEY, TEST_CASE_ID)
                .put(NOTIFICATION_RESP_NAME, TEST_RESPONDENT_FULL_NAME)
                .build()),
            anyString()
        );
    }
}
