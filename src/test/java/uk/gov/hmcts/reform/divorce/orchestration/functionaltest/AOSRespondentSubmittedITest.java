package uk.gov.hmcts.reform.divorce.orchestration.functionaltest;

import com.google.common.collect.ImmutableMap;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.PropertySource;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import uk.gov.hmcts.reform.divorce.orchestration.OrchestrationServiceApplication;
import uk.gov.hmcts.reform.divorce.orchestration.client.EmailClient;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CaseDetails;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CcdCallbackResponse;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CreateEvent;
import uk.gov.hmcts.reform.divorce.orchestration.testutil.ObjectMapperTestUtil;
import uk.gov.service.notify.NotificationClientException;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D_8_INFERRED_RESPONDENT_GENDER;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D_8_PETITIONER_EMAIL;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D_8_PETITIONER_FIRST_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D_8_PETITIONER_LAST_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.ID;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NOTIFICATION_ADDRESSEE_FIRST_NAME_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NOTIFICATION_ADDRESSEE_LAST_NAME_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NOTIFICATION_REFERENCE_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NOTIFICATION_RELATIONSHIP_KEY;

@RunWith(SpringRunner.class)
@ContextConfiguration(classes = OrchestrationServiceApplication.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@PropertySource(value = "classpath:application.yml")
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
@AutoConfigureMockMvc
public class AOSRespondentSubmittedITest {

    private static final String EMAIL_CONTEXT_PATH = "/v2/notifications/email";
    private static final String API_URL = "/aos-received";
    private static final String USER_TOKEN = "anytoken";

    private static final String PETITIONER_FIRST_NAME = "any-name";
    private static final String PETITIONER_LAST_NAME = "any-last-name";
    private static final String RESPONDENT_MALE_GENDER = "male";
    private static final String RESPONDENT_FEMALE_GENDER = "female";
    private static final String PETITIONER_EMAIL = "some-test-email@@notifications.service.gov.uk";
    private static final String EVENT_ID = "event-id";
    private static final String CASE_ID = "case-id";


    private static final String TEMPLATE_ID = "templateId";
    @Autowired
    private MockMvc webClient;

    @MockBean
    private EmailClient mockClient;


    @Test
    public void givenEmptyBody_whenPerformAOSReceived_thenReturnBadRequestResponse() throws Exception {
        webClient.perform(post(API_URL)
                .header(AUTHORIZATION, USER_TOKEN)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void givenWithoutPetitionerEmail_whenPerformAOSReceived_thenReturnBadRequestResponse()
            throws Exception {
        mockEmailClient("null", PETITIONER_FIRST_NAME, PETITIONER_LAST_NAME, "wife", CASE_ID);
        Map<String, Object> caseDetailMap =   ImmutableMap.of(
                ID, CASE_ID,
                D_8_PETITIONER_FIRST_NAME, PETITIONER_FIRST_NAME,
                D_8_PETITIONER_LAST_NAME, PETITIONER_LAST_NAME,
                D_8_INFERRED_RESPONDENT_GENDER, RESPONDENT_FEMALE_GENDER
        );

        CcdCallbackResponse ccdCallbackResponse = CcdCallbackResponse
                .builder()
                .errors(Collections.singletonList("java.lang.Exception: error"))
                .build();

        String expectedResponse = ObjectMapperTestUtil.convertObjectToJsonString(ccdCallbackResponse);

        CreateEvent caseEvent = CreateEvent.builder().eventId(EVENT_ID)
                .caseDetails(CaseDetails.builder()
                        .caseId(CASE_ID)
                        .caseData(caseDetailMap)
                        .build())
                .build();

        webClient.perform(post(API_URL)
                .header(AUTHORIZATION, USER_TOKEN)
                .content(ObjectMapperTestUtil.convertObjectToJsonString(caseEvent))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().string(expectedResponse));
    }

    @Test
    public void givenCaseData_whenPerformAOSReceived_thenReturnCaseData() throws Exception {
        Map<String, Object> caseDetailMap =   ImmutableMap.of(
                D_8_PETITIONER_EMAIL, D_8_PETITIONER_EMAIL,
                D_8_PETITIONER_FIRST_NAME, PETITIONER_FIRST_NAME,
                D_8_PETITIONER_LAST_NAME, PETITIONER_LAST_NAME,
                D_8_INFERRED_RESPONDENT_GENDER, RESPONDENT_FEMALE_GENDER
        );

        CreateEvent caseEvent = CreateEvent.builder().eventId(CASE_ID)
                .caseDetails(CaseDetails.builder()
                        .caseData(caseDetailMap)
                        .build())
                .build();

        CcdCallbackResponse ccdCallbackResponse = CcdCallbackResponse
                .builder()
                .data(caseEvent.getCaseDetails().getCaseData())
                .build();
        String expectedResponse = ObjectMapperTestUtil.convertObjectToJsonString(ccdCallbackResponse);
        webClient.perform(post(API_URL)
                .header(AUTHORIZATION, USER_TOKEN)
                .content(ObjectMapperTestUtil.convertObjectToJsonString(caseEvent))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().string(expectedResponse));
    }

    @Test
    public void givenCaseWithoutId_whenPerformAOSReceived_thenReturnBadRequestResponse() throws Exception {
        Map<String, Object> caseDetailMap =   ImmutableMap.of(
                D_8_PETITIONER_EMAIL, D_8_PETITIONER_EMAIL,
                D_8_PETITIONER_FIRST_NAME, PETITIONER_FIRST_NAME,
                D_8_PETITIONER_LAST_NAME, PETITIONER_LAST_NAME,
                D_8_INFERRED_RESPONDENT_GENDER, RESPONDENT_FEMALE_GENDER
        );

        CreateEvent caseEvent = CreateEvent.builder().eventId(EVENT_ID)
                .caseDetails(CaseDetails.builder()
                        .caseId(CASE_ID)
                        .caseData(caseDetailMap)
                        .build())
                .build();

        CcdCallbackResponse ccdCallbackResponse = CcdCallbackResponse
                .builder()
                .errors(Collections.singletonList("java.lang.Exception: error"))
                .build();

        mockEmailClient(D_8_PETITIONER_EMAIL, PETITIONER_FIRST_NAME, PETITIONER_LAST_NAME, "wife", CASE_ID);

        String expectedResponse = ObjectMapperTestUtil.convertObjectToJsonString(ccdCallbackResponse);
        webClient.perform(post(API_URL)
                .header(AUTHORIZATION, USER_TOKEN)
                .content(ObjectMapperTestUtil.convertObjectToJsonString(caseEvent))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().string(expectedResponse));
    }

    private void mockEmailClient(String email, String firstName,  String lastName, String relationship, String ref )
            throws NotificationClientException {
        Map<String, String> notificationTemplateVars = new HashMap<>();
        notificationTemplateVars.put(NOTIFICATION_ADDRESSEE_FIRST_NAME_KEY, firstName);
        notificationTemplateVars.put(NOTIFICATION_ADDRESSEE_LAST_NAME_KEY, lastName);
        notificationTemplateVars.put(NOTIFICATION_RELATIONSHIP_KEY, relationship);
        notificationTemplateVars.put(NOTIFICATION_REFERENCE_KEY, ref);
        when(mockClient.sendEmail(any(), eq(email), eq(notificationTemplateVars), any()))
                .thenThrow(new NotificationClientException(new Exception("error")));
    }
}
