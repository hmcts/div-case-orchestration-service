package uk.gov.hmcts.reform.divorce.orchestration.functionaltest;

import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.matching.EqualToPattern;
import com.google.common.collect.ImmutableMap;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.servlet.MockMvc;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.Features;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CaseDetails;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.documentgeneration.DocumentUpdateRequest;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.documentgeneration.GenerateDocumentRequest;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.documentgeneration.GeneratedDocumentInfo;
import uk.gov.hmcts.reform.divorce.orchestration.service.impl.FeatureToggleServiceImpl;
import uk.gov.hmcts.reform.divorce.orchestration.util.CcdUtil;

import java.util.HashMap;
import java.util.Map;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalToJson;
import static com.jayway.jsonpath.matchers.JsonPathMatchers.hasJsonPath;
import static com.jayway.jsonpath.matchers.JsonPathMatchers.hasNoJsonPath;
import static com.jayway.jsonpath.matchers.JsonPathMatchers.isJson;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonMap;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.equalTo;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.MediaType.APPLICATION_JSON_UTF8_VALUE;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.AWAITING_CLARIFICATION;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.AWAITING_PRONOUNCEMENT;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CASE_DETAILS_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CCD_CASE_DATA_FIELD;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DECREE_NISI_GRANTED_CCD_FIELD;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DECREE_NISI_REFUSAL_DOCUMENT_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DECREE_NISI_REFUSAL_ORDER_CLARIFICATION_TEMPLATE_ID;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DECREE_NISI_REFUSAL_ORDER_DOCUMENT_TYPE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DIVORCE_COSTS_CLAIM_CCD_FIELD;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DIVORCE_COSTS_CLAIM_GRANTED_CCD_FIELD;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DN_DECISION_DATE_FIELD;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DN_OUTCOME_FLAG_CCD_FIELD;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DN_REFUSED;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DOCUMENT_CASE_DETAILS_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.ID;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NO_VALUE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.REFUSAL_DECISION_CCD_FIELD;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.REFUSAL_DECISION_MORE_INFO_VALUE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.STATE_CCD_FIELD;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.WHO_PAYS_CCD_CODE_FOR_RESPONDENT;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.WHO_PAYS_COSTS_CCD_FIELD;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.YES_VALUE;
import static uk.gov.hmcts.reform.divorce.orchestration.tasks.SetDNDecisionStateTask.DN_REFUSED_REJECT_OPTION;
import static uk.gov.hmcts.reform.divorce.orchestration.testutil.ObjectMapperTestUtil.convertObjectToJsonString;

public class DecreeNisiAboutToBeGrantedTest extends MockedFunctionalTest {

    private static final String API_URL = "/dn-about-to-be-granted";
    private static final String CCD_RESPONSE_DATA_FIELD = "data";

    private static final String ADD_DOCUMENTS_CONTEXT_PATH = "/caseformatter/version/1/add-documents";
    private static final String GENERATE_DOCUMENT_CONTEXT_PATH = "/version/1/generatePDF";

    @Autowired
    private MockMvc webClient;

    @Autowired
    private CcdUtil ccdUtil;

    @Autowired
    private FeatureToggleServiceImpl featureToggleService;

    @Before
    public void setup() {
        setDnFeature(true);
    }

