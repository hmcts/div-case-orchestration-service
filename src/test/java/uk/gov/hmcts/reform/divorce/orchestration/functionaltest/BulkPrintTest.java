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
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.PropertySource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import uk.gov.hmcts.reform.divorce.orchestration.OrchestrationServiceApplication;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CaseDetails;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CcdCallbackResponse;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CollectionMember;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CreateEvent;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.Document;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.DocumentLink;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ff4j.FeatureToggle;
import uk.gov.hmcts.reform.sendletter.api.SendLetterResponse;

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
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.MediaType.ALL_VALUE;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_SERVICE_AUTH_TOKEN;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DOCUMENT_TYPE_INVITATION;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DOCUMENT_TYPE_PETITION;
import static uk.gov.hmcts.reform.divorce.orchestration.testutil.ObjectMapperTestUtil.convertObjectToJsonString;

@RunWith(SpringRunner.class)
@ContextConfiguration(classes = OrchestrationServiceApplication.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@PropertySource(value = "classpath:application.yml")
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
public class BulkPrintTest {

    private static final String API_URL = "/bulk-print";

    private static final String SERVICE_AUTH_CONTEXT_PATH = "/lease";

    private static final Map<String, Object> CASE_DATA = new HashMap<>();

    private static final String SERVICE_AUTHORIZATION = "ServiceAuthorization";

    private static final String APPLICATION_VND_UK_GOV_HMCTS_LETTER_SERVICE_IN_LETTER =
        "application/vnd.uk.gov.hmcts.letter-service.in.letter";

    private static final String DUE_DATE = "dueDate";
    @ClassRule
    public static WireMockClassRule documentStore = new WireMockClassRule(4020);
    @ClassRule
    public static WireMockClassRule serviceAuthProviderServer = new WireMockClassRule(4504);
    @ClassRule
    public static WireMockClassRule sendLetterService = new WireMockClassRule(4021);
    @ClassRule
    public static WireMockClassRule featureToggleService = new WireMockClassRule(4028);
    @Autowired
    private MockMvc webClient;
    @Value("${feature-toggle.toggle.bulk-printer-toggle-name}")
    private String bulkPrintFeatureToggleName;


    @Before
    public void setup() {
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

    @Test
    public void givenValidCaseDataWithDocs_whenCalledBulkPrint_thenExpectDueDateInCCDResponse() throws Exception {
        stubFeatureToggleService(true);
        stubSendLetterService(HttpStatus.OK);

        CreateEvent createEvent = createCaseEventWithDocuments();
        Map<String, Object> caseData = createEvent.getCaseDetails().getCaseData();
        caseData.put("dueDate", LocalDate.now().plus(9, ChronoUnit.DAYS).format(DateTimeFormatter.ISO_LOCAL_DATE));
        CcdCallbackResponse expected = CcdCallbackResponse.builder()
            .data(caseData)
            .errors(Collections.emptyList())
            .warnings(Collections.emptyList())
            .build();

        webClient.perform(post(API_URL)
            .content(convertObjectToJsonString(createCaseEventWithDocuments()))
            .header(AUTHORIZATION, AUTH_TOKEN)
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content().json(convertObjectToJsonString(expected)));
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
            .content(convertObjectToJsonString(createCaseEventWithDocuments()))
            .header(AUTHORIZATION, AUTH_TOKEN)
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content().json(convertObjectToJsonString(expected)));
    }

    private CreateEvent createCaseEventWithDocuments() {
        CASE_DATA.put("D8DocumentsGenerated", Arrays.asList(
            newDocument("http://localhost:4020/binary", "issue", DOCUMENT_TYPE_PETITION),
            newDocument("http://localhost:4020/binary", "aosletter", DOCUMENT_TYPE_INVITATION)
        ));
        return new CreateEvent("abacccd", "BulkPrint", CaseDetails.builder()
            .caseData(CASE_DATA)
            .caseId("12345")
            .state("AOSPackGenerated").build());
    }

    @Test
    public void givenValidCaseDataWithSendLetterApi_whenCalledBulkPrintWithFeatureToggleOff_thenExpectNoCallToLetterService() throws Exception {
        sendLetterService.resetAll();
        stubFeatureToggleService(false);
        stubSendLetterService(HttpStatus.OK);
        CreateEvent createEvent = createCaseEventWithDocuments();
        Map<String, Object> caseData = createEvent.getCaseDetails().getCaseData();
        caseData.put(DUE_DATE, LocalDate.now().plus(9, ChronoUnit.DAYS).format(DateTimeFormatter.ISO_LOCAL_DATE));
        CcdCallbackResponse expected = CcdCallbackResponse.builder()
            .data(caseData)
            .errors(Collections.emptyList())
            .warnings(Collections.emptyList())
            .build();

        webClient.perform(post(API_URL)
            .content(convertObjectToJsonString(createEvent))
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
