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
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CcdCallbackResponse;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CreateEvent;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.documentgeneration.DocumentUpdateRequest;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.documentgeneration.GenerateDocumentRequest;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.documentgeneration.GeneratedDocumentInfo;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.validation.ValidationRequest;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.validation.ValidationResponse;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalToJson;
import static org.hamcrest.CoreMatchers.containsString;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.MediaType.APPLICATION_JSON_UTF8_VALUE;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CASE_DETAILS_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DOCUMENT_CASE_DETAILS_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DOCUMENT_TYPE_PETITION;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.FORM_ID;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.MINI_PETITION_FILE_NAME_FORMAT;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.MINI_PETITION_TEMPLATE_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.testutil.ObjectMapperTestUtil.convertObjectToJsonString;

@RunWith(SpringRunner.class)
@ContextConfiguration(classes = OrchestrationServiceApplication.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@PropertySource(value = "classpath:application.yml")
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
public class PetitionIssuedITest {
    private static final String API_URL = "/petition-issued";
    private static final String VALIDATION_CONTEXT_PATH = "/version/1/validate";
    private static final String ADD_DOCUMENTS_CONTEXT_PATH = "/caseformatter/version/1/add-documents";
    private static final String GENERATE_DOCUMENT_CONTEXT_PATH = "/version/1/generatePDF";

    private static final String USER_TOKEN = "Some JWT Token";
    private static final String CASE_ID = "12345";

    private static final Map<String, Object> CASE_DATA = Collections.emptyMap();
    private static final CaseDetails CASE_DETAILS = CaseDetails.builder()
            .caseData(CASE_DATA)
            .caseId(CASE_ID)
            .build();

    private static final CreateEvent CREATE_EVENT = CreateEvent.builder()
            .caseDetails(CASE_DETAILS)
            .build();

    @Autowired
    private MockMvc webClient;

    @ClassRule
    public static WireMockClassRule validationServiceServer = new WireMockClassRule(4008);

    @ClassRule
    public static WireMockClassRule documentGeneratorServiceServer = new WireMockClassRule(4007);

    @ClassRule
    public static WireMockClassRule formatterServiceServer = new WireMockClassRule(4011);

    @Test
    public void givenCreateEventIsNull_whenPetitionIssued_thenReturnBadRequest()
            throws Exception {
        webClient.perform(post(API_URL)
                .header(AUTHORIZATION, USER_TOKEN)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void givenJWTTokenIsNull_whenPetitionIssued_thenReturnBadRequest()
            throws Exception {
        webClient.perform(post(API_URL)
                .content(convertObjectToJsonString(CREATE_EVENT))
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void givenThereIsAConnectionError_whenPetitionIssued_thenReturnBadGateway()
            throws Exception {
        final String errorMessage = "some error message";

        stubValidationServerEndpoint(HttpStatus.BAD_GATEWAY,
                ValidationRequest.builder().data(CASE_DATA).formId(FORM_ID).build(), errorMessage);

        webClient.perform(post(API_URL)
                .content(convertObjectToJsonString(CREATE_EVENT))
                .header(AUTHORIZATION, USER_TOKEN)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is5xxServerError());
    }

    // @Test - TODO
    public void givenValidationFailed_whenPetitionIssued_thenReturnCaseWithValidationErrors()
            throws Exception {
        final List<String> errors = Collections.singletonList("Some Error");
        final List<String> warnings = Collections.singletonList("Some Warning");

        final ValidationResponse validationResponse =
                ValidationResponse.builder()
                        .errors(errors)
                        .warnings(warnings)
                        .build();

        final CcdCallbackResponse ccdCallbackResponse =
                CcdCallbackResponse.builder()
                        .data(CASE_DATA)
                        .errors(errors)
                        .warnings(warnings)
                        .build();

        stubValidationServerEndpoint(HttpStatus.OK,
                ValidationRequest.builder().data(CASE_DATA).formId(FORM_ID).build(),
                convertObjectToJsonString(validationResponse));

        webClient.perform(post(API_URL)
                .content(convertObjectToJsonString(CREATE_EVENT))
                .header(AUTHORIZATION, USER_TOKEN)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().json(convertObjectToJsonString(ccdCallbackResponse)));
    }

    // @Test - TODO
    public void givenEverythingWorksAsExpected_whenPetitionIssued_thenReturnCaseExpectedChanges()
            throws Exception {
        final ValidationResponse validationResponse = ValidationResponse.builder().build();

        final GenerateDocumentRequest generateDocumentRequest =
                GenerateDocumentRequest.builder()
                        .template(MINI_PETITION_TEMPLATE_NAME)
                        .values(Collections.singletonMap(DOCUMENT_CASE_DETAILS_JSON_KEY, CASE_DETAILS))
                        .build();

        final GeneratedDocumentInfo generatedDocumentInfo =
                GeneratedDocumentInfo.builder()
                        .documentType(DOCUMENT_TYPE_PETITION)
                        .fileName(String.format(MINI_PETITION_FILE_NAME_FORMAT, CASE_ID))
                        .build();

        final DocumentUpdateRequest documentUpdateRequest =
                DocumentUpdateRequest.builder()
                        .documents(Collections.singletonList(generatedDocumentInfo))
                        .caseData(CASE_DATA)
                        .build();

        final Map<String, Object> formattedCaseData = Collections.emptyMap();

        stubValidationServerEndpoint(HttpStatus.OK,
                ValidationRequest.builder().data(CASE_DATA).formId(FORM_ID).build(),
                convertObjectToJsonString(validationResponse));
        stubDocumentGeneratorServerEndpoint(generateDocumentRequest, generatedDocumentInfo);
        stubFormatterServerEndpoint(documentUpdateRequest, formattedCaseData);

        webClient.perform(post(API_URL)
                .content(convertObjectToJsonString(CREATE_EVENT))
                .header(AUTHORIZATION, USER_TOKEN)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().json(convertObjectToJsonString(formattedCaseData)));
    }

    private void stubValidationServerEndpoint(HttpStatus status,
                                              ValidationRequest validationRequest, String body)
            throws Exception {
        validationServiceServer.stubFor(WireMock.post(VALIDATION_CONTEXT_PATH)
                .withRequestBody(equalToJson(convertObjectToJsonString(validationRequest)))
                .willReturn(aResponse()
                        .withStatus(status.value())
                        .withHeader(CONTENT_TYPE, APPLICATION_JSON_UTF8_VALUE)
                        .withBody(body)));
    }

    private void stubDocumentGeneratorServerEndpoint(GenerateDocumentRequest generateDocumentRequest,
                                                     GeneratedDocumentInfo response)
            throws Exception {
        documentGeneratorServiceServer.stubFor(WireMock.post(GENERATE_DOCUMENT_CONTEXT_PATH)
                .withRequestBody(equalToJson(convertObjectToJsonString(generateDocumentRequest)))
                .withHeader(AUTHORIZATION, new EqualToPattern(USER_TOKEN))
                .willReturn(aResponse()
                        .withStatus(HttpStatus.OK.value())
                        .withHeader(CONTENT_TYPE, APPLICATION_JSON_UTF8_VALUE)
                        .withBody(convertObjectToJsonString(response))));
    }

    private void stubFormatterServerEndpoint(DocumentUpdateRequest documentUpdateRequest,
                                             Map<String, Object> response)
            throws Exception {
        formatterServiceServer.stubFor(WireMock.post(ADD_DOCUMENTS_CONTEXT_PATH)
                .withRequestBody(equalToJson(convertObjectToJsonString(documentUpdateRequest)))
                .willReturn(aResponse()
                        .withStatus(HttpStatus.OK.value())
                        .withHeader(CONTENT_TYPE, APPLICATION_JSON_UTF8_VALUE)
                        .withBody(convertObjectToJsonString(response))));
    }
}
