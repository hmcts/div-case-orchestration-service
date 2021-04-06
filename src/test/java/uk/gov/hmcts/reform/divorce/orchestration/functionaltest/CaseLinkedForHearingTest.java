package uk.gov.hmcts.reform.divorce.orchestration.functionaltest;

import com.google.common.collect.ImmutableMap;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import uk.gov.hmcts.reform.divorce.orchestration.client.EmailClient;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CcdCallbackRequest;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CcdCallbackResponse;

import java.util.Map;

import static com.jayway.jsonpath.matchers.JsonPathMatchers.hasJsonPath;
import static com.jayway.jsonpath.matchers.JsonPathMatchers.isJson;
import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_D8_CASE_REFERENCE;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_PETITIONER_EMAIL;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_PETITIONER_FIRST_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_PETITIONER_FULL_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_PETITIONER_LAST_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_RELATIONSHIP_HUSBAND;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_RESPONDENT_EMAIL;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_RESPONDENT_FIRST_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_RESPONDENT_FULL_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_RESPONDENT_LAST_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_RESP_SOLICITOR_EMAIL;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_RESP_SOLICITOR_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.COSTS_CLAIM_GRANTED;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.COSTS_CLAIM_NOT_GRANTED;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.COURT_NAME_TEMPLATE_ID;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D8_RESPONDENT_SOLICITOR_EMAIL;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D8_RESPONDENT_SOLICITOR_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DATE_OF_HEARING;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.LIMIT_DATE_TO_CONTACT_COURT;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NOTIFICATION_ADDRESSEE_FIRST_NAME_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NOTIFICATION_ADDRESSEE_LAST_NAME_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NOTIFICATION_CASE_NUMBER_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NOTIFICATION_CCD_REFERENCE_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NOTIFICATION_EMAIL;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NOTIFICATION_HUSBAND_OR_WIFE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NOTIFICATION_OPTIONAL_TEXT_NO_VALUE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NOTIFICATION_OPTIONAL_TEXT_YES_VALUE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NOTIFICATION_PET_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NOTIFICATION_RESP_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NOTIFICATION_SOLICITOR_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.testutil.ObjectMapperTestUtil.convertObjectToJsonString;
import static uk.gov.hmcts.reform.divorce.orchestration.testutil.ObjectMapperTestUtil.getJsonFromResourceFile;

public class CaseLinkedForHearingTest extends MockedFunctionalTest {

    private static final String API_URL = "/case-linked-for-hearing";

    private static final String PETITIONER_COE_NOTIFICATION_EMAIL_TEMPLATE_ID = "9937c8bc-dc7a-4210-a25b-20aceb82d48d";
    private static final String RESPONDENT_COE_NOTIFICATION_EMAIL_TEMPLATE_ID = "80b986e1-056b-4577-a343-bb2e72e2a3f0";
    private static final String SOL_RESP_COE_NOTIFICATION_TEMPLATE_ID = "e7117ed4-83ff-43f9-8521-e70d31063c7e";

    private static final String TEST_COURT_NAME = "Liverpool Civil and Family Court Hearing Centre";
    private static final String TEST_DATE_OF_HEARING = "21 April 2019";
    private static final String TEST_LIMIT_DATE_TO_CONTACT_COURT = "7 April 2019";
    private static final String TEST_CCD_REFERENCE = "0123456789012345";

    @Autowired
    private MockMvc webClient;

    @MockBean
    private EmailClient mockClient;

