package uk.gov.hmcts.reform.divorce.orchestration.functionaltest;

import com.google.common.collect.ImmutableMap;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import uk.gov.hmcts.reform.divorce.orchestration.TestConstants;
import uk.gov.hmcts.reform.divorce.orchestration.client.EmailClient;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CaseDetails;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CcdCallbackRequest;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CcdCallbackResponse;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.courts.Court;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.TaskCommons;
import uk.gov.hmcts.reform.divorce.orchestration.testutil.ObjectMapperTestUtil;
import uk.gov.service.notify.NotificationClientException;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_COURT;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_EMAIL;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_EXPECTED_DUE_DATE;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_EXPECTED_DUE_DATE_FORMATTED;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_FORM_WESLH_SUBMISSION_DUE_DATE;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_PETITIONER_FIRST_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_PETITIONER_FULL_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_PETITIONER_LAST_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_RESPONDENT_FIRST_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_RESPONDENT_FULL_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_RESPONDENT_LAST_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_SOLICITOR_EMAIL;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_SOLICITOR_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.CcdFields.RESPONDENT_FIRST_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.CcdFields.RESPONDENT_LAST_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CO_RESPONDENT_DEFENDS_DIVORCE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CO_RESPONDENT_DUE_DATE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CO_RESP_EMAIL_ADDRESS;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D8_REASON_FOR_DIVORCE_ADULTERY_3RD_PARTY_FNAME;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D8_REASON_FOR_DIVORCE_ADULTERY_3RD_PARTY_LNAME;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D_8_CASE_REFERENCE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D_8_DIVORCE_UNIT;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D_8_PETITIONER_FIRST_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D_8_PETITIONER_LAST_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NOTIFICATION_ADDRESSEE_FIRST_NAME_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NOTIFICATION_ADDRESSEE_LAST_NAME_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NOTIFICATION_CASE_NUMBER_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NOTIFICATION_CCD_REFERENCE_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NOTIFICATION_COURT_ADDRESS_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NOTIFICATION_EMAIL;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NOTIFICATION_FORM_SUBMISSION_DATE_LIMIT_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NOTIFICATION_PET_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NOTIFICATION_RDC_NAME_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NOTIFICATION_RESP_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NOTIFICATION_SOLICITOR_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NOTIFICATION_WELSH_FORM_SUBMISSION_DATE_LIMIT_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NO_VALUE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.PETITIONER_SOLICITOR_EMAIL;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.PETITIONER_SOLICITOR_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.YES_VALUE;

public class CoRespondentSubmittedITest extends MockedFunctionalTest {
    private static final String API_URL = "/co-respondent-received";

    private static final String USER_TOKEN = "anytoken";

    private static final String CO_RESP_FIRST_NAME = "any-name";
    private static final String CO_RESP_LAST_NAME = "any-last-name";

    private static final String EVENT_ID = "event-id";
    private static final String CASE_ID = "case-id";
    private static final String D8_ID = "d8-id";

    private static final String SOL_APPLICANT_CORESP_RESPONDED_TEMPLATE_ID = "cff97b35-fcf7-40b7-ac10-87d34369d15e";

    @Autowired
    private MockMvc webClient;

    @Autowired
    private TaskCommons taskCommons;

    @MockBean
    private EmailClient emailClient;