    @Test
    public void shouldReturnCaseDataPlusDnGrantedDate_AndState_WhenDNGranted_AndCostsOrderGranted() throws Exception {
        HashMap<Object, Object> caseData = new HashMap<>();
        caseData.put(DECREE_NISI_GRANTED_CCD_FIELD, YES_VALUE);
        caseData.put(DIVORCE_COSTS_CLAIM_CCD_FIELD, YES_VALUE);
        caseData.put(DIVORCE_COSTS_CLAIM_GRANTED_CCD_FIELD, YES_VALUE);
        String inputJson = JSONObject.valueToString(singletonMap(CASE_DETAILS_JSON_KEY,
            singletonMap(CCD_CASE_DATA_FIELD, caseData)
        ));

        webClient.perform(post(API_URL).header(AUTHORIZATION, AUTH_TOKEN).content(inputJson).contentType(APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content().string(allOf(
                isJson(),
                hasJsonPath(CCD_RESPONSE_DATA_FIELD, allOf(
                    hasJsonPath(DECREE_NISI_GRANTED_CCD_FIELD, equalTo(YES_VALUE)),
                    hasJsonPath(STATE_CCD_FIELD, equalTo(AWAITING_PRONOUNCEMENT)),
                    hasJsonPath(WHO_PAYS_COSTS_CCD_FIELD, equalTo(WHO_PAYS_CCD_CODE_FOR_RESPONDENT)),
                    hasJsonPath(DN_OUTCOME_FLAG_CCD_FIELD, equalTo(YES_VALUE)),
                    hasJsonPath(DN_DECISION_DATE_FIELD, equalTo(ccdUtil.getCurrentDateCcdFormat()))
                ))
            )));
    }

    @Test
    public void shouldReturnCaseDataPlusDnGrantedDate_AndState_WhenDNGranted_ButCostsOrderNotGranted() throws Exception {
        String inputJson = JSONObject.valueToString(singletonMap(CASE_DETAILS_JSON_KEY,
            singletonMap(CCD_CASE_DATA_FIELD,
                singletonMap(DECREE_NISI_GRANTED_CCD_FIELD, YES_VALUE)
            )
        ));

        webClient.perform(post(API_URL).header(AUTHORIZATION, AUTH_TOKEN).content(inputJson).contentType(APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content().string(allOf(
                isJson(),
                hasJsonPath(CCD_RESPONSE_DATA_FIELD, allOf(
                    hasJsonPath(DECREE_NISI_GRANTED_CCD_FIELD, equalTo(YES_VALUE)),
                    hasJsonPath(STATE_CCD_FIELD, equalTo(AWAITING_PRONOUNCEMENT)),
                    hasJsonPath(DN_OUTCOME_FLAG_CCD_FIELD, equalTo(YES_VALUE)),
                    hasNoJsonPath(WHO_PAYS_COSTS_CCD_FIELD),
                    hasJsonPath(DN_DECISION_DATE_FIELD, equalTo(ccdUtil.getCurrentDateCcdFormat()))
                ))
            )));
    }

    @Test
    public void shouldReturnCaseDataPlusDnGrantedDate_AndState_WhenDN_NotGranted() throws Exception {
        String inputJson = JSONObject.valueToString(singletonMap(CASE_DETAILS_JSON_KEY,
            singletonMap(CCD_CASE_DATA_FIELD,
                singletonMap(DECREE_NISI_GRANTED_CCD_FIELD, NO_VALUE)
            )
        ));

        webClient.perform(post(API_URL).header(AUTHORIZATION, AUTH_TOKEN).content(inputJson).contentType(APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content().string(allOf(
                isJson(),
                hasJsonPath(CCD_RESPONSE_DATA_FIELD, allOf(
                    hasJsonPath(DECREE_NISI_GRANTED_CCD_FIELD, equalTo(NO_VALUE)),
                    hasJsonPath(STATE_CCD_FIELD, equalTo(AWAITING_CLARIFICATION)),
                    hasJsonPath(DN_DECISION_DATE_FIELD, equalTo(ccdUtil.getCurrentDateCcdFormat())),
                    hasNoJsonPath(WHO_PAYS_COSTS_CCD_FIELD),
                    hasNoJsonPath(DN_OUTCOME_FLAG_CCD_FIELD)
                ))
            )));
    }

    @Test
    public void shouldReturnCaseDataPlusClarificationDocument_AndState_WhenDN_NotGranted_AndDnRefusedForMoreInfo() throws Exception {
        Map<String, Object> caseData = ImmutableMap.of(
            DECREE_NISI_GRANTED_CCD_FIELD, NO_VALUE,
            REFUSAL_DECISION_CCD_FIELD, REFUSAL_DECISION_MORE_INFO_VALUE
        );

        CaseDetails caseDetails = CaseDetails.builder().caseId(TEST_CASE_ID).caseData(caseData).build();

        final GenerateDocumentRequest documentGenerationRequest =
            GenerateDocumentRequest.builder()
                .template(DECREE_NISI_REFUSAL_ORDER_CLARIFICATION_TEMPLATE_ID)
                .values(singletonMap(DOCUMENT_CASE_DETAILS_JSON_KEY, caseDetails))
                .build();

        final GeneratedDocumentInfo documentGenerationResponse =
            GeneratedDocumentInfo.builder()
                .documentType(DECREE_NISI_REFUSAL_ORDER_DOCUMENT_TYPE)
                .fileName(DECREE_NISI_REFUSAL_DOCUMENT_NAME + TEST_CASE_ID)
                .build();

        Map<String, Object> expectedDocumentUpdateRequestData = new HashMap<>();
        expectedDocumentUpdateRequestData.putAll(caseData);
        // Additional fields
        expectedDocumentUpdateRequestData.putAll(ImmutableMap.of(
            STATE_CCD_FIELD, AWAITING_CLARIFICATION,
            DN_DECISION_DATE_FIELD, ccdUtil.getCurrentDateCcdFormat()
        ));

        final DocumentUpdateRequest documentUpdateRequest =
            DocumentUpdateRequest.builder()
                .documents(asList(documentGenerationResponse))
                .caseData(expectedDocumentUpdateRequestData)
                .build();

        stubDocumentGeneratorServerEndpoint(documentGenerationRequest, documentGenerationResponse);
        stubFormatterServerEndpoint(documentUpdateRequest, expectedDocumentUpdateRequestData);

        String inputJson = JSONObject.valueToString(singletonMap(CASE_DETAILS_JSON_KEY,
            ImmutableMap.of(
                ID, TEST_CASE_ID,
                CCD_CASE_DATA_FIELD, caseData
            )
        ));

        webClient.perform(post(API_URL).header(AUTHORIZATION, AUTH_TOKEN).content(inputJson).contentType(APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content().string(allOf(
                isJson(),
                hasJsonPath(CCD_RESPONSE_DATA_FIELD, allOf(
                    hasJsonPath(DECREE_NISI_GRANTED_CCD_FIELD, equalTo(NO_VALUE)),
                    hasJsonPath(STATE_CCD_FIELD, equalTo(AWAITING_CLARIFICATION)),
                    hasJsonPath(DN_DECISION_DATE_FIELD, equalTo(ccdUtil.getCurrentDateCcdFormat())),
                    hasNoJsonPath(WHO_PAYS_COSTS_CCD_FIELD),
                    hasNoJsonPath(DN_OUTCOME_FLAG_CCD_FIELD)
                ))
            )));
    }

    @Test
    public void givenCostDecision_whenNoClaimCost_thenReturnError() throws Exception {
        String inputJson = JSONObject.valueToString(singletonMap(CASE_DETAILS_JSON_KEY,
            singletonMap(CCD_CASE_DATA_FIELD,
                ImmutableMap.of(DIVORCE_COSTS_CLAIM_CCD_FIELD, NO_VALUE,
                DIVORCE_COSTS_CLAIM_GRANTED_CCD_FIELD, YES_VALUE,
                DECREE_NISI_GRANTED_CCD_FIELD, YES_VALUE)
            ))
        );
        webClient.perform(post(API_URL).header(AUTHORIZATION, AUTH_TOKEN).content(inputJson).contentType(APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content().string(allOf(
                isJson(),
                hasJsonPath("errors", contains("Cost decision can only be made if cost has been requested")
                ))
            ));
    }

    @Test
    public void givenNoCostDecision_whenClaimCost_thenReturnError() throws Exception {
        String inputJson = JSONObject.valueToString(singletonMap(CASE_DETAILS_JSON_KEY,
            singletonMap(CCD_CASE_DATA_FIELD,
                ImmutableMap.of(DIVORCE_COSTS_CLAIM_CCD_FIELD, YES_VALUE,
                    DECREE_NISI_GRANTED_CCD_FIELD, YES_VALUE)
            ))
        );
        webClient.perform(post(API_URL).header(AUTHORIZATION, AUTH_TOKEN).content(inputJson).contentType(APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content().string(allOf(
                isJson(),
                hasJsonPath("errors", contains("Cost decision expected")
                ))
            ));
    }

    @Test
    public void givenRejection_whenMakeDecision_thenReturnRightState() throws Exception {
        String inputJson = JSONObject.valueToString(singletonMap(CASE_DETAILS_JSON_KEY,
            singletonMap(CCD_CASE_DATA_FIELD,
                ImmutableMap.of(REFUSAL_DECISION_CCD_FIELD, DN_REFUSED_REJECT_OPTION,
                    DECREE_NISI_GRANTED_CCD_FIELD, NO_VALUE)
            ))
        );
        webClient.perform(post(API_URL).header(AUTHORIZATION, AUTH_TOKEN).content(inputJson).contentType(APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content().string(allOf(
                isJson(),
                hasJsonPath(CCD_RESPONSE_DATA_FIELD, allOf(
                    hasJsonPath(DECREE_NISI_GRANTED_CCD_FIELD, equalTo(NO_VALUE)),
                    hasJsonPath(STATE_CCD_FIELD, equalTo(DN_REFUSED)),
                    hasJsonPath(DN_DECISION_DATE_FIELD, equalTo(ccdUtil.getCurrentDateCcdFormat()))
                ))
            )));
    }

    @Test
    public void givenRejection_andToggleDisabled_whenMakeDecision_thenReturnRightState() throws Exception {
        setDnFeature(false);

        String inputJson = JSONObject.valueToString(singletonMap(CASE_DETAILS_JSON_KEY,
            singletonMap(CCD_CASE_DATA_FIELD,
                ImmutableMap.of(REFUSAL_DECISION_CCD_FIELD, DN_REFUSED_REJECT_OPTION,
                    DECREE_NISI_GRANTED_CCD_FIELD, NO_VALUE)
            ))
        );
        webClient.perform(post(API_URL).header(AUTHORIZATION, AUTH_TOKEN).content(inputJson).contentType(APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content().string(allOf(
                isJson(),
                hasJsonPath(CCD_RESPONSE_DATA_FIELD, allOf(
                    hasJsonPath(DECREE_NISI_GRANTED_CCD_FIELD, equalTo(NO_VALUE)),
                    hasJsonPath(STATE_CCD_FIELD, equalTo(AWAITING_CLARIFICATION)),
                    hasJsonPath(DN_DECISION_DATE_FIELD, equalTo(ccdUtil.getCurrentDateCcdFormat()))
                ))
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

    private void stubFormatterServerEndpoint(DocumentUpdateRequest documentUpdateRequest,
                                             Map<String, Object> response) {
        formatterServiceServer.stubFor(WireMock.post(ADD_DOCUMENTS_CONTEXT_PATH)
                .withRequestBody(equalToJson(convertObjectToJsonString(documentUpdateRequest)))
                .willReturn(aResponse()
                        .withHeader(CONTENT_TYPE, APPLICATION_JSON_UTF8_VALUE)
                        .withStatus(HttpStatus.OK.value())
                        .withBody(convertObjectToJsonString(response))));
    }

    private void setDnFeature(Boolean enableFeature) {
        featureToggleService.getToggle().put(Features.DN_REFUSAL.getName(), enableFeature.toString());
    }
}