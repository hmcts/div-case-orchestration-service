package uk.gov.hmcts.reform.divorce.orchestration.functionaltest;

import com.github.tomakehurst.wiremock.client.WireMock;
import com.google.common.collect.ImmutableMap;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import uk.gov.hmcts.reform.divorce.orchestration.client.EmailClient;
import uk.gov.hmcts.reform.divorce.orchestration.config.EmailTemplatesConfig;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.Features;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CaseDetails;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CcdCallbackRequest;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CcdCallbackResponse;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CollectionMember;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.Document;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.documentgeneration.GeneratedDocumentInfo;
import uk.gov.hmcts.reform.divorce.orchestration.service.BulkPrintService;
import uk.gov.hmcts.reform.divorce.orchestration.service.FeatureToggleService;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.bulk.printing.CostOrderCoRespondentCoverLetterGenerationTask;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.bulk.printing.CostOrderCoRespondentSolicitorCoverLetterGenerationTask;

import java.util.List;
import java.util.Map;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static java.util.Arrays.asList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_CASE_FAMILY_MAN_ID;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_CO_RESPONDENTS_FIRST_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_CO_RESPONDENTS_LAST_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_CO_RESPONDENT_SOLICITOR_ADDRESS;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_CO_RESPONDENT_SOLICITOR_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_D8_CASE_REFERENCE;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_D8_DERIVED_3RD_PARTY_ADDRESS;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_FIRST_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_LAST_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_PETITIONER_EMAIL;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_PETITIONER_FIRST_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_PETITIONER_LAST_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_RESPONDENT_EMAIL;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_RESPONDENT_FIRST_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_RESPONDENT_LAST_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_SERVICE_AUTH_TOKEN;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_SOLICITOR_REFERENCE;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_STATE;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_USER_EMAIL;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.COSTS_ORDER_DOCUMENT_TYPE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.COSTS_ORDER_TEMPLATE_ID;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CO_RESPONDENT_IS_USING_DIGITAL_CHANNEL;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CO_RESPONDENT_REPRESENTED;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CO_RESPONDENT_SOLICITOR_ADDRESS;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CO_RESPONDENT_SOLICITOR_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CO_RESP_EMAIL_ADDRESS;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D8DOCUMENTS_GENERATED;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D8_DERIVED_REASON_FOR_DIVORCE_ADULTERY_3RD_PARTY_ADDRESS;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D8_REASON_FOR_DIVORCE_ADULTERY_3RD_PARTY_FNAME;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D8_REASON_FOR_DIVORCE_ADULTERY_3RD_PARTY_LNAME;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DATETIME_OF_HEARING_CCD_FIELD;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DIVORCE_COSTS_CLAIM_CCD_FIELD;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D_8_CASE_REFERENCE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D_8_PETITIONER_EMAIL;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D_8_PETITIONER_FIRST_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D_8_PETITIONER_LAST_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NO_VALUE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.RESPONDENT_EMAIL_ADDRESS;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.RESP_FIRST_NAME_CCD_FIELD;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.RESP_IS_USING_DIGITAL_CHANNEL;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.RESP_LAST_NAME_CCD_FIELD;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.SOLICITOR_REFERENCE_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.WHO_PAYS_CCD_CODE_FOR_BOTH;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.WHO_PAYS_CCD_CODE_FOR_CO_RESPONDENT;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.WHO_PAYS_CCD_CODE_FOR_RESPONDENT;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.WHO_PAYS_COSTS_CCD_FIELD;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.YES_VALUE;
import static uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor.DatesDataExtractorTest.createHearingDatesList;
import static uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor.FullNamesDataExtractor.CaseDataKeys.PETITIONER_FIRST_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor.FullNamesDataExtractor.CaseDataKeys.PETITIONER_LAST_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor.FullNamesDataExtractor.CaseDataKeys.RESPONDENT_FIRST_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor.FullNamesDataExtractor.CaseDataKeys.RESPONDENT_LAST_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.testutil.CaseDataTestHelper.createCollectionMemberDocument;
import static uk.gov.hmcts.reform.divorce.orchestration.testutil.ObjectMapperTestUtil.convertObjectToJsonString;

