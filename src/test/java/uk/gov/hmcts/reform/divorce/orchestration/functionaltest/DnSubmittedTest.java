package uk.gov.hmcts.reform.divorce.orchestration.functionaltest;

import com.google.common.collect.ImmutableMap;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import uk.gov.hmcts.reform.divorce.orchestration.client.EmailClient;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CcdCallbackRequest;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CcdCallbackResponse;

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
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_CASE_FAMILY_MAN_ID;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_EMAIL;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_PETITIONER_FIRST_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_PETITIONER_LAST_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_RESPONDENT_FIRST_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_RESPONDENT_LAST_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_SOLICITOR_EMAIL;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_SOLICITOR_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D_8_CASE_REFERENCE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D_8_PETITIONER_EMAIL;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D_8_PETITIONER_FIRST_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D_8_PETITIONER_LAST_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.LANGUAGE_PREFERENCE_WELSH;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NOTIFICATION_ADDRESSEE_FIRST_NAME_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NOTIFICATION_ADDRESSEE_LAST_NAME_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NOTIFICATION_CCD_REFERENCE_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NOTIFICATION_EMAIL;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NOTIFICATION_PET_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NOTIFICATION_REFERENCE_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NOTIFICATION_RESP_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NOTIFICATION_SOLICITOR_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NO_VALUE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.PETITIONER_SOLICITOR_EMAIL;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.PETITIONER_SOLICITOR_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.RESP_FIRST_NAME_CCD_FIELD;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.RESP_LAST_NAME_CCD_FIELD;
import static uk.gov.hmcts.reform.divorce.orchestration.testutil.ObjectMapperTestUtil.convertObjectToJsonString;

@WebMvcTest
public class DnSubmittedTest extends MockedFunctionalTest {

    private static final String API_URL = "/handle-post-dn-submitted";

    private static final String SOL_APPLICANT_DN_SUBMITTED_TEMPLATE_ID = "5d653b12-2f4e-400d-b724-4081f77a00a9";
    private static final String DN_SUBMISSION_TEMPLATE_ID = "edf3bce9-f63a-4be0-93a9-d0c80dff7983";

    private static final Map<String, Object> CASE_DATA = ImmutableMap.<String, Object>builder()
        .put(D_8_PETITIONER_FIRST_NAME, TEST_PETITIONER_FIRST_NAME)
        .put(D_8_PETITIONER_LAST_NAME, TEST_PETITIONER_LAST_NAME)
        .put(D_8_PETITIONER_EMAIL, TEST_EMAIL)
        .put(LANGUAGE_PREFERENCE_WELSH, NO_VALUE)
        .put(D_8_CASE_REFERENCE, TEST_CASE_FAMILY_MAN_ID)
        .build();

    @MockBean
    private EmailClient mockEmailClient;

    @Autowired
    private MockMvc webClient;

    @Test
    public void shouldSendEmailToPetitioner() throws Exception {
        Map<String, Object> caseDataWithoutPetSolEmail = new HashMap<>(CASE_DATA);

        callApiEndpointSuccessfully(getCcdCallbackRequest(caseDataWithoutPetSolEmail));

        verifyEmailWasSentToPetitioner();
        verifyEmailNeverSentToPetSol();
    }

    @Test
    public void shouldSendEmailToPetitionerSolicitor() throws Exception {
        Map<String, Object> caseDataWithPetSolExpectedFields = addDataExpectedBySolicitorEmail();

        callApiEndpointSuccessfully(getCcdCallbackRequest(caseDataWithPetSolExpectedFields));

        verifyEmailWasSentToPetSol();
        verifyEmailNeverSentToPetitioner();
    }

    @Test
    public void givenBadRequestBody_thenReturnBadRequest() throws Exception {
        webClient.perform(post(API_URL)
            .header(AUTHORIZATION, AUTH_TOKEN)
            .contentType(APPLICATION_JSON)
            .accept(APPLICATION_JSON))
            .andExpect(status().isBadRequest());
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

    private void verifyEmailWasSentToPetSol() throws Exception {
        verify(mockEmailClient).sendEmail(
            eq(SOL_APPLICANT_DN_SUBMITTED_TEMPLATE_ID),
            eq(TEST_SOLICITOR_EMAIL),
            eq(
                ImmutableMap.of(
                    NOTIFICATION_CCD_REFERENCE_KEY, TEST_CASE_ID,
                    NOTIFICATION_EMAIL, TEST_SOLICITOR_EMAIL,
                    NOTIFICATION_PET_NAME, TEST_PETITIONER_FIRST_NAME + " " + TEST_PETITIONER_LAST_NAME,
                    NOTIFICATION_RESP_NAME, TEST_RESPONDENT_FIRST_NAME + " " + TEST_RESPONDENT_LAST_NAME,
                    NOTIFICATION_SOLICITOR_NAME, TEST_SOLICITOR_NAME
                )
            ),
            anyString()
        );
    }

    private void verifyEmailNeverSentToPetSol() throws Exception {
        verify(mockEmailClient, never()).sendEmail(
            eq(SOL_APPLICANT_DN_SUBMITTED_TEMPLATE_ID),
            eq(TEST_SOLICITOR_EMAIL),
            any(),
            any()
        );
    }

    private void verifyEmailWasSentToPetitioner() throws Exception {
        verify(mockEmailClient).sendEmail(
            eq(DN_SUBMISSION_TEMPLATE_ID),
            eq(TEST_EMAIL),
            eq(
                ImmutableMap.of(
                    NOTIFICATION_ADDRESSEE_FIRST_NAME_KEY, TEST_PETITIONER_FIRST_NAME,
                    NOTIFICATION_ADDRESSEE_LAST_NAME_KEY, TEST_PETITIONER_LAST_NAME,
                    NOTIFICATION_REFERENCE_KEY, TEST_CASE_FAMILY_MAN_ID
                )
            ),
            any()
        );
    }

    private void verifyEmailNeverSentToPetitioner() throws Exception {
        verify(mockEmailClient, never()).sendEmail(
            eq(DN_SUBMISSION_TEMPLATE_ID),
            eq(TEST_EMAIL),
            any(),
            any()
        );
    }

    private Map<String, Object> addDataExpectedBySolicitorEmail() {
        Map<String, Object> caseDataWithRespSolEmail = new HashMap<>(CASE_DATA);
        caseDataWithRespSolEmail.put(PETITIONER_SOLICITOR_EMAIL, TEST_SOLICITOR_EMAIL);
        caseDataWithRespSolEmail.put(PETITIONER_SOLICITOR_NAME, TEST_SOLICITOR_NAME);
        caseDataWithRespSolEmail.put(RESP_FIRST_NAME_CCD_FIELD, TEST_RESPONDENT_FIRST_NAME);
        caseDataWithRespSolEmail.put(RESP_LAST_NAME_CCD_FIELD, TEST_RESPONDENT_LAST_NAME);

        return caseDataWithRespSolEmail;
    }
}
