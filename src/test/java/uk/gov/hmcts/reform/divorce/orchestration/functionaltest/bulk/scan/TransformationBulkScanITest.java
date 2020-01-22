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
import uk.gov.hmcts.reform.bsp.common.model.validation.out.OcrValidationResult;
import uk.gov.hmcts.reform.divorce.orchestration.OrchestrationServiceApplication;
import uk.gov.hmcts.reform.divorce.orchestration.service.bulk.scan.validation.D8FormValidator;

import static com.jayway.jsonpath.matchers.JsonPathMatchers.hasJsonPath;
import static com.jayway.jsonpath.matchers.JsonPathMatchers.hasNoJsonPath;
import static com.jayway.jsonpath.matchers.JsonPathMatchers.isJson;
import static java.util.Collections.emptyList;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
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

    private static final String FULL_D8_FORM_JSON_PATH = "jsonExamples/payloads/bulk/scan/d8/fullD8Form.json";
    private static final String PARTIAL_D8_FORM_JSON_PATH = "jsonExamples/payloads/bulk/scan/d8/partialD8Form.json";
    private static final String TRANSFORMATION_URL = "/transform-exception-record";

    private String jsonPayload;

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    AuthTokenValidator authTokenValidator;

    @MockBean
    D8FormValidator bulkScanFormValidator;

    @Before
    public void setup() throws Exception {
        when(authTokenValidator.getServiceName(ALLOWED_SERVICE_TOKEN)).thenReturn("bulk_scan_orchestrator");
        when(authTokenValidator.getServiceName(I_AM_NOT_ALLOWED_SERVICE_TOKEN)).thenReturn("don't let me do it!");

        jsonPayload = loadResourceAsString(FULL_D8_FORM_JSON_PATH);
    }

    @Test
    public void shouldReturnForbiddenStatusWhenInvalidS2SToken() throws Exception {
        mockMvc.perform(
            post(TRANSFORMATION_URL)
                .contentType(APPLICATION_JSON)
                .content(jsonPayload)
                .header(SERVICE_AUTHORISATION_HEADER, I_AM_NOT_ALLOWED_SERVICE_TOKEN)
        ).andExpect(status().isForbidden());
    }

    @Test
    public void shouldReturnUnauthorisedStatusWhenNoS2SToken() throws Exception {
        mockMvc.perform(
            post(TRANSFORMATION_URL)
                .contentType(APPLICATION_JSON)
                .content(jsonPayload)
                .header(SERVICE_AUTHORISATION_HEADER, "")
        ).andExpect(status().isUnauthorized());
    }

    @Test
    public void shouldReturnSuccessResponseForTransformationEndpointWhenUsingFullDataSet() throws Exception {
        when(bulkScanFormValidator.validateBulkScanForm(any())).thenReturn(OcrValidationResult.builder().build());

        mockMvc.perform(
            post(TRANSFORMATION_URL)
                .contentType(APPLICATION_JSON)
                .content(jsonPayload)
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
                        hasJsonPath("event_id", is("caseCreate")),
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
                            hasJsonPath("D8PetitionerHomeAddress", allOf(
                                hasJsonPath("AddressLine1", is("19 West Park Road")),
                                hasJsonPath("County", is("West Midlands")),
                                hasJsonPath("PostCode", is("SE1 2BP")),
                                hasJsonPath("PostTown", is("Smethwick"))
                            )),
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
                            hasJsonPath("PetitionerSolicitorAddress", allOf(
                                hasJsonPath("AddressLine1", is("20 solicitors road")),
                                hasJsonPath("County", is("East Midlands")),
                                hasJsonPath("PostCode", is("GL51 0EX")),
                                hasJsonPath("PostTown", is("Soltown"))
                            )),
                            hasJsonPath("D8PetitionerCorrespondenceAddress", allOf(
                                hasJsonPath("AddressLine1", is("20 correspondence road")),
                                hasJsonPath("County", is("South Midlands")),
                                hasJsonPath("PostCode", is("SE12BP")),
                                hasJsonPath("PostTown", is("Correspondencetown"))
                            )),
                            hasJsonPath("D8ReasonForDivorceAdultery3rdPartyFName", is("Alex")),
                            hasJsonPath("D8ReasonForDivorceAdultery3rdPartyLName", is("Third")),
                            hasJsonPath("D8ReasonForDivorceAdultery3rdAddress", allOf(
                                hasJsonPath("AddressLine1", is("third party street")),
                                hasJsonPath("County", is("North Midlands")),
                                hasJsonPath("PostCode", is("SE3 5PP")),
                                hasJsonPath("PostTown", is("Thirdtown"))
                            )),
                            hasJsonPath("D8MarriedInUk", is("Yes")),
                            hasJsonPath("D8ApplicationToIssueWithoutCertificate", is("No")),
                            hasJsonPath("D8MarriagePlaceOfMarriage", is("Slough")),
                            hasJsonPath("D8MarriageDate", is("1998-01-07")),
                            hasJsonPath("D8MarriageCertificateCorrect", is("No")),
                            hasJsonPath("D8MarriageCertificateCorrectExplain", is("Providing a scanned copy from registry office.")),
                            hasJsonPath("D8FinancialOrder", is("Yes")),
                            hasJsonPath("D8FinancialOrderFor", is("petitioner, children")),
                            hasJsonPath("D8ReasonForDivorce", is("desertion")),
                            hasJsonPath("D8LegalProceedings", is("Yes")),
                            hasJsonPath("D8LegalProceedingsDetailsCaseNumber", is("case1234")),
                            hasJsonPath("D8LegalProceedingsDetails", is("details of previous cases")),

                            ///

                            hasJsonPath("D8RespondentHomeAddress", allOf(
                                hasJsonPath("AddressLine1", is("18 West Park Road")),
                                hasJsonPath("County", is("West Midlands")),
                                hasJsonPath("PostCode", is("WE1 MI2")),
                                hasJsonPath("PostTown", is("Smethwick"))
                            )),
                            hasJsonPath("D8PetitionerNameDifferentToMarriageCert", is("Yes")),
                            hasJsonPath("RespNameDifferentToMarriageCertExplain", is("Dog ate the homework")),
                            hasJsonPath("D8RespondentEmailAddress", is("jack@daily.mail.com")),
                            hasJsonPath("D8RespondentCorrespondenceSendToSol", is("Yes")),

                            hasJsonPath("D8RespondentSolicitorName", is("Judge Law")),
                            hasJsonPath("D8RespondentSolicitorReference", is("JL007")),
                            hasJsonPath("D8RespondentSolicitorCompany", is("A-Team")),

                            hasJsonPath("D8RespondentSolicitorAddress", allOf(
                                hasJsonPath("AddressLine1", is("50 Licitor")),
                                hasJsonPath("County", is("Higher Midlands")),
                                hasJsonPath("PostCode", is("SO2 7OR")),
                                hasJsonPath("PostTown", is("Lawyerpool"))
                            )),

                            ///

                            hasJsonPath("D8AppliesForStatementOfTruth", is("marriage")),
                            hasJsonPath("D8DivorceClaimFrom", is("correspondent")),
                            hasJsonPath("D8FinancialOrderStatementOfTruth", is("petitioner, children")),
                            hasJsonPath("D8FullNameStatementOfTruth", is("Peter F. Griffin")),
                            hasJsonPath("D8StatementOfTruthSignature", is("Yes")),
                            hasJsonPath("D8StatementOfTruthDate", is("16/01/2020")),
                            hasJsonPath("D8SolicitorsFirmStatementOfTruth", is("Quahog Solicitors Ltd."))

                        ))
                    ))
                )));
    }

    @Test
    public void shouldReturnSuccessResponseForTransformationEndpointWhenUsingOnlyASubsetOfData() throws Exception {
        String formToTransform = loadResourceAsString(PARTIAL_D8_FORM_JSON_PATH);

        when(bulkScanFormValidator.validateBulkScanForm(any())).thenReturn(OcrValidationResult.builder().build());

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
                        hasJsonPath("event_id", is("caseCreate")),
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
                            hasNoJsonPath("D8CertificateInEnglish"),
                            hasNoJsonPath("D8SolicitorsFirmStatementOfTruth")
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