public class DnPronouncedNotificationTest extends MockedFunctionalTest {
    private static final String API_URL = "/dn-pronounced";
    private static final String GENERIC_UPDATE_TEMPLATE_ID = "6ee6ec29-5e88-4516-99cb-2edc30256575";
    private static final String GENERIC_UPDATE_RESPONDENT_TEMPLATE_ID = "dc47109d-95f0-4a55-a11f-de41a5201cbc";
    private static final String COST_ORDER_ID = "7d10126d-0e88-4f0e-b475-628b54a87ca6";
    private static final String SERVICE_AUTH_CONTEXT_PATH = "/lease";
    private static final ImmutableMap<String, Object> BASE_CASE_DATA = ImmutableMap.<String, Object>builder()
        .put(D8_DERIVED_REASON_FOR_DIVORCE_ADULTERY_3RD_PARTY_ADDRESS, TEST_D8_DERIVED_3RD_PARTY_ADDRESS)
        .put(CO_RESPONDENT_IS_USING_DIGITAL_CHANNEL, NO_VALUE)
        .put(DIVORCE_COSTS_CLAIM_CCD_FIELD, YES_VALUE)
        .put(D_8_CASE_REFERENCE, TEST_D8_CASE_REFERENCE)
        .put(DATETIME_OF_HEARING_CCD_FIELD, createHearingDatesList())
        .put(PETITIONER_FIRST_NAME, TEST_PETITIONER_FIRST_NAME)
        .put(PETITIONER_LAST_NAME, TEST_PETITIONER_LAST_NAME)
        .put(RESPONDENT_FIRST_NAME, TEST_RESPONDENT_FIRST_NAME)
        .put(RESPONDENT_LAST_NAME, TEST_RESPONDENT_LAST_NAME)
        .put(D8_REASON_FOR_DIVORCE_ADULTERY_3RD_PARTY_FNAME, TEST_CO_RESPONDENTS_FIRST_NAME)
        .put(D8_REASON_FOR_DIVORCE_ADULTERY_3RD_PARTY_LNAME, TEST_CO_RESPONDENTS_LAST_NAME)
        .build();
    private Map<String, Object> ccdCallbackResponseData;
    private CcdCallbackRequest ccdCallbackRequest;

    @MockBean
    private EmailClient emailClient;

    @MockBean
    private BulkPrintService bulkPrintService;

    @MockBean
    private FeatureToggleService featureToggleService;

    @Autowired
    private EmailTemplatesConfig emailTemplatesConfig;

    @Autowired
    private MockMvc webClient;

    @Captor
    private ArgumentCaptor<List<GeneratedDocumentInfo>> documentsToPrintCaptor;


    @Test
    public void givenCaseDataWithNoPaysCosts_whenDnPronounced_thenSendGenericNotifications() throws Exception {
        ImmutableMap<String, Object> additionalEntries = ImmutableMap.<String, Object>builder().build();
        ccdCallbackResponseData = buildCaseDataForEmailNotifications(additionalEntries);
        ccdCallbackRequest = buildCallbackRequestForEmailNotifications(additionalEntries);

        CcdCallbackResponse expected = CcdCallbackResponse.builder()
            .data(ccdCallbackResponseData)
            .build();

        webClient.perform(post(API_URL)
            .header(AUTHORIZATION, AUTH_TOKEN)
            .content(convertObjectToJsonString(ccdCallbackRequest))
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content().json(convertObjectToJsonString(expected)));

        verify(emailClient, times(1)).sendEmail(eq(GENERIC_UPDATE_TEMPLATE_ID),
            eq(TEST_PETITIONER_EMAIL),
            any(), any());
        verify(emailClient, times(1)).sendEmail(eq(GENERIC_UPDATE_RESPONDENT_TEMPLATE_ID),
            eq(TEST_RESPONDENT_EMAIL),
            any(), any());
        verify(emailClient, never()).sendEmail(eq(GENERIC_UPDATE_RESPONDENT_TEMPLATE_ID),
            eq(TEST_USER_EMAIL),
            any(), any());
    }

