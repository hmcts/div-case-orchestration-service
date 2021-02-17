package uk.gov.hmcts.reform.divorce.orchestration.functionaltest;

import com.google.common.collect.ImmutableMap;
import org.json.JSONObject;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import uk.gov.hmcts.reform.divorce.orchestration.client.EmailClient;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CcdCallbackRequest;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CcdCallbackResponse;
import uk.gov.service.notify.NotificationClientException;

import java.util.Map;

import static com.jayway.jsonpath.matchers.JsonPathMatchers.hasJsonPath;
import static com.jayway.jsonpath.matchers.JsonPathMatchers.isJson;
import static java.util.Collections.singletonList;
import static java.util.Collections.singletonMap;
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
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_PETITIONER_EMAIL;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_RESPONDENT_EMAIL;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_RESPONDENT_FIRST_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_RESPONDENT_LAST_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_SOLICITOR_EMAIL;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CASE_DETAILS_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CCD_CASE_DATA_FIELD;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D_8_CASE_REFERENCE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D_8_INFERRED_PETITIONER_GENDER;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.RESPONDENT_EMAIL_ADDRESS;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.RESP_FIRST_NAME_CCD_FIELD;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.RESP_LAST_NAME_CCD_FIELD;
import static uk.gov.hmcts.reform.divorce.orchestration.testutil.ObjectMapperTestUtil.convertObjectToJsonString;
import static uk.gov.hmcts.reform.divorce.orchestration.testutil.ObjectMapperTestUtil.getJsonFromResourceFile;

public class DecreeAbsoluteRequestedRespondentNotificationTest extends MockedFunctionalTest {

    private static final String API_URL = "/da-requested-by-applicant";
    private static final String DECREE_ABSOLUTE_REQUESTED_NOTIFICATION_EMAIL_TEMPLATE_ID = "43b52d1a-b9be-4de5-b5ae-627c51a55111";
    private static final String DECREE_ABSOLUTE_REQUESTED_PETITIONER_SOLICITOR_EMAIL_TEMPLATE_ID = "8d546d3c-9df4-420d-b11c-9706ef3a7e89";

    private static final Map<String, Object> CASE_DATA = ImmutableMap.<String, Object>builder()
        .put(RESPONDENT_EMAIL_ADDRESS, TEST_RESPONDENT_EMAIL)
        .put(RESP_FIRST_NAME_CCD_FIELD, TEST_RESPONDENT_FIRST_NAME)
        .put(RESP_LAST_NAME_CCD_FIELD, TEST_RESPONDENT_LAST_NAME)
        .put(D_8_CASE_REFERENCE, TEST_CASE_ID)
        .put(D_8_INFERRED_PETITIONER_GENDER, TEST_INFERRED_GENDER)
        .build();

    private static final Map CASE_DETAILS = singletonMap(CASE_DETAILS_JSON_KEY,
        ImmutableMap.<String, Object>builder()
            .put(CCD_CASE_DATA_FIELD, CASE_DATA)
            .build()
    );

    @MockBean
    private EmailClient mockEmailClient;

    @Autowired
    private MockMvc webClient;


    @Test
    public void testThatPetSolAndRespAreSentEmails() throws Exception {
        CcdCallbackRequest ccdCallbackRequest = getJsonFromResourceFile(
            "", // TODO
            CcdCallbackRequest.class);
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

        verify(mockEmailClient).sendEmail(
            eq(DECREE_ABSOLUTE_REQUESTED_PETITIONER_SOLICITOR_EMAIL_TEMPLATE_ID),
            eq(TEST_SOLICITOR_EMAIL),
            any(),
            anyString());
        verify(mockEmailClient).sendEmail(
            eq(DECREE_ABSOLUTE_REQUESTED_NOTIFICATION_EMAIL_TEMPLATE_ID),
            eq(TEST_RESPONDENT_EMAIL),
            any(),
            anyString());
    }

    @Test
    public void testThatPetSolAndRespSolIsSentEmails() throws Exception {

    }

    @Test
    public void testOnlyRespondentIsSentEmails() throws Exception {

        verify(mockEmailClient, never()).sendEmail(
            eq(DECREE_ABSOLUTE_REQUESTED_PETITIONER_SOLICITOR_EMAIL_TEMPLATE_ID),
            eq(TEST_PETITIONER_EMAIL),
            any(),
            anyString());
        verify(mockEmailClient).sendEmail(
            eq(DECREE_ABSOLUTE_REQUESTED_PETITIONER_SOLICITOR_EMAIL_TEMPLATE_ID),
            eq(TEST_RESPONDENT_EMAIL),
            any(),
            anyString());
    }

    @Test
    public void testOnlyRespondentSolicitorIsSentEmails() throws Exception {

    }


    @Test
    public void testRespondentAndPetitionerAreSentEmails() throws Exception {

        CcdCallbackResponse expectedResponse = CcdCallbackResponse.builder().data(CASE_DATA).build();
        when(mockEmailClient.sendEmail(anyString(), anyString(), anyMap(), anyString()))
            .thenReturn(null);

        String inputJson = JSONObject.valueToString(CASE_DETAILS);

        webClient.perform(post(API_URL)
            .header(AUTHORIZATION, AUTH_TOKEN)
            .content(inputJson)
            .contentType(APPLICATION_JSON)
            .accept(APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content().json(convertObjectToJsonString(expectedResponse)));

        // verify email was actually send
        verify(mockEmailClient).sendEmail(
            eq(DECREE_ABSOLUTE_REQUESTED_PETITIONER_SOLICITOR_EMAIL_TEMPLATE_ID),
            eq("petitioner@justice.uk"),
            any(),
            any()
        );
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

        String inputJson = JSONObject.valueToString(CASE_DETAILS);
        CcdCallbackResponse expectedResponse = CcdCallbackResponse.builder()
            .errors(singletonList("test exception")).build();

        webClient.perform(post(API_URL)
            .header(AUTHORIZATION, AUTH_TOKEN)
            .content(inputJson)
            .contentType(APPLICATION_JSON)
            .accept(APPLICATION_JSON))
            .andExpect(content().json(convertObjectToJsonString(expectedResponse)));
    }
}