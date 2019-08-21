package uk.gov.hmcts.reform.divorce.orchestration.functionaltest;

import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.junit.WireMockClassRule;
import com.github.tomakehurst.wiremock.matching.EqualToPattern;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import uk.gov.hmcts.reform.divorce.orchestration.client.EmailClient;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CaseDetails;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CcdCallbackRequest;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CcdCallbackResponse;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CollectionMember;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.Document;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.DocumentLink;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ff4j.FeatureToggle;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.idam.Pin;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.idam.PinRequest;
import uk.gov.hmcts.reform.divorce.orchestration.workflows.CcdCallbackBulkPrintWorkflow;
import uk.gov.hmcts.reform.sendletter.api.SendLetterResponse;
import uk.gov.service.notify.NotificationClientException;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static java.util.Collections.emptyMap;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.MediaType.ALL_VALUE;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.BEARER_AUTH_TOKEN_1;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_LETTER_HOLDER_ID_CODE;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_PIN_CODE;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_SERVICE_AUTH_TOKEN;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D8_RESPONDENT_SOLICITOR_EMAIL;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DOCUMENT_TYPE_CO_RESPONDENT_INVITATION;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DOCUMENT_TYPE_PETITION;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DOCUMENT_TYPE_RESPONDENT_INVITATION;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D_8_PETITIONER_FIRST_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D_8_PETITIONER_LAST_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.testutil.ObjectMapperTestUtil.convertObjectToJsonString;

@RunWith(SpringRunner.class)
public class BulkPrintTest extends IdamTestSupport {

    private static final String API_URL = "/bulk-print";

    private static final String SERVICE_AUTH_CONTEXT_PATH = "/lease";

    private static final String SERVICE_AUTHORIZATION = "ServiceAuthorization";

    private static final String APPLICATION_VND_UK_GOV_HMCTS_LETTER_SERVICE_IN_LETTER = "application/vnd.uk.gov.hmcts.letter-service.in.letter";

    private static final String DUE_DATE = "dueDate";

    private static final String SOLICITOR_AOS_INVITATION_EMAIL_ID = "a193f039-2252-425d-861c-6dba255b7e6e";

    @ClassRule
    public static WireMockClassRule documentStore = new WireMockClassRule(4020);

    @Autowired
    private MockMvc webClient;

    @Value("${feature-toggle.toggle.bulk-printer-toggle-name}")
    private String bulkPrintFeatureToggleName;

    @MockBean
    private EmailClient emailClient;

    @Autowired
    private CcdCallbackBulkPrintWorkflow ccdCallbackBulkPrintWorkflow;

    @Before
    public void setup() {
        sendLetterService.resetAll();
        stubDMStore(HttpStatus.OK);
        stubServiceAuthProvider(HttpStatus.OK, TEST_SERVICE_AUTH_TOKEN);

    }

    private void stubFeatureToggleService(boolean toggle) {
        FeatureToggle featureToggle = new FeatureToggle();
        featureToggle.setEnable(String.valueOf(toggle));
        featureToggle.setUid("divorce_bulk_print");
        featureToggle.setDescription("some description");

        featureToggleService.stubFor(WireMock.get("/api/ff4j/store/features/" + bulkPrintFeatureToggleName)
            .withHeader("Content-Type", new EqualToPattern(APPLICATION_JSON_VALUE))
            .willReturn(aResponse()
                .withHeader("Content-Type", APPLICATION_JSON_VALUE)
                .withStatus(HttpStatus.OK.value())
                .withBody(convertObjectToJsonString(featureToggle))));

    }

    private void stubSendLetterService(HttpStatus status) {
        sendLetterService.stubFor(WireMock.post("/letters")
            .withHeader("ServiceAuthorization", new EqualToPattern("Bearer " + TEST_SERVICE_AUTH_TOKEN))
            .withHeader("Content-Type", new EqualToPattern(APPLICATION_VND_UK_GOV_HMCTS_LETTER_SERVICE_IN_LETTER
                + ".v2+json"))
            .willReturn(aResponse()
                .withStatus(status.value())
                .withBody(convertObjectToJsonString(new SendLetterResponse(UUID.randomUUID())))));
    }

    private void mockEmailClientError() throws NotificationClientException {
        when(emailClient.sendEmail(any(), any(), any(), any())).thenThrow(new NotificationClientException(new Exception("error")));
    }

