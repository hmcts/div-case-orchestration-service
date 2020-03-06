package uk.gov.hmcts.reform.divorce.orchestration.functionaltest.aospack.offline;

import com.github.tomakehurst.wiremock.client.MappingBuilder;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.matching.EqualToPattern;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.json.JSONArray;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CaseDetails;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CcdCallbackRequest;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.documentgeneration.GenerateDocumentRequest;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.documentgeneration.GeneratedDocumentInfo;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ff4j.FeatureToggle;
import uk.gov.hmcts.reform.divorce.orchestration.functionaltest.MockedFunctionalTest;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.equalToJson;
import static com.github.tomakehurst.wiremock.client.WireMock.matchingJsonPath;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.jayway.jsonpath.matchers.JsonPathMatchers.hasJsonPath;
import static com.jayway.jsonpath.matchers.JsonPathMatchers.hasNoJsonPath;
import static com.jayway.jsonpath.matchers.JsonPathMatchers.isJson;
import static java.lang.String.format;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonMap;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.core.AllOf.allOf;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.MediaType.ALL_VALUE;
import static org.springframework.http.MediaType.APPLICATION_JSON_UTF8_VALUE;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.test.annotation.DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_SERVICE_AUTH_TOKEN;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.AOSPackOfflineConstants.AOS_OFFLINE_ADULTERY_CO_RESPONDENT_DOCUMENT_TYPE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.AOSPackOfflineConstants.AOS_OFFLINE_ADULTERY_CO_RESPONDENT_FILENAME;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.AOSPackOfflineConstants.AOS_OFFLINE_TWO_YEAR_SEPARATION_DOCUMENT_TYPE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.AOSPackOfflineConstants.AOS_OFFLINE_TWO_YEAR_SEPARATION_FILENAME;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.AOSPackOfflineConstants.CO_RESPONDENT_AOS_INVITATION_LETTER_DOCUMENT_TYPE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.AOSPackOfflineConstants.CO_RESPONDENT_AOS_INVITATION_LETTER_FILENAME;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.AOSPackOfflineConstants.RESPONDENT_AOS_INVITATION_LETTER_DOCUMENT_TYPE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.AOSPackOfflineConstants.RESPONDENT_AOS_INVITATION_LETTER_FILENAME;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D8DOCUMENTS_GENERATED;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DOCUMENT_CASE_DETAILS_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NO_VALUE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.facts.DivorceFacts.ADULTERY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.facts.DivorceFacts.SEPARATION_TWO_YEARS;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.parties.DivorceParty.CO_RESPONDENT;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.parties.DivorceParty.RESPONDENT;
import static uk.gov.hmcts.reform.divorce.orchestration.testutil.ObjectMapperTestUtil.convertObjectToJsonString;
import static uk.gov.hmcts.reform.divorce.orchestration.testutil.ObjectMapperTestUtil.getJsonFromResourceFile;

@DirtiesContext(classMode = BEFORE_EACH_TEST_METHOD)
public class IssueAosPackOfflineTest extends MockedFunctionalTest {

    private static final String API_URL = "/issue-aos-pack-offline/parties/%s";
    private static final String USER_TOKEN = "anytoken";

    private static final String GENERATE_DOCUMENT_CONTEXT_PATH = "/version/1/generatePDF";
    private static final String ADD_DOCUMENTS_CONTEXT_PATH = "/caseformatter/version/1/add-documents";

    private static final String SERVICE_AUTHORIZATION = "ServiceAuthorization";

    private static final String SERVICE_AUTH_CONTEXT_PATH = "/lease";

    private static final byte[] FIRST_FILE_BYTES = "firstFile".getBytes();
    private static final byte[] SECOND_FILE_BYTES = "secondFile".getBytes();
    private static final String EXPECTED_ENCODED_FILES_CONTENT = new JSONArray()
        .put(Base64.getEncoder().encodeToString(FIRST_FILE_BYTES))
        .put(Base64.getEncoder().encodeToString(SECOND_FILE_BYTES))
        .toString();

