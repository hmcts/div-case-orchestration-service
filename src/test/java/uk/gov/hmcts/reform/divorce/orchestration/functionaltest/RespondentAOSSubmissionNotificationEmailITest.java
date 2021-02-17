package uk.gov.hmcts.reform.divorce.orchestration.functionaltest;

import org.junit.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import uk.gov.hmcts.reform.divorce.orchestration.client.EmailClient;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.LanguagePreference;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CcdCallbackRequest;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CcdCallbackResponse;
import uk.gov.hmcts.reform.divorce.orchestration.service.TemplateConfigService;
import uk.gov.service.notify.NotificationClientException;

import java.util.Map;

import static com.jayway.jsonpath.matchers.JsonPathMatchers.hasJsonPath;
import static com.jayway.jsonpath.matchers.JsonPathMatchers.isJson;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.core.Is.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_INFERRED_MALE_GENDER;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_RELATIONSHIP_HUSBAND;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_WELSH_MALE_GENDER_IN_RELATION;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.RESP_WILL_DEFEND_DIVORCE;
import static uk.gov.hmcts.reform.divorce.orchestration.testutil.ObjectMapperTestUtil.convertObjectToJsonString;
import static uk.gov.hmcts.reform.divorce.orchestration.testutil.ObjectMapperTestUtil.getJsonFromResourceFile;

public class RespondentAOSSubmissionNotificationEmailITest extends MockedFunctionalTest {

    private static final String API_URL = "/aos-submitted";
    private static final String DEFENDED_DIVORCE_EMAIL_TEMPLATE_ID = "eac41143-b296-4879-ba60-a0ea6f97c757";
    private static final String UNDEFENDED_DIVORCE_EMAIL_TEMPLATE_ID = "277fd3f3-2fdb-4c79-9354-1b3db8d44cca";

    @Autowired
    private MockMvc webClient;

    @MockBean
    private EmailClient mockClient;

    @Mock
    TemplateConfigService templateConfigService;

    private static final String USER_TOKEN = "anytoken";

    @Test
    public void testResponseHasDataAndNoErrors_whenEmailCanBeSent_forDefendedDivorce() throws Exception {

        CcdCallbackRequest ccdCallbackRequest = getJsonFromResourceFile(
                "/jsonExamples/payloads/respondentAcknowledgesServiceDefendingDivorce.json", CcdCallbackRequest.class);
        Map<String, Object> caseData = ccdCallbackRequest.getCaseDetails().getCaseData();
        CcdCallbackResponse expected = CcdCallbackResponse.builder()
                .data(caseData)
                .build();

        webClient.perform(post(API_URL)
                .header(AUTHORIZATION, USER_TOKEN)
                .content(convertObjectToJsonString(ccdCallbackRequest))
                .contentType(APPLICATION_JSON)
                .accept(APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().json(convertObjectToJsonString(expected)))
                .andExpect(content().string(allOf(
                        isJson(),
                        hasJsonPath("$.errors", nullValue())
                )));

        verify(mockClient).sendEmail(eq(DEFENDED_DIVORCE_EMAIL_TEMPLATE_ID),
                eq("respondent@divorce.co.uk"),
                any(), any());
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
                )));

        verify(mockClient).sendEmail(eq(UNDEFENDED_DIVORCE_EMAIL_TEMPLATE_ID),
                eq("respondent@divorce.co.uk"),
                any(), any());
    }

    @Test
    public void testResponseHasDataAndNoErrors_WhenEmailCanBeSent_ForUndefendedButNoAdmitDivorce() throws Exception {
        CcdCallbackRequest ccdCallbackRequest = getJsonFromResourceFile(
                "/jsonExamples/payloads/respondentAcknowledgesServiceNotDefendingNotAdmittingDivorce.json", CcdCallbackRequest.class);
        Map<String, Object> caseData = ccdCallbackRequest.getCaseDetails().getCaseData();
        CcdCallbackResponse expected = CcdCallbackResponse.builder()
                .data(caseData)
                .build();

        when(templateConfigService.getRelationshipTermByGender(eq(TEST_INFERRED_MALE_GENDER),eq(LanguagePreference.ENGLISH)))
            .thenReturn(TEST_RELATIONSHIP_HUSBAND);
        when(templateConfigService.getRelationshipTermByGender(eq(TEST_INFERRED_MALE_GENDER),eq(LanguagePreference.WELSH)))
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
                )));

        verify(mockClient).sendEmail(eq(UNDEFENDED_DIVORCE_EMAIL_TEMPLATE_ID),
                eq("respondent@divorce.co.uk"),
                any(), any());
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
            )));
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

        when(templateConfigService.getRelationshipTermByGender(eq(TEST_INFERRED_MALE_GENDER),eq(LanguagePreference.ENGLISH)))
            .thenReturn(TEST_RELATIONSHIP_HUSBAND);
        when(templateConfigService.getRelationshipTermByGender(eq(TEST_INFERRED_MALE_GENDER),eq(LanguagePreference.WELSH)))
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
                )));
    }

}
