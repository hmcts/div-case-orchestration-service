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
import uk.gov.service.notify.NotificationClientException;

import java.util.HashMap;
import java.util.Map;

import static com.jayway.jsonpath.matchers.JsonPathMatchers.hasJsonPath;
import static com.jayway.jsonpath.matchers.JsonPathMatchers.isJson;
import static java.util.Collections.singletonList;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
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
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_PETITIONER_FIRST_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_PETITIONER_FULL_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_PETITIONER_LAST_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_RELATIONSHIP;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_RESPONDENT_EMAIL;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_RESPONDENT_FIRST_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_RESPONDENT_FULL_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_RESPONDENT_LAST_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_RESP_SOLICITOR_EMAIL;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_RESP_SOLICITOR_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_SOLICITOR_EMAIL;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_SOLICITOR_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_WELSH_FEMALE_GENDER_IN_RELATION;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D8_RESPONDENT_SOLICITOR_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D_8_CASE_REFERENCE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D_8_INFERRED_PETITIONER_GENDER;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D_8_PETITIONER_FIRST_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D_8_PETITIONER_LAST_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NOTIFICATION_ADDRESSEE_FIRST_NAME_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NOTIFICATION_ADDRESSEE_LAST_NAME_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NOTIFICATION_CASE_NUMBER_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NOTIFICATION_CCD_REFERENCE_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NOTIFICATION_EMAIL_ADDRESS_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NOTIFICATION_HUSBAND_OR_WIFE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NOTIFICATION_PET_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NOTIFICATION_RESP_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NOTIFICATION_SOLICITOR_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NOTIFICATION_WELSH_HUSBAND_OR_WIFE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.PETITIONER_SOLICITOR_EMAIL;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.PETITIONER_SOLICITOR_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.RESPONDENT_EMAIL_ADDRESS;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.RESPONDENT_SOLICITOR_EMAIL_ADDRESS;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.RESP_FIRST_NAME_CCD_FIELD;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.RESP_LAST_NAME_CCD_FIELD;
import static uk.gov.hmcts.reform.divorce.orchestration.testutil.ObjectMapperTestUtil.convertObjectToJsonString;

public class DecreeAbsoluteRequestedRespondentNotificationTest extends MockedFunctionalTest {

    private static final String API_URL = "/da-requested-by-applicant";

    private static final String DA_APPLICATION_HAS_BEEN_RECEIVED_TEMPLATE_ID = "8d546d3c-9df4-420d-b11c-9706ef3a7e89";
    private static final String DECREE_ABSOLUTE_REQUESTED_NOTIFICATION_TEMPLATE_ID = "b1296cb4-1df2-4d89-b32c-23600a0a8070";
    private static final String DECREE_ABSOLUTE_REQUESTED_NOTIFICATION_SOLICITOR_TEMPLATE_ID = "43b52d1a-b9be-4de5-b5ae-627c51a55111";

    private static final Map<String, Object> CASE_DATA = ImmutableMap.<String, Object>builder()
        .put(PETITIONER_SOLICITOR_EMAIL, TEST_SOLICITOR_EMAIL)
        .put(PETITIONER_SOLICITOR_NAME, TEST_SOLICITOR_NAME)
        .put(RESPONDENT_EMAIL_ADDRESS, TEST_RESPONDENT_EMAIL)
        .put(D_8_PETITIONER_FIRST_NAME, TEST_PETITIONER_FIRST_NAME)
        .put(D_8_PETITIONER_LAST_NAME, TEST_PETITIONER_LAST_NAME)
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
    public void shouldSendEmails_ToPetitionerSolicitorAndRespondent() throws Exception {
        setRespondentJourneyFeatureToggleOn();

        callApiEndpointSuccessfully(getCcdCallbackRequest(CASE_DATA));

        verifyEmailWasSentToPetSol();
        verifyEmailWasSentToRespondent();
    }

    @Test
    public void shouldSendEmail_ToRespondent_WhenNoPetitionerSolicitorEmailProvided() throws Exception {
        setRespondentJourneyFeatureToggleOn();

        Map<String, Object> caseDataWithoutPetSolEmail = new HashMap<>(CASE_DATA);
        caseDataWithoutPetSolEmail.remove(PETITIONER_SOLICITOR_EMAIL);

        callApiEndpointSuccessfully(getCcdCallbackRequest(caseDataWithoutPetSolEmail));

        verifyEmailNeverSentToPetSol();
        verifyEmailWasSentToRespondent();
    }

    @Test
    public void shouldSendEmails_ToPetitionerSolicitorAndRespondentSolicitor() throws Exception {
        setRespondentJourneyFeatureToggleOn();

        Map<String, Object> caseDataWithRespSolEmail = new HashMap<>(CASE_DATA);
        caseDataWithRespSolEmail.put(D8_RESPONDENT_SOLICITOR_NAME, TEST_RESP_SOLICITOR_NAME);
        caseDataWithRespSolEmail.put(RESPONDENT_SOLICITOR_EMAIL_ADDRESS, TEST_RESP_SOLICITOR_EMAIL);
        caseDataWithRespSolEmail.remove(RESPONDENT_EMAIL_ADDRESS);

        callApiEndpointSuccessfully(getCcdCallbackRequest(caseDataWithRespSolEmail));

        verifyEmailWasSentToPetSol();
        verifyEmailWasSentToRespSol();
    }

    @Test
    public void shouldSendEmail_ToRespondent_WhenRespondentJourneyFeatureToggleOff() throws Exception {
        setRespondentJourneyFeatureToggleOff();

        callApiEndpointSuccessfully(getCcdCallbackRequest(CASE_DATA));

        verifyEmailNeverSentToPetSol();
        verifyEmailWasSentToRespondent();
    }