    private static final String RESPONDENT_AOS_INVITATION_LETTER_TEMPLATE_ID = "FL-DIV-LET-ENG-00075.doc";
    private static final String CO_RESPONDENT_AOS_INVITATION_LETTER_TEMPLATE_ID = "FL-DIV-LET-ENG-00076.doc";
    private static final String AOS_OFFLINE_TWO_YEAR_SEPARATION_TEMPLATE_ID = "FL-DIV-APP-ENG-00080.docx";
    private static final String AOS_OFFLINE_ADULTERY_CO_RESPONDENT_TEMPLATE_ID = "FL-DIV-APP-ENG-00084.docx";



    @Autowired
    private MockMvc webClient;

    @Value("${feature-toggle.toggle.bulk-printer-toggle-name}")
    private String bulkPrintFeatureToggleName;

    @Before
    public void setUp() {
        documentGeneratorServiceServer.resetAll();
        formatterServiceServer.resetAll();
        sendLetterService.resetAll();
        documentStore.resetAll();
        serviceAuthProviderServer.resetAll();
        featureToggleService.resetAll();

        stubSendLetterService(OK);
        stubServiceAuthProvider(OK, TEST_SERVICE_AUTH_TOKEN);
        stubFeatureToggleService(true);
        stubDMStore(OK);
    }

    @Test
    public void testEndpointReturnsAdequateResponse_ForRespondent() throws Exception {
        CcdCallbackRequest ccdCallbackRequest = getJsonFromResourceFile(
            "/jsonExamples/payloads/genericPetitionerData.json", CcdCallbackRequest.class);
        CaseDetails caseDetails = ccdCallbackRequest.getCaseDetails();
        caseDetails.getCaseData().put("D8ReasonForDivorce", SEPARATION_TWO_YEARS.getValue());

        final GenerateDocumentRequest invitationLetterDocumentRequest = stubDgsInvitationLetterUsing(
                RESPONDENT_AOS_INVITATION_LETTER_TEMPLATE_ID, RESPONDENT_AOS_INVITATION_LETTER_DOCUMENT_TYPE.getValue(),
                caseDetails);
        String invitationLetterFilename = RESPONDENT_AOS_INVITATION_LETTER_FILENAME.getValue() + caseDetails.getCaseId();

        final GenerateDocumentRequest formDocumentRequest = stubDgsFormUsing(AOS_OFFLINE_TWO_YEAR_SEPARATION_TEMPLATE_ID,
                AOS_OFFLINE_TWO_YEAR_SEPARATION_DOCUMENT_TYPE.getValue(), AOS_OFFLINE_TWO_YEAR_SEPARATION_FILENAME.getValue(), caseDetails);
        String formFilename = AOS_OFFLINE_TWO_YEAR_SEPARATION_FILENAME.getValue() + caseDetails.getCaseId();

        //Stubbing CFS
        stubFormatterServerEndpoint(asList(
            ImmutablePair.of(invitationLetterFilename, RESPONDENT_AOS_INVITATION_LETTER_DOCUMENT_TYPE.getValue()),
            ImmutablePair.of(formFilename, AOS_OFFLINE_TWO_YEAR_SEPARATION_DOCUMENT_TYPE.getValue())
        ));

        webClient.perform(post(format(API_URL, RESPONDENT.getDescription()))
            .contentType(MediaType.APPLICATION_JSON)
            .header(AUTHORIZATION, USER_TOKEN)
            .content(convertObjectToJsonString(ccdCallbackRequest)))
            .andExpect(status().isOk())
            .andExpect(content().string(allOf(
                isJson(),
                hasJsonPath("$.data.RespContactMethodIsDigital", is(NO_VALUE)),
                hasJsonPath("$.data.D8DocumentsGenerated", hasSize(2)),
                hasJsonPath("$.data.D8DocumentsGenerated", hasItems(
                    hasJsonPath("value.DocumentFileName", is(invitationLetterFilename)),
                    hasJsonPath("value.DocumentFileName", is(formFilename))
                ))
            )));

        //Verifying interactions
        documentGeneratorServiceServer.verify(postRequestedFor(urlEqualTo(GENERATE_DOCUMENT_CONTEXT_PATH))
            .withHeader(AUTHORIZATION, equalTo(USER_TOKEN))
            .withRequestBody(equalToJson(convertObjectToJsonString(invitationLetterDocumentRequest))));
        documentGeneratorServiceServer.verify(postRequestedFor(urlEqualTo(GENERATE_DOCUMENT_CONTEXT_PATH))
            .withHeader(AUTHORIZATION, equalTo(USER_TOKEN))
            .withRequestBody(equalToJson(convertObjectToJsonString(formDocumentRequest))));

        formatterServiceServer.verify(postRequestedFor(urlEqualTo(ADD_DOCUMENTS_CONTEXT_PATH))
            .withHeader(CONTENT_TYPE, equalTo(APPLICATION_JSON))
        );
        sendLetterService.verify(postRequestedFor(urlEqualTo("/letters")).withRequestBody(
            matchingJsonPath("$.documents", equalToJson(EXPECTED_ENCODED_FILES_CONTENT, false, false))
        ).withRequestBody(
            matchingJsonPath("$.additional_data.letterType", equalTo("aos-pack-offline-respondent"))
        ));
    }

