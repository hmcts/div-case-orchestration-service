package uk.gov.hmcts.reform.divorce.orchestration.functionaltest;

import com.google.common.collect.ImmutableMap;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import uk.gov.hmcts.reform.divorce.orchestration.client.EmailClient;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.Features;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CaseDetails;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CcdCallbackRequest;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CcdCallbackResponse;
import uk.gov.hmcts.reform.divorce.orchestration.service.FeatureToggleService;

import java.util.HashMap;
import java.util.Map;

import static com.jayway.jsonpath.matchers.JsonPathMatchers.hasJsonPath;
import static com.jayway.jsonpath.matchers.JsonPathMatchers.isJson;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_INFERRED_GENDER;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_RESPONDENT_EMAIL;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_RESPONDENT_FIRST_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_RESPONDENT_LAST_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_SOLICITOR_EMAIL;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D_8_CASE_REFERENCE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D_8_INFERRED_PETITIONER_GENDER;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D_8_PETITIONER_FIRST_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D_8_PETITIONER_LAST_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.PETITIONER_SOLICITOR_EMAIL;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.RESPONDENT_EMAIL_ADDRESS;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.RESP_FIRST_NAME_CCD_FIELD;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.RESP_LAST_NAME_CCD_FIELD;
import static uk.gov.hmcts.reform.divorce.orchestration.testutil.ObjectMapperTestUtil.convertObjectToJsonString;
import static uk.gov.hmcts.reform.divorce.orchestration.testutil.ObjectMapperTestUtil.getJsonFromResourceFile;

public class DecreeAbsoluteRequestedRespondentNotificationTest extends MockedFunctionalTest {

    private static final String API_URL = "/da-requested-by-applicant";

    private static final String DA_APPLICATION_HAS_BEEN_RECEIVED_TEMPLATE_ID = "8d546d3c-9df4-420d-b11c-9706ef3a7e89";
    private static final String DECREE_ABSOLUTE_REQUESTED_NOTIFICATION_TEMPLATE_ID = "b1296cb4-1df2-4d89-b32c-23600a0a8070";

    private static final Map<String, Object> caseData = ImmutableMap.<String, Object>builder()
        .put(PETITIONER_SOLICITOR_EMAIL, TEST_SOLICITOR_EMAIL)
        .put(RESPONDENT_EMAIL_ADDRESS, TEST_RESPONDENT_EMAIL)
        .put(D_8_PETITIONER_FIRST_NAME, "D8PetitionerFirstName")
        .put(D_8_PETITIONER_LAST_NAME, "D8PetitionerLastName")
        .put(RESP_FIRST_NAME_CCD_FIELD, TEST_RESPONDENT_FIRST_NAME)
        .put(RESP_LAST_NAME_CCD_FIELD, TEST_RESPONDENT_LAST_NAME)
        .put(D_8_CASE_REFERENCE, TEST_CASE_ID)
        .put(D_8_INFERRED_PETITIONER_GENDER, TEST_INFERRED_GENDER)
        .build();

    @MockBean
    private EmailClient mockEmailClient;

    @MockBean
    private FeatureToggleService featureToggleService;

    @Autowired
    private MockMvc webClient;