    @Test
    public void givenCaseDataWithRespondentPaysCosts_whenDnPronounced_thenSendGenericNotifications() throws Exception {
        ImmutableMap<String, Object> additionalEntries = ImmutableMap.<String, Object>builder()
            .put(WHO_PAYS_COSTS_CCD_FIELD, WHO_PAYS_CCD_CODE_FOR_RESPONDENT)
            .build();
        ccdCallbackResponseData = buildCaseDataForEmailNotifications(additionalEntries);
        ccdCallbackRequest = buildCallbackRequestForEmailNotifications(additionalEntries);

        CcdCallbackResponse expected = CcdCallbackResponse.builder()
            .data(ccdCallbackResponseData)
            .build();

        webClient.perform(post(API_URL)
            .header(AUTHORIZATION, AUTH_TOKEN)
            .content(convertObjectToJsonString(ccdCallbackRequest))
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content().json(convertObjectToJsonString(expected)));

        verify(emailClient, times(1)).sendEmail(eq(GENERIC_UPDATE_TEMPLATE_ID),
            eq(TEST_PETITIONER_EMAIL),
            any(), any());
        verify(emailClient, times(1)).sendEmail(eq(GENERIC_UPDATE_RESPONDENT_TEMPLATE_ID),
            eq(TEST_RESPONDENT_EMAIL),
            any(), any());
        verify(emailClient, never()).sendEmail(eq(GENERIC_UPDATE_RESPONDENT_TEMPLATE_ID),
            eq(TEST_USER_EMAIL),
            any(), any());
    }

    @Test
    public void givenCaseDataWithCoRespondentPaysCosts_whenDnPronounced_thenSendGenericNotifications() throws Exception {
        Map<String, Object> additionalEntries = ImmutableMap.<String, Object>builder()
            .put(WHO_PAYS_COSTS_CCD_FIELD, WHO_PAYS_CCD_CODE_FOR_CO_RESPONDENT)
            .build();
        ccdCallbackResponseData = buildCaseDataForEmailNotifications(additionalEntries);
        ccdCallbackRequest = buildCallbackRequestForEmailNotifications(additionalEntries);

        CcdCallbackResponse expected = CcdCallbackResponse.builder()
            .data(ccdCallbackResponseData)
            .build();

        webClient.perform(post(API_URL)
            .header(AUTHORIZATION, AUTH_TOKEN)
            .content(convertObjectToJsonString(ccdCallbackRequest))
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content().json(convertObjectToJsonString(expected)));

        verify(emailClient, times(1)).sendEmail(eq(GENERIC_UPDATE_TEMPLATE_ID),
            eq(TEST_PETITIONER_EMAIL),
            any(), any());
        verify(emailClient, times(1)).sendEmail(eq(GENERIC_UPDATE_RESPONDENT_TEMPLATE_ID),
            eq(TEST_RESPONDENT_EMAIL),
            any(), any());
        verify(emailClient, times(1)).sendEmail(eq(GENERIC_UPDATE_RESPONDENT_TEMPLATE_ID),
            eq(TEST_USER_EMAIL),
            any(), any());
    }

    @Test
    public void givenCaseDataWithBothPaysCosts_whenDnPronounced_thenSendGenericNotifications() throws Exception {
        ImmutableMap<String, Object> additionalEntries = ImmutableMap.<String, Object>builder()
            .put(WHO_PAYS_COSTS_CCD_FIELD, WHO_PAYS_CCD_CODE_FOR_BOTH)
            .build();
        ccdCallbackResponseData = buildCaseDataForEmailNotifications(additionalEntries);
        ccdCallbackRequest = buildCallbackRequestForEmailNotifications(additionalEntries);

        CcdCallbackResponse expected = CcdCallbackResponse.builder()
            .data(ccdCallbackResponseData)
            .build();

        webClient.perform(post(API_URL)
            .header(AUTHORIZATION, AUTH_TOKEN)
            .content(convertObjectToJsonString(ccdCallbackRequest))
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content().json(convertObjectToJsonString(expected)));

        verify(emailClient, times(1)).sendEmail(eq(GENERIC_UPDATE_TEMPLATE_ID),
            eq(TEST_PETITIONER_EMAIL),
            any(), any());
        verify(emailClient, times(1)).sendEmail(eq(GENERIC_UPDATE_RESPONDENT_TEMPLATE_ID),
            eq(TEST_RESPONDENT_EMAIL),
            any(), any());
        verify(emailClient, times(1)).sendEmail(eq(GENERIC_UPDATE_RESPONDENT_TEMPLATE_ID),
            eq(TEST_USER_EMAIL),
            any(), any());
    }

