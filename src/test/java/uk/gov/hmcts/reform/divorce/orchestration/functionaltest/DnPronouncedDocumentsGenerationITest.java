package uk.gov.hmcts.reform.divorce.orchestration.functionaltest;

import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.matching.EqualToPattern;
import com.google.common.collect.ImmutableMap;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import uk.gov.hmcts.reform.divorce.model.ccd.CollectionMember;
import uk.gov.hmcts.reform.divorce.model.ccd.Document;
import uk.gov.hmcts.reform.divorce.model.ccd.DocumentLink;
import uk.gov.hmcts.reform.divorce.model.documentupdate.GeneratedDocumentInfo;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CaseDetails;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CcdCallbackRequest;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CcdCallbackResponse;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.documentgeneration.GenerateDocumentRequest;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalToJson;
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
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DIVORCE_COSTS_CLAIM_CCD_FIELD;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DIVORCE_COSTS_CLAIM_GRANTED_CCD_FIELD;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DN_COSTS_ENDCLAIM_VALUE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DN_COSTS_OPTIONS_CCD_FIELD;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DOCUMENT_CASE_DETAILS_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NO_VALUE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.YES_VALUE;
import static uk.gov.hmcts.reform.divorce.orchestration.functionaltest.ProcessBulkCaseITest.buildCaseLink;
import static uk.gov.hmcts.reform.divorce.orchestration.testutil.ObjectMapperTestUtil.convertObjectToJsonString;

public class DnPronouncedDocumentsGenerationITest extends MockedFunctionalTest {
    private static final String API_URL = "/generate-dn-pronouncement-documents";
    private static final String GENERATE_DOCUMENT_CONTEXT_PATH = "/version/1/generatePDF";

    private static final Map<String, Object> CASE_DATA = ImmutableMap.of(
        DIVORCE_COSTS_CLAIM_CCD_FIELD, YES_VALUE,
        DIVORCE_COSTS_CLAIM_GRANTED_CCD_FIELD, YES_VALUE,
        BULK_LISTING_CASE_ID_FIELD, buildCaseLink(TEST_CASE_ID)
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

        Map<String, Object> caseData = new HashMap<>(CASE_DATA);
        caseData.put("D8DocumentsGenerated", new ArrayList<CollectionMember<Document>>() {
            {
                Document document = new Document();
                document.setDocumentFileName("decreeNisitest.case.id");
                document.setDocumentType("dnGranted");
                document.setDocumentLink(new DocumentLink() {
                    {
                        setDocumentBinaryUrl("null/binary");
                        setDocumentFilename("decreeNisitest.case.id.pdf");
                    }
                });
                CollectionMember<Document> collectionMember = new CollectionMember<>();
                collectionMember.setValue(document);
                add(collectionMember);
            }
        });

        CaseDetails caseDetails = CaseDetails.builder()
                .caseData(caseData)
                .caseId(TEST_CASE_ID)
                .build();

        final GenerateDocumentRequest costsOrderDocumentGenerationRequest =
            GenerateDocumentRequest.builder()
                .template(COSTS_ORDER_TEMPLATE_ID)
                .values(singletonMap(DOCUMENT_CASE_DETAILS_JSON_KEY, caseDetails))
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

        stubDocumentGeneratorServerEndpoint(dnDocumentGenerationRequest, dnDocumentGenerationResponse);

        stubDocumentGeneratorServerEndpoint(costsOrderDocumentGenerationRequest, costsOrderDocumentGenerationResponse);

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
            BULK_LISTING_CASE_ID_FIELD, buildCaseLink(TEST_CASE_ID)
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

        stubDocumentGeneratorServerEndpoint(dnDocumentGenerationRequest, dnDocumentGenerationResponse);

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
    public void happyPathWithoutCostsOrderWhenPetitionerEndsClaim() throws Exception {

        Map<String, Object> caseData = ImmutableMap.of(
            DIVORCE_COSTS_CLAIM_CCD_FIELD, YES_VALUE,
            DIVORCE_COSTS_CLAIM_GRANTED_CCD_FIELD, YES_VALUE,
            DN_COSTS_OPTIONS_CCD_FIELD, DN_COSTS_ENDCLAIM_VALUE,
            BULK_LISTING_CASE_ID_FIELD, buildCaseLink(TEST_CASE_ID)
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

        stubDocumentGeneratorServerEndpoint(dnDocumentGenerationRequest, dnDocumentGenerationResponse);

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
}
