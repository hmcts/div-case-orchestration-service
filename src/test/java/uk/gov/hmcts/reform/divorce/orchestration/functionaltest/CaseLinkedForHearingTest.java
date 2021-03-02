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

        verify(mockClient).sendEmail(
            eq(PETITIONER_COE_NOTIFICATION_EMAIL_TEMPLATE_ID),
            eq("petitioner@justice.uk"),
            any(),
            any()
        );

        verify(mockClient).sendEmail(
            eq(RESPONDENT_COE_NOTIFICATION_EMAIL_TEMPLATE_ID),
            eq("respondent@justice.uk"),
            any(),
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

        verify(mockClient).sendEmail(
            eq(PETITIONER_COE_NOTIFICATION_EMAIL_TEMPLATE_ID),
            eq("petitioner@justice.uk"),
            eq(ImmutableMap.<String, Object>builder()
                .put(NOTIFICATION_CASE_NUMBER_KEY, "HR290831")
                .put(NOTIFICATION_ADDRESSEE_LAST_NAME_KEY, "Johnson")
                .put(NOTIFICATION_ADDRESSEE_FIRST_NAME_KEY, "James")
                .put(LIMIT_DATE_TO_CONTACT_COURT, "7 April 2019")
                .put(COSTS_CLAIM_GRANTED, "yes")
                .put(DATE_OF_HEARING, "21 April 2019")
                .put(NOTIFICATION_EMAIL, "petitioner@justice.uk")
                .put(COSTS_CLAIM_NOT_GRANTED, "no")
                .put(COURT_NAME_TEMPLATE_ID, "Liverpool Civil and Family Court Hearing Centre")
                .build()),
            any()
        );

        verify(mockClient).sendEmail(
            eq(SOL_RESP_COE_NOTIFICATION_TEMPLATE_ID),
            eq(TEST_RESP_SOLICITOR_EMAIL),
            eq(ImmutableMap.<String, Object>builder()
                .put(NOTIFICATION_SOLICITOR_NAME, TEST_RESP_SOLICITOR_NAME)
                .put(LIMIT_DATE_TO_CONTACT_COURT, "7 April 2019")
                .put(COSTS_CLAIM_GRANTED, "yes")
                .put(DATE_OF_HEARING, "21 April 2019")
                .put(NOTIFICATION_EMAIL, TEST_RESP_SOLICITOR_EMAIL)
                .put(COURT_NAME_TEMPLATE_ID, "Liverpool Civil and Family Court Hearing Centre")
                .put(NOTIFICATION_PET_NAME, "James Johnson")
                .put(NOTIFICATION_CCD_REFERENCE_KEY, "0123456789012345")
                .put(NOTIFICATION_RESP_NAME, "Jane Jamed")
                .build()),
            any()
        );
    }
}
