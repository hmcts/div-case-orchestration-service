package uk.gov.hmcts.reform.divorce.orchestration.functionaltest.aospack.offline;

import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.junit.WireMockClassRule;
import com.github.tomakehurst.wiremock.matching.EqualToPattern;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CaseDetails;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CcdCallbackRequest;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.documentgeneration.GenerateDocumentRequest;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.documentgeneration.GeneratedDocumentInfo;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.equalToJson;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.jayway.jsonpath.matchers.JsonPathMatchers.hasJsonPath;
import static com.jayway.jsonpath.matchers.JsonPathMatchers.isJson;
import static java.lang.String.format;
import static java.util.Collections.singletonMap;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.core.AllOf.allOf;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.MediaType.APPLICATION_JSON_UTF8_VALUE;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.AOS_OFFLINE_TWO_YEAR_SEPARATION_DOCUMENT_TYPE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.AOS_OFFLINE_TWO_YEAR_SEPARATION_FILENAME;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.AOS_OFFLINE_TWO_YEAR_SEPARATION_TEMPLATE_ID;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CO_RESPONDENT_AOS_INVITATION_LETTER_DOCUMENT_TYPE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CO_RESPONDENT_AOS_INVITATION_LETTER_FILENAME;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CO_RESPONDENT_AOS_INVITATION_LETTER_TEMPLATE_ID;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DOCUMENT_CASE_DETAILS_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.RESPONDENT_AOS_INVITATION_LETTER_DOCUMENT_TYPE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.RESPONDENT_AOS_INVITATION_LETTER_FILENAME;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.RESPONDENT_AOS_INVITATION_LETTER_TEMPLATE_ID;
import static uk.gov.hmcts.reform.divorce.orchestration.testutil.ObjectMapperTestUtil.convertObjectToJsonString;
import static uk.gov.hmcts.reform.divorce.orchestration.testutil.ObjectMapperTestUtil.getJsonFromResourceFile;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
public class AosPackRespondentOfflineTest {

    private static final String API_URL = "/issue-aos-pack-offline/party/%s";
    private static final String USER_TOKEN = "anytoken";

    private static final String GENERATE_DOCUMENT_CONTEXT_PATH = "/version/1/generatePDF";

    @Autowired
    private MockMvc webClient;

    @ClassRule
    public static WireMockClassRule documentGeneratorServer = new WireMockClassRule(4007);

    @Before
    public void setUp() {
        documentGeneratorServer.resetAll();
    }

    @Test
    public void testEndpointReturnsAdequateResponse_ForRespondent() throws Exception {
        CcdCallbackRequest ccdCallbackRequest = getJsonFromResourceFile(
            "/jsonExamples/payloads/genericPetitionerData.json", CcdCallbackRequest.class);
        CaseDetails caseDetails = ccdCallbackRequest.getCaseDetails();
        GenerateDocumentRequest documentRequest = GenerateDocumentRequest.builder()
            .template(RESPONDENT_AOS_INVITATION_LETTER_TEMPLATE_ID)
            .values(singletonMap(DOCUMENT_CASE_DETAILS_JSON_KEY, caseDetails))
            .build();
        GeneratedDocumentInfo documentInfo = GeneratedDocumentInfo.builder()
            .documentType(RESPONDENT_AOS_INVITATION_LETTER_DOCUMENT_TYPE)
            .fileName(RESPONDENT_AOS_INVITATION_LETTER_FILENAME)
            .build();
        stubDocumentGeneratorServerEndpoint(documentRequest, documentInfo);
        String filename = RESPONDENT_AOS_INVITATION_LETTER_FILENAME + caseDetails.getCaseId();

        webClient.perform(post(format(API_URL, "respondent"))
            .contentType(MediaType.APPLICATION_JSON)
            .header(AUTHORIZATION, USER_TOKEN)
            .content(convertObjectToJsonString(ccdCallbackRequest)))
            .andExpect(status().isOk())
            .andExpect(content().string(allOf(
                isJson(),
                hasJsonPath("$.data.D8DocumentsGenerated", hasSize(1)),
                hasJsonPath("$.data.D8DocumentsGenerated", hasItem(
                    hasJsonPath("value.DocumentFileName", is(filename))
                ))
            )));

        documentGeneratorServer.verify(postRequestedFor(urlEqualTo(GENERATE_DOCUMENT_CONTEXT_PATH))
            .withHeader(AUTHORIZATION, equalTo(USER_TOKEN))
            .withRequestBody(equalToJson(convertObjectToJsonString(documentRequest))));

    }

