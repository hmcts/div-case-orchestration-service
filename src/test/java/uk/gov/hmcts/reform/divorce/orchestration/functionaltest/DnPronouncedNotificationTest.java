package uk.gov.hmcts.reform.divorce.orchestration.functionaltest;

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
import uk.gov.hmcts.reform.divorce.orchestration.config.EmailTemplatesConfig;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.Features;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CaseDetails;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CcdCallbackRequest;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CcdCallbackResponse;
import uk.gov.hmcts.reform.divorce.orchestration.service.BulkPrintService;
import uk.gov.hmcts.reform.divorce.orchestration.service.FeatureToggleService;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.bulk.printing.CostOrderCoRespondentCoverLetterGenerationTask;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.bulk.printing.CostOrderCoRespondentSolicitorCoverLetterGenerationTask;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.bulk.printing.DnGrantedRespondentCoverLetterGenerationTask;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.bulk.printing.DnGrantedRespondentSolicitorCoverLetterGenerationTask;
import uk.gov.service.notify.NotificationClientException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.Arrays.asList;
import static java.util.Collections.EMPTY_MAP;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_CASE_FAMILY_MAN_ID;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_CO_RESPONDENT_FIRST_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_CO_RESPONDENT_LAST_NAME;
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
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_SOLICITOR_EMAIL;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_SOLICITOR_REFERENCE;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_STATE;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_USER_EMAIL;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.COSTS_ORDER_DOCUMENT_TYPE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CO_RESPONDENT_IS_USING_DIGITAL_CHANNEL;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CO_RESPONDENT_REPRESENTED;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CO_RESPONDENT_SOLICITOR_ADDRESS;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CO_RESPONDENT_SOLICITOR_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CO_RESP_EMAIL_ADDRESS;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D8DOCUMENTS_GENERATED;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D8_DERIVED_REASON_FOR_DIVORCE_ADULTERY_3RD_PARTY_ADDRESS;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D8_REASON_FOR_DIVORCE_ADULTERY_3RD_PARTY_FNAME;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D8_REASON_FOR_DIVORCE_ADULTERY_3RD_PARTY_LNAME;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D8_RESPONDENT_SOLICITOR_REFERENCE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DATETIME_OF_HEARING_CCD_FIELD;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DECREE_NISI_DOCUMENT_TYPE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DECREE_NISI_FILENAME;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D_8_CASE_REFERENCE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D_8_CO_RESPONDENT_NAMED;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D_8_PETITIONER_EMAIL;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D_8_PETITIONER_FIRST_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D_8_PETITIONER_LAST_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D_8_REASON_FOR_DIVORCE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NO_VALUE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.PETITIONER_SOLICITOR_EMAIL;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.PETITIONER_SOLICITOR_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.RESPONDENT_EMAIL_ADDRESS;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.RESP_FIRST_NAME_CCD_FIELD;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.RESP_IS_USING_DIGITAL_CHANNEL;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.RESP_LAST_NAME_CCD_FIELD;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.RESP_SOL_REPRESENTED;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.SOLICITOR_REFERENCE_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.WHO_PAYS_CCD_CODE_FOR_BOTH;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.WHO_PAYS_CCD_CODE_FOR_RESPONDENT;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.WHO_PAYS_COSTS_CCD_FIELD;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.YES_VALUE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.facts.DivorceFact.ADULTERY;
import static uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor.AddresseeDataExtractor.CaseDataKeys.RESPONDENT_ADDRESS;
import static uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor.AddresseeDataExtractor.CaseDataKeys.RESPONDENT_SOLICITOR_ADDRESS;
import static uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor.CoECoverLetterDataExtractor.CaseDataKeys.COSTS_CLAIM_GRANTED;
import static uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor.DatesDataExtractorTest.createHearingDatesList;
import static uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor.FullNamesDataExtractor.CaseDataKeys.PETITIONER_FIRST_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor.FullNamesDataExtractor.CaseDataKeys.PETITIONER_LAST_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor.FullNamesDataExtractor.CaseDataKeys.RESPONDENT_FIRST_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor.FullNamesDataExtractor.CaseDataKeys.RESPONDENT_LAST_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.testutil.CaseDataTestHelper.createCollectionMemberDocument;
import static uk.gov.hmcts.reform.divorce.orchestration.testutil.ObjectMapperTestUtil.convertObjectToJsonString;

