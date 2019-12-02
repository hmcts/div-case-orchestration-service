package uk.gov.hmcts.reform.divorce.orchestration.functionaltest.bulk.scan;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
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
import static com.jayway.jsonpath.matchers.JsonPathMatchers.hasNoJsonPath;
import static com.jayway.jsonpath.matchers.JsonPathMatchers.isJson;
import static java.util.Collections.emptyList;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.reform.divorce.orchestration.controller.BulkScanController.SERVICE_AUTHORISATION_HEADER;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D_8_PETITIONER_FIRST_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D_8_PETITIONER_LAST_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D_8_REASON_FOR_DIVORCE_SEPARATION_DAY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D_8_REASON_FOR_DIVORCE_SEPARATION_MONTH;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D_8_REASON_FOR_DIVORCE_SEPARATION_YEAR;
import static uk.gov.hmcts.reform.divorce.orchestration.functionaltest.bulk.scan.S2SAuthTokens.ALLOWED_SERVICE_TOKEN;
import static uk.gov.hmcts.reform.divorce.orchestration.functionaltest.bulk.scan.S2SAuthTokens.I_AM_NOT_ALLOWED_SERVICE_TOKEN;
import static uk.gov.hmcts.reform.divorce.orchestration.testutil.ResourceLoader.loadResourceAsString;