    @Test
    public void whenCallbackEndpointIsCalled_ThenResponseIsSuccessful() throws Exception {
        CcdCallbackRequest ccdCallbackRequest = getJsonFromResourceFile(
            "/jsonExamples/payloads/caseListedForHearing.json", CcdCallbackRequest.class);
        Map<String, Object> caseData = ccdCallbackRequest.getCaseDetails().getCaseData();
        CcdCallbackResponse expectedResponse = CcdCallbackResponse.builder()
            .data(caseData)
            .build();

        webClient.perform(post(API_URL)
            .header(AUTHORIZATION, AUTH_TOKEN)
            .content(convertObjectToJsonString(ccdCallbackRequest))
            .contentType(APPLICATION_JSON)
            .accept(APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content().json(convertObjectToJsonString(expectedResponse)))
            .andExpect(content().string(allOf(
                isJson(),
                hasJsonPath("$.errors", nullValue())
            )));

        verifyPetitionerCoENotificationEmailWasSent();

        verify(mockClient).sendEmail(
            eq(RESPONDENT_COE_NOTIFICATION_EMAIL_TEMPLATE_ID),
            eq(TEST_RESPONDENT_EMAIL),
            eq(ImmutableMap.<String, Object>builder()
                .put(NOTIFICATION_CASE_NUMBER_KEY, TEST_D8_CASE_REFERENCE)
                .put(NOTIFICATION_HUSBAND_OR_WIFE, TEST_RELATIONSHIP_HUSBAND)
                .put(NOTIFICATION_ADDRESSEE_LAST_NAME_KEY, TEST_RESPONDENT_LAST_NAME)
                .put(NOTIFICATION_ADDRESSEE_FIRST_NAME_KEY, TEST_RESPONDENT_FIRST_NAME)
                .put(LIMIT_DATE_TO_CONTACT_COURT, TEST_LIMIT_DATE_TO_CONTACT_COURT)
                .put(COSTS_CLAIM_GRANTED, NOTIFICATION_OPTIONAL_TEXT_YES_VALUE)
                .put(DATE_OF_HEARING, TEST_DATE_OF_HEARING)
                .put(NOTIFICATION_EMAIL, TEST_RESPONDENT_EMAIL)
                .put(COURT_NAME_TEMPLATE_ID, TEST_COURT_NAME)
                .build()),
            any()
        );
    }

    @Test
    public void givenRepresentedRespondent_andRespSolIsDigital_whenDecisionIsApprove_thenRespSolIsSentEmail() throws Exception {
        CcdCallbackRequest ccdCallbackRequest = getJsonFromResourceFile(
            "/jsonExamples/payloads/caseListedForHearing.json", CcdCallbackRequest.class);
        Map<String, Object> caseData = ccdCallbackRequest.getCaseDetails().getCaseData();
        caseData.put(D8_RESPONDENT_SOLICITOR_EMAIL, TEST_RESP_SOLICITOR_EMAIL);
        caseData.put(D8_RESPONDENT_SOLICITOR_NAME, TEST_RESP_SOLICITOR_NAME);
        CcdCallbackResponse expectedResponse = CcdCallbackResponse.builder()
            .data(caseData)
            .build();

        webClient.perform(post(API_URL)
            .header(AUTHORIZATION, AUTH_TOKEN)
            .content(convertObjectToJsonString(ccdCallbackRequest))
            .contentType(APPLICATION_JSON)
            .accept(APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content().json(convertObjectToJsonString(expectedResponse)))
            .andExpect(content().string(allOf(
                isJson(),
                hasJsonPath("$.errors", nullValue())
            )));

        verifyPetitionerCoENotificationEmailWasSent();

        verify(mockClient).sendEmail(
            eq(SOL_RESP_COE_NOTIFICATION_TEMPLATE_ID),
            eq(TEST_RESP_SOLICITOR_EMAIL),
            eq(ImmutableMap.<String, Object>builder()
                .put(NOTIFICATION_SOLICITOR_NAME, TEST_RESP_SOLICITOR_NAME)
                .put(LIMIT_DATE_TO_CONTACT_COURT, TEST_LIMIT_DATE_TO_CONTACT_COURT)
                .put(COSTS_CLAIM_GRANTED, NOTIFICATION_OPTIONAL_TEXT_YES_VALUE)
                .put(DATE_OF_HEARING, TEST_DATE_OF_HEARING)
                .put(NOTIFICATION_EMAIL, TEST_RESP_SOLICITOR_EMAIL)
                .put(COURT_NAME_TEMPLATE_ID, TEST_COURT_NAME)
                .put(NOTIFICATION_PET_NAME, TEST_PETITIONER_FULL_NAME)
                .put(NOTIFICATION_CCD_REFERENCE_KEY, TEST_CCD_REFERENCE)
                .put(NOTIFICATION_RESP_NAME, TEST_RESPONDENT_FULL_NAME)
                .build()),
            any()
        );
    }

    private void verifyPetitionerCoENotificationEmailWasSent() throws Exception {
        verify(mockClient).sendEmail(
            eq(PETITIONER_COE_NOTIFICATION_EMAIL_TEMPLATE_ID),
            eq(TEST_PETITIONER_EMAIL),
            eq(ImmutableMap.<String, Object>builder()
                .put(NOTIFICATION_CASE_NUMBER_KEY, TEST_D8_CASE_REFERENCE)
                .put(NOTIFICATION_ADDRESSEE_LAST_NAME_KEY, TEST_PETITIONER_LAST_NAME)
                .put(NOTIFICATION_ADDRESSEE_FIRST_NAME_KEY, TEST_PETITIONER_FIRST_NAME)
                .put(LIMIT_DATE_TO_CONTACT_COURT, TEST_LIMIT_DATE_TO_CONTACT_COURT)
                .put(COSTS_CLAIM_GRANTED, NOTIFICATION_OPTIONAL_TEXT_YES_VALUE)
                .put(DATE_OF_HEARING, TEST_DATE_OF_HEARING)
                .put(NOTIFICATION_EMAIL, TEST_PETITIONER_EMAIL)
                .put(COSTS_CLAIM_NOT_GRANTED, NOTIFICATION_OPTIONAL_TEXT_NO_VALUE)
                .put(COURT_NAME_TEMPLATE_ID, TEST_COURT_NAME)
                .build()),
            any()
        );
    }
}