    @Test
    public void givenBadRequestBody_thenReturnBadRequest() throws Exception {
        webClient.perform(post(API_URL)
            .header(AUTHORIZATION, AUTH_TOKEN)
            .contentType(APPLICATION_JSON)
            .accept(APPLICATION_JSON))
            .andExpect(status().isBadRequest());
    }

    @Test
    public void givenEmailServiceReturns500_ThenInternalServerErrorResponse() throws Exception {
        NotificationClientException exception = new NotificationClientException("test exception");
        when(mockEmailClient.sendEmail(anyString(), anyString(), anyMap(), anyString()))
            .thenThrow(exception);

        CcdCallbackResponse expectedResponse = CcdCallbackResponse.builder()
            .errors(singletonList("test exception")).build();

        webClient.perform(post(API_URL)
            .header(AUTHORIZATION, AUTH_TOKEN)
            .content(convertObjectToJsonString(getCcdCallbackRequest(CASE_DATA)))
            .contentType(APPLICATION_JSON)
            .accept(APPLICATION_JSON))
            .andExpect(content().json(convertObjectToJsonString(expectedResponse)));
    }

    private void callApiEndpointSuccessfully(CcdCallbackRequest ccdCallbackRequest)
        throws Exception {

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
    }

    private void setRespondentJourneyFeatureToggleOn() {
        when(featureToggleService.isFeatureEnabled(Features.REPRESENTED_RESPONDENT_JOURNEY)).thenReturn(true);
    }

    private void setRespondentJourneyFeatureToggleOff() {
        when(featureToggleService.isFeatureEnabled(Features.REPRESENTED_RESPONDENT_JOURNEY)).thenReturn(false);
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

    private void verifyEmailWasSentToPetSol() throws Exception {
        verify(mockEmailClient).sendEmail(
            eq(DA_APPLICATION_HAS_BEEN_RECEIVED_TEMPLATE_ID),
            eq(TEST_SOLICITOR_EMAIL),
            eq(
                ImmutableMap.of(
                    NOTIFICATION_PET_NAME, TEST_PETITIONER_FIRST_NAME + " " + TEST_PETITIONER_LAST_NAME,
                    NOTIFICATION_RESP_NAME, TEST_RESPONDENT_FIRST_NAME + " " + TEST_RESPONDENT_LAST_NAME,
                    NOTIFICATION_CCD_REFERENCE_KEY, TEST_CASE_ID,
                    NOTIFICATION_SOLICITOR_NAME, TEST_SOLICITOR_NAME
                )
            ),
            anyString()
        );
    }

    private void verifyEmailNeverSentToPetSol() throws Exception {
        verify(mockEmailClient, never()).sendEmail(
            eq(DA_APPLICATION_HAS_BEEN_RECEIVED_TEMPLATE_ID),
            eq(TEST_SOLICITOR_EMAIL),
            any(),
            anyString()
        );
    }

    private void verifyEmailWasSentToRespondent() throws Exception {
        verify(mockEmailClient).sendEmail(
            eq(DECREE_ABSOLUTE_REQUESTED_NOTIFICATION_TEMPLATE_ID),
            eq(TEST_RESPONDENT_EMAIL),
            eq(ImmutableMap.<String, Object>builder()
                .put(NOTIFICATION_CASE_NUMBER_KEY, TEST_CASE_ID)
                .put(NOTIFICATION_HUSBAND_OR_WIFE, TEST_RELATIONSHIP)
                .put(NOTIFICATION_ADDRESSEE_LAST_NAME_KEY, TEST_RESPONDENT_LAST_NAME)
                .put(NOTIFICATION_ADDRESSEE_FIRST_NAME_KEY, TEST_RESPONDENT_FIRST_NAME)
                .put(NOTIFICATION_EMAIL_ADDRESS_KEY, TEST_RESPONDENT_EMAIL)
                .put(NOTIFICATION_WELSH_HUSBAND_OR_WIFE, TEST_WELSH_FEMALE_GENDER_IN_RELATION)
                .build()),
            anyString()
        );
        verifyEmailNeverSentToRespSol();
    }

    private void verifyEmailNeverSentToRespondent() throws Exception {
        verify(mockEmailClient, never()).sendEmail(
            eq(DECREE_ABSOLUTE_REQUESTED_NOTIFICATION_TEMPLATE_ID),
            eq(TEST_RESPONDENT_EMAIL),
            any(),
            anyString()
        );
    }

    private void verifyEmailWasSentToRespSol() throws Exception {
        verify(mockEmailClient).sendEmail(
            eq(DECREE_ABSOLUTE_REQUESTED_NOTIFICATION_SOLICITOR_TEMPLATE_ID),
            eq(TEST_RESP_SOLICITOR_EMAIL),
            eq(ImmutableMap.<String, Object>builder()
                .put(NOTIFICATION_SOLICITOR_NAME, TEST_RESP_SOLICITOR_NAME)
                .put(NOTIFICATION_EMAIL_ADDRESS_KEY, TEST_RESP_SOLICITOR_EMAIL)
                .put(NOTIFICATION_PET_NAME, TEST_PETITIONER_FULL_NAME)
                .put(NOTIFICATION_CCD_REFERENCE_KEY, TEST_CASE_ID)
                .put(NOTIFICATION_RESP_NAME, TEST_RESPONDENT_FULL_NAME)
                .build()),
            anyString()
        );
        verifyEmailNeverSentToRespondent();
    }

    private void verifyEmailNeverSentToRespSol() throws Exception {
        verify(mockEmailClient, never()).sendEmail(
            eq(DECREE_ABSOLUTE_REQUESTED_NOTIFICATION_SOLICITOR_TEMPLATE_ID),
            eq(TEST_RESP_SOLICITOR_EMAIL),
            any(),
            anyString()
        );
    }
}