    private CcdCallbackRequest buildCallbackRequestForEmailNotifications(Map<String, Object> additionalEntries) {
        Map<String, Object> caseData = buildCaseDataForEmailNotifications(additionalEntries);
        CaseDetails caseDetails = CaseDetails.builder()
            .caseId(TEST_CASE_ID)
            .state(TEST_STATE)
            .caseData(caseData)
            .build();

        return CcdCallbackRequest.builder()
            .caseDetails(caseDetails)
            .build();
    }

    // Offline journeys
    @Test
    public void givenOfflineCoRespondent_CostsClaimGranted_NotRepresented_ThenOkResponse() throws Exception {
        //Given
        when(featureToggleService.isFeatureEnabled(Features.PAPER_UPDATE)).thenReturn(true);
        stubServiceAuthProvider(TEST_SERVICE_AUTH_TOKEN);

        //Newly generated document cover letter
        byte[] coRespondentCoverLetterBytes = new byte[] {1, 2, 3};
        String daGrantedLetterDocumentId =
            stubDocumentGeneratorService(
                CostOrderCoRespondentCoverLetterGenerationTask.FileMetadata.TEMPLATE_ID,
                CostOrderCoRespondentCoverLetterGenerationTask.FileMetadata.DOCUMENT_TYPE);
        stubDMStore(daGrantedLetterDocumentId, coRespondentCoverLetterBytes);

        //Existing document
        byte[] costOrderBytes = new byte[] {4, 5, 6};
        stubDMStore(COST_ORDER_ID, costOrderBytes);
        Map<String, Object> coRespCaseData = buildCaseDataForCoRespondentNotRepresented();
        CollectionMember<Document> costOrderDocument = createCollectionMemberDocument(getDocumentStoreTestUrl(COST_ORDER_ID),
            COSTS_ORDER_DOCUMENT_TYPE,
            COSTS_ORDER_TEMPLATE_ID);
        Map<String, Object> caseData = new ImmutableMap.Builder<String, Object>()
            .putAll(coRespCaseData)
            .put(RESP_IS_USING_DIGITAL_CHANNEL, NO_VALUE)
            .put(D8DOCUMENTS_GENERATED, asList(costOrderDocument))
            .build();

        //When
        CcdCallbackResponse expectedResponse = CcdCallbackResponse.builder()
            .data(coRespCaseData)
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
            .andExpect(content().json(convertObjectToJsonString(expectedResponse)));

        //Then
        verify(bulkPrintService).send(eq(TEST_CASE_ID), anyString(), documentsToPrintCaptor.capture());
        List<GeneratedDocumentInfo> documentsSentToBulkPrint = documentsToPrintCaptor.getValue();
        assertThat(documentsSentToBulkPrint, hasSize(2));
        assertThat(documentsSentToBulkPrint.get(0).getBytes(), is(coRespondentCoverLetterBytes));
        assertThat(documentsSentToBulkPrint.get(1).getBytes(), is(costOrderBytes));
        verifyZeroInteractions(emailClient);
    }

