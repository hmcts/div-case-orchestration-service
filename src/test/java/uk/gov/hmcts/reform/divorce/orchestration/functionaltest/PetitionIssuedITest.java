package uk.gov.hmcts.reform.divorce.orchestration.functionaltest;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.junit.WireMockClassRule;
import com.github.tomakehurst.wiremock.matching.EqualToPattern;
import com.google.common.collect.ImmutableMap;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.idam.AuthenticateUserResponse;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.idam.Pin;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.idam.PinRequest;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.idam.TokenExchangeResponse;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.validation.ValidationRequest;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.validation.ValidationResponse;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Base64;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalToJson;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.MediaType.APPLICATION_JSON_UTF8_VALUE;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.AUTH_TOKEN_1;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.BEARER_AUTH_TOKEN_1;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_ERROR;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_LETTER_HOLDER_ID_CODE;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_PIN_CODE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.ACCESS_CODE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.BASIC;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DOCUMENT_CASE_DETAILS_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DOCUMENT_TYPE_INVITATION;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DOCUMENT_TYPE_PETITION;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D_8_PETITIONER_FIRST_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D_8_PETITIONER_LAST_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.FORM_ID;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.INVITATION_FILE_NAME_FORMAT;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.MINI_PETITION_FILE_NAME_FORMAT;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.MINI_PETITION_TEMPLATE_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.RESPONDENT_INVITATION_TEMPLATE_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.RESPONDENT_LETTER_HOLDER_ID;
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
    private static final String IDAM_PIN_DETAILS_CONTEXT_PATH = "/pin";
    private static final String IDAM_AUTHORIZE_CONTEXT_PATH = "/oauth2/authorize";
    private static final String IDAM_EXCHANGE_CODE_CONTEXT_PATH = "/oauth2/token";
    private static final String CODE = "code";
    private static final String AUTHORIZATION_CODE = "authorization_code";
    private static final String RESPONDENT_FIRST_NAME = "respondent first name";
    private static final String RESPONDENT_LAST_NAME = "respondent last name";

    private static final Map<String, Object> CASE_DATA = new HashMap<>();

    private static final CaseDetails CASE_DETAILS = CaseDetails.builder()
            .caseData(CASE_DATA)
            .caseId(TEST_CASE_ID)
            .build();

    private static final CreateEvent CREATE_EVENT = CreateEvent.builder()
            .caseDetails(CASE_DETAILS)
            .build();

    @Value("${idam.api.redirect-url}")
    private String authRedirectUrl;

    @Value("${auth2.client.id}")
    private String authClientId;

    @Value("${auth2.client.secret}")
    private String authClientSecret;

    @Value("${idam.citizen.username}")
    private String citizenUserName;

    @Value("${idam.citizen.password}")
    private String citizenPassword;

    @Autowired
    private MockMvc webClient;

    @ClassRule
    public static WireMockClassRule validationServiceServer = new WireMockClassRule(4008);

    @ClassRule
    public static WireMockClassRule documentGeneratorServiceServer = new WireMockClassRule(4007);

    @ClassRule
    public static WireMockClassRule formatterServiceServer = new WireMockClassRule(4011);

    @ClassRule
    public static WireMockClassRule idamUserServer = new WireMockClassRule(4503);

    @BeforeClass
    public static void beforeClass() {
        CASE_DATA.put(D_8_PETITIONER_FIRST_NAME, RESPONDENT_FIRST_NAME);
        CASE_DATA.put(D_8_PETITIONER_LAST_NAME, RESPONDENT_LAST_NAME);
        CASE_DATA.put(RESPONDENT_LETTER_HOLDER_ID, TEST_LETTER_HOLDER_ID_CODE);
    }

    @Test
    public void givenCreateEventIsNull_whenPetitionIssued_thenReturnBadRequest()
            throws Exception {
        webClient.perform(post(API_URL)
                .header(AUTHORIZATION, AUTH_TOKEN)
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
                .header(AUTHORIZATION, AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is5xxServerError());
    }

    @Test
    public void givenValidationFailed_whenPetitionIssued_thenReturnCaseWithValidationErrors()
            throws Exception {
        final List<String> errors = Collections.singletonList(TEST_ERROR);

        final ValidationResponse validationResponse =
                ValidationResponse.builder()
                        .errors(errors)
                        .build();

        final CcdCallbackResponse ccdCallbackResponse =
                CcdCallbackResponse.builder()
                        .errors(errors)
                        .build();

        stubValidationServerEndpoint(HttpStatus.OK,
                ValidationRequest.builder().data(CASE_DATA).formId(FORM_ID).build(),
                convertObjectToJsonString(validationResponse));

        webClient.perform(post(API_URL)
                .content(convertObjectToJsonString(CREATE_EVENT))
                .header(AUTHORIZATION, AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().json(convertObjectToJsonString(ccdCallbackResponse)));
    }

    @Test
    public void givenEverythingWorksAsExpected_whenPetitionIssued_thenReturnCaseExpectedChanges()
        throws Exception {
        final ValidationResponse validationResponse = ValidationResponse.builder().build();

        final PinRequest pinRequest =
            PinRequest.builder()
                .firstName(RESPONDENT_FIRST_NAME)
                .lastName(RESPONDENT_LAST_NAME)
                .build();

        final Pin pin = Pin.builder().pin(TEST_PIN_CODE).userId(TEST_LETTER_HOLDER_ID_CODE).build();

        final GenerateDocumentRequest generateMiniPetitionRequest =
                GenerateDocumentRequest.builder()
                        .template(MINI_PETITION_TEMPLATE_NAME)
                        .values(Collections.singletonMap(DOCUMENT_CASE_DETAILS_JSON_KEY, CASE_DETAILS))
                        .build();

        final GeneratedDocumentInfo generatedMiniPetitionInfo =
                GeneratedDocumentInfo.builder()
                        .documentType(DOCUMENT_TYPE_PETITION)
                        .fileName(String.format(MINI_PETITION_FILE_NAME_FORMAT, TEST_CASE_ID))
                        .build();

        final GenerateDocumentRequest generateAosInvitationRequest =
            GenerateDocumentRequest.builder()
                .template(RESPONDENT_INVITATION_TEMPLATE_NAME)
                .values(ImmutableMap.of(
                    DOCUMENT_CASE_DETAILS_JSON_KEY, CASE_DETAILS,
                    ACCESS_CODE, TEST_PIN_CODE))
                .build();


        final GeneratedDocumentInfo generatedAosInvitationInfo =
            GeneratedDocumentInfo.builder()
                .documentType(DOCUMENT_TYPE_INVITATION)
                .fileName(String.format(INVITATION_FILE_NAME_FORMAT, TEST_CASE_ID))
                .build();

        final DocumentUpdateRequest documentUpdateRequest =
                DocumentUpdateRequest.builder()
                        .documents(Arrays.asList(generatedMiniPetitionInfo, generatedAosInvitationInfo))
                        .caseData(CASE_DATA)
                        .build();

        final Map<String, Object> formattedCaseData = Collections.emptyMap();

        stubSignIn();
        stubPinDetailsEndpoint(BEARER_AUTH_TOKEN_1, pinRequest, pin);
        stubValidationServerEndpoint(HttpStatus.OK,
                ValidationRequest.builder().data(CASE_DATA).formId(FORM_ID).build(),
                convertObjectToJsonString(validationResponse));
        stubDocumentGeneratorServerEndpoint(generateMiniPetitionRequest, generatedMiniPetitionInfo);
        stubDocumentGeneratorServerEndpoint(generateAosInvitationRequest, generatedAosInvitationInfo);

        stubFormatterServerEndpoint(documentUpdateRequest, formattedCaseData);

        webClient.perform(post(API_URL)
                .content(convertObjectToJsonString(CREATE_EVENT))
                .header(AUTHORIZATION, AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().json(convertObjectToJsonString(formattedCaseData)));
    }

    private void stubSignIn() {
        try {
            final String authCode = "AuthCode";

            final AuthenticateUserResponse authenticateUserResponse =
                AuthenticateUserResponse.builder()
                .code(authCode)
                .build();

            final TokenExchangeResponse tokenExchangeResponse =
                TokenExchangeResponse.builder()
                .accessToken(AUTH_TOKEN_1)
                .build();

            idamUserServer.stubFor(WireMock.post(IDAM_AUTHORIZE_CONTEXT_PATH
                + "?response_type=" + CODE
                + "&client_id=" + authClientId
                + "&redirect_uri=" + URLEncoder.encode(authRedirectUrl, StandardCharsets.UTF_8.name()))
                .withHeader(AUTHORIZATION, new EqualToPattern(getBasicAuthHeader(citizenUserName, citizenPassword)))
                .withHeader(CONTENT_TYPE, new EqualToPattern(MediaType.APPLICATION_JSON_VALUE))
                .willReturn(aResponse()
                    .withStatus(HttpStatus.OK.value())
                    .withHeader(CONTENT_TYPE, APPLICATION_JSON_UTF8_VALUE)
                    .withBody(convertObjectToJsonString(authenticateUserResponse))));

            idamUserServer.stubFor(WireMock.post(IDAM_EXCHANGE_CODE_CONTEXT_PATH
                + "?code=" + authCode
                + "&grant_type=" + AUTHORIZATION_CODE
                + "&redirect_uri=" + URLEncoder.encode(authRedirectUrl, StandardCharsets.UTF_8.name())
                + "&client_id=" + authClientId
                + "&client_secret=" + authClientSecret)
                .willReturn(aResponse()
                    .withStatus(HttpStatus.OK.value())
                    .withHeader(CONTENT_TYPE, APPLICATION_JSON_UTF8_VALUE)
                    .withBody(convertObjectToJsonString(tokenExchangeResponse))));

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
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
                .withHeader(AUTHORIZATION, new EqualToPattern(AUTH_TOKEN))
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

    private void stubPinDetailsEndpoint(String authHeader,
                                        PinRequest pinRequest, Pin response) throws JsonProcessingException {
        idamUserServer.stubFor(WireMock.post(IDAM_PIN_DETAILS_CONTEXT_PATH)
            .withHeader(AUTHORIZATION, new EqualToPattern(authHeader))
            .withHeader(CONTENT_TYPE, new EqualToPattern(APPLICATION_JSON_UTF8_VALUE))
            .withRequestBody(equalToJson(convertObjectToJsonString(pinRequest)))
            .willReturn(aResponse()
                .withStatus(HttpStatus.OK.value())
                .withHeader(CONTENT_TYPE, APPLICATION_JSON_UTF8_VALUE)
                .withBody(convertObjectToJsonString(response))));
    }

    private String getBasicAuthHeader(String username, String password) {
        String authorisation = username + ":" + password;
        return BASIC + Base64.getEncoder().encodeToString(authorisation.getBytes());
    }
}