    @Test
    public void testEndpointReturnsAdequateResponse_ForCoRespondent() throws Exception {
        CcdCallbackRequest ccdCallbackRequest = getJsonFromResourceFile(
            "/jsonExamples/payloads/genericPetitionerData.json", CcdCallbackRequest.class);
        CaseDetails caseDetails = ccdCallbackRequest.getCaseDetails();
        caseDetails.getCaseData().put("D8ReasonForDivorce", ADULTERY.getValue());

        final GenerateDocumentRequest invitationLetterDocumentRequest = stubDgsInvitationLetterUsing(
                CO_RESPONDENT_AOS_INVITATION_LETTER_TEMPLATE_ID, CO_RESPONDENT_AOS_INVITATION_LETTER_DOCUMENT_TYPE.getValue(), caseDetails);
        String invitationLetterFilename = CO_RESPONDENT_AOS_INVITATION_LETTER_FILENAME.getValue() + caseDetails.getCaseId();

        final GenerateDocumentRequest formDocumentRequest = stubDgsFormUsing(AOS_OFFLINE_ADULTERY_CO_RESPONDENT_TEMPLATE_ID,
                AOS_OFFLINE_ADULTERY_CO_RESPONDENT_DOCUMENT_TYPE.getValue(), AOS_OFFLINE_ADULTERY_CO_RESPONDENT_FILENAME.getValue(), caseDetails);
        String formFilename = AOS_OFFLINE_ADULTERY_CO_RESPONDENT_FILENAME.getValue() + caseDetails.getCaseId();

        //Stubbing CFS
        stubFormatterServerEndpoint(asList(
            ImmutablePair.of(invitationLetterFilename, CO_RESPONDENT_AOS_INVITATION_LETTER_DOCUMENT_TYPE.getValue()),
            ImmutablePair.of(formFilename, AOS_OFFLINE_ADULTERY_CO_RESPONDENT_DOCUMENT_TYPE.getValue())
        ));

        webClient.perform(post(format(API_URL, CO_RESPONDENT.getDescription()))
            .contentType(MediaType.APPLICATION_JSON)
            .header(AUTHORIZATION, USER_TOKEN)
            .content(convertObjectToJsonString(ccdCallbackRequest)))
            .andExpect(status().isOk())
            .andExpect(content().string(allOf(
                isJson(),
                hasJsonPath("$.data.CoRespContactMethodIsDigital", is(NO_VALUE)),
                hasJsonPath("$.data.D8DocumentsGenerated", hasSize(2)),
                hasJsonPath("$.data.D8DocumentsGenerated", hasItems(
                    hasJsonPath("value.DocumentFileName", is(invitationLetterFilename)),
                    hasJsonPath("value.DocumentFileName", is(formFilename))
                ))
            )));

        //Verifying interactions
        documentGeneratorServiceServer.verify(postRequestedFor(urlEqualTo(GENERATE_DOCUMENT_CONTEXT_PATH))
            .withHeader(AUTHORIZATION, equalTo(USER_TOKEN))
            .withRequestBody(equalToJson(convertObjectToJsonString(invitationLetterDocumentRequest))));
        documentGeneratorServiceServer.verify(postRequestedFor(urlEqualTo(GENERATE_DOCUMENT_CONTEXT_PATH))
            .withHeader(AUTHORIZATION, equalTo(USER_TOKEN))
            .withRequestBody(equalToJson(convertObjectToJsonString(formDocumentRequest))));

        formatterServiceServer.verify(postRequestedFor(urlEqualTo(ADD_DOCUMENTS_CONTEXT_PATH))
            .withHeader(CONTENT_TYPE, equalTo(APPLICATION_JSON))
        );
        sendLetterService.verify(postRequestedFor(urlEqualTo("/letters")).withRequestBody(
            matchingJsonPath("$.documents", equalToJson(EXPECTED_ENCODED_FILES_CONTENT, false, false))
        ).withRequestBody(
            matchingJsonPath("$.additional_data.letterType", equalTo("aos-pack-offline-co-respondent"))
        ));
    }

