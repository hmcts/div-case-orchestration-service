
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
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.Features;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CaseDetails;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CcdCallbackRequest;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CcdCallbackResponse;
import uk.gov.hmcts.reform.divorce.orchestration.functionaltest.MockedFunctionalTest;
import uk.gov.hmcts.reform.divorce.orchestration.service.BulkPrintService;
import uk.gov.hmcts.reform.divorce.orchestration.service.EmailService;
import uk.gov.hmcts.reform.divorce.orchestration.service.FeatureToggleService;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.bulk.printing.CoECoRespondentCoverLetterGenerationTask;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.bulk.printing.CoERespondentCoverLetterGenerationTask;
import uk.gov.service.notify.NotificationClientException;

import java.util.List;
import java.util.Map;

import static com.jayway.jsonpath.matchers.JsonPathMatchers.hasJsonPath;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static java.util.Collections.singletonMap;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_COURT_ID;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_INFERRED_GENDER;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_PETITIONER_EMAIL;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_PETITIONER_FIRST_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_PETITIONER_LAST_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_RESPONDENT_EMAIL;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_RESPONDENT_FIRST_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_RESPONDENT_LAST_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_SERVICE_AUTH_TOKEN;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.BulkCaseConstants.COURT_NAME_CCD_FIELD;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D8DOCUMENTS_GENERATED;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DATETIME_OF_HEARING_CCD_FIELD;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DIVORCE_COSTS_CLAIM_GRANTED_CCD_FIELD;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DOCUMENT_TYPE_COE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D_8_CASE_REFERENCE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D_8_INFERRED_PETITIONER_GENDER;
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
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.document.AOSPackOfflineConstants.COE_RESPONDENT_LETTER_DOCUMENT_TYPE;
import static uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor.CoECoverLetterDataExtractor.CaseDataKeys.HEARING_DATE;
import static uk.gov.hmcts.reform.divorce.orchestration.tasks.bulk.printing.CoERespondentCoverLetterGenerationTask.FileMetadata.TEMPLATE_ID;
import static uk.gov.hmcts.reform.divorce.orchestration.testutil.CaseDataTestHelper.createCollectionMemberDocument;
import static uk.gov.hmcts.reform.divorce.orchestration.testutil.ObjectMapperTestUtil.convertObjectToJsonString;

public class CaseListedForHearingCallbackTest extends MockedFunctionalTest {

    private static final String API_URL = "/case-linked-for-hearing";

    private static final String CERTIFICATE_OF_ENTITLEMENT_ID = "7d10123a-0t88-4e0e-b475-528b54t87ca6";

    private static final List<Map<String, Object>> DATE_TIME_OF_HEARINGS = singletonList(singletonMap("value", ImmutableMap.of(
        HEARING_DATE, "2020-06-20"
    )));

    private static final Map<String, Object> BASE_CASE_DATA = ImmutableMap.<String, Object>builder()
        .put(D_8_PETITIONER_FIRST_NAME, TEST_PETITIONER_FIRST_NAME)
        .put(D_8_PETITIONER_LAST_NAME, TEST_PETITIONER_LAST_NAME)
        .put(D_8_PETITIONER_EMAIL, TEST_PETITIONER_EMAIL)
        .put(D_8_INFERRED_PETITIONER_GENDER, TEST_INFERRED_GENDER)
        .put(RESP_FIRST_NAME_CCD_FIELD, TEST_RESPONDENT_FIRST_NAME)
        .put(RESP_LAST_NAME_CCD_FIELD, TEST_RESPONDENT_LAST_NAME)
        .put(RESPONDENT_EMAIL_ADDRESS, TEST_RESPONDENT_EMAIL)
        .put(RESPONDENT_DERIVED_CORRESPONDENCE_ADDRESS, "221B Baker Street")
        .put(D_8_CASE_REFERENCE, TEST_CASE_ID)
        .put(DATETIME_OF_HEARING_CCD_FIELD, DATE_TIME_OF_HEARINGS)
        .put(COURT_NAME_CCD_FIELD, TEST_COURT_ID)
        .put(DIVORCE_COSTS_CLAIM_GRANTED_CCD_FIELD, YES_VALUE)
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
        verify(mockEmailService).sendEmailAndReturnExceptionIfFails(eq(TEST_PETITIONER_EMAIL), anyString(), anyMap(), anyString(), any());
        verify(mockEmailService).sendEmailAndReturnExceptionIfFails(eq(TEST_RESPONDENT_EMAIL), anyString(), anyMap(), anyString(), any());
    }

    @Test
    public void givenOfflineRespondentDetails_ThenOkResponse() throws Exception {
        //Given
        when(featureToggleService.isFeatureEnabled(Features.PAPER_UPDATE)).thenReturn(true);
        stubServiceAuthProvider(HttpStatus.OK, TEST_SERVICE_AUTH_TOKEN);

        //Newly generated document
        byte[] coeLetterBytes = new byte[] {1, 2, 3};
        String coeLetterDocumentId =
            stubDocumentGeneratorService(TEMPLATE_ID, COE_RESPONDENT_LETTER_DOCUMENT_TYPE);
        stubDMStore(coeLetterDocumentId, coeLetterBytes);

        //Existing document
        byte[] coeDocumentBytes = new byte[] {4, 5, 6};
        stubDMStore(CERTIFICATE_OF_ENTITLEMENT_ID, coeDocumentBytes);
        CollectionMember<Document> coeDocument = createCollectionMemberDocument(
            getDocumentStoreTestUrl(CERTIFICATE_OF_ENTITLEMENT_ID),
            DOCUMENT_TYPE_COE,
            "certificateOfEntitlement"
        );

        Map<String, Object> caseData = new ImmutableMap.Builder<String, Object>().putAll(BASE_CASE_DATA)
            .put(RESP_IS_USING_DIGITAL_CHANNEL, NO_VALUE)
            .put(D8DOCUMENTS_GENERATED, asList(coeDocument))
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
        verify(bulkPrintService, never())
            .send(
                anyString(),
                eq(CoECoRespondentCoverLetterGenerationTask.FileMetadata.DOCUMENT_TYPE),
                any()
            );

        verify(bulkPrintService, times(1))
            .send(
                eq(TEST_CASE_ID),
                eq(CoERespondentCoverLetterGenerationTask.FileMetadata.DOCUMENT_TYPE),
                documentsToPrintCaptor.capture()
            );

        List<GeneratedDocumentInfo> documentsSentToBulkPrint = documentsToPrintCaptor.getValue();
        assertThat(documentsSentToBulkPrint, hasSize(2));
        assertThat(documentsSentToBulkPrint.get(0).getBytes(), is(coeLetterBytes));
        assertThat(documentsSentToBulkPrint.get(1).getBytes(), is(coeDocumentBytes));
    }

    @Test
    public void responseShouldContainErrorsIfServiceFails() throws Exception {
        doThrow(new NotificationClientException("This has failed."))
            .when(mockEmailService).sendEmailAndReturnExceptionIfFails(anyString(), anyString(), anyMap(), anyString(),  any());

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
            .andExpect(content().string(hasJsonPath("$.errors", hasItem("Failed to send e-mail"))));
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

}