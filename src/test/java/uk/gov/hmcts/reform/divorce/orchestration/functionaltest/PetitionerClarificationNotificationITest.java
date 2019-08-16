package uk.gov.hmcts.reform.divorce.orchestration.functionaltest;

import com.google.common.collect.ImmutableMap;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import uk.gov.hmcts.reform.divorce.orchestration.client.EmailClient;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CaseDetails;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CcdCallbackRequest;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CcdCallbackResponse;
import uk.gov.hmcts.reform.divorce.orchestration.testutil.ObjectMapperTestUtil;

import java.util.HashMap;
import java.util.Map;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_CASE_FAMILY_MAN_ID;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_PETITIONER_FIRST_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_PETITIONER_LAST_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_USER_EMAIL;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D_8_CASE_REFERENCE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D_8_PETITIONER_EMAIL;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D_8_PETITIONER_FIRST_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D_8_PETITIONER_LAST_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NOTIFICATION_ADDRESSEE_FIRST_NAME_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NOTIFICATION_ADDRESSEE_LAST_NAME_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NOTIFICATION_CASE_NUMBER_KEY;

@RunWith(SpringRunner.class)
public class PetitionerClarificationNotificationITest extends MockedFunctionalTest {
    private static final String API_URL = "/request-clarification-petitioner";
    private static final String EMAIL_TEMPLATE_ID = "686ce418-6d76-48ce-b903-a87d2b832125";

    @Autowired
    private MockMvc webClient;

    @MockBean
    private EmailClient mockEmailClient;

    @Test
    public void givenEmptyBody_whenEndPointCalled_thenReturnBadRequestResponse() throws Exception {
        webClient.perform(post(API_URL)
            .contentType(APPLICATION_JSON))
            .andExpect(status().isBadRequest());
    }

    @Test
    public void happyPath() throws Exception {
        final Map<String, Object> caseDetailMap = ImmutableMap.of(
            D_8_CASE_REFERENCE, TEST_CASE_FAMILY_MAN_ID,
            D_8_PETITIONER_EMAIL, TEST_USER_EMAIL,
            D_8_PETITIONER_FIRST_NAME, TEST_PETITIONER_FIRST_NAME,
            D_8_PETITIONER_LAST_NAME, TEST_PETITIONER_LAST_NAME);

        final CcdCallbackRequest ccdCallbackRequest = CcdCallbackRequest.builder().eventId(TEST_CASE_ID)
            .caseDetails(CaseDetails.builder()
                .caseData(caseDetailMap)
                .build())
            .build();

        final CcdCallbackResponse ccdCallbackResponse = CcdCallbackResponse
            .builder()
            .data(ccdCallbackRequest.getCaseDetails().getCaseData())
            .build();

        final String expectedResponse = ObjectMapperTestUtil.convertObjectToJsonString(ccdCallbackResponse);

        webClient.perform(post(API_URL)
            .content(ObjectMapperTestUtil.convertObjectToJsonString(ccdCallbackRequest))
            .contentType(APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content().string(expectedResponse));

        final Map<String, String> notificationTemplateVars = new HashMap<>();
        notificationTemplateVars.put(NOTIFICATION_CASE_NUMBER_KEY, TEST_CASE_FAMILY_MAN_ID);
        notificationTemplateVars.put(NOTIFICATION_ADDRESSEE_FIRST_NAME_KEY, TEST_PETITIONER_FIRST_NAME);
        notificationTemplateVars.put(NOTIFICATION_ADDRESSEE_LAST_NAME_KEY, TEST_PETITIONER_LAST_NAME);

        verify(mockEmailClient).sendEmail(eq(EMAIL_TEMPLATE_ID), eq(TEST_USER_EMAIL), eq(notificationTemplateVars), anyString());
    }

    @Test
    public void emailNotSentIfEmailAddressMissing() throws Exception {
        final Map<String, Object> caseDetailMap = ImmutableMap.of(
            D_8_CASE_REFERENCE, TEST_CASE_FAMILY_MAN_ID,
            D_8_PETITIONER_FIRST_NAME, TEST_PETITIONER_FIRST_NAME,
            D_8_PETITIONER_LAST_NAME, TEST_PETITIONER_LAST_NAME);

        final CcdCallbackRequest ccdCallbackRequest = CcdCallbackRequest.builder().eventId(TEST_CASE_ID)
            .caseDetails(CaseDetails.builder()
                .caseData(caseDetailMap)
                .build())
            .build();

        final CcdCallbackResponse ccdCallbackResponse = CcdCallbackResponse
            .builder()
            .data(ccdCallbackRequest.getCaseDetails().getCaseData())
            .build();

        final String expectedResponse = ObjectMapperTestUtil.convertObjectToJsonString(ccdCallbackResponse);

        webClient.perform(post(API_URL)
            .content(ObjectMapperTestUtil.convertObjectToJsonString(ccdCallbackRequest))
            .contentType(APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content().string(expectedResponse));

        verifyZeroInteractions(mockEmailClient);

    }
}
