package uk.gov.hmcts.reform.divorce.orchestration.functionaltest;

import com.google.common.collect.ImmutableMap;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import uk.gov.hmcts.reform.divorce.model.ccd.CaseLink;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CaseDetails;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CcdCallbackRequest;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CcdCallbackResponse;

import java.util.Map;

import static java.util.Collections.singletonList;
import static java.util.Collections.singletonMap;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.BULK_LISTING_CASE_ID_FIELD;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.COSTS_ORDER_DOCUMENT_TYPE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D8DOCUMENTS_GENERATED;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DECREE_NISI_DOCUMENT_TYPE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DECREE_NISI_FILENAME;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DIVORCE_COSTS_CLAIM_CCD_FIELD;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DIVORCE_COSTS_CLAIM_GRANTED_CCD_FIELD;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DN_COSTS_ENDCLAIM_VALUE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DN_COSTS_OPTIONS_CCD_FIELD;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DOCUMENT_CASE_DETAILS_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NO_VALUE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.YES_VALUE;
import static uk.gov.hmcts.reform.divorce.orchestration.testutil.CaseDataTestHelper.createCollectionMemberDocumentAsMap;
import static uk.gov.hmcts.reform.divorce.orchestration.testutil.ObjectMapperTestUtil.convertObjectToJsonString;

public class DnPronouncedDocumentsGenerationITest extends MockedFunctionalTest {
    private static final String API_URL = "/generate-dn-pronouncement-documents";
    private static final String COSTS_ORDER_TEMPLATE_ID = "FL-DIV-DEC-ENG-00060.docx";
    private static final String DECREE_NISI_TEMPLATE_ID = "FL-DIV-GNO-ENG-00021.docx";

    private static final Map<String, Object> CASE_DATA = ImmutableMap.of(
        DIVORCE_COSTS_CLAIM_CCD_FIELD, YES_VALUE,
        DIVORCE_COSTS_CLAIM_GRANTED_CCD_FIELD, YES_VALUE,
        BULK_LISTING_CASE_ID_FIELD, CaseLink.builder().caseReference(TEST_CASE_ID).build()
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
    public void givenBodyIsNull_whenEndpointInvoked_thenReturnBadRequest() throws Exception {
        webClient.perform(post(API_URL)
            .header(AUTHORIZATION, AUTH_TOKEN)
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isBadRequest());
    }

    @Test
    public void givenAuthHeaderIsNull_whenEndpointInvoked_thenReturnBadRequest() throws Exception {
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
        String firstDocumentId = stubDocumentGeneratorService(DECREE_NISI_TEMPLATE_ID,
            singletonMap(DOCUMENT_CASE_DETAILS_JSON_KEY, CASE_DETAILS),
            DECREE_NISI_DOCUMENT_TYPE);

        Map<String, Object> caseDataWithFirstDocumentAdded = ImmutableMap.<String, Object>builder()
            .putAll(CASE_DATA)
            .put(D8DOCUMENTS_GENERATED, singletonList(
                createCollectionMemberDocumentAsMap(getDocumentStoreTestUrl(firstDocumentId),
                    DECREE_NISI_DOCUMENT_TYPE,
                    DECREE_NISI_FILENAME + TEST_CASE_ID)
            )).build();
        stubDocumentGeneratorService(COSTS_ORDER_TEMPLATE_ID, singletonMap(DOCUMENT_CASE_DETAILS_JSON_KEY,
            CaseDetails.builder()
                .caseData(caseDataWithFirstDocumentAdded)
                .caseId(TEST_CASE_ID)
                .build()),
            COSTS_ORDER_DOCUMENT_TYPE);

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
            BULK_LISTING_CASE_ID_FIELD, CaseLink.builder().caseReference(TEST_CASE_ID).build()
        );

        CaseDetails caseDetails = CaseDetails.builder()
            .caseData(caseData)
            .caseId(TEST_CASE_ID)
            .build();

        CcdCallbackRequest ccdCallbackRequest = CcdCallbackRequest.builder()
            .caseDetails(caseDetails)
            .build();

        stubDocumentGeneratorService(DECREE_NISI_TEMPLATE_ID,
            singletonMap(DOCUMENT_CASE_DETAILS_JSON_KEY, caseDetails), DECREE_NISI_DOCUMENT_TYPE);

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
            BULK_LISTING_CASE_ID_FIELD, CaseLink.builder().caseReference(TEST_CASE_ID).build()
        );

        CaseDetails caseDetails = CaseDetails.builder()
            .caseData(caseData)
            .caseId(TEST_CASE_ID)
            .build();

        CcdCallbackRequest ccdCallbackRequest = CcdCallbackRequest.builder()
            .caseDetails(caseDetails)
            .build();

        stubDocumentGeneratorService(DECREE_NISI_TEMPLATE_ID,
            singletonMap(DOCUMENT_CASE_DETAILS_JSON_KEY, caseDetails), DECREE_NISI_DOCUMENT_TYPE);

        CcdCallbackResponse expectedResponse = CcdCallbackResponse.builder().data(caseData).build();

        webClient.perform(post(API_URL)
            .header(AUTHORIZATION, AUTH_TOKEN)
            .content(convertObjectToJsonString(ccdCallbackRequest))
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content().json(convertObjectToJsonString(expectedResponse)));
    }

}
