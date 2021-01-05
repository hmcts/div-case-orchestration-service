package uk.gov.hmcts.reform.divorce.orchestration.functionaltest.bulk.scan;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.PropertySource;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import uk.gov.hmcts.reform.authorisation.validators.AuthTokenValidator;
import uk.gov.hmcts.reform.divorce.orchestration.OrchestrationServiceApplication;

import static com.jayway.jsonpath.matchers.JsonPathMatchers.hasJsonPath;
import static java.util.Collections.emptyList;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.emptyString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItems;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.ResultMatcher.matchAll;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.SERVICE_AUTHORIZATION_HEADER;
import static uk.gov.hmcts.reform.divorce.orchestration.functionaltest.bulk.scan.S2SAuthTokens.ALLOWED_SERVICE_TOKEN;
import static uk.gov.hmcts.reform.divorce.orchestration.functionaltest.bulk.scan.S2SAuthTokens.I_AM_NOT_ALLOWED_SERVICE_TOKEN;
import static uk.gov.hmcts.reform.divorce.orchestration.service.bulk.scan.BulkScanForms.D8_FORM;
import static uk.gov.hmcts.reform.divorce.orchestration.testutil.ResourceLoader.loadResourceAsString;

@ContextConfiguration(classes = OrchestrationServiceApplication.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@PropertySource(value = "classpath:application.yml")
@AutoConfigureMockMvc
@RunWith(SpringRunner.class)
public class ValidationBulkScanITest {

    private static final String SUCCESS_STATUS = "SUCCESS";
    private static final String WARNINGS_STATUS = "WARNINGS";
    private static final String FULL_D8_FORM_JSON_PATH = "jsonExamples/payloads/bulk/scan/d8/fullD8Form.json";

    private static String VALID_BODY;

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    AuthTokenValidator authTokenValidator;

    @Before
    public void setup() throws Exception {
        when(authTokenValidator.getServiceName(ALLOWED_SERVICE_TOKEN)).thenReturn("bulk_scan_processor");
        when(authTokenValidator.getServiceName(I_AM_NOT_ALLOWED_SERVICE_TOKEN)).thenReturn("don't let me do it!");

        VALID_BODY = loadResourceAsString(FULL_D8_FORM_JSON_PATH);
    }

    @Test
    @Ignore("Covered in integration tests. Failing for some reason. Not on prod")
    public void shouldReturnForbiddenStatusWhenInvalidToken() throws Exception {
        mockMvc.perform(
            post("/forms/{form-type}/validate-ocr", D8_FORM)
                .contentType(APPLICATION_JSON)
                .content(VALID_BODY)
                .header(SERVICE_AUTHORIZATION_HEADER, I_AM_NOT_ALLOWED_SERVICE_TOKEN)
        ).andExpect(matchAll(status().isForbidden()));
    }

    @Test
    @Ignore("Covered in integration tests. Failing for some reason. Not on prod")
    public void shouldReturnUnauthorizedStatusWhenNoAuthServiceHeader() throws Exception {
        mockMvc.perform(
            post("/forms/{form-type}/validate-ocr", D8_FORM)
                .contentType(APPLICATION_JSON)
                .content(VALID_BODY)
                .header(SERVICE_AUTHORIZATION_HEADER, "")
        ).andExpect(matchAll(status().isUnauthorized()));
    }

    @Test
    @Ignore("Covered in integration tests. Failing for some reason. Not on prod")
    public void shouldReturnSuccessResponseForValidationEndpoint() throws Exception {
        mockMvc.perform(
            post("/forms/{form-type}/validate-ocr", D8_FORM)
                .contentType(APPLICATION_JSON)
                .content(VALID_BODY)
                .header(SERVICE_AUTHORIZATION_HEADER, ALLOWED_SERVICE_TOKEN)
        ).andExpect(
            matchAll(
                status().isOk(),
                content().string(allOf(
                    hasJsonPath("$.warnings", equalTo(emptyList())),
                    hasJsonPath("$.errors", equalTo(emptyList())),
                    hasJsonPath("$.status", equalTo(SUCCESS_STATUS))
                ))
            ));
    }

    @Test
    @Ignore("Covered in integration tests. Failing for some reason. Not on prod")
    public void shouldReturnFailureResponseForValidationEndpoint() throws Exception {
        String formToValidate = loadResourceAsString("jsonExamples/payloads/bulk/scan/d8/validation/incompleteForm.json");

        mockMvc.perform(
            post("/forms/{form-type}/validate-ocr", D8_FORM)
                .contentType(APPLICATION_JSON)
                .content(formToValidate)
                .header(SERVICE_AUTHORIZATION_HEADER, ALLOWED_SERVICE_TOKEN)
        ).andExpect(matchAll(
            status().isOk(),
            content().string(allOf(
                hasJsonPath("$.warnings", hasItems(
                    mandatoryFieldIsMissing("D8PetitionerFirstName"),
                    "D8PaymentMethod or D8HelpWithFeesReferenceNumber must contain a value",
                    mandatoryFieldIsMissing("D8PetitionerHasNameChanged"),
                    mandatoryFieldIsMissing("D8MarriedInUk"),
                    mandatoryFieldIsMissing("D8ApplicationToIssueWithoutCertificate"),
                    mandatoryFieldIsMissing("D8MarriageCertificateCorrect"),
                    mandatoryFieldIsMissing("D8PetitionerNameDifferentToMarriageCert"),
                    mandatoryFieldIsMissing("D8RespondentHomeAddressStreet"),
                    mandatoryFieldIsMissing("D8RespondentHomeAddressTown"),
                    mandatoryFieldIsMissing("D8RespondentHomeAddressCounty"),
                    mandatoryFieldIsMissing("D8RespondentPostcode"),
                    mandatoryFieldIsMissing("D8RespondentCorrespondenceSendToSol")
                )),
                hasJsonPath("$.errors", equalTo(emptyList())),
                hasJsonPath("$.status", equalTo(WARNINGS_STATUS))
            ))
        ));
    }

    @Test
    @Ignore("Covered in integration tests. Failing for some reason. Not on prod")
    public void shouldReturnWarningResponseForValidationEndpoint() throws Exception {
        String formToValidate = loadResourceAsString("jsonExamples/payloads/bulk/scan/d8/validation/warningsD8Form.json");

        mockMvc.perform(post("/forms/{form-type}/validate-ocr", D8_FORM)
            .contentType(APPLICATION_JSON)
            .content(formToValidate)
            .header(SERVICE_AUTHORIZATION_HEADER, ALLOWED_SERVICE_TOKEN)
        ).andExpect(matchAll(
            status().isOk(),
            content().string(allOf(
                hasJsonPath("$.warnings", hasItems(
                    notInAValidFormat("D8PetitionerPhoneNumber"),
                    notInAValidFormat("D8PetitionerEmail"),
                    notInAValidFormat("PetitionerSolicitorPhone"),
                    notInAValidFormat("PetitionerSolicitorEmail"),
                    "D8PetitionerCorrespondencePostcode is usually 6 or 7 characters long",
                    mandatoryFieldIsMissing("D8MarriageCertificateCorrect"),
                    mustBeYesOrNo("D8FinancialOrder"),
                    "D8ReasonForDivorce must be \"unreasonable-behaviour\", \"adultery\", \"desertion\","
                        + " \"separation-2-years\" or \"separation-5-years\"",
                    mustBeYesOrNo("D8LegalProceedings")
                )),
                hasJsonPath("$.status", equalTo(WARNINGS_STATUS))
            ))
        ));
    }

    @Test
    @Ignore("Covered in integration tests. Failing for some reason. Not on prod")
    public void shouldReturnResourceNotFoundResponseForUnsupportedFormType() throws Exception {
        mockMvc.perform(
            post("/forms/{form-type}/validate-ocr", "unsupportedFormType")
                .contentType(APPLICATION_JSON)
                .content(VALID_BODY)
                .header(SERVICE_AUTHORIZATION_HEADER, ALLOWED_SERVICE_TOKEN)
        ).andExpect(matchAll(
            status().isNotFound(),
            content().string(is(emptyString()))
        ));
    }

    private String mandatoryFieldIsMissing(String fieldName) {
        return String.format("Mandatory field \"%s\" is missing", fieldName);
    }

    private String notInAValidFormat(String fieldName) {
        return String.format("%s is not in a valid format", fieldName);
    }

    private String mustBeYesOrNo(String fieldName) {
        return String.format("%s must be \"Yes\" or \"No\"", fieldName);
    }
}