@ContextConfiguration(classes = OrchestrationServiceApplication.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@PropertySource(value = "classpath:application.yml")
@AutoConfigureMockMvc
@RunWith(SpringRunner.class)
public class TransformationBulkScanITest {

    private static final String FULL_D8_FORM_JSON_PATH = "jsonExamples/payloads/bulk/scan/fullD8Form.json";
    private static final String PARTIAL_D8_FORM_JSON_PATH = "jsonExamples/payloads/bulk/scan/partialD8Form.json";
    private static final String TRANSFORMATION_URL = "/transform-exception-record";

    private static String VALID_BODY;

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    AuthTokenValidator authTokenValidator;

    @Before
    public void setup() throws Exception {
        when(authTokenValidator.getServiceName(ALLOWED_SERVICE_TOKEN)).thenReturn("bulk_scan_orchestrator");
        when(authTokenValidator.getServiceName(I_AM_NOT_ALLOWED_SERVICE_TOKEN)).thenReturn("don't let me do it!");

        VALID_BODY = loadResourceAsString(FULL_D8_FORM_JSON_PATH);
    }

    @Test
    public void shouldReturnForbiddenStatusWhenInvalidS2SToken() throws Exception {
        mockMvc.perform(
            post(TRANSFORMATION_URL)
                .contentType(APPLICATION_JSON)
                .content(VALID_BODY)
                .header(SERVICE_AUTHORISATION_HEADER, I_AM_NOT_ALLOWED_SERVICE_TOKEN)
        ).andExpect(status().isForbidden());
    }

    @Test
    public void shouldReturnUnauthorisedStatusWhenNoS2SToken() throws Exception {
        mockMvc.perform(
            post(TRANSFORMATION_URL)
                .contentType(APPLICATION_JSON)
                .content(VALID_BODY)
        ).andExpect(status().isUnauthorized());
    }

    @Test
    public void shouldReturnSuccessResponseForTransformationEndpointWhenUsingFullDataSet() throws Exception {
        mockMvc.perform(
            post(TRANSFORMATION_URL)
                .contentType(APPLICATION_JSON)
                .content(VALID_BODY)
                .header(SERVICE_AUTHORISATION_HEADER, ALLOWED_SERVICE_TOKEN)
        )
            .andExpect(status().isOk())
            .andExpect(content().string(
                allOf(
                    isJson(),
                    hasJsonPath("$.warnings", equalTo(emptyList())),
                    hasJsonPath("$.case_creation_details.*", hasSize(3)),
                    hasJsonPath("$.case_creation_details", allOf(
                        hasJsonPath("case_type_id", is("DIVORCE")),
                        hasJsonPath("event_id", is("bulkScanCaseCreate")),
                        hasJsonPath("case_data.*", hasSize(29)),
                        hasJsonPath("case_data", allOf(
                            hasJsonPath("bulkScanCaseReference", is("LV481297")),
                            hasJsonPath("D8PaymentMethod", is("Card")),
                            hasJsonPath(D_8_PETITIONER_FIRST_NAME, is("Christopher")),
                            hasJsonPath(D_8_PETITIONER_LAST_NAME, is("O'John")),
                            hasJsonPath("D8PetitionerPhoneNumber", is("1111111111")),
                            hasJsonPath("D8PetitionerEmail", is("test.testerson@mailinator.com")),
                            hasJsonPath("D8LegalProcess", is("Divorce")),
                            hasJsonPath("D8ScreenHasMarriageCert", is("True")),
                            hasJsonPath("D8CertificateInEnglish", is("True")),
                            hasJsonPath("D8RespondentFirstName", is("Jane")),
                            hasJsonPath("D8RespondentLastName", is("Doe")),
                            hasJsonPath("D8RespondentPhoneNumber", is("22222222222")),
                            hasJsonPath("D8PetitionerNameChangedHow", is("Yes")),
                            hasJsonPath("D8PetitionerContactDetailsConfidential", is("No")),
                            hasJsonPath("D8PetitionerHomeAddress",
                                hasJsonPath("PostCode", is("SE1 2BP"))),
                            hasJsonPath("D8MarriagePetitionerName", is("Christopher O'John")),
                            hasJsonPath(D_8_REASON_FOR_DIVORCE_SEPARATION_DAY, is("20")),
                            hasJsonPath(D_8_REASON_FOR_DIVORCE_SEPARATION_MONTH, is("11")),
                            hasJsonPath(D_8_REASON_FOR_DIVORCE_SEPARATION_YEAR, is("2008")),
                            hasJsonPath("PetitionerSolicitor", is("Yes")),
                            hasJsonPath("PetitionerSolicitorName", is("Homer Simpson")),
                            hasJsonPath("D8SolicitorReference", is("SolicitorReference")),
                            hasJsonPath("PetitionerSolicitorFirm", is("DivorceIn30Mins Ltd")),
                            hasJsonPath("PetitionerSolicitorPhone", is("0121 465 2141")),
                            hasJsonPath("PetitionerSolicitorEmail", is("homer.solicitor@quickdivorce.org")),
                            hasJsonPath("D8PetitionerCorrespondenceUseHomeAddress", is("No")),
                            hasJsonPath("PetitionerSolicitorAddress",
                                hasJsonPath("PostCode", is("GL51 0EX"))),
                            hasJsonPath("D8PetitionerCorrespondenceAddress",
                                hasJsonPath("PostCode", is("SE12BP")))
                        ))
                    ))
                )));
    }

    @Test
    public void shouldReturnSuccessResponseForTransformationEndpointWhenUsingOnlyASubsetOfData() throws Exception {
        String formToTransform = loadResourceAsString(PARTIAL_D8_FORM_JSON_PATH);

        mockMvc.perform(
            post(TRANSFORMATION_URL)
                .contentType(APPLICATION_JSON)
                .content(formToTransform)
                .header(SERVICE_AUTHORISATION_HEADER, ALLOWED_SERVICE_TOKEN)
        )
            .andExpect(status().isOk())
            .andExpect(content().string(
                allOf(
                    isJson(),
                    hasJsonPath("$.warnings", equalTo(emptyList())),
                    hasJsonPath("$.case_creation_details.*", hasSize(3)),
                    hasJsonPath("$.case_creation_details", allOf(
                        hasJsonPath("case_type_id", is("DIVORCE")),
                        hasJsonPath("event_id", is("bulkScanCaseCreate")),
                        hasJsonPath("case_data.*", hasSize(10)),
                        hasJsonPath("case_data", allOf(
                            hasJsonPath("bulkScanCaseReference", is("LV481297")),
                            hasJsonPath("D8PaymentMethod", is("Card")),
                            hasJsonPath(D_8_PETITIONER_FIRST_NAME, is("Christopher")),
                            hasJsonPath(D_8_PETITIONER_LAST_NAME, is("O'John")),
                            hasJsonPath("D8PetitionerPhoneNumber", is("07456 78 90 11")),
                            hasJsonPath("D8PetitionerEmail", is("test.testerson@mailinator.com")),
                            hasJsonPath("D8LegalProcess", is("Divorce")),
                            hasJsonPath("D8PetitionerContactDetailsConfidential", is("Yes")),
                            hasJsonPath("D8MarriagePetitionerName", is("Christopher O'John")),
                            hasJsonPath("D8MarriageRespondentName", is("Jane Doe")),
                            hasNoJsonPath("D8CertificateInEnglish")
                        ))
                    ))
                )));
    }

    @Test
    public void shouldReturnErrorResponseForUnsupportedFormType() throws Exception {
        String jsonToTransform = new ObjectMapper().createObjectNode()
            .put("form_type", "unsupportedFormType")
            .toPrettyString();

        mockMvc.perform(
            post(TRANSFORMATION_URL)
                .contentType(APPLICATION_JSON)
                .content(jsonToTransform)
                .header(SERVICE_AUTHORISATION_HEADER, ALLOWED_SERVICE_TOKEN)
        ).andExpect(status().isUnprocessableEntity());
    }

}