    @Test
    public void givenCaseDataWithNoSolicitor_whenCalledBulkPrint_thenExpectDueDateInCCDResponse() throws Exception {
        stubFeatureToggleService(true);
        stubSendLetterService(HttpStatus.OK);

        Map<String, Object> expectedCaseData = caseDataWithDocuments();
        expectedCaseData.put("dueDate", LocalDate.now().plus(9, ChronoUnit.DAYS).format(DateTimeFormatter.ISO_LOCAL_DATE));
        CcdCallbackResponse expected = CcdCallbackResponse.builder()
            .data(expectedCaseData)
            .errors(Collections.emptyList())
            .warnings(Collections.emptyList())
            .build();

        webClient.perform(post(API_URL)
            .content(convertObjectToJsonString(callbackWithDocuments()))
            .header(AUTHORIZATION, AUTH_TOKEN)
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content().json(convertObjectToJsonString(expected)));

        verifyZeroInteractions(emailClient);
    }

    @Test
    public void givenCaseDataWithRespondentSolicitor_whenCalledBulkPrint_thenEmailIsSent() throws Exception {
        stubFeatureToggleService(true);
        stubSendLetterService(HttpStatus.OK);
        
        ReflectionTestUtils.setField(ccdCallbackBulkPrintWorkflow, "featureToggleRespSolicitor", true);

        final String petitionerFirstName = "petitioner first name";
        final String petitionerLastName = "petitioner last name";

        final PinRequest pinRequest =
            PinRequest.builder()
                .firstName(petitionerFirstName)
                .lastName(petitionerLastName)
                .build();

        final Pin pin = Pin.builder().pin(TEST_PIN_CODE).userId(TEST_LETTER_HOLDER_ID_CODE).build();

        stubSignIn();
        stubPinDetailsEndpoint(BEARER_AUTH_TOKEN_1, pinRequest, pin);

        final CcdCallbackRequest callbackRequest = callbackWithDocuments();
        final Map<String, Object> caseData = callbackRequest.getCaseDetails().getCaseData();
        caseData.put(D_8_PETITIONER_FIRST_NAME, petitionerFirstName);
        caseData.put(D_8_PETITIONER_LAST_NAME, petitionerLastName);
        caseData.put(D8_RESPONDENT_SOLICITOR_EMAIL, "solicitor@localhost.local");

        Map<String, Object> expectedCaseData = caseDataWithDocuments();
        expectedCaseData.put("dueDate", LocalDate.now().plus(9, ChronoUnit.DAYS).format(DateTimeFormatter.ISO_LOCAL_DATE));
        CcdCallbackResponse expected = CcdCallbackResponse.builder()
            .data(expectedCaseData)
            .errors(Collections.emptyList())
            .warnings(Collections.emptyList())
            .build();

        webClient.perform(post(API_URL)
            .content(convertObjectToJsonString(callbackRequest))
            .header(AUTHORIZATION, AUTH_TOKEN)
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content().json(convertObjectToJsonString(expected)));

        verify(emailClient).sendEmail(eq(SOLICITOR_AOS_INVITATION_EMAIL_ID), eq("solicitor@localhost.local"), any(), any());

        // Only the co-respondent letter should have gone out via SendLetter service
        sendLetterService.verify(1, postRequestedFor(urlEqualTo("/letters")));
    }

    @Test
    public void givenCaseDataWithRespondentSolicitorAndEmailServiceIsDown_whenCalledBulkPrint_thenExpectErrors() throws Exception {
        stubFeatureToggleService(true);
        mockEmailClientError();
        stubSendLetterService(HttpStatus.OK);

        ReflectionTestUtils.setField(ccdCallbackBulkPrintWorkflow, "featureToggleRespSolicitor", true);

        final String petitionerFirstName = "petitioner first name";
        final String petitionerLastName = "petitioner last name";

        final PinRequest pinRequest =
            PinRequest.builder()
                .firstName(petitionerFirstName)
                .lastName(petitionerLastName)
                .build();

        final Pin pin = Pin.builder().pin(TEST_PIN_CODE).userId(TEST_LETTER_HOLDER_ID_CODE).build();

        stubSignIn();
        stubPinDetailsEndpoint(BEARER_AUTH_TOKEN_1, pinRequest, pin);

        final CcdCallbackRequest callbackRequest = callbackWithDocuments();
        final Map<String, Object> caseData = callbackRequest.getCaseDetails().getCaseData();
        caseData.put(D_8_PETITIONER_FIRST_NAME, petitionerFirstName);
        caseData.put(D_8_PETITIONER_LAST_NAME, petitionerLastName);
        caseData.put(D8_RESPONDENT_SOLICITOR_EMAIL, "solicitor@localhost.local");

        webClient.perform(post(API_URL)
            .content(convertObjectToJsonString(callbackRequest))
            .header(AUTHORIZATION, AUTH_TOKEN)
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isInternalServerError())
            .andExpect(content().string("Failed to send e-mail"));
    }