    @Test
    public void testEndpointReturnsAdequateResponse_ForRespondent_ForTwoYearsSeparation() throws Exception {
        CcdCallbackRequest ccdCallbackRequest = getJsonFromResourceFile(
            "/jsonExamples/payloads/genericPetitionerData.json", CcdCallbackRequest.class);
        CaseDetails caseDetails = ccdCallbackRequest.getCaseDetails();
        caseDetails.getCaseData().put("D8ReasonForDivorce", "separation-2-years");

        //Stubbing DGS mock for invitation letter
        GenerateDocumentRequest invitationLetterDocumentRequest = GenerateDocumentRequest.builder()
            .template(RESPONDENT_AOS_INVITATION_LETTER_TEMPLATE_ID)
            .values(singletonMap(DOCUMENT_CASE_DETAILS_JSON_KEY, caseDetails))
            .build();
        GeneratedDocumentInfo invitationLetterDocumentInfo = GeneratedDocumentInfo.builder()
            .documentType(RESPONDENT_AOS_INVITATION_LETTER_DOCUMENT_TYPE)
            .fileName(RESPONDENT_AOS_INVITATION_LETTER_FILENAME)
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

        documentGeneratorServer.verify(postRequestedFor(urlEqualTo(GENERATE_DOCUMENT_CONTEXT_PATH))
            .withHeader(AUTHORIZATION, equalTo(USER_TOKEN))
            .withRequestBody(equalToJson(convertObjectToJsonString(invitationLetterDocumentRequest))));
        documentGeneratorServer.verify(postRequestedFor(urlEqualTo(GENERATE_DOCUMENT_CONTEXT_PATH))
            .withHeader(AUTHORIZATION, equalTo(USER_TOKEN))
            .withRequestBody(equalToJson(convertObjectToJsonString(formDocumentRequest))));
    }

    @Test
    public void testEndpointReturnsAdequateResponse_ForCoRespondent() throws Exception {
        CcdCallbackRequest ccdCallbackRequest = getJsonFromResourceFile(
            "/jsonExamples/payloads/genericPetitionerData.json", CcdCallbackRequest.class);
        CaseDetails caseDetails = ccdCallbackRequest.getCaseDetails();
        GenerateDocumentRequest documentRequest = GenerateDocumentRequest.builder()
            .template(CO_RESPONDENT_AOS_INVITATION_LETTER_TEMPLATE_ID)
            .values(singletonMap(DOCUMENT_CASE_DETAILS_JSON_KEY, caseDetails))
            .build();
        GeneratedDocumentInfo documentInfo = GeneratedDocumentInfo.builder()
            .documentType(CO_RESPONDENT_AOS_INVITATION_LETTER_DOCUMENT_TYPE)
            .fileName(RESPONDENT_AOS_INVITATION_LETTER_FILENAME)
            .build();
        stubDocumentGeneratorServerEndpoint(documentRequest, documentInfo);
        String filename = CO_RESPONDENT_AOS_INVITATION_LETTER_FILENAME + caseDetails.getCaseId();

        webClient.perform(post(format(API_URL, "co-respondent"))
            .contentType(MediaType.APPLICATION_JSON)
            .header(AUTHORIZATION, USER_TOKEN)
            .content(convertObjectToJsonString(ccdCallbackRequest)))
            .andExpect(status().isOk())
            .andExpect(content().string(allOf(
                isJson(),
                hasJsonPath("$.data.D8DocumentsGenerated[0].value.DocumentFileName", is(filename))
            )));

        documentGeneratorServer.verify(postRequestedFor(urlEqualTo(GENERATE_DOCUMENT_CONTEXT_PATH))
            .withHeader(AUTHORIZATION, equalTo(USER_TOKEN))
            .withRequestBody(equalToJson(convertObjectToJsonString(documentRequest))));

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

        documentGeneratorServer.verify(0, postRequestedFor(urlEqualTo(GENERATE_DOCUMENT_CONTEXT_PATH)));
    }

    private void stubDocumentGeneratorServerEndpoint(GenerateDocumentRequest documentRequest, GeneratedDocumentInfo documentInfo) {
        documentGeneratorServer.stubFor(WireMock.post(GENERATE_DOCUMENT_CONTEXT_PATH)
            .withRequestBody(equalToJson(convertObjectToJsonString(documentRequest)))
            .withHeader(AUTHORIZATION, new EqualToPattern(USER_TOKEN))
            .willReturn(aResponse()
                .withHeader(CONTENT_TYPE, APPLICATION_JSON_UTF8_VALUE)
                .withStatus(HttpStatus.OK.value())
                .withBody(convertObjectToJsonString(documentInfo))));
    }
}