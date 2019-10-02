package uk.gov.hmcts.reform.divorce.orchestration.functionaltest.aospack.offline;

import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.matching.EqualToPattern;
import org.json.JSONArray;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;
import uk.gov.hmcts.reform.divorce.model.documentupdate.GeneratedDocumentInfo;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CaseDetails;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CcdCallbackRequest;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.documentgeneration.GenerateDocumentRequest;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ff4j.FeatureToggle;
import uk.gov.hmcts.reform.divorce.orchestration.functionaltest.MockedFunctionalTest;

import java.util.Base64;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.equalToJson;
import static com.github.tomakehurst.wiremock.client.WireMock.matchingJsonPath;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.jayway.jsonpath.matchers.JsonPathMatchers.hasJsonPath;
import static com.jayway.jsonpath.matchers.JsonPathMatchers.isJson;
import static java.lang.String.format;
import static java.util.Collections.singletonMap;
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
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.AOSPackOfflineConstants.AOS_OFFLINE_ADULTERY_CO_RESPONDENT_TEMPLATE_ID;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.AOSPackOfflineConstants.AOS_OFFLINE_TWO_YEAR_SEPARATION_DOCUMENT_TYPE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.AOSPackOfflineConstants.AOS_OFFLINE_TWO_YEAR_SEPARATION_FILENAME;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.AOSPackOfflineConstants.AOS_OFFLINE_TWO_YEAR_SEPARATION_TEMPLATE_ID;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.AOSPackOfflineConstants.CO_RESPONDENT_AOS_INVITATION_LETTER_DOCUMENT_TYPE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.AOSPackOfflineConstants.CO_RESPONDENT_AOS_INVITATION_LETTER_FILENAME;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.AOSPackOfflineConstants.CO_RESPONDENT_AOS_INVITATION_LETTER_TEMPLATE_ID;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.AOSPackOfflineConstants.RESPONDENT_AOS_INVITATION_LETTER_DOCUMENT_TYPE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.AOSPackOfflineConstants.RESPONDENT_AOS_INVITATION_LETTER_FILENAME;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.AOSPackOfflineConstants.RESPONDENT_AOS_INVITATION_LETTER_TEMPLATE_ID;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DOCUMENT_CASE_DETAILS_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.facts.DivorceFacts.ADULTERY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.facts.DivorceFacts.SEPARATION_TWO_YEARS;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.parties.DivorceParty.CO_RESPONDENT;
import static uk.gov.hmcts.reform.divorce.orchestration.testutil.ObjectMapperTestUtil.convertObjectToJsonString;
import static uk.gov.hmcts.reform.divorce.orchestration.testutil.ObjectMapperTestUtil.getJsonFromResourceFile;

@DirtiesContext(classMode = BEFORE_EACH_TEST_METHOD)
public class AosPackRespondentOfflineTest extends MockedFunctionalTest {

    private static final String API_URL = "/issue-aos-pack-offline/party/%s";
    private static final String USER_TOKEN = "anytoken";
    private static final String GENERATE_DOCUMENT_CONTEXT_PATH = "/version/1/generatePDF";
    private static final String SERVICE_AUTHORIZATION = "ServiceAuthorization";

    private static final byte[] FIRST_FILE_BYTES = "firstFile".getBytes();
    private static final byte[] SECOND_FILE_BYTES = "secondFile".getBytes();
    private static final String EXPECTED_ENCODED_FILES_CONTENT = new JSONArray()
        .put(Base64.getEncoder().encodeToString(FIRST_FILE_BYTES))
        .put(Base64.getEncoder().encodeToString(SECOND_FILE_BYTES))
        .toString();

    @Autowired
    private MockMvc webClient;

    @Value("${feature-toggle.toggle.bulk-printer-toggle-name}")
    private String bulkPrintFeatureToggleName;