    @Test
    public void givenOfflineCoRespondent_CostsClaimGranted_Represented_ThenOkResponse() throws Exception {
        //Given
        when(featureToggleService.isFeatureEnabled(Features.PAPER_UPDATE)).thenReturn(true);
        stubServiceAuthProvider(TEST_SERVICE_AUTH_TOKEN);

        //Newly generated document cover letter
        byte[] coRespondentCoverLetterBytes = new byte[] {1, 2, 3};
        String daGrantedLetterDocumentId =
            stubDocumentGeneratorService(
                CostOrderCoRespondentSolicitorCoverLetterGenerationTask.FileMetadata.TEMPLATE_ID,
                CostOrderCoRespondentSolicitorCoverLetterGenerationTask.FileMetadata.DOCUMENT_TYPE);
        stubDMStore(daGrantedLetterDocumentId, coRespondentCoverLetterBytes);

        //Existing document
        byte[] costOrderBytes = new byte[] {4, 5, 6};
        stubDMStore(COST_ORDER_ID, costOrderBytes);
        Map<String, Object> coRespCaseData = buildCaseDataForCoRespondentRepresented();
        CollectionMember<Document> costOrderDocument = createCollectionMemberDocument(getDocumentStoreTestUrl(COST_ORDER_ID),
            COSTS_ORDER_DOCUMENT_TYPE,
            COSTS_ORDER_TEMPLATE_ID);
        Map<String, Object> caseData = new ImmutableMap.Builder<String, Object>()
            .putAll(coRespCaseData)
            .put(RESP_IS_USING_DIGITAL_CHANNEL, NO_VALUE)
            .put(D8DOCUMENTS_GENERATED, asList(costOrderDocument))
            .build();

        //When
        CcdCallbackResponse expectedResponse = CcdCallbackResponse.builder()
            .data(coRespCaseData)
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
            .andExpect(content().json(convertObjectToJsonString(expectedResponse)));

        //Then
        verify(bulkPrintService).send(eq(TEST_CASE_ID), anyString(), documentsToPrintCaptor.capture());
        List<GeneratedDocumentInfo> documentsSentToBulkPrint = documentsToPrintCaptor.getValue();
        assertThat(documentsSentToBulkPrint, hasSize(2));
        assertThat(documentsSentToBulkPrint.get(0).getBytes(), is(coRespondentCoverLetterBytes));
        assertThat(documentsSentToBulkPrint.get(1).getBytes(), is(costOrderBytes));
        verifyZeroInteractions(emailClient);
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
        Map<String, Object> baseCaseData = buildCaseDataForEmailNotifications(ImmutableMap.<String, Object>builder().build());

        webClient.perform(post(API_URL)
            .content(convertObjectToJsonString(
                CcdCallbackRequest.builder()
                    .caseDetails(CaseDetails.builder().caseId(TEST_CASE_ID).caseData(baseCaseData).build())
                    .build()
            ))
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isBadRequest());
    }

    private void stubServiceAuthProvider(String response) {
        serviceAuthProviderServer.stubFor(WireMock.post(SERVICE_AUTH_CONTEXT_PATH)
            .willReturn(aResponse()
                .withStatus(HttpStatus.OK.value())
                .withBody(response)));
    }

    private Map<String, Object> buildCaseDataForEmailNotifications(Map<String, Object> extraData) {
        return ImmutableMap.<String, Object>builder()
            .putAll(extraData)
            .put(D_8_PETITIONER_EMAIL, TEST_PETITIONER_EMAIL)
            .put(D_8_CASE_REFERENCE, TEST_CASE_FAMILY_MAN_ID)
            .put(RESPONDENT_EMAIL_ADDRESS, TEST_RESPONDENT_EMAIL)
            .put(CO_RESP_EMAIL_ADDRESS, TEST_USER_EMAIL)
            .put(D_8_PETITIONER_FIRST_NAME, TEST_FIRST_NAME)
            .put(D_8_PETITIONER_LAST_NAME, TEST_LAST_NAME)
            .put(RESP_FIRST_NAME_CCD_FIELD, TEST_FIRST_NAME)
            .put(RESP_LAST_NAME_CCD_FIELD, TEST_LAST_NAME)
            .put(D8_REASON_FOR_DIVORCE_ADULTERY_3RD_PARTY_FNAME, TEST_FIRST_NAME)
            .put(D8_REASON_FOR_DIVORCE_ADULTERY_3RD_PARTY_LNAME, TEST_LAST_NAME)
            .build();
    }

    private Map<String, Object> buildCaseDataForCoRespondentNotRepresented() {
        return ImmutableMap.<String, Object>builder()
            .putAll(BASE_CASE_DATA)
            .put(CO_RESPONDENT_REPRESENTED, NO_VALUE)
            .build();
    }

    private Map<String, Object> buildCaseDataForCoRespondentRepresented() {
        return ImmutableMap.<String, Object>builder()
            .putAll(BASE_CASE_DATA)
            .put(CO_RESPONDENT_REPRESENTED, YES_VALUE)
            .put(CO_RESPONDENT_SOLICITOR_NAME, TEST_CO_RESPONDENT_SOLICITOR_NAME)
            .put(SOLICITOR_REFERENCE_JSON_KEY, TEST_SOLICITOR_REFERENCE)
            .put(CO_RESPONDENT_SOLICITOR_ADDRESS, TEST_CO_RESPONDENT_SOLICITOR_ADDRESS)
            .build();
    }

}
