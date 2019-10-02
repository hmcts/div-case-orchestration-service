package uk.gov.hmcts.reform.divorce.orchestration.functionaltest;

import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.matching.EqualToPattern;
import com.google.common.collect.ImmutableMap;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import uk.gov.hmcts.reform.divorce.model.documentupdate.GeneratedDocumentInfo;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CaseDetails;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CcdCallbackRequest;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.documentgeneration.GenerateDocumentRequest;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.idam.Pin;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.idam.PinRequest;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.DefaultTaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskContext;

import java.util.Map;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalToJson;
import static com.jayway.jsonpath.matchers.JsonPathMatchers.hasJsonPath;
import static com.jayway.jsonpath.matchers.JsonPathMatchers.isJson;
import static java.util.Collections.singletonMap;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.nullValue;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.MediaType.ALL_VALUE;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.http.MediaType.APPLICATION_JSON_UTF8_VALUE;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.BEARER_AUTH_TOKEN_1;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_LETTER_HOLDER_ID_CODE;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_PIN_CODE;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_SERVICE_AUTH_TOKEN;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.AOSPackOfflineConstants.RESPONDENT_AOS_INVITATION_LETTER_DOCUMENT_TYPE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.AOSPackOfflineConstants.RESPONDENT_AOS_INVITATION_LETTER_FILENAME;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.AOSPackOfflineConstants.RESPONDENT_AOS_INVITATION_LETTER_TEMPLATE_ID;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.ACCESS_CODE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.AUTH_TOKEN_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CASE_DETAILS_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DOCUMENT_CASE_DETAILS_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DOCUMENT_TYPE_RESPONDENT_INVITATION;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.RESPONDENT_INVITATION_TEMPLATE_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.RESPONDENT_LETTER_HOLDER_ID;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.RESPONDENT_PIN;
import static uk.gov.hmcts.reform.divorce.orchestration.testutil.ObjectMapperTestUtil.convertObjectToJsonString;
import static uk.gov.hmcts.reform.divorce.orchestration.testutil.ObjectMapperTestUtil.getJsonFromResourceFile;

public class RespondentSolicitorNominatedITest extends IdamTestSupport {

    private static final String API_URL = "/aos-solicitor-nominated";
    private static final String AOS_SOL_NOMINATED_JSON = "/jsonExamples/payloads/aosSolicitorNominated.json";
    private static final String GENERATE_DOCUMENT_CONTEXT_PATH = "/version/1/generatePDF";
    private static final String SERVICE_AUTHORIZATION = "ServiceAuthorization";
    private static final byte[] FIRST_FILE_BYTES = "firstFile".getBytes();

    @Autowired
    private MockMvc webClient;

    @Before
    public void setUp() {
        documentGeneratorServiceServer.resetAll();
        documentStore.resetAll();
        serviceAuthProviderServer.resetAll();
    }

    @Test
    public void givenRespondentSolicitorNominated_whenCallbackCalled_linkingFieldsAreReset() throws Exception {
        final PinRequest pinRequest = PinRequest.builder()
            .firstName("")
            .lastName("")
            .build();

        final Pin pin = Pin.builder().pin(TEST_PIN_CODE).userId(TEST_LETTER_HOLDER_ID_CODE).build();

        CcdCallbackRequest ccdCallbackRequest = getJsonFromResourceFile(
            AOS_SOL_NOMINATED_JSON, CcdCallbackRequest.class
        );

        Map<String, Object> caseData = ccdCallbackRequest.getCaseDetails().getCaseData();
        caseData.put(RESPONDENT_LETTER_HOLDER_ID, TEST_LETTER_HOLDER_ID_CODE);

        String caseId = ccdCallbackRequest.getCaseDetails().getCaseId();

        final CaseDetails caseDetails = CaseDetails.builder()
            .caseId(caseId)
            .caseData(caseData)
            .build();

        final TaskContext context = new DefaultTaskContext();
        context.setTransientObject(AUTH_TOKEN_JSON_KEY, AUTH_TOKEN);
        context.setTransientObject(CASE_DETAILS_JSON_KEY, caseDetails);
        context.setTransientObject(RESPONDENT_PIN, TEST_PIN_CODE);

        final GeneratedDocumentInfo expectedAosInvitation =
            GeneratedDocumentInfo.builder()
                .documentType(DOCUMENT_TYPE_RESPONDENT_INVITATION)
                .fileName(AOS_SOL_NOMINATED_JSON)
                .url("http://localhost:4020/1")
                .build();

        final GenerateDocumentRequest generateDocumentRequest =
            GenerateDocumentRequest.builder()
                .template(RESPONDENT_INVITATION_TEMPLATE_NAME)
                .values(
                    ImmutableMap.of(
                        DOCUMENT_CASE_DETAILS_JSON_KEY, caseDetails,
                        ACCESS_CODE, TEST_PIN_CODE)
                )
                .build();

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

        stubDMStore(HttpStatus.OK);
        stubDocumentGeneratorServerEndpoint(generateDocumentRequest, expectedAosInvitation);
        stubSignIn();
        stubPinDetailsEndpoint(BEARER_AUTH_TOKEN_1, pinRequest, pin);
        stubServiceAuthProvider(HttpStatus.OK);

        webClient.perform(MockMvcRequestBuilders.post(API_URL)
            .header(AUTHORIZATION, AUTH_TOKEN)
            .content(convertObjectToJsonString(ccdCallbackRequest))
            .contentType(APPLICATION_JSON)
            .accept(APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content().string(allOf(
                isJson(),
                hasJsonPath("$.errors", nullValue())
            )));
    }

    private void stubDocumentGeneratorServerEndpoint(GenerateDocumentRequest generateDocumentRequest,
                                                     GeneratedDocumentInfo response) {
        documentGeneratorServiceServer.stubFor(WireMock.post(GENERATE_DOCUMENT_CONTEXT_PATH)
            .withRequestBody(equalToJson(convertObjectToJsonString(generateDocumentRequest)))
            .withHeader(AUTHORIZATION, new EqualToPattern(AUTH_TOKEN))
            .willReturn(aResponse()
                .withHeader(CONTENT_TYPE, APPLICATION_JSON_UTF8_VALUE)
                .withStatus(HttpStatus.OK.value())
                .withBody(convertObjectToJsonString(response))));
    }

    private void stubServiceAuthProvider(HttpStatus status) {
        serviceAuthProviderServer.stubFor(WireMock.post(SERVICE_AUTH_CONTEXT_PATH)
            .willReturn(aResponse()
                .withStatus(status.value())
                .withBody(TEST_SERVICE_AUTH_TOKEN)));
    }

    private void stubDMStore(HttpStatus status) {
        documentStore.stubFor(WireMock.get("/1/binary")
            .withHeader(SERVICE_AUTHORIZATION, new EqualToPattern("Bearer " + TEST_SERVICE_AUTH_TOKEN))
            .withHeader("user-roles", new EqualToPattern("caseworker-divorce"))
            .willReturn(aResponse()
                .withStatus(status.value())
                .withHeader(CONTENT_TYPE, ALL_VALUE)
                .withBody(FIRST_FILE_BYTES)));
    }
}