    @Before
    public void setUp() {
        documentGeneratorServiceServer.resetAll();
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
        caseDetails.getCaseData().put("D8ReasonForDivorce", SEPARATION_TWO_YEARS);

        //Stubbing DGS mock for invitation letter
        GenerateDocumentRequest invitationLetterDocumentRequest = GenerateDocumentRequest.builder()
            .template(RESPONDENT_AOS_INVITATION_LETTER_TEMPLATE_ID)
            .values(singletonMap(DOCUMENT_CASE_DETAILS_JSON_KEY, caseDetails))
            .build();
        GeneratedDocumentInfo invitationLetterDocumentInfo = GeneratedDocumentInfo.builder()
            .documentType(RESPONDENT_AOS_INVITATION_LETTER_DOCUMENT_TYPE)
            .fileName(RESPONDENT_AOS_INVITATION_LETTER_FILENAME)
            .url("http://localhost:4020/1")
            .build();
        stubDocumentGeneratorServerEndpoint(invitationLetterDocumentRequest, invitationLetterDocumentInfo);
        String invitationLetterFilename = RESPONDENT_AOS_INVITATION_LETTER_FILENAME + caseDetails.getCaseId();

        //Stubbing DGS mock for form
        GenerateDocumentRequest formDocumentRequest = GenerateDocumentRequest.builder()
            .template(AOS_OFFLINE_TWO_YEAR_SEPARATION_TEMPLATE_ID)
            .values(singletonMap(DOCUMENT_CASE_DETAILS_JSON_KEY, caseDetails))
            .build();
        GeneratedDocumentInfo formDocumentInfo = GeneratedDocumentInfo.builder()
            .documentType(AOS_OFFLINE_TWO_YEAR_SEPARATION_DOCUMENT_TYPE)
            .fileName(AOS_OFFLINE_TWO_YEAR_SEPARATION_FILENAME)
            .url("http://localhost:4020/2")
            .build();

        stubDocumentGeneratorServerEndpoint(formDocumentRequest, formDocumentInfo);
        String formFilename = AOS_OFFLINE_TWO_YEAR_SEPARATION_FILENAME + caseDetails.getCaseId();

        webClient.perform(post(format(API_URL, "respondent"))
            .contentType(MediaType.APPLICATION_JSON)
            .header(AUTHORIZATION, USER_TOKEN)
            .content(convertObjectToJsonString(ccdCallbackRequest)))
            .andExpect(status().isOk())
            .andExpect(content().string(allOf(
                isJson(),
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
        caseDetails.getCaseData().put("D8ReasonForDivorce", ADULTERY);

        //Stubbing DGS mock for invitation letter
        GenerateDocumentRequest invitationLetterDocumentRequest = GenerateDocumentRequest.builder()
            .template(CO_RESPONDENT_AOS_INVITATION_LETTER_TEMPLATE_ID)
            .values(singletonMap(DOCUMENT_CASE_DETAILS_JSON_KEY, caseDetails))
            .build();
        GeneratedDocumentInfo invitationLetterDocumentInfo = GeneratedDocumentInfo.builder()
            .documentType(CO_RESPONDENT_AOS_INVITATION_LETTER_DOCUMENT_TYPE)
            .fileName(CO_RESPONDENT_AOS_INVITATION_LETTER_FILENAME)
            .url("http://localhost:4020/1")
            .build();
        stubDocumentGeneratorServerEndpoint(invitationLetterDocumentRequest, invitationLetterDocumentInfo);
        String invitationLetterFilename = CO_RESPONDENT_AOS_INVITATION_LETTER_FILENAME + caseDetails.getCaseId();

        //Stubbing DGS mock for form
        GenerateDocumentRequest formDocumentRequest = GenerateDocumentRequest.builder()
            .template(AOS_OFFLINE_ADULTERY_CO_RESPONDENT_TEMPLATE_ID)
            .values(singletonMap(DOCUMENT_CASE_DETAILS_JSON_KEY, caseDetails))
            .build();
        GeneratedDocumentInfo formDocumentInfo = GeneratedDocumentInfo.builder()
            .documentType(AOS_OFFLINE_ADULTERY_CO_RESPONDENT_DOCUMENT_TYPE)
            .fileName(AOS_OFFLINE_ADULTERY_CO_RESPONDENT_FILENAME)
            .url("http://localhost:4020/2")
            .build();
        stubDocumentGeneratorServerEndpoint(formDocumentRequest, formDocumentInfo);
        String formFilename = AOS_OFFLINE_ADULTERY_CO_RESPONDENT_FILENAME + caseDetails.getCaseId();

        webClient.perform(post(format(API_URL, CO_RESPONDENT.getDescription()))
            .contentType(MediaType.APPLICATION_JSON)
            .header(AUTHORIZATION, USER_TOKEN)
            .content(convertObjectToJsonString(ccdCallbackRequest)))
            .andExpect(status().isOk())
            .andExpect(content().string(allOf(
                isJson(),
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

    private void stubDMStore(HttpStatus status) {
        documentStore.stubFor(WireMock.get("/1/binary")
            .withHeader(SERVICE_AUTHORIZATION, new EqualToPattern("Bearer " + TEST_SERVICE_AUTH_TOKEN))
            .withHeader("user-roles", new EqualToPattern("caseworker-divorce"))
            .willReturn(aResponse()
                .withStatus(status.value())
                .withHeader(CONTENT_TYPE, ALL_VALUE)
                .withBody(FIRST_FILE_BYTES)));

        documentStore.stubFor(WireMock.get("/2/binary")
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