    @Test
    public void givenEmptyBody_whenPerformAOSReceived_thenReturnBadRequestResponse() throws Exception {
        webClient.perform(post(API_URL)
                .header(AUTHORIZATION, USER_TOKEN)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void givenCaseData_whenPerformCoRespReceived_thenReturnCaseData() throws Exception {
        Map<String, Object> caseDetailMap = new HashMap<>();
        caseDetailMap.put(D_8_CASE_REFERENCE, D8_ID);
        caseDetailMap.put(D8_REASON_FOR_DIVORCE_ADULTERY_3RD_PARTY_FNAME, CO_RESP_FIRST_NAME);
        caseDetailMap.put(D8_REASON_FOR_DIVORCE_ADULTERY_3RD_PARTY_LNAME, CO_RESP_LAST_NAME);
        caseDetailMap.put(CO_RESP_EMAIL_ADDRESS, TEST_EMAIL);
        caseDetailMap.put(CO_RESPONDENT_DEFENDS_DIVORCE, NO_VALUE);
        caseDetailMap.put(D_8_PETITIONER_FIRST_NAME, TestConstants.TEST_USER_FIRST_NAME);
        caseDetailMap.put(D_8_PETITIONER_LAST_NAME, TestConstants.TEST_USER_LAST_NAME);

        CcdCallbackRequest ccdCallbackRequest = CcdCallbackRequest.builder().eventId(CASE_ID)
                .caseDetails(CaseDetails.builder()
                        .caseData(caseDetailMap)
                        .build())
                .build();

        CcdCallbackResponse ccdCallbackResponse = CcdCallbackResponse
                .builder()
                .data(ccdCallbackRequest.getCaseDetails().getCaseData())
                .build();
        String expectedResponse = ObjectMapperTestUtil.convertObjectToJsonString(ccdCallbackResponse);
        webClient.perform(post(API_URL)
                .header(AUTHORIZATION, USER_TOKEN)
                .content(ObjectMapperTestUtil.convertObjectToJsonString(ccdCallbackRequest))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().string(expectedResponse));

        verifyEmailSent(TEST_EMAIL, Collections.EMPTY_MAP);
    }

    @Test
    public void givenCaseData_whenPerformCoRespReceived_andRepresentedPetitioner_thenReturnCaseData() throws Exception {
        Map<String, Object> caseData = new HashMap<>();
        caseData.put(D_8_CASE_REFERENCE, D8_ID);
        caseData.put(D8_REASON_FOR_DIVORCE_ADULTERY_3RD_PARTY_FNAME, CO_RESP_FIRST_NAME);
        caseData.put(D8_REASON_FOR_DIVORCE_ADULTERY_3RD_PARTY_LNAME, CO_RESP_LAST_NAME);
        caseData.put(CO_RESP_EMAIL_ADDRESS, TEST_EMAIL);
        caseData.put(CO_RESPONDENT_DEFENDS_DIVORCE, NO_VALUE);
        caseData.put(D_8_PETITIONER_FIRST_NAME, TEST_PETITIONER_FIRST_NAME);
        caseData.put(D_8_PETITIONER_LAST_NAME, TEST_PETITIONER_LAST_NAME);
        caseData.put(PETITIONER_SOLICITOR_EMAIL, TEST_SOLICITOR_EMAIL);
        caseData.put(PETITIONER_SOLICITOR_NAME, TEST_SOLICITOR_NAME);
        caseData.put(RESPONDENT_FIRST_NAME, TEST_RESPONDENT_FIRST_NAME);
        caseData.put(RESPONDENT_LAST_NAME, TEST_RESPONDENT_LAST_NAME);

        CcdCallbackRequest ccdCallbackRequest = CcdCallbackRequest.builder()
            .eventId(CASE_ID)
            .caseDetails(CaseDetails.builder()
                .caseData(caseData)
                .caseId(CASE_ID)
                .build())
            .build();

        CcdCallbackResponse ccdCallbackResponse = CcdCallbackResponse
            .builder()
            .data(ccdCallbackRequest.getCaseDetails().getCaseData())
            .build();
        String expectedResponse = ObjectMapperTestUtil.convertObjectToJsonString(ccdCallbackResponse);
        webClient.perform(post(API_URL)
            .header(AUTHORIZATION, USER_TOKEN)
            .content(ObjectMapperTestUtil.convertObjectToJsonString(ccdCallbackRequest))
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content().string(expectedResponse));

        verifyEmailSent(TEST_EMAIL, Collections.EMPTY_MAP);

        verify(emailClient).sendEmail(
            eq(SOL_APPLICANT_CORESP_RESPONDED_TEMPLATE_ID),
            eq(TEST_SOLICITOR_EMAIL),
            eq(ImmutableMap.<String, Object>builder()
                .put(NOTIFICATION_SOLICITOR_NAME, TEST_SOLICITOR_NAME)
                .put(NOTIFICATION_EMAIL, TEST_SOLICITOR_EMAIL)
                .put(NOTIFICATION_PET_NAME, TEST_PETITIONER_FULL_NAME)
                .put(NOTIFICATION_CCD_REFERENCE_KEY, CASE_ID)
                .put(NOTIFICATION_RESP_NAME, TEST_RESPONDENT_FULL_NAME)
                .build()),
            anyString()
        );
        verifyNoMoreInteractions(emailClient);
    }

    @Test
    public void givenDefendedCoRespondCase_whenPerformCoRespReceived_thenReturnCaseData() throws Exception {
        Map<String, Object> caseDetailMap = new HashMap<>();
        caseDetailMap.put(D_8_CASE_REFERENCE, D8_ID);
        caseDetailMap.put(D8_REASON_FOR_DIVORCE_ADULTERY_3RD_PARTY_FNAME, CO_RESP_FIRST_NAME);
        caseDetailMap.put(D8_REASON_FOR_DIVORCE_ADULTERY_3RD_PARTY_LNAME, CO_RESP_LAST_NAME);
        caseDetailMap.put(CO_RESP_EMAIL_ADDRESS, TEST_EMAIL);
        caseDetailMap.put(CO_RESPONDENT_DEFENDS_DIVORCE, YES_VALUE);
        caseDetailMap.put(CO_RESPONDENT_DUE_DATE, TEST_EXPECTED_DUE_DATE);
        caseDetailMap.put(D_8_DIVORCE_UNIT, TEST_COURT);
        caseDetailMap.put(D_8_PETITIONER_FIRST_NAME, TestConstants.TEST_USER_FIRST_NAME);
        caseDetailMap.put(D_8_PETITIONER_LAST_NAME, TestConstants.TEST_USER_LAST_NAME);


        CcdCallbackRequest ccdCallbackRequest = CcdCallbackRequest.builder().eventId(EVENT_ID)
                .caseDetails(CaseDetails.builder()
                        .caseId(CASE_ID)
                        .caseData(caseDetailMap)
                        .build())
                .build();

        CcdCallbackResponse ccdCallbackResponse = CcdCallbackResponse
            .builder()
            .data(ccdCallbackRequest.getCaseDetails().getCaseData())
            .build();

        String expectedResponse = ObjectMapperTestUtil.convertObjectToJsonString(ccdCallbackResponse);
        webClient.perform(post(API_URL)
                .header(AUTHORIZATION, USER_TOKEN)
                .content(ObjectMapperTestUtil.convertObjectToJsonString(ccdCallbackRequest))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().string(expectedResponse));
        Court court = taskCommons.getCourt(TEST_COURT);

        Map<String, String> expectedExtraFields = ImmutableMap.of(
            NOTIFICATION_COURT_ADDRESS_KEY, court.getFormattedAddress(),
            NOTIFICATION_RDC_NAME_KEY, court.getIdentifiableCentreName(),
            NOTIFICATION_FORM_SUBMISSION_DATE_LIMIT_KEY, TEST_EXPECTED_DUE_DATE_FORMATTED,
            NOTIFICATION_WELSH_FORM_SUBMISSION_DATE_LIMIT_KEY, TEST_FORM_WESLH_SUBMISSION_DUE_DATE
        );
        verifyEmailSent(TEST_EMAIL, expectedExtraFields);

    }

    private void verifyEmailSent(String email, Map<String, String> additionalData) throws NotificationClientException {
        Map<String, String> notificationTemplateVars = new HashMap<>(additionalData);
        notificationTemplateVars.put(NOTIFICATION_ADDRESSEE_FIRST_NAME_KEY, CO_RESP_FIRST_NAME);
        notificationTemplateVars.put(NOTIFICATION_ADDRESSEE_LAST_NAME_KEY, CO_RESP_LAST_NAME);
        notificationTemplateVars.put(NOTIFICATION_CASE_NUMBER_KEY, D8_ID);
        verify(emailClient).sendEmail(any(), eq(email), eq(notificationTemplateVars), any());
    }
}