    @Test
    public void testThatPetSolAndRespAreSentEmails() throws Exception {
        when(featureToggleService.isFeatureEnabled(Features.REPRESENTED_RESPONDENT_JOURNEY)).thenReturn(true);

        CcdCallbackRequest ccdCallbackRequest = getCcdCallbackRequest(caseData);

        CcdCallbackResponse expected = CcdCallbackResponse.builder()
            .data(ccdCallbackRequest.getCaseDetails().getCaseData())
            .build();

        webClient.perform(post(API_URL)
            .content(convertObjectToJsonString(ccdCallbackRequest))
            .header(AUTHORIZATION, AUTH_TOKEN)
            .contentType(APPLICATION_JSON)
            .accept(APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content().json(convertObjectToJsonString(expected)))
            .andExpect(content().string(allOf(
                isJson(),
                hasJsonPath("$.errors", nullValue())
                ))
            );

        verify(mockEmailClient).sendEmail(
            eq(DA_APPLICATION_HAS_BEEN_RECEIVED_TEMPLATE_ID),
            eq(TEST_SOLICITOR_EMAIL),
            any(),
            anyString()
        );

        verify(mockEmailClient).sendEmail(
            eq(DECREE_ABSOLUTE_REQUESTED_NOTIFICATION_TEMPLATE_ID),
            eq(TEST_RESPONDENT_EMAIL),
            any(),
            anyString()
        );
    }

    @Test
    public void testRespIsSentEmailWhenNoPetSolEmailProvided() throws Exception {
        when(featureToggleService.isFeatureEnabled(Features.REPRESENTED_RESPONDENT_JOURNEY)).thenReturn(true);

        Map<String, Object> caseDataWithoutPetSolEmail = new HashMap<>(caseData);
        caseDataWithoutPetSolEmail.remove(PETITIONER_SOLICITOR_EMAIL);

        CcdCallbackRequest ccdCallbackRequest = getCcdCallbackRequest(caseDataWithoutPetSolEmail);

        CcdCallbackResponse expected = CcdCallbackResponse.builder()
            .data(ccdCallbackRequest.getCaseDetails().getCaseData())
            .build();

        webClient.perform(post(API_URL)
            .content(convertObjectToJsonString(ccdCallbackRequest))
            .header(AUTHORIZATION, AUTH_TOKEN)
            .contentType(APPLICATION_JSON)
            .accept(APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content().json(convertObjectToJsonString(expected)))
            .andExpect(content().string(allOf(
                isJson(),
                hasJsonPath("$.errors", nullValue())
                ))
            );

        verify(mockEmailClient, never()).sendEmail(
            eq(DA_APPLICATION_HAS_BEEN_RECEIVED_TEMPLATE_ID),
            eq(TEST_SOLICITOR_EMAIL),
            any(),
            anyString()
        );

        verify(mockEmailClient).sendEmail(
            eq(DECREE_ABSOLUTE_REQUESTED_NOTIFICATION_TEMPLATE_ID),
            eq(TEST_RESPONDENT_EMAIL),
            any(),
            anyString()
        );
    }


    private CcdCallbackRequest getCcdCallbackRequest(Map<String, Object> caseData) {
        return CcdCallbackRequest.builder()
            .caseDetails(
                CaseDetails.builder()
                    .caseId(TEST_CASE_ID)
                    .caseData(caseData)
                    .build()
            )
            .build();
    }

    @Test
    public void testThatRespIsSentEmailWhenFeatureToggleOff() throws Exception {
        when(featureToggleService.isFeatureEnabled(Features.REPRESENTED_RESPONDENT_JOURNEY)).thenReturn(false);

        CcdCallbackRequest ccdCallbackRequest = getCcdCallbackRequest(caseData);

        CcdCallbackResponse expected = CcdCallbackResponse.builder()
            .data(ccdCallbackRequest.getCaseDetails().getCaseData())
            .build();

        webClient.perform(post(API_URL)
            .content(convertObjectToJsonString(ccdCallbackRequest))
            .header(AUTHORIZATION, AUTH_TOKEN)
            .contentType(APPLICATION_JSON)
            .accept(APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content().json(convertObjectToJsonString(expected)))
            .andExpect(content().string(allOf(
                isJson(),
                hasJsonPath("$.errors", nullValue())
                ))
            );

        verify(mockEmailClient, never()).sendEmail(
            eq(DA_APPLICATION_HAS_BEEN_RECEIVED_TEMPLATE_ID),
            eq(TEST_SOLICITOR_EMAIL),
            any(),
            anyString()
        );

        verify(mockEmailClient).sendEmail(
            eq(DECREE_ABSOLUTE_REQUESTED_NOTIFICATION_TEMPLATE_ID),
            eq(TEST_RESPONDENT_EMAIL),
            any(),
            anyString()
        );
    }
//
//    @Test
//    public void testThatPetSolAndRespSolIsSentEmails() throws Exception {
//
//    }
//
//    @Test
//    public void testOnlyRespondentIsSentEmails() throws Exception {
//
//        verify(mockEmailClient, never()).sendEmail(
//            eq(DECREE_ABSOLUTE_REQUESTED_PETITIONER_SOLICITOR_EMAIL_TEMPLATE_ID),
//            eq(TEST_PETITIONER_EMAIL),
//            any(),
//            anyString());
//        verify(mockEmailClient).sendEmail(
//            eq(DECREE_ABSOLUTE_REQUESTED_PETITIONER_SOLICITOR_EMAIL_TEMPLATE_ID),
//            eq(TEST_RESPONDENT_EMAIL),
//            any(),
//            anyString());
//    }
//
//    @Test
//    public void testOnlyRespondentSolicitorIsSentEmails() throws Exception {
//
//    }
//
//
//    @Test
//    public void testRespondentAndPetitionerAreSentEmails() throws Exception {
//
//        CcdCallbackResponse expectedResponse = CcdCallbackResponse.builder().data(CASE_DATA).build();
//        when(mockEmailClient.sendEmail(anyString(), anyString(), anyMap(), anyString()))
//            .thenReturn(null);
//
//        String inputJson = JSONObject.valueToString(CASE_DETAILS);
//
//        webClient.perform(post(API_URL)
//            .header(AUTHORIZATION, AUTH_TOKEN)
//            .content(inputJson)
//            .contentType(APPLICATION_JSON)
//            .accept(APPLICATION_JSON))
//            .andExpect(status().isOk())
//            .andExpect(content().json(convertObjectToJsonString(expectedResponse)));
//
//        // verify email was actually send
//        verify(mockEmailClient).sendEmail(
//            eq(DECREE_ABSOLUTE_REQUESTED_PETITIONER_SOLICITOR_EMAIL_TEMPLATE_ID),
//            eq("petitioner@justice.uk"),
//            any(),
//            any()
//        );
//    }
//
//    @Test
//    public void givenBadRequestBody_thenReturnBadRequest() throws Exception {
//        webClient.perform(post(API_URL)
//            .header(AUTHORIZATION, AUTH_TOKEN)
//            .contentType(APPLICATION_JSON)
//            .accept(APPLICATION_JSON))
//            .andExpect(status().isBadRequest());
//    }
//
//    @Test
//    public void givenEmailServiceReturns500_ThenInternalServerErrorResponse() throws Exception {
//        NotificationClientException exception = new NotificationClientException("test exception");
//        when(mockEmailClient.sendEmail(anyString(), anyString(), anyMap(), anyString()))
//            .thenThrow(exception);
//
//        String inputJson = JSONObject.valueToString(CASE_DETAILS);
//        CcdCallbackResponse expectedResponse = CcdCallbackResponse.builder()
//            .errors(singletonList("test exception")).build();
//
//        webClient.perform(post(API_URL)
//            .header(AUTHORIZATION, AUTH_TOKEN)
//            .content(inputJson)
//            .contentType(APPLICATION_JSON)
//            .accept(APPLICATION_JSON))
//            .andExpect(content().json(convertObjectToJsonString(expectedResponse)));
//    }
}