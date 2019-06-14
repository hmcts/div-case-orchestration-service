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
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CcdCallbackRequest;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CcdCallbackResponse;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.documentgeneration.DocumentUpdateRequest;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.documentgeneration.GenerateDocumentRequest;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.documentgeneration.GeneratedDocumentInfo;
import uk.gov.hmcts.reform.divorce.orchestration.util.DateUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalToJson;
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
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DOCUMENT_CASE_DETAILS_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DOCUMENT_TYPE_PETITION;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D_8_MENTAL_SEP_DATE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D_8_PHYSICAL_SEP_DAIE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D_8_REASON_FOR_DIVORCE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D_8_REASON_FOR_DIVORCE_SEP_DATE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D_8_SEP_REF_DATE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D_8_SEP_TIME_TOGETHER_PERMITTED;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.MINI_PETITION_FILE_NAME_FORMAT;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.MINI_PETITION_TEMPLATE_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.SEPARATION_5YRS;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.SEP_YEARS;
import static uk.gov.hmcts.reform.divorce.orchestration.testutil.ObjectMapperTestUtil.convertObjectToJsonString;

@RunWith(SpringRunner.class)
@ContextConfiguration(classes = OrchestrationServiceApplication.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@PropertySource(value = "classpath:application.yml")
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
public class CalculateSeparationFieldsITest {
    private static final String API_URL = "/calculate-separation-fields";
    private static final String GENERATE_DOCUMENT_CONTEXT_PATH = "/version/1/generatePDF";
    private static final String ADD_DOCUMENTS_CONTEXT_PATH = "/caseformatter/version/1/add-documents";

    @Autowired
    private MockMvc webClient;

    @ClassRule
    public static WireMockClassRule documentGeneratorServiceServer = new WireMockClassRule(4007);

    @ClassRule
    public static WireMockClassRule formatterServiceServer = new WireMockClassRule(4011);

    @Test
    public void givenCaseData_whenCalSepFields_thenReturnPayloadWithCalcFields() throws Exception {

        String pastDate5Yrs8Mnths = DateUtils.formatDateFromDateTime(LocalDateTime.now().minusYears(5).minusMonths(8));
        String pastDate5Yrs9Mnths = DateUtils.formatDateFromDateTime(LocalDateTime.now().minusYears(5).minusMonths(9));
        String pastDate5Yrs6Mnths = DateUtils.formatDateWithCustomerFacingFormat(LocalDate.now().minusYears(5).minusMonths(6));

        Map<String, Object> testCaseData = ImmutableMap.of(D_8_REASON_FOR_DIVORCE, SEPARATION_5YRS,
            D_8_MENTAL_SEP_DATE, pastDate5Yrs8Mnths,
            D_8_PHYSICAL_SEP_DAIE, pastDate5Yrs9Mnths);

        Map<String, Object> expectedData = new HashMap<>();
        expectedData.putAll(testCaseData);
        expectedData.put(D_8_SEP_TIME_TOGETHER_PERMITTED, "6 months");
        expectedData.put(D_8_REASON_FOR_DIVORCE_SEP_DATE, pastDate5Yrs8Mnths);
        expectedData.put(D_8_SEP_REF_DATE, pastDate5Yrs6Mnths);
        expectedData.put(SEP_YEARS, "5");

        CaseDetails caseDetails = CaseDetails.builder()
                .caseData(testCaseData)
                .build();

        final GenerateDocumentRequest generateMiniPetitionRequest =
                GenerateDocumentRequest.builder()
                        .template(MINI_PETITION_TEMPLATE_NAME)
                        .values(singletonMap(DOCUMENT_CASE_DETAILS_JSON_KEY, caseDetails))
                        .build();

        final GeneratedDocumentInfo generatedMiniPetitionResponse =
                GeneratedDocumentInfo.builder()
                        .documentType(DOCUMENT_TYPE_PETITION)
                        .fileName(String.format(MINI_PETITION_FILE_NAME_FORMAT, TEST_CASE_ID))
                        .build();

        final DocumentUpdateRequest documentUpdateRequest =
                DocumentUpdateRequest.builder()
                        .documents(Collections.singletonList(generatedMiniPetitionResponse))
                        .caseData(testCaseData)
                        .build();

        final Map<String, Object> formattedCaseData = emptyMap();

        stubDocumentGeneratorServerEndpoint(generateMiniPetitionRequest, generatedMiniPetitionResponse);
        stubFormatterServerEndpoint(documentUpdateRequest, formattedCaseData);

        CcdCallbackRequest ccdCallbackRequest = CcdCallbackRequest.builder()
            .caseDetails(caseDetails)
            .build();

        CcdCallbackResponse expected = CcdCallbackResponse.builder()
            .data(expectedData)
            .build();

        webClient.perform(post(API_URL)
            .content(convertObjectToJsonString(ccdCallbackRequest))
            .contentType(MediaType.APPLICATION_JSON)
            .header(AUTHORIZATION, AUTH_TOKEN)
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content().json(convertObjectToJsonString(expected)));
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
