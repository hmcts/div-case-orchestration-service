package uk.gov.hmcts.reform.divorce.orchestration.functionaltest;

import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.junit.WireMockClassRule;
import com.github.tomakehurst.wiremock.matching.EqualToPattern;
import com.google.common.collect.ImmutableMap;
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
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CaseLink;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CcdCallbackRequest;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CcdCallbackResponse;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.documentgeneration.DocumentUpdateRequest;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.documentgeneration.GenerateDocumentRequest;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.documentgeneration.GeneratedDocumentInfo;

import java.util.Map;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalToJson;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyMap;
import static java.util.Collections.singletonMap;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.MediaType.APPLICATION_JSON_UTF8_VALUE;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.BULK_LISTING_CASE_ID_FIELD;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.COSTS_ORDER_DOCUMENT_TYPE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.COSTS_ORDER_TEMPLATE_ID;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DECREE_NISI_DOCUMENT_TYPE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DECREE_NISI_FILENAME;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DECREE_NISI_TEMPLATE_ID;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DIVORCE_COSTS_CLAIM_GRANTED_CCD_FIELD;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DOCUMENT_CASE_DETAILS_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NO_VALUE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.YES_VALUE;
import static uk.gov.hmcts.reform.divorce.orchestration.testutil.ObjectMapperTestUtil.convertObjectToJsonString;