    @Test
    public void testEndpointReturnsErrorMessages_WhenDivorcePartyIsInvalid() throws Exception {
        CcdCallbackRequest ccdCallbackRequest = getJsonFromResourceFile(
            "/jsonExamples/payloads/genericPetitionerData.json", CcdCallbackRequest.class);

        webClient.perform(post(format(API_URL, "invalid-divorce-party"))
            .contentType(MediaType.APPLICATION_JSON)
            .header(AUTHORIZATION, USER_TOKEN)
            .content(convertObjectToJsonString(ccdCallbackRequest)))
            .andExpect(status().isBadRequest());

        documentGeneratorServiceServer.verify(0, postRequestedFor(urlEqualTo(GENERATE_DOCUMENT_CONTEXT_PATH)));
        formatterServiceServer.verify(0, postRequestedFor(urlEqualTo(ADD_DOCUMENTS_CONTEXT_PATH)));
    }

    @Test
    public void testEndpointReturnsExpectedDueDateInResponse_ForRespondent() throws Exception {

        CcdCallbackRequest ccdCallbackRequest = getJsonFromResourceFile(
                "/jsonExamples/payloads/genericPetitionerData.json", CcdCallbackRequest.class);
        CaseDetails caseDetails = ccdCallbackRequest.getCaseDetails();
        caseDetails.getCaseData().put("D8ReasonForDivorce", SEPARATION_TWO_YEARS.getValue());

        GenerateDocumentRequest invitationLetterDocumentRequest = stubDgsInvitationLetterUsing(
                RESPONDENT_AOS_INVITATION_LETTER_TEMPLATE_ID, RESPONDENT_AOS_INVITATION_LETTER_DOCUMENT_TYPE.getValue(), caseDetails);
        String invitationLetterFilename =
                RESPONDENT_AOS_INVITATION_LETTER_FILENAME.getValue() + caseDetails.getCaseId();

        GenerateDocumentRequest formDocumentRequest = stubDgsFormUsing(AOS_OFFLINE_TWO_YEAR_SEPARATION_TEMPLATE_ID,
                AOS_OFFLINE_TWO_YEAR_SEPARATION_DOCUMENT_TYPE.getValue(),
                AOS_OFFLINE_TWO_YEAR_SEPARATION_FILENAME.getValue(), caseDetails);
        String formFilename = AOS_OFFLINE_TWO_YEAR_SEPARATION_FILENAME.getValue() + caseDetails.getCaseId();

        //Stubbing CFS
        stubFormatterServerEndpoint(asList(
                ImmutablePair.of(invitationLetterFilename, RESPONDENT_AOS_INVITATION_LETTER_DOCUMENT_TYPE.getValue()),
                ImmutablePair.of(formFilename, AOS_OFFLINE_TWO_YEAR_SEPARATION_DOCUMENT_TYPE.getValue())
        ));

        String expectedDueDate = LocalDate.now().plus(9, ChronoUnit.DAYS).format(DateTimeFormatter.ISO_LOCAL_DATE);
        webClient.perform(post(format(API_URL, RESPONDENT.getDescription()))
                .content(convertObjectToJsonString(ccdCallbackRequest))
                .header(AUTHORIZATION, USER_TOKEN)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().string(allOf(
                        isJson(),
                        hasJsonPath("$.data.dueDate", is(expectedDueDate)))));
    }