    @Test
    public void givenValidCaseDataWithSendLetterApiDown_whenCalledBulkPrint_thenExpectErrorInCCDResponse() throws Exception {
        stubFeatureToggleService(true);
        stubSendLetterService(HttpStatus.INTERNAL_SERVER_ERROR);
        
        CcdCallbackResponse expected = CcdCallbackResponse.builder()
            .data(emptyMap())
            .errors(Collections.singletonList("Failed to bulk print documents"))
            .warnings(Collections.emptyList())
            .build();

        webClient.perform(post(API_URL)
            .content(convertObjectToJsonString(callbackWithDocuments()))
            .header(AUTHORIZATION, AUTH_TOKEN)
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content().json(convertObjectToJsonString(expected)));
    }

    private CcdCallbackRequest callbackWithDocuments() {
        final Map<String, Object> caseData = caseDataWithDocuments();
        return new CcdCallbackRequest("abacccd", "BulkPrint", CaseDetails.builder()
            .caseData(caseData)
            .caseId("12345")
            .state("AOSPackGenerated").build());
    }

    private Map<String, Object> caseDataWithDocuments() {
        final Map<String, Object> caseData = new HashMap<>();
        caseData.put("D8DocumentsGenerated", Arrays.asList(
            newDocument("http://localhost:4020/binary", "issue", DOCUMENT_TYPE_PETITION),
            newDocument("http://localhost:4020/binary", "aosletter", DOCUMENT_TYPE_RESPONDENT_INVITATION),
            newDocument("http://localhost:4020/binary", "coRespondentletter", DOCUMENT_TYPE_CO_RESPONDENT_INVITATION)
        ));
        return caseData;
    }

    @Test
    public void givenValidCaseData_whenCalledBulkPrintWithFeatureToggleOff_thenExpectNoCallToLetterService() throws Exception {
        stubFeatureToggleService(false);
        stubSendLetterService(HttpStatus.OK);

        Map<String, Object> caseData = caseDataWithDocuments();
        caseData.put(DUE_DATE, LocalDate.now().plus(9, ChronoUnit.DAYS).format(DateTimeFormatter.ISO_LOCAL_DATE));
        CcdCallbackResponse expected = CcdCallbackResponse.builder()
            .data(caseData)
            .errors(Collections.emptyList())
            .warnings(Collections.emptyList())
            .build();

        webClient.perform(post(API_URL)
            .content(convertObjectToJsonString(callbackWithDocuments()))
            .header(AUTHORIZATION, AUTH_TOKEN)
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content().json(convertObjectToJsonString(expected)));

        sendLetterService.verify(0, postRequestedFor(urlEqualTo("/letters")));

    }

    private CollectionMember<Document> newDocument(String url, String name, String type) {
        Document document = new Document();
        DocumentLink documentLink = new DocumentLink();
        documentLink.setDocumentBinaryUrl(url);
        documentLink.setDocumentFilename(name);
        document.setDocumentLink(documentLink);
        document.setDocumentType(type);
        CollectionMember<Document> collectionMember = new CollectionMember<>();
        collectionMember.setValue(document);
        return collectionMember;
    }

    private void stubDMStore(HttpStatus status) {
        documentStore.stubFor(WireMock.get("/binary")
            .withHeader(SERVICE_AUTHORIZATION, new EqualToPattern("Bearer " + TEST_SERVICE_AUTH_TOKEN))
            .withHeader("user-roles", new EqualToPattern("caseworker-divorce"))
            .willReturn(aResponse()
                .withStatus(status.value())
                .withHeader(CONTENT_TYPE, ALL_VALUE)
                .withBody("imagecontent".getBytes())));
    }

    private void stubServiceAuthProvider(HttpStatus status, String response) {
        serviceAuthProviderServer.stubFor(WireMock.post(SERVICE_AUTH_CONTEXT_PATH)
            .willReturn(aResponse()
                .withStatus(status.value())
                .withBody(response)));
    }

}
