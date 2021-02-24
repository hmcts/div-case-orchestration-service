package uk.gov.hmcts.reform.divorce.orchestration.functionaltest;

import com.google.common.collect.ImmutableMap;
import org.junit.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import uk.gov.hmcts.reform.divorce.orchestration.client.EmailClient;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.LanguagePreference;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CaseDetails;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CcdCallbackRequest;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CcdCallbackResponse;
import uk.gov.hmcts.reform.divorce.orchestration.service.TemplateConfigService;
import uk.gov.service.notify.NotificationClientException;

import java.util.HashMap;
import java.util.Map;

import static com.jayway.jsonpath.matchers.JsonPathMatchers.hasJsonPath;
import static com.jayway.jsonpath.matchers.JsonPathMatchers.isJson;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.core.Is.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.D8_CASE_ID;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_D8_CASE_REFERENCE;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_INFERRED_MALE_GENDER;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_PETITIONER_EMAIL;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_PETITIONER_FIRST_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_PETITIONER_FULL_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_PETITIONER_LAST_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_RELATIONSHIP;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_RELATIONSHIP_HUSBAND;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_RESPONDENT_EMAIL;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_RESPONDENT_FIRST_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_RESPONDENT_FULL_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_RESPONDENT_LAST_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_SOLICITOR_EMAIL;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_SOLICITOR_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_WELSH_FEMALE_GENDER_IN_RELATION;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_WELSH_MALE_GENDER_IN_RELATION;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D_8_CASE_REFERENCE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D_8_CO_RESPONDENT_NAMED;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D_8_DIVORCE_UNIT;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D_8_INFERRED_PETITIONER_GENDER;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D_8_INFERRED_RESPONDENT_GENDER;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D_8_PETITIONER_EMAIL;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D_8_PETITIONER_FIRST_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D_8_PETITIONER_LAST_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D_8_REASON_FOR_DIVORCE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NOTIFICATION_ADDRESSEE_FIRST_NAME_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NOTIFICATION_ADDRESSEE_LAST_NAME_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NOTIFICATION_CASE_NUMBER_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NOTIFICATION_CCD_REFERENCE_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NOTIFICATION_COURT_ADDRESS_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NOTIFICATION_EMAIL;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NOTIFICATION_EMAIL_ADDRESS_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NOTIFICATION_FORM_SUBMISSION_DATE_LIMIT_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NOTIFICATION_HUSBAND_OR_WIFE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NOTIFICATION_PET_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NOTIFICATION_RDC_NAME_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NOTIFICATION_REFERENCE_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NOTIFICATION_RELATIONSHIP_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NOTIFICATION_RESP_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NOTIFICATION_SOLICITOR_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NOTIFICATION_WELSH_FORM_SUBMISSION_DATE_LIMIT_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NOTIFICATION_WELSH_HUSBAND_OR_WIFE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NO_VALUE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.PETITIONER_SOLICITOR_EMAIL;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.PETITIONER_SOLICITOR_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.RECEIVED_AOS_FROM_CO_RESP;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.RESPONDENT_EMAIL_ADDRESS;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.RESP_ADMIT_OR_CONSENT_TO_FACT;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.RESP_FIRST_NAME_CCD_FIELD;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.RESP_LAST_NAME_CCD_FIELD;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.RESP_SOL_REPRESENTED;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.RESP_WILL_DEFEND_DIVORCE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.YES_VALUE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.facts.DivorceFact.ADULTERY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.facts.DivorceFact.SEPARATION_FIVE_YEARS;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.facts.DivorceFact.SEPARATION_TWO_YEARS;
import static uk.gov.hmcts.reform.divorce.orchestration.testutil.ObjectMapperTestUtil.convertObjectToJsonString;
import static uk.gov.hmcts.reform.divorce.orchestration.testutil.ObjectMapperTestUtil.getJsonFromResourceFile;

public class RespondentAOSSubmissionNotificationEmailITest extends MockedFunctionalTest {