    @Test
    public void testEndpointReturnsNoDueDateInResponse_ForCoRespondent() throws Exception {

        CcdCallbackRequest ccdCallbackRequest = getJsonFromResourceFile(
                "/jsonExamples/payloads/genericPetitionerData.json", CcdCallbackRequest.class);
        CaseDetails caseDetails = ccdCallbackRequest.getCaseDetails();
        caseDetails.getCaseData().put("D8ReasonForDivorce", ADULTERY.getValue());

        GenerateDocumentRequest invitationLetterDocumentRequest = stubDgsInvitationLetterUsing(
                CO_RESPONDENT_AOS_INVITATION_LETTER_TEMPLATE_ID, CO_RESPONDENT_AOS_INVITATION_LETTER_DOCUMENT_TYPE.getValue(), caseDetails);
        String invitationLetterFilename = CO_RESPONDENT_AOS_INVITATION_LETTER_FILENAME.getValue() + caseDetails.getCaseId();

        GenerateDocumentRequest formDocumentRequest = stubDgsFormUsing(AOS_OFFLINE_ADULTERY_CO_RESPONDENT_TEMPLATE_ID,
                AOS_OFFLINE_ADULTERY_CO_RESPONDENT_DOCUMENT_TYPE.getValue(), AOS_OFFLINE_ADULTERY_CO_RESPONDENT_FILENAME.getValue(), caseDetails);
        String formFilename = AOS_OFFLINE_ADULTERY_CO_RESPONDENT_FILENAME.getValue() + caseDetails.getCaseId();

        //Stubbing CFS
        stubFormatterServerEndpoint(asList(
                ImmutablePair.of(invitationLetterFilename, CO_RESPONDENT_AOS_INVITATION_LETTER_DOCUMENT_TYPE.getValue()),
                ImmutablePair.of(formFilename, AOS_OFFLINE_ADULTERY_CO_RESPONDENT_DOCUMENT_TYPE.getValue())
        ));

        webClient.perform(post(format(API_URL, CO_RESPONDENT.getDescription()))
                .content(convertObjectToJsonString(ccdCallbackRequest))
                .header(AUTHORIZATION, USER_TOKEN)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().string(allOf(isJson())))
                .andExpect(content().string(hasNoJsonPath("$.data.dueDate")));
    }

    private GenerateDocumentRequest stubDgsInvitationLetterUsing(String templateId, String documentType, CaseDetails caseDetails) {
        GenerateDocumentRequest invitationLetterDocumentRequest = GenerateDocumentRequest.builder()
                .template(templateId)
                .values(singletonMap(DOCUMENT_CASE_DETAILS_JSON_KEY, caseDetails))
                .build();
        GeneratedDocumentInfo invitationLetterDocumentInfo = GeneratedDocumentInfo.builder()
                .documentType(documentType)
                .fileName(RESPONDENT_AOS_INVITATION_LETTER_FILENAME.getValue())
                .build();
        stubDocumentGeneratorServerEndpoint(invitationLetterDocumentRequest, invitationLetterDocumentInfo);
        return invitationLetterDocumentRequest;
    }