public class DnPronouncedNotificationTest extends MockedFunctionalTest {
    private static final String API_URL = "/dn-pronounced";

    private static final String GENERIC_UPDATE_EMAIL_TEMPLATE_ID = "6ee6ec29-5e88-4516-99cb-2edc30256575";
    private static final String SOLICITOR_GENERIC_UPDATE_EMAIL_TEMPLATE_ID = "951d26d9-e5fc-40de-a9da-d3ab957cb5e3";
    private static final String GENERIC_UPDATE_RESPONDENT_EMAIL_TEMPLATE_ID = "dc47109d-95f0-4a55-a11f-de41a5201cbc";

    private static final String COST_ORDER_DM_ID = "812f2709-7891-4f7e-835e-11a84a1fa008";
    private static final String DN_GRANTED_DM_ID = "23423432-5675-2543-b324-53g324234sd2";

    private static final String TEST_CO_RESPONDENT_EMAIL = TEST_USER_EMAIL;

    private static final byte[] DN_GRANTED_RESPONDENT_COVER_LETTER_BYTES = {1, 2, 3};
    private static final byte[] DN_GRANTED_RESPONDENT_SOLICITOR_COVER_LETTER_BYTES = {5, 5, 5};
    private static final byte[] COST_ORDER_CO_RESPONDENT_COVER_LETTER_BYTES = {9, 0, 1};
    private static final byte[] COST_ORDER_CO_RESPONDENT_SOLICITOR_COVER_LETTER_BYTES = {3, 2, 5};
    private static final byte[] COST_ORDER_BYTES = {4, 5, 6};
    private static final byte[] DN_GRANTED_BYTES = {7, 8, 9};

    private static final ImmutableMap<String, Object> BASE_CASE_DATA = ImmutableMap.<String, Object>builder()
        .put(D_8_PETITIONER_EMAIL, TEST_PETITIONER_EMAIL)
        .put(D8_DERIVED_REASON_FOR_DIVORCE_ADULTERY_3RD_PARTY_ADDRESS, TEST_D8_DERIVED_3RD_PARTY_ADDRESS)
        .put(CO_RESPONDENT_IS_USING_DIGITAL_CHANNEL, NO_VALUE)
        .put(COSTS_CLAIM_GRANTED, YES_VALUE)
        .put(D_8_CASE_REFERENCE, TEST_D8_CASE_REFERENCE)
        .put(DATETIME_OF_HEARING_CCD_FIELD, createHearingDatesList())
        .put(PETITIONER_FIRST_NAME, TEST_PETITIONER_FIRST_NAME)
        .put(PETITIONER_LAST_NAME, TEST_PETITIONER_LAST_NAME)
        .put(RESPONDENT_FIRST_NAME, TEST_RESPONDENT_FIRST_NAME)
        .put(RESPONDENT_LAST_NAME, TEST_RESPONDENT_LAST_NAME)
        .put(D8_REASON_FOR_DIVORCE_ADULTERY_3RD_PARTY_FNAME, TEST_CO_RESPONDENT_FIRST_NAME)
        .put(D8_REASON_FOR_DIVORCE_ADULTERY_3RD_PARTY_LNAME, TEST_CO_RESPONDENT_LAST_NAME)
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

    private static final String COSTS_ORDER_TEMPLATE_ID = "FL-DIV-DEC-ENG-00060.docx";

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

