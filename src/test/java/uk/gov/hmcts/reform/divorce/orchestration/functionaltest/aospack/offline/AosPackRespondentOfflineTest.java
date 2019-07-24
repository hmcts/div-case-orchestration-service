package uk.gov.hmcts.reform.divorce.orchestration.functionaltest.aospack.offline;

import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.junit.WireMockClassRule;
import com.github.tomakehurst.wiremock.matching.EqualToPattern;
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
import static com.github.tomakehurst.wiremock.client.WireMock.verify;
import static java.util.Collections.singletonMap;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.MediaType.APPLICATION_JSON_UTF8_VALUE;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.AOS_INVITATION_LETTER_DOCUMENT_TYPE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.AOS_INVITATION_LETTER_FILENAME;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.AOS_INVITATION_LETTER_TEMPLATE_ID;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DOCUMENT_CASE_DETAILS_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.testutil.ObjectMapperTestUtil.convertObjectToJsonString;
import static uk.gov.hmcts.reform.divorce.orchestration.testutil.ObjectMapperTestUtil.getJsonFromResourceFile;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
public class AosPackRespondentOfflineTest {

    private static final String API_URL = "/issue-aos-pack-offline/party/respondent";
    private static final String USER_TOKEN = "anytoken";
    private static final String GENERATE_DOCUMENT_CONTEXT_PATH = "/version/1/generatePDF";

    @Autowired
    private MockMvc webClient;

    @ClassRule
    public static WireMockClassRule documentGeneratorServer = new WireMockClassRule(4007);

    @Test
    public void testEndpointReturnsAdequateResponse() throws Exception {
        CcdCallbackRequest ccdCallbackRequest = getJsonFromResourceFile(
            "/jsonExamples/payloads/genericPetitionerData.json", CcdCallbackRequest.class);
        CaseDetails caseDetails = ccdCallbackRequest.getCaseDetails();
        GenerateDocumentRequest documentRequest = GenerateDocumentRequest.builder()
            .template(AOS_INVITATION_LETTER_TEMPLATE_ID)
            .values(singletonMap(DOCUMENT_CASE_DETAILS_JSON_KEY, caseDetails))
            .build();
        GeneratedDocumentInfo documentInfo = GeneratedDocumentInfo.builder()
            .documentType(AOS_INVITATION_LETTER_DOCUMENT_TYPE)
            .fileName(AOS_INVITATION_LETTER_FILENAME)
            .build();
        stubDocumentGeneratorServerEndpoint(documentRequest, documentInfo);

        webClient.perform(post(API_URL)
            .contentType(MediaType.APPLICATION_JSON)
            .header(AUTHORIZATION, USER_TOKEN)
            .content(convertObjectToJsonString(ccdCallbackRequest)))
            .andExpect(status().isOk());

        verify(postRequestedFor(urlEqualTo(GENERATE_DOCUMENT_CONTEXT_PATH))
            .withHeader(AUTHORIZATION, equalTo(USER_TOKEN))
            .withRequestBody(equalToJson(convertObjectToJsonString(documentRequest))));
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