    private static final String API_URL = "/aos-submitted";
    private static final String SOL_APPLICANT_AOS_RECEIVED_TEMPLATE_ID = "162ffa54-b008-470e-92b2-a3f2ecb6d30c";
    private static final String DEFENDED_DIVORCE_EMAIL_TEMPLATE_ID = "eac41143-b296-4879-ba60-a0ea6f97c757";
    private static final String UNDEFENDED_DIVORCE_EMAIL_TEMPLATE_ID = "277fd3f3-2fdb-4c79-9354-1b3db8d44cca";
    private static final String RESPONDENT_SUBMISSION_CONSENT_TEMPLATE_ID = "594dc500-93ca-4f4b-931b-acbf9ee83d25";
    private static final String AOS_RECEIVED_UNDEFENDED_NO_ADMIT_ADULTERY_CORESP_NOT_REPLIED_TEMPLATE_ID = "341119b9-5a8d-4c5e-9296-2e6bfa37c49d";
    private static final String AOS_RECEIVED_UNDEFENDED_NO_ADMIT_ADULTERY_TEMPLATE_ID = "78e21621-66bd-4c70-a294-15210724b0f6";
    private static final String AOS_RECEIVED_UNDEFENDED_NO_CONSENT_2_YEARS_TEMPLATE_ID = "2781acfa-3f60-4fc9-8d5b-de35cf121893";
    private static final String RESPONDENT_SUBMISSION_CONSENT_CORESP_NOT_REPLIED_TEMPLATE_ID = "44e2dd30-4303-4f4c-a394-ce0b54af81dd";

    @Autowired
    private MockMvc webClient;

    @MockBean
    private EmailClient mockClient;

    @Mock
    private TemplateConfigService templateConfigService;

