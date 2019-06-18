package uk.gov.hmcts.reform.divorce.orchestration.functionaltest;

import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.junit.WireMockClassRule;
import com.github.tomakehurst.wiremock.matching.EqualToPattern;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
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
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CcdCallbackRequest;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CcdCallbackResponse;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.courts.CourtEnum;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.documentgeneration.DocumentUpdateRequest;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.documentgeneration.GenerateDocumentRequest;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.documentgeneration.GeneratedDocumentInfo;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.AddMiniPetitionDraftTask;
import uk.gov.hmcts.reform.divorce.orchestration.testutil.ObjectMapperTestUtil;
import uk.gov.hmcts.reform.divorce.orchestration.util.CcdUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalToJson;
import static java.util.Collections.singletonMap;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.MediaType.APPLICATION_JSON_UTF8_VALUE;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_STATE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CREATED_DATE_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DIVORCE_CENTRE_SITEID_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DIVORCE_UNIT_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DOCUMENT_CASE_DETAILS_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DRAFT_MINI_PETITION_TEMPLATE_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.testutil.ObjectMapperTestUtil.convertObjectToJsonString;

@RunWith(SpringRunner.class)
@ContextConfiguration(classes = OrchestrationServiceApplication.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@PropertySource(value = "classpath:application.yml")
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
public class SolicitorCreateITest {
    private static final String API_URL = "/solicitor-create";

    private static final String GENERATE_DOCUMENT_CONTEXT_PATH = "/version/1/generatePDF";
    private static final String FORMAT_ADD_DOCUMENTS_CONTEXT_PATH = "/caseformatter/version/1/add-documents";

    private static final Map<String, Object> CASE_DATA = Collections.emptyMap();
    private static final CaseDetails CASE_DETAILS =
        CaseDetails.builder()
            .caseData(CASE_DATA)
            .caseId(TEST_CASE_ID)
            .state(TEST_STATE)
            .build();

    private static final CcdCallbackRequest CREATE_EVENT = CcdCallbackRequest.builder()
        .caseDetails(CASE_DETAILS)
        .build();

    @Autowired
    private CcdUtil ccdUtil;

    @Autowired
    private MockMvc webClient;

    @ClassRule
    public static WireMockClassRule documentGeneratorServer = new WireMockClassRule(4007);

    @ClassRule
    public static WireMockClassRule formatterServiceServer = new WireMockClassRule(4011);

    @Test
    public void givenCaseData_whenSolicitorCreate_thenReturnEastMidlandsCourtAllocation() throws Exception {
        final String caseId = "my-case-1";
        Map<String, Object> expectedData = new HashMap<>();
        expectedData.put(CREATED_DATE_JSON_KEY, ccdUtil.getCurrentDateCcdFormat());
        expectedData.put(DIVORCE_UNIT_JSON_KEY, CourtEnum.EASTMIDLANDS.getId());
        expectedData.put(DIVORCE_CENTRE_SITEID_JSON_KEY, CourtEnum.EASTMIDLANDS.getSiteId());

        CaseDetails fullCase = CaseDetails.builder()
                .caseId(caseId)
                .caseData(expectedData)
                .build();

        CcdCallbackResponse expectedResponse = CcdCallbackResponse.builder()
            .data(expectedData)
            .build();

        final GenerateDocumentRequest documentRequest =
            GenerateDocumentRequest.builder()
                .template(DRAFT_MINI_PETITION_TEMPLATE_NAME)
                .values(singletonMap(DOCUMENT_CASE_DETAILS_JSON_KEY, fullCase))
                .build();

        final GeneratedDocumentInfo documentInfo =
            GeneratedDocumentInfo.builder()
                .documentType(AddMiniPetitionDraftTask.DOCUMENT_TYPE)
                .fileName(AddMiniPetitionDraftTask.DOCUMENT_NAME + caseId)
                .build();

        final Set<GeneratedDocumentInfo> documentsForFormatter = new HashSet<>();
        documentsForFormatter.add(documentInfo);

        DocumentUpdateRequest documentFormatRequest = DocumentUpdateRequest.builder()
            .caseData(expectedData)
            .documents(new ArrayList<>(documentsForFormatter))
            .build();

        CcdCallbackRequest ccdCallbackRequest = CcdCallbackRequest.builder()
            .eventId("solicitorCreate")
            .caseDetails(fullCase)
            .build();

        stubDocumentGeneratorServerEndpoint(documentRequest, documentInfo);
        stubFormatterServerEndpoint(documentFormatRequest);

        webClient.perform(post(API_URL)
            .header(AUTHORIZATION, AUTH_TOKEN)
            .content(ObjectMapperTestUtil.convertObjectToJsonString(ccdCallbackRequest))
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk());
    }

    private void stubDocumentGeneratorServerEndpoint(GenerateDocumentRequest generateDocumentRequest,
                                                     GeneratedDocumentInfo response) {
        documentGeneratorServer.stubFor(WireMock.post(GENERATE_DOCUMENT_CONTEXT_PATH)
            .withRequestBody(equalToJson(convertObjectToJsonString(generateDocumentRequest)))
            .withHeader(AUTHORIZATION, new EqualToPattern(AUTH_TOKEN))
            .willReturn(aResponse()
                .withHeader(CONTENT_TYPE, APPLICATION_JSON_UTF8_VALUE)
                .withStatus(HttpStatus.OK.value())
                .withBody(convertObjectToJsonString(response))));
    }

    private void stubFormatterServerEndpoint(DocumentUpdateRequest data) {
        formatterServiceServer.stubFor(WireMock.post(FORMAT_ADD_DOCUMENTS_CONTEXT_PATH)
            .withRequestBody(equalToJson(convertObjectToJsonString(data)))
            .willReturn(aResponse()
                .withStatus(HttpStatus.OK.value())
                .withHeader(CONTENT_TYPE, APPLICATION_JSON_UTF8_VALUE)
                .withBody(convertObjectToJsonString(data))));
    }
}