        verifyEmailWasSentTo(GENERIC_UPDATE_EMAIL_TEMPLATE_ID, TEST_PETITIONER_EMAIL);
        verifyEmailWasSentTo(GENERIC_UPDATE_RESPONDENT_EMAIL_TEMPLATE_ID, TEST_RESPONDENT_EMAIL);
        verifyEmailNeverSentTo(GENERIC_UPDATE_RESPONDENT_EMAIL_TEMPLATE_ID, TEST_CO_RESPONDENT_EMAIL);
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

        verifyEmailWasSentTo(GENERIC_UPDATE_EMAIL_TEMPLATE_ID, TEST_PETITIONER_EMAIL);
        verifyEmailWasSentTo(GENERIC_UPDATE_RESPONDENT_EMAIL_TEMPLATE_ID, TEST_RESPONDENT_EMAIL);
        verifyEmailNeverSentTo(GENERIC_UPDATE_RESPONDENT_EMAIL_TEMPLATE_ID, TEST_CO_RESPONDENT_EMAIL);
    }

    @Test
    public void givenCaseDataWithBothPaysCosts_whenDnPronounced_thenSendGenericNotifications_paperToggleOn() throws Exception {
        when(featureToggleService.isFeatureEnabled(Features.PAPER_UPDATE)).thenReturn(true);
        runTestBothPaysCostsSendGenericNotificationsToAll(EMPTY_MAP);
        verifyEmailWasSentTo(GENERIC_UPDATE_EMAIL_TEMPLATE_ID, TEST_PETITIONER_EMAIL);
        verifyEmailWasSentTo(GENERIC_UPDATE_RESPONDENT_EMAIL_TEMPLATE_ID, TEST_RESPONDENT_EMAIL);
        verifyEmailWasSentTo(GENERIC_UPDATE_RESPONDENT_EMAIL_TEMPLATE_ID, TEST_CO_RESPONDENT_EMAIL);
    }

    @Test
    public void givenCaseDataWithBothPaysCosts_whenDnPronouncedAllRepresented_thenSendGenericNotifications() throws Exception {
        when(featureToggleService.isFeatureEnabled(Features.PAPER_UPDATE)).thenReturn(true);

        runTestBothPaysCostsSendGenericNotificationsToAll(ImmutableMap.of(
            PETITIONER_SOLICITOR_EMAIL, TEST_SOLICITOR_EMAIL,
            PETITIONER_SOLICITOR_NAME, "James Petitioner-Solicitor"
        ));

        verifyEmailWasSentTo(SOLICITOR_GENERIC_UPDATE_EMAIL_TEMPLATE_ID, TEST_SOLICITOR_EMAIL);
        verifyEmailWasSentTo(GENERIC_UPDATE_RESPONDENT_EMAIL_TEMPLATE_ID, TEST_RESPONDENT_EMAIL);
        verifyEmailWasSentTo(GENERIC_UPDATE_RESPONDENT_EMAIL_TEMPLATE_ID, TEST_CO_RESPONDENT_EMAIL);
    }

    @Test
    public void givenCaseDataWithBothPaysCosts_whenDnPronounced_thenSendGenericNotifications_paperToggleOff() throws Exception {
        when(featureToggleService.isFeatureEnabled(Features.PAPER_UPDATE)).thenReturn(false);
        runTestBothPaysCostsSendGenericNotificationsToAll(EMPTY_MAP);
        verifyEmailWasSentTo(GENERIC_UPDATE_EMAIL_TEMPLATE_ID, TEST_PETITIONER_EMAIL);
        verifyEmailWasSentTo(GENERIC_UPDATE_RESPONDENT_EMAIL_TEMPLATE_ID, TEST_RESPONDENT_EMAIL);
        verifyEmailWasSentTo(GENERIC_UPDATE_RESPONDENT_EMAIL_TEMPLATE_ID, TEST_CO_RESPONDENT_EMAIL);
    }

    private void runTestBothPaysCostsSendGenericNotificationsToAll(Map<String, Object> moreData) throws Exception {
        ImmutableMap<String, Object> additionalEntries = ImmutableMap.<String, Object>builder()
            .put(WHO_PAYS_COSTS_CCD_FIELD, WHO_PAYS_CCD_CODE_FOR_BOTH)
            .putAll(moreData)
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
    }

    // Offline journeys
    @Test
    public void givenOfflineCoRespondent_CostsClaimGranted_NotRepresented_ThenOkResponse() throws Exception {
        //Given
        when(featureToggleService.isFeatureEnabled(Features.PAPER_UPDATE)).thenReturn(true);
        stubServiceAuthProvider(HttpStatus.OK, TEST_SERVICE_AUTH_TOKEN);

        //Newly generated document cover letter
        stubNewlyGeneratedDocument(
            CostOrderCoRespondentCoverLetterGenerationTask.FileMetadata.TEMPLATE_ID,
            CostOrderCoRespondentCoverLetterGenerationTask.FileMetadata.DOCUMENT_TYPE,
            COST_ORDER_CO_RESPONDENT_COVER_LETTER_BYTES
        );

        stubNewlyGeneratedDocument(
            DnGrantedRespondentCoverLetterGenerationTask.FileMetadata.TEMPLATE_ID,
            DnGrantedRespondentCoverLetterGenerationTask.FileMetadata.DOCUMENT_TYPE,
            DN_GRANTED_RESPONDENT_COVER_LETTER_BYTES
        );

        Map<String, Object> coRespCaseData = buildCaseDataForCoRespondentNotRepresented();

        // Existing document
        CollectionMember<Document> costOrderDocument = buildDocumentCollectionMemberAndStubInDmStore(
            COST_ORDER_DM_ID, COST_ORDER_BYTES, COSTS_ORDER_DOCUMENT_TYPE, COSTS_ORDER_TEMPLATE_ID
        );
        CollectionMember<Document> dnGrantedDocument = buildDocumentCollectionMemberAndStubInDmStore(
            DN_GRANTED_DM_ID, DN_GRANTED_BYTES, DECREE_NISI_DOCUMENT_TYPE, DECREE_NISI_FILENAME
        );

        Map<String, Object> caseData = new ImmutableMap.Builder<String, Object>()
            .putAll(coRespCaseData)
            .put(RESP_IS_USING_DIGITAL_CHANNEL, NO_VALUE)
            .put(D8DOCUMENTS_GENERATED, asList(costOrderDocument, dnGrantedDocument))
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
        verifyBulkPrintWasCalledForDocuments(
            DnGrantedRespondentCoverLetterGenerationTask.FileMetadata.DOCUMENT_TYPE,
            DN_GRANTED_RESPONDENT_COVER_LETTER_BYTES,
            DN_GRANTED_BYTES,
            COST_ORDER_BYTES
        );

        verifyBulkPrintWasCalledForDocuments(
            CostOrderCoRespondentCoverLetterGenerationTask.FileMetadata.DOCUMENT_TYPE,
            COST_ORDER_CO_RESPONDENT_COVER_LETTER_BYTES,
            COST_ORDER_BYTES
        );

        verifyEmailWasSentTo(GENERIC_UPDATE_EMAIL_TEMPLATE_ID, TEST_PETITIONER_EMAIL);
        verifyEmailNeverSentTo(GENERIC_UPDATE_RESPONDENT_EMAIL_TEMPLATE_ID, TEST_RESPONDENT_EMAIL);
        verifyEmailNeverSentTo(GENERIC_UPDATE_RESPONDENT_EMAIL_TEMPLATE_ID, TEST_CO_RESPONDENT_EMAIL);
    }

    @Test
    public void givenOfflineCoRespondent_CostsClaimGranted_Represented_ThenOkResponse() throws Exception {
        //Given
        when(featureToggleService.isFeatureEnabled(Features.PAPER_UPDATE)).thenReturn(true);
        stubServiceAuthProvider(HttpStatus.OK, TEST_SERVICE_AUTH_TOKEN);

        //Newly generated document cover letter
        stubNewlyGeneratedDocument(
            CostOrderCoRespondentSolicitorCoverLetterGenerationTask.FileMetadata.TEMPLATE_ID,
            CostOrderCoRespondentSolicitorCoverLetterGenerationTask.FileMetadata.DOCUMENT_TYPE,
            COST_ORDER_CO_RESPONDENT_SOLICITOR_COVER_LETTER_BYTES
        );

        stubNewlyGeneratedDocument(
            DnGrantedRespondentSolicitorCoverLetterGenerationTask.FileMetadata.TEMPLATE_ID,
            DnGrantedRespondentSolicitorCoverLetterGenerationTask.FileMetadata.DOCUMENT_TYPE,
            DN_GRANTED_RESPONDENT_SOLICITOR_COVER_LETTER_BYTES
        );

        CollectionMember<Document> costOrderDocument = buildDocumentCollectionMemberAndStubInDmStore(
            COST_ORDER_DM_ID, COST_ORDER_BYTES, COSTS_ORDER_DOCUMENT_TYPE, COSTS_ORDER_TEMPLATE_ID
        );
        CollectionMember<Document> dnGrantedDocument = buildDocumentCollectionMemberAndStubInDmStore(
            DN_GRANTED_DM_ID, DN_GRANTED_BYTES, DECREE_NISI_DOCUMENT_TYPE, DECREE_NISI_FILENAME
        );

        Map<String, Object> caseData = buildCaseDataForCoRespondentRepresented();
        caseData.put(RESP_IS_USING_DIGITAL_CHANNEL, NO_VALUE);
        caseData.put(RESP_SOL_REPRESENTED, YES_VALUE);
        caseData.put(RESPONDENT_SOLICITOR_ADDRESS, "resp sol addrr");
        caseData.put(D8_RESPONDENT_SOLICITOR_REFERENCE, "respSolReference");
        caseData.put(D8DOCUMENTS_GENERATED, asList(costOrderDocument, dnGrantedDocument));

        CcdCallbackResponse expectedResponse = CcdCallbackResponse.builder()
            .data(caseData)
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

        verifyBulkPrintWasCalledForDocuments(
            CostOrderCoRespondentSolicitorCoverLetterGenerationTask.FileMetadata.DOCUMENT_TYPE,
            COST_ORDER_CO_RESPONDENT_SOLICITOR_COVER_LETTER_BYTES,
            COST_ORDER_BYTES
        );

        verifyBulkPrintWasCalledForDocuments(
            DnGrantedRespondentSolicitorCoverLetterGenerationTask.FileMetadata.DOCUMENT_TYPE,
            DN_GRANTED_RESPONDENT_SOLICITOR_COVER_LETTER_BYTES,
            DN_GRANTED_BYTES,
            COST_ORDER_BYTES
        );

        verifyEmailWasSentTo(GENERIC_UPDATE_EMAIL_TEMPLATE_ID, TEST_PETITIONER_EMAIL);
        verifyEmailNeverSentTo(GENERIC_UPDATE_RESPONDENT_EMAIL_TEMPLATE_ID, TEST_RESPONDENT_EMAIL);
        verifyEmailNeverSentTo(GENERIC_UPDATE_RESPONDENT_EMAIL_TEMPLATE_ID, TEST_CO_RESPONDENT_EMAIL);
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

    private void stubNewlyGeneratedDocument(String templateId, String documentType, byte[] documentAsBytes) {
        String letterId = stubDocumentGeneratorService(templateId, documentType);
        stubDMStore(letterId, documentAsBytes);
    }

    private Map<String, Object> buildCaseDataForEmailNotifications(Map<String, Object> extraData) {
        return ImmutableMap.<String, Object>builder()
            .putAll(extraData)
            .put(D_8_PETITIONER_EMAIL, TEST_PETITIONER_EMAIL)
            .put(D_8_CASE_REFERENCE, TEST_CASE_FAMILY_MAN_ID)
            .put(RESPONDENT_EMAIL_ADDRESS, TEST_RESPONDENT_EMAIL)
            .put(CO_RESP_EMAIL_ADDRESS, TEST_CO_RESPONDENT_EMAIL)
            .put(D_8_PETITIONER_FIRST_NAME, TEST_FIRST_NAME)
            .put(D_8_PETITIONER_LAST_NAME, TEST_LAST_NAME)
            .put(RESP_FIRST_NAME_CCD_FIELD, TEST_FIRST_NAME)
            .put(RESP_LAST_NAME_CCD_FIELD, TEST_LAST_NAME)
            .put(D8_REASON_FOR_DIVORCE_ADULTERY_3RD_PARTY_FNAME, TEST_FIRST_NAME)
            .put(D8_REASON_FOR_DIVORCE_ADULTERY_3RD_PARTY_LNAME, TEST_LAST_NAME)
            .put(CO_RESPONDENT_REPRESENTED, YES_VALUE)
            .put(D_8_CO_RESPONDENT_NAMED, YES_VALUE)
            .put(CO_RESPONDENT_SOLICITOR_NAME, TEST_CO_RESPONDENT_SOLICITOR_NAME)
            .put(D_8_REASON_FOR_DIVORCE, ADULTERY.getValue())
            .build();
    }

    private Map<String, Object> buildCaseDataForCoRespondentNotRepresented() {
        return new HashMap<>(ImmutableMap.<String, Object>builder()
            .putAll(BASE_CASE_DATA)
            .put(CO_RESPONDENT_REPRESENTED, NO_VALUE)
            .put(D_8_REASON_FOR_DIVORCE, ADULTERY.getValue())
            .put(D_8_CO_RESPONDENT_NAMED, YES_VALUE)
            .put(RESPONDENT_ADDRESS, "address of resp")
            .build());
    }

    private Map<String, Object> buildCaseDataForCoRespondentRepresented() {
        Map<String, Object> caseData = new HashMap<>(buildCaseDataForCoRespondentNotRepresented());
        caseData.put(CO_RESPONDENT_REPRESENTED, YES_VALUE);
        caseData.put(CO_RESPONDENT_SOLICITOR_NAME, TEST_CO_RESPONDENT_SOLICITOR_NAME);
        caseData.put(SOLICITOR_REFERENCE_JSON_KEY, TEST_SOLICITOR_REFERENCE);
        caseData.put(CO_RESPONDENT_SOLICITOR_ADDRESS, TEST_CO_RESPONDENT_SOLICITOR_ADDRESS);

        return caseData;
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

    private void verifyEmailWasSentTo(String emailTemplateId, String testPetitionerEmail) throws NotificationClientException {
        verify(emailClient, times(1))
            .sendEmail(
                eq(emailTemplateId),
                eq(testPetitionerEmail),
                any(),
                any()
            );
    }

    private void verifyEmailNeverSentTo(String emailTemplateId, String testPetitionerEmail) throws NotificationClientException {
        verify(emailClient, never())
            .sendEmail(
                eq(emailTemplateId),
                eq(testPetitionerEmail),
                any(),
                any()
            );
    }

    private CollectionMember<Document> buildDocumentCollectionMemberAndStubInDmStore(
        String docId, byte[] bytes, String docType, String fileName) {
        stubDMStore(docId, bytes);
        return createCollectionMemberDocument(getDocumentStoreTestUrl(docId), docType, fileName);
    }

    private void verifyBulkPrintWasCalledForDocuments(String bulkPrintLetterType, byte[]... docBytes) {
        verify(bulkPrintService).send(
            eq(TEST_CASE_ID),
            eq(bulkPrintLetterType),
            documentsToPrintCaptor.capture()
        );
        List<GeneratedDocumentInfo> dnGrantedLetterBulkPrintDocs = documentsToPrintCaptor.getValue();
        assertThat(dnGrantedLetterBulkPrintDocs, hasSize(docBytes.length));
        for (int i = 0; i < docBytes.length; i++) {
            assertThat(dnGrantedLetterBulkPrintDocs.get(i).getBytes(), is(docBytes[i]));
        }
    }
}