    @Test
    public void testResponseHasDataAndNoErrors_whenEmailCanBeSent_forDefendedDivorce() throws Exception {
        CcdCallbackRequest ccdCallbackRequest = getJsonFromResourceFile(
            "/jsonExamples/payloads/respondentAcknowledgesServiceDefendingDivorce.json", CcdCallbackRequest.class);
        Map<String, Object> caseData = ccdCallbackRequest.getCaseDetails().getCaseData();
        CcdCallbackResponse expected = CcdCallbackResponse.builder()
            .data(caseData)
            .build();

        webClient.perform(post(API_URL)
            .header(AUTHORIZATION, AUTH_TOKEN)
            .content(convertObjectToJsonString(ccdCallbackRequest))
            .contentType(APPLICATION_JSON)
            .accept(APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content().json(convertObjectToJsonString(expected)))
            .andExpect(content().string(allOf(
                isJson(),
                hasJsonPath("$.errors", nullValue())
                ))
            );

        verify(mockClient).sendEmail(
            eq(DEFENDED_DIVORCE_EMAIL_TEMPLATE_ID),
            eq(TEST_RESPONDENT_EMAIL),
            eq(ImmutableMap.<String, Object>builder()
                .put(NOTIFICATION_CASE_NUMBER_KEY, D8_CASE_ID)
                .put(NOTIFICATION_HUSBAND_OR_WIFE, TEST_RELATIONSHIP)
                .put(NOTIFICATION_ADDRESSEE_LAST_NAME_KEY, "Jones")
                .put(NOTIFICATION_ADDRESSEE_FIRST_NAME_KEY, "Ted")
                .put(NOTIFICATION_COURT_ADDRESS_KEY, "West Midlands Regional Divorce Centre\nPO Box 3650\nStoke-on-Trent\nST4 9NH")
                .put(NOTIFICATION_FORM_SUBMISSION_DATE_LIMIT_KEY, "20 September 2018")
                .put(NOTIFICATION_EMAIL_ADDRESS_KEY, TEST_RESPONDENT_EMAIL)
                .put(NOTIFICATION_RDC_NAME_KEY, "West Midlands Regional Divorce Centre")
                .put(NOTIFICATION_WELSH_HUSBAND_OR_WIFE, TEST_WELSH_FEMALE_GENDER_IN_RELATION)
                .put(NOTIFICATION_WELSH_FORM_SUBMISSION_DATE_LIMIT_KEY, "20 Medi 2018")
                .build()
            ),
            any()
        );
    }

    @Test
    public void testResponseHasDataAndNoErrors_WhenEmailCanBeSent_ForUndefendedDivorce() throws Exception {
        CcdCallbackRequest ccdCallbackRequest = getJsonFromResourceFile(
            "/jsonExamples/payloads/respondentAcknowledgesServiceNotDefendingDivorce.json", CcdCallbackRequest.class);
        Map<String, Object> caseData = ccdCallbackRequest.getCaseDetails().getCaseData();
        CcdCallbackResponse expected = CcdCallbackResponse.builder()
            .data(caseData)
            .build();

        webClient.perform(post(API_URL)
            .content(convertObjectToJsonString(ccdCallbackRequest))
            .contentType(APPLICATION_JSON)
            .accept(APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content().json(convertObjectToJsonString(expected)))
            .andExpect(content().string(allOf(
                isJson(),
                hasJsonPath("$.errors", nullValue())
                ))
            );

        verify(mockClient).sendEmail(
            eq(UNDEFENDED_DIVORCE_EMAIL_TEMPLATE_ID),
            eq(TEST_RESPONDENT_EMAIL),
            eq(ImmutableMap.<String, Object>builder()
                .put(NOTIFICATION_CASE_NUMBER_KEY, D8_CASE_ID)
                .put(NOTIFICATION_HUSBAND_OR_WIFE, TEST_RELATIONSHIP_HUSBAND)
                .put(NOTIFICATION_ADDRESSEE_LAST_NAME_KEY, "Jones")
                .put(NOTIFICATION_ADDRESSEE_FIRST_NAME_KEY, "Sarah")
                .put(NOTIFICATION_EMAIL_ADDRESS_KEY, TEST_RESPONDENT_EMAIL)
                .put(NOTIFICATION_RDC_NAME_KEY, "West Midlands Regional Divorce Centre")
                .put(NOTIFICATION_WELSH_HUSBAND_OR_WIFE, TEST_WELSH_MALE_GENDER_IN_RELATION)
                .build()
            ),
            any()
        );
    }

    @Test
    public void testResponseHasDataAndNoErrors_WhenEmailCanBeSent_ForUndefendedDivorce_AndPetIsNotRepresented() throws Exception {
        Map<String, Object> caseData = new HashMap<>();
        buildPetitionerCaseData(caseData);

        runTestForUndefendedDivorce_AndPetitionerIsNotRepresented(caseData, RESPONDENT_SUBMISSION_CONSENT_TEMPLATE_ID);
    }

    @Test
    public void testResponseHasDataAndNoErrors_WhenEmailCanBeSent_ForUndefendedDivorce_AndPetIsNotRepresented_AndIsAdulteryAndIsCoRespNamed()
        throws Exception {
        Map<String, Object> caseData = new HashMap<>();
        buildPetitionerCaseData(caseData);
        addAdulteryAndNoConsent(caseData);
        addCoRespNamedAndNotReplied(caseData);

        runTestForUndefendedDivorce_AndPetitionerIsNotRepresented(
            caseData,
            AOS_RECEIVED_UNDEFENDED_NO_ADMIT_ADULTERY_CORESP_NOT_REPLIED_TEMPLATE_ID
        );
    }

    @Test
    public void testResponseHasDataAndNoErrors_WhenEmailCanBeSent_ForUndefendedDivorce_AndPetIsNotRepresented_AndIsAdulteryAndIsNotCoRespNamed()
        throws Exception {
        Map<String, Object> caseData = new HashMap<>();
        buildPetitionerCaseData(caseData);
        addAdulteryAndNoConsent(caseData);
        addCoRespNotNamedAndNotReplied(caseData);

        runTestForUndefendedDivorce_AndPetitionerIsNotRepresented(caseData, AOS_RECEIVED_UNDEFENDED_NO_ADMIT_ADULTERY_TEMPLATE_ID);
    }

    @Test
    public void testResponseHasDataAndNoErrors_WhenEmailCanBeSent_ForUndefendedDivorce_AndPetIsNotRepresented_AndIsSep2YrAndNoConsent()
        throws Exception {
        Map<String, Object> caseData = new HashMap<>();
        buildPetitionerCaseData(caseData);
        addSep2YrAndNoConsent(caseData);

        runTestForUndefendedDivorce_AndPetitionerIsNotRepresented(caseData, AOS_RECEIVED_UNDEFENDED_NO_CONSENT_2_YEARS_TEMPLATE_ID);
    }

    @Test
    public void testResponseHasDataAndNoErrors_WhenEmailCanBeSent_ForUndefendedDivorce_AndPetIsNotRepresented_AndIsCoRespNamedAndNotReplied()
        throws Exception {
        Map<String, Object> caseData = new HashMap<>();
        buildPetitionerCaseData(caseData);
        addSep5YrAndNoConsent(caseData); // !isAdulteryAndNoConsent && !isSep2YrAndNoConsent
        addCoRespNamedAndNotReplied(caseData);

        runTestForUndefendedDivorce_AndPetitionerIsNotRepresented(caseData, RESPONDENT_SUBMISSION_CONSENT_CORESP_NOT_REPLIED_TEMPLATE_ID);
    }

    @Test
    public void testResponseHasDataAndNoErrors_WhenEmailCanBeSent_ForUndefendedButNoAdmitDivorce() throws Exception {
        CcdCallbackRequest ccdCallbackRequest = getJsonFromResourceFile(
            "/jsonExamples/payloads/respondentAcknowledgesServiceNotDefendingNotAdmittingDivorce.json", CcdCallbackRequest.class);
        Map<String, Object> caseData = ccdCallbackRequest.getCaseDetails().getCaseData();
        CcdCallbackResponse expected = CcdCallbackResponse.builder()
            .data(caseData)
            .build();

        when(templateConfigService.getRelationshipTermByGender(eq(TEST_INFERRED_MALE_GENDER), eq(LanguagePreference.ENGLISH)))
            .thenReturn(TEST_RELATIONSHIP_HUSBAND);
        when(templateConfigService.getRelationshipTermByGender(eq(TEST_INFERRED_MALE_GENDER), eq(LanguagePreference.WELSH)))
            .thenReturn(TEST_WELSH_MALE_GENDER_IN_RELATION);

        webClient.perform(post(API_URL)
            .content(convertObjectToJsonString(ccdCallbackRequest))
            .contentType(APPLICATION_JSON)
            .accept(APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content().json(convertObjectToJsonString(expected)))
            .andExpect(content().string(allOf(
                isJson(),
                hasJsonPath("$.errors", nullValue())
                ))
            );

        verify(mockClient).sendEmail(
            eq(UNDEFENDED_DIVORCE_EMAIL_TEMPLATE_ID),
            eq(TEST_RESPONDENT_EMAIL),
            eq(ImmutableMap.<String, Object>builder()
                .put(NOTIFICATION_CASE_NUMBER_KEY, D8_CASE_ID)
                .put(NOTIFICATION_HUSBAND_OR_WIFE, TEST_RELATIONSHIP_HUSBAND)
                .put(NOTIFICATION_ADDRESSEE_LAST_NAME_KEY, "Jones")
                .put(NOTIFICATION_ADDRESSEE_FIRST_NAME_KEY, "Sarah")
                .put(NOTIFICATION_EMAIL_ADDRESS_KEY, TEST_RESPONDENT_EMAIL)
                .put(NOTIFICATION_RDC_NAME_KEY, "West Midlands Regional Divorce Centre")
                .put(NOTIFICATION_WELSH_HUSBAND_OR_WIFE, TEST_WELSH_MALE_GENDER_IN_RELATION)
                .build()
            ),
            any()
        );
    }

    @Test
    public void testResponseHasDataAndNoErrors_WhenUsingRespondentSolicitor() throws Exception {
        CcdCallbackRequest ccdCallbackRequest = getJsonFromResourceFile(
            "/jsonExamples/payloads/respondentAcknowledgesServiceDefendingDivorce.json", CcdCallbackRequest.class);
        Map<String, Object> caseData = ccdCallbackRequest.getCaseDetails().getCaseData();
        caseData.put(RESP_SOL_REPRESENTED, YES_VALUE);
        CcdCallbackResponse expected = CcdCallbackResponse.builder()
            .data(caseData)
            .build();

        webClient.perform(post(API_URL)
            .content(convertObjectToJsonString(ccdCallbackRequest))
            .contentType(APPLICATION_JSON)
            .accept(APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content().json(convertObjectToJsonString(expected)))
            .andExpect(content().string(allOf(
                isJson(),
                hasJsonPath("$.errors", nullValue())
                ))
            );

        verify(mockClient, never()).sendEmail(any(), any(), any(), any());
    }

    @Test
    public void testEmailCanBeSentToPetitionerSolicitor_ForUndefendedDivorce() throws Exception {
        CcdCallbackRequest ccdCallbackRequest = CcdCallbackRequest.builder()
            .caseDetails(
                CaseDetails.builder()
                    .caseId(TEST_CASE_ID)
                    .caseData(buildCaseDataForPetSolicitor())
                    .build()
            ).build();

        CcdCallbackResponse expected = CcdCallbackResponse.builder()
            .data(ccdCallbackRequest.getCaseDetails().getCaseData())
            .build();

        when(templateConfigService.getRelationshipTermByGender(TEST_INFERRED_MALE_GENDER, LanguagePreference.ENGLISH))
            .thenReturn(TEST_RELATIONSHIP_HUSBAND);

        webClient.perform(post(API_URL)
            .content(convertObjectToJsonString(ccdCallbackRequest))
            .contentType(APPLICATION_JSON)
            .accept(APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content().json(convertObjectToJsonString(expected)))
            .andExpect(content().string(allOf(
                isJson(),
                hasJsonPath("$.errors", nullValue())
                ))
            );

        verify(mockClient).sendEmail(
            eq(UNDEFENDED_DIVORCE_EMAIL_TEMPLATE_ID),
            eq(TEST_RESPONDENT_EMAIL),
            eq(
                ImmutableMap.<String, Object>builder()
                    .put(NOTIFICATION_EMAIL, TEST_RESPONDENT_EMAIL)
                    .put(NOTIFICATION_ADDRESSEE_FIRST_NAME_KEY, TEST_RESPONDENT_FIRST_NAME)
                    .put(NOTIFICATION_ADDRESSEE_LAST_NAME_KEY, TEST_RESPONDENT_LAST_NAME)
                    .put(NOTIFICATION_HUSBAND_OR_WIFE, TEST_RELATIONSHIP_HUSBAND)
                    .put(NOTIFICATION_CASE_NUMBER_KEY, TEST_D8_CASE_REFERENCE)
                    .put(NOTIFICATION_RDC_NAME_KEY, "West Midlands Regional Divorce Centre")
                    .put(NOTIFICATION_WELSH_HUSBAND_OR_WIFE, TEST_WELSH_MALE_GENDER_IN_RELATION)
                    .build()
            ),
            anyString()
        );

        verify(mockClient).sendEmail(
            eq(SOL_APPLICANT_AOS_RECEIVED_TEMPLATE_ID),
            eq(TEST_SOLICITOR_EMAIL),
            eq(
                ImmutableMap.of(
                    NOTIFICATION_SOLICITOR_NAME, TEST_SOLICITOR_NAME,
                    NOTIFICATION_PET_NAME, TEST_PETITIONER_FULL_NAME,
                    NOTIFICATION_RESP_NAME, TEST_RESPONDENT_FULL_NAME,
                    NOTIFICATION_CCD_REFERENCE_KEY, TEST_CASE_ID
                )
            ),
            anyString()
        );
    }

    @Test
    public void testResponseHasValidationErrors_WhenItIsNotClearIfDivorceWillBeDefended() throws Exception {
        runTestResponseWithValidErrors(
            "/jsonExamples/payloads/unclearAcknowledgementOfService.json",
            String.format("%s field doesn't contain a valid value: null", RESP_WILL_DEFEND_DIVORCE)
        );
    }

    @Test
    public void testResponseHasValidationErrors_WhenCaseIdIsMissing_ForDefendedDivorce() throws Exception {
        runTestResponseWithValidErrors(
            "/jsonExamples/payloads/defendedDivorceAOSMissingCaseId.json",
            "Could not evaluate value of mandatory property \"D8caseReference\""
        );
    }

    @Test
    public void testResponseHasValidationErrors_WhenFieldsAreMissing_ForDefendedDivorce() throws Exception {
        runTestResponseWithValidErrors(
            "/jsonExamples/payloads/defendedDivorceAOSMissingFields.json",
            "Could not evaluate value of mandatory property \"D8InferredPetitionerGender\""
        );
    }

    @Test
    public void testResponseHasValidationErrors_WhenFieldsAreMissing_ForUndefendedDivorce() throws Exception {
        runTestResponseWithValidErrors(
            "/jsonExamples/payloads/undefendedDivorceAOSMissingFields.json",
            "Could not evaluate value of mandatory property \"D8DivorceUnit\""
        );
    }

    @Test
    public void testResponseHasError_IfEmailCannotBeSent() throws Exception {
        CcdCallbackRequest ccdCallbackRequest = getJsonFromResourceFile(
            "/jsonExamples/payloads/respondentAcknowledgesServiceDefendingDivorce.json", CcdCallbackRequest.class);
        doThrow(NotificationClientException.class).when(mockClient).sendEmail(any(), any(), any(), any());

        when(templateConfigService.getRelationshipTermByGender(eq(TEST_INFERRED_MALE_GENDER), eq(LanguagePreference.ENGLISH)))
            .thenReturn(TEST_RELATIONSHIP_HUSBAND);
        when(templateConfigService.getRelationshipTermByGender(eq(TEST_INFERRED_MALE_GENDER), eq(LanguagePreference.WELSH)))
            .thenReturn(TEST_WELSH_MALE_GENDER_IN_RELATION);

        webClient.perform(post(API_URL)
            .content(convertObjectToJsonString(ccdCallbackRequest))
            .contentType(APPLICATION_JSON)
            .accept(APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content().string(allOf(
                isJson(),
                hasJsonPath("$.data", is(nullValue())),
                hasJsonPath("$.errors", hasItem("Failed to send e-mail"))
                ))
            );
    }

    private void buildPetitionerCaseData(Map<String, Object> caseData) {
        caseData.put(D_8_PETITIONER_EMAIL, TEST_PETITIONER_EMAIL);
    }

    private void addCoRespNamedAndNotReplied(Map<String, Object> caseData) {
        caseData.put(D_8_CO_RESPONDENT_NAMED, YES_VALUE);
        caseData.put(RECEIVED_AOS_FROM_CO_RESP, NO_VALUE);
    }

    private void addCoRespNotNamedAndNotReplied(Map<String, Object> caseData) {
        caseData.put(D_8_CO_RESPONDENT_NAMED, NO_VALUE);
        caseData.put(RECEIVED_AOS_FROM_CO_RESP, NO_VALUE);
    }

    private void addAdulteryAndNoConsent(Map<String, Object> caseData) {
        caseData.put(D_8_REASON_FOR_DIVORCE, ADULTERY.getValue());
        caseData.put(RESP_ADMIT_OR_CONSENT_TO_FACT, NO_VALUE);
    }

    private void addSep2YrAndNoConsent(Map<String, Object> caseData) {
        caseData.put(D_8_REASON_FOR_DIVORCE, SEPARATION_TWO_YEARS.getValue());
        caseData.put(RESP_ADMIT_OR_CONSENT_TO_FACT, NO_VALUE);
    }

    private void addSep5YrAndNoConsent(Map<String, Object> caseData) {
        caseData.put(D_8_REASON_FOR_DIVORCE, SEPARATION_FIVE_YEARS);
        caseData.put(RESP_ADMIT_OR_CONSENT_TO_FACT, NO_VALUE);
    }

    private void runTestForUndefendedDivorce_AndPetitionerIsNotRepresented(
        Map<String, Object> extraCaseData, String petitionerEmailTemplateID
    ) throws Exception {
        CcdCallbackRequest ccdCallbackRequest = getJsonFromResourceFile(
            "/jsonExamples/payloads/respondentAcknowledgesServiceNotDefendingDivorce.json", CcdCallbackRequest.class);
        Map<String, Object> caseData = ccdCallbackRequest.getCaseDetails().getCaseData();
        caseData.put(D_8_PETITIONER_FIRST_NAME, TEST_PETITIONER_FIRST_NAME);
        caseData.put(D_8_PETITIONER_LAST_NAME, TEST_PETITIONER_LAST_NAME);
        caseData.put(D_8_INFERRED_RESPONDENT_GENDER, TEST_INFERRED_MALE_GENDER);
        caseData.putAll(extraCaseData);
        CcdCallbackResponse expected = CcdCallbackResponse.builder()
            .data(caseData)
            .build();

        webClient.perform(post(API_URL)
            .content(convertObjectToJsonString(ccdCallbackRequest))
            .contentType(APPLICATION_JSON)
            .accept(APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content().json(convertObjectToJsonString(expected)))
            .andExpect(content().string(allOf(
                isJson(),
                hasJsonPath("$.errors", nullValue())
                ))
            );

        Map<String, Object> templateVars = new HashMap<>();
        templateVars.put(NOTIFICATION_ADDRESSEE_FIRST_NAME_KEY, TEST_PETITIONER_FIRST_NAME);
        templateVars.put(NOTIFICATION_ADDRESSEE_LAST_NAME_KEY, TEST_PETITIONER_LAST_NAME);
        templateVars.put(NOTIFICATION_RELATIONSHIP_KEY, TEST_RELATIONSHIP_HUSBAND);
        templateVars.put(NOTIFICATION_WELSH_HUSBAND_OR_WIFE, TEST_WELSH_MALE_GENDER_IN_RELATION);
        templateVars.put(NOTIFICATION_REFERENCE_KEY, D8_CASE_ID);

        verify(mockClient).sendEmail(
            eq(petitionerEmailTemplateID),
            eq(TEST_PETITIONER_EMAIL),
            eq(templateVars),
            anyString()
        );

        verify(mockClient).sendEmail(
            eq(UNDEFENDED_DIVORCE_EMAIL_TEMPLATE_ID),
            eq(TEST_RESPONDENT_EMAIL),
            eq(ImmutableMap.<String, Object>builder()
                .put(NOTIFICATION_CASE_NUMBER_KEY, D8_CASE_ID)
                .put(NOTIFICATION_HUSBAND_OR_WIFE, TEST_RELATIONSHIP_HUSBAND)
                .put(NOTIFICATION_ADDRESSEE_LAST_NAME_KEY, "Jones")
                .put(NOTIFICATION_ADDRESSEE_FIRST_NAME_KEY, "Sarah")
                .put(NOTIFICATION_EMAIL_ADDRESS_KEY, TEST_RESPONDENT_EMAIL)
                .put(NOTIFICATION_RDC_NAME_KEY, "West Midlands Regional Divorce Centre")
                .put(NOTIFICATION_WELSH_HUSBAND_OR_WIFE, TEST_WELSH_MALE_GENDER_IN_RELATION)
                .build()
            ),
            anyString()
        );
    }

    private Map<String, Object> buildCaseDataForPetSolicitor() {
        Map<String, Object> caseData = new HashMap<>();
        caseData.put(RESP_WILL_DEFEND_DIVORCE, NO_VALUE);

        caseData.put(PETITIONER_SOLICITOR_EMAIL, TEST_SOLICITOR_EMAIL);
        caseData.put(D_8_PETITIONER_EMAIL, TEST_PETITIONER_EMAIL);
        caseData.put(D_8_CASE_REFERENCE, TEST_D8_CASE_REFERENCE);
        caseData.put(D_8_PETITIONER_FIRST_NAME, TEST_PETITIONER_FIRST_NAME);
        caseData.put(D_8_PETITIONER_LAST_NAME, TEST_PETITIONER_LAST_NAME);
        caseData.put(RESP_FIRST_NAME_CCD_FIELD, TEST_RESPONDENT_FIRST_NAME);
        caseData.put(RESP_LAST_NAME_CCD_FIELD, TEST_RESPONDENT_LAST_NAME);
        caseData.put(RESPONDENT_EMAIL_ADDRESS, TEST_RESPONDENT_EMAIL);
        caseData.put(PETITIONER_SOLICITOR_NAME, TEST_SOLICITOR_NAME);
        caseData.put(D_8_DIVORCE_UNIT, "westMidlands");

        caseData.put(D_8_INFERRED_PETITIONER_GENDER, TEST_INFERRED_MALE_GENDER);

        return caseData;
    }

    private void runTestResponseWithValidErrors(String filePath, String errorMsg) throws Exception {
        CcdCallbackRequest ccdCallbackRequest = getJsonFromResourceFile(
            filePath, CcdCallbackRequest.class
        );

        when(templateConfigService.getRelationshipTermByGender(
            eq(TEST_INFERRED_MALE_GENDER), eq(LanguagePreference.ENGLISH))
        ).thenReturn(TEST_RELATIONSHIP_HUSBAND);

        when(templateConfigService.getRelationshipTermByGender(
            eq(TEST_INFERRED_MALE_GENDER), eq(LanguagePreference.WELSH))
        ).thenReturn(TEST_WELSH_MALE_GENDER_IN_RELATION);

        webClient.perform(post(API_URL)
            .content(convertObjectToJsonString(ccdCallbackRequest))
            .contentType(APPLICATION_JSON)
            .accept(APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content().string(allOf(
                isJson(),
                hasJsonPath("$.data", is(nullValue())),
                hasJsonPath("$.errors",
                    hasItem(errorMsg))
                ))
            );
    }
}
