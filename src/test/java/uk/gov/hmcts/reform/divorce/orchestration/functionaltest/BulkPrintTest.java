package uk.gov.hmcts.reform.divorce.orchestration.functionaltest;

import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.matching.EqualToPattern;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import uk.gov.hmcts.reform.divorce.model.ccd.CollectionMember;
import uk.gov.hmcts.reform.divorce.model.ccd.Document;
import uk.gov.hmcts.reform.divorce.model.ccd.DocumentLink;
import uk.gov.hmcts.reform.divorce.orchestration.client.EmailClient;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CaseDetails;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CcdCallbackRequest;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CcdCallbackResponse;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ff4j.FeatureToggle;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.jayway.jsonpath.matchers.JsonPathMatchers.hasJsonPath;
import static com.jayway.jsonpath.matchers.JsonPathMatchers.isJson;
import static java.util.Collections.emptyMap;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.core.Is.is;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.MediaType.ALL_VALUE;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.PERSONAL_SERVICE_VALUE;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.SOL_SERVICE_METHOD_CCD_FIELD;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_SERVICE_AUTH_TOKEN;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DOCUMENT_TYPE_CO_RESPONDENT_INVITATION;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DOCUMENT_TYPE_PETITION;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DOCUMENT_TYPE_RESPONDENT_INVITATION;
import static uk.gov.hmcts.reform.divorce.orchestration.testutil.ObjectMapperTestUtil.convertObjectToJsonString;

public class BulkPrintTest extends IdamTestSupport {

    private static final String API_URL = "/bulk-print";

    private static final String SERVICE_AUTH_CONTEXT_PATH = "/lease";

    private static final String SERVICE_AUTHORIZATION = "ServiceAuthorization";

    private static final String DUE_DATE = "dueDate";

    @Autowired
    private MockMvc webClient;

    @Value("${feature-toggle.toggle.bulk-printer-toggle-name}")
    private String bulkPrintFeatureToggleName;

    @MockBean
    private EmailClient emailClient;

    @Before
    public void setup() {
        sendLetterService.resetAll();
        stubDMStore(HttpStatus.OK);
        stubServiceAuthProvider(HttpStatus.OK, TEST_SERVICE_AUTH_TOKEN);
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
            newDocument("http://localhost:4020/binary", "coRespondentletter", DOCUMENT_TYPE_CO_RESPONDENT_INVITATION),
            newDocument("http://localhost:4020/binary", "aosletter", DOCUMENT_TYPE_RESPONDENT_INVITATION)
        ));
        return caseData;
    }

    private Map<String, Object> expectedCaseDataWithDocuments() {
        final Map<String, Object> caseData = new HashMap<>();
        Document document = new Document();
        document.setDocumentFileName("aosinvitationtest.case.id");
        DocumentLink documentLink = new DocumentLink();
        documentLink.setDocumentBinaryUrl("null/binary");
        documentLink.setDocumentFilename("aosinvitationtest.case.id.pdf");
        document.setDocumentLink(documentLink);
        document.setDocumentType("aos");
        CollectionMember<Document> collectionMember = new CollectionMember<>();
        collectionMember.setValue(document);


        caseData.put("D8DocumentsGenerated", Arrays.asList(
            newDocument("http://localhost:4020/binary", "issue", DOCUMENT_TYPE_PETITION),
            newDocument("http://localhost:4020/binary", "coRespondentletter", DOCUMENT_TYPE_CO_RESPONDENT_INVITATION),
            collectionMember
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

    @Test
    public void givenServiceMethodIsPersonalServiceAndStateIsNotAwaitingService_thenResponseContainsErrors() throws Exception {

        final Map<String, Object> caseData = Collections.singletonMap(
            SOL_SERVICE_METHOD_CCD_FIELD, PERSONAL_SERVICE_VALUE
        );

        final CaseDetails caseDetails = CaseDetails.builder()
            .state("Issued")
            .caseData(caseData)
            .build();

        CcdCallbackRequest request = CcdCallbackRequest.builder()
            .caseDetails(caseDetails)
            .build();

        webClient.perform(post(API_URL)
            .content(convertObjectToJsonString(request))
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON)
            .header(AUTHORIZATION, AUTH_TOKEN))
            .andExpect(status().isOk())
            .andExpect(content().string(allOf(
                isJson(),
                hasJsonPath("$.data", is(Collections.emptyMap())),
                hasJsonPath("$.errors",
                    hasItem("Failed to bulk print documents - This event cannot be used when "
                        + "service method is Personal Service and the case is not in Awaiting Service.")
                )
            )));
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
}