@RunWith(SpringRunner.class)
@ContextConfiguration(classes = OrchestrationServiceApplication.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@PropertySource(value = "classpath:application.yml")
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
public class DnPronouncedDocumentsGenerationITest {
    private static final String API_URL = "/generate-dn-pronouncement-documents";
    private static final String ADD_DOCUMENTS_CONTEXT_PATH = "/caseformatter/version/1/add-documents";
    private static final String GENERATE_DOCUMENT_CONTEXT_PATH = "/version/1/generatePDF";

    private static final Map<String, Object> CASE_DATA = ImmutableMap.of(
        DIVORCE_COSTS_CLAIM_GRANTED_CCD_FIELD, YES_VALUE,
        BULK_LISTING_CASE_ID_FIELD, new CaseLink(TEST_CASE_ID)
    );

    private static final CaseDetails CASE_DETAILS = CaseDetails.builder()
        .caseData(CASE_DATA)
        .caseId(TEST_CASE_ID)
        .build();

    private static final CcdCallbackRequest CCD_CALLBACK_REQUEST = CcdCallbackRequest.builder()
        .caseDetails(CASE_DETAILS)
        .build();

    @Autowired
    private MockMvc webClient;

    @ClassRule
    public static WireMockClassRule documentGeneratorServiceServer = new WireMockClassRule(4007);

    @ClassRule
    public static WireMockClassRule formatterServiceServer = new WireMockClassRule(4011);

    @Test
    public void givenBodyIsNull_whenEndpointInvoked_thenReturnBadRequest()
        throws Exception {
        webClient.perform(post(API_URL)
            .header(AUTHORIZATION, AUTH_TOKEN)
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isBadRequest());
    }

    @Test
    public void givenAuthHeaderIsNull_whenEndpointInvoked_thenReturnBadRequest()
        throws Exception {
        webClient.perform(post(API_URL)
            .content(convertObjectToJsonString(CCD_CALLBACK_REQUEST))
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isBadRequest());
    }

    @Test
    public void happyPathWithNoBulkCaseLinkId() throws Exception {

        Map<String, Object> caseData = ImmutableMap.of(
            DIVORCE_COSTS_CLAIM_GRANTED_CCD_FIELD, NO_VALUE
        );

        CaseDetails caseDetails = CaseDetails.builder()
                .caseData(caseData)
                .caseId(TEST_CASE_ID)
                .build();

        CcdCallbackRequest ccdCallbackRequest = CcdCallbackRequest.builder()
                .caseDetails(caseDetails)
                .build();

        CcdCallbackResponse expectedResponse = CcdCallbackResponse.builder().data(caseData).build();

        webClient.perform(post(API_URL)
                .header(AUTHORIZATION, AUTH_TOKEN)
                .content(convertObjectToJsonString(ccdCallbackRequest))
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().json(convertObjectToJsonString(expectedResponse)));
    }

    @Test
    public void happyPathWithCostsOrder() throws Exception {

        final GenerateDocumentRequest dnDocumentGenerationRequest =
            GenerateDocumentRequest.builder()
                .template(DECREE_NISI_TEMPLATE_ID)
                .values(singletonMap(DOCUMENT_CASE_DETAILS_JSON_KEY, CASE_DETAILS))
                .build();

        final GenerateDocumentRequest costsOrderDocumentGenerationRequest =
                GenerateDocumentRequest.builder()
                        .template(COSTS_ORDER_TEMPLATE_ID)
                        .values(singletonMap(DOCUMENT_CASE_DETAILS_JSON_KEY, CASE_DETAILS))
                        .build();

        final GeneratedDocumentInfo dnDocumentGenerationResponse =
            GeneratedDocumentInfo.builder()
                .documentType(DECREE_NISI_DOCUMENT_TYPE)
                .fileName(DECREE_NISI_FILENAME + TEST_CASE_ID)
                .build();

        final GeneratedDocumentInfo costsOrderDocumentGenerationResponse =
                GeneratedDocumentInfo.builder()
                        .documentType(COSTS_ORDER_DOCUMENT_TYPE)
                        .fileName(COSTS_ORDER_DOCUMENT_TYPE + TEST_CASE_ID)
                        .build();

        final DocumentUpdateRequest dnDocumentUpdateRequest =
            DocumentUpdateRequest.builder()
                .documents(asList(dnDocumentGenerationResponse))
                .caseData(CASE_DATA)
                .build();

        final DocumentUpdateRequest costsOrderDocumentUpdateRequest =
                DocumentUpdateRequest.builder()
                        .documents(asList(costsOrderDocumentGenerationResponse))
                        .caseData(CASE_DATA)
                        .build();

        final Map<String, Object> emptyCaseData =  emptyMap();

        stubDocumentGeneratorServerEndpoint(dnDocumentGenerationRequest, dnDocumentGenerationResponse);
        stubFormatterServerEndpoint(dnDocumentUpdateRequest, emptyCaseData);

        stubDocumentGeneratorServerEndpoint(costsOrderDocumentGenerationRequest, costsOrderDocumentGenerationResponse);
        stubFormatterServerEndpoint(costsOrderDocumentUpdateRequest, emptyCaseData);

        CcdCallbackResponse expectedResponse = CcdCallbackResponse.builder().data(CASE_DATA).build();

        webClient.perform(post(API_URL)
            .header(AUTHORIZATION, AUTH_TOKEN)
            .content(convertObjectToJsonString(CCD_CALLBACK_REQUEST))
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content().json(convertObjectToJsonString(expectedResponse)));
    }

    @Test
    public void happyPathWithoutCostsOrder() throws Exception {

        Map<String, Object> caseData = ImmutableMap.of(
            DIVORCE_COSTS_CLAIM_GRANTED_CCD_FIELD, NO_VALUE,
            BULK_LISTING_CASE_ID_FIELD, new CaseLink(TEST_CASE_ID)
        );

        CaseDetails caseDetails = CaseDetails.builder()
                .caseData(caseData)
                .caseId(TEST_CASE_ID)
                .build();

        CcdCallbackRequest ccdCallbackRequest = CcdCallbackRequest.builder()
                .caseDetails(caseDetails)
                .build();

        final GenerateDocumentRequest dnDocumentGenerationRequest =
                GenerateDocumentRequest.builder()
                        .template(DECREE_NISI_TEMPLATE_ID)
                        .values(singletonMap(DOCUMENT_CASE_DETAILS_JSON_KEY, caseDetails))
                        .build();

        final GeneratedDocumentInfo dnDocumentGenerationResponse =
                GeneratedDocumentInfo.builder()
                        .documentType(DECREE_NISI_DOCUMENT_TYPE)
                        .fileName(DECREE_NISI_FILENAME + TEST_CASE_ID)
                        .build();

        final DocumentUpdateRequest dnDocumentUpdateRequest =
                DocumentUpdateRequest.builder()
                        .documents(asList(dnDocumentGenerationResponse))
                        .caseData(caseData)
                        .build();

        final Map<String, Object> emptyCaseData =  emptyMap();

        stubDocumentGeneratorServerEndpoint(dnDocumentGenerationRequest, dnDocumentGenerationResponse);
        stubFormatterServerEndpoint(dnDocumentUpdateRequest, emptyCaseData);

        CcdCallbackResponse expectedResponse = CcdCallbackResponse.builder().data(caseData).build();

        webClient.perform(post(API_URL)
                .header(AUTHORIZATION, AUTH_TOKEN)
                .content(convertObjectToJsonString(ccdCallbackRequest))
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().json(convertObjectToJsonString(expectedResponse)));
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

    private void stubFormatterServerEndpoint(DocumentUpdateRequest documentUpdateRequest,
                                             Map<String, Object> response) {
        formatterServiceServer.stubFor(WireMock.post(ADD_DOCUMENTS_CONTEXT_PATH)
            .withRequestBody(equalToJson(convertObjectToJsonString(documentUpdateRequest)))
            .willReturn(aResponse()
                .withHeader(CONTENT_TYPE, APPLICATION_JSON_UTF8_VALUE)
                .withStatus(HttpStatus.OK.value())
                .withBody(convertObjectToJsonString(response))));
    }

}