    private GenerateDocumentRequest stubDgsFormUsing(String templateId, String documentType, String filename, CaseDetails caseDetails) {
        GenerateDocumentRequest formDocumentRequest = GenerateDocumentRequest.builder()
                .template(templateId)
                .values(singletonMap(DOCUMENT_CASE_DETAILS_JSON_KEY, caseDetails))
                .build();
        GeneratedDocumentInfo formDocumentInfo = GeneratedDocumentInfo.builder()
                .documentType(documentType)
                .fileName(filename)
                .build();
        stubDocumentGeneratorServerEndpoint(formDocumentRequest, formDocumentInfo);
        return formDocumentRequest;
    }

    private void stubDocumentGeneratorServerEndpoint(GenerateDocumentRequest documentRequest, GeneratedDocumentInfo documentInfo) {
        documentGeneratorServiceServer.stubFor(WireMock.post(GENERATE_DOCUMENT_CONTEXT_PATH)
            .withRequestBody(equalToJson(convertObjectToJsonString(documentRequest)))
            .withHeader(AUTHORIZATION, new EqualToPattern(USER_TOKEN))
            .willReturn(aResponse()
                .withHeader(CONTENT_TYPE, APPLICATION_JSON_UTF8_VALUE)
                .withStatus(OK.value())
                .withBody(convertObjectToJsonString(documentInfo))));
    }

    private void stubFormatterServerEndpoint(List<Pair<String, String>> documentsToStub) {
        MappingBuilder mappingBuilder = WireMock.post(ADD_DOCUMENTS_CONTEXT_PATH);
        List<Map> documentsGenerated = new ArrayList<>();
        for (int i = 0; i < documentsToStub.size(); i++) {
            Pair<String, String> documentToStub = documentsToStub.get(i);
            HashMap<String, Object> value = new HashMap<>();
            value.put("DocumentFileName", documentToStub.getKey());
            value.put("DocumentType", documentToStub.getValue());
            value.put("DocumentLink", singletonMap("document_binary_url", "http://localhost:4020/binary/" + (i + 1)));
            documentsGenerated.add(singletonMap("value", value));

            String documentJsonPath = "$.documents[" + i + "]";
            mappingBuilder.withRequestBody(matchingJsonPath(documentJsonPath, matchingJsonPath("documentType", equalTo(documentToStub.getValue()))));
            mappingBuilder.withRequestBody(matchingJsonPath(documentJsonPath, matchingJsonPath("fileName", equalTo(documentToStub.getKey()))));
        }

        Map<String, Object> responseFromCFS = singletonMap(D8DOCUMENTS_GENERATED, documentsGenerated);
        formatterServiceServer.stubFor(mappingBuilder.willReturn(aResponse()
            .withHeader(CONTENT_TYPE, APPLICATION_JSON_UTF8_VALUE)
            .withStatus(OK.value())
            .withBody(convertObjectToJsonString(responseFromCFS))));
    }

    private void stubDMStore(HttpStatus status) {
        documentStore.stubFor(WireMock.get("/binary/1")
            .withHeader(SERVICE_AUTHORIZATION, new EqualToPattern("Bearer " + TEST_SERVICE_AUTH_TOKEN))
            .withHeader("user-roles", new EqualToPattern("caseworker-divorce"))
            .willReturn(aResponse()
                .withStatus(status.value())
                .withHeader(CONTENT_TYPE, ALL_VALUE)
                .withBody(FIRST_FILE_BYTES)));

        documentStore.stubFor(WireMock.get("/binary/2")
            .withHeader(SERVICE_AUTHORIZATION, new EqualToPattern("Bearer " + TEST_SERVICE_AUTH_TOKEN))
            .withHeader("user-roles", new EqualToPattern("caseworker-divorce"))
            .willReturn(aResponse()
                .withStatus(status.value())
                .withHeader(CONTENT_TYPE, ALL_VALUE)
                .withBody(SECOND_FILE_BYTES)));
    }

    private void stubServiceAuthProvider(HttpStatus status, String response) {
        serviceAuthProviderServer.stubFor(WireMock.post(SERVICE_AUTH_CONTEXT_PATH)
            .willReturn(aResponse()
                .withStatus(status.value())
                .withBody(response)));
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
                .withStatus(OK.value())
                .withBody(convertObjectToJsonString(featureToggle))));

    }

}