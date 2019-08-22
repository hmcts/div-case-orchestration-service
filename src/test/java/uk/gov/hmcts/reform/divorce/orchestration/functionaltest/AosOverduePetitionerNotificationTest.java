package uk.gov.hmcts.reform.divorce.orchestration.functionaltest;

import com.google.common.collect.ImmutableMap;
import org.json.JSONObject;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import uk.gov.hmcts.reform.divorce.orchestration.client.EmailClient;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CcdCallbackResponse;
import uk.gov.service.notify.NotificationClientException;

import java.util.Map;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonMap;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_PETITIONER_EMAIL;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_RELATIONSHIP;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_RESPONDENT_FIRST_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_RESPONDENT_LAST_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CASE_DETAILS_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CCD_CASE_DATA_FIELD;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D_8_DIVORCED_WHO;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D_8_PETITIONER_EMAIL;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D_8_PETITIONER_FIRST_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D_8_PETITIONER_LAST_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.testutil.ObjectMapperTestUtil.convertObjectToJsonString;

@RunWith(SpringRunner.class)
public class AosOverduePetitionerNotificationTest extends MockedFunctionalTest {

    private static final String API_URL = "/aos-overdue";
    private static final Map<String, Object> CASE_DATA = ImmutableMap.<String, Object>builder()
        .put(D_8_PETITIONER_EMAIL, TEST_PETITIONER_EMAIL)
        .put(D_8_PETITIONER_FIRST_NAME, TEST_RESPONDENT_FIRST_NAME)
        .put(D_8_PETITIONER_LAST_NAME, TEST_RESPONDENT_LAST_NAME)
        .put(D_8_DIVORCED_WHO, TEST_RELATIONSHIP)
        .build();


    private static final Map CASE_DETAILS = singletonMap(CASE_DETAILS_JSON_KEY,
        ImmutableMap.<String, Object>builder()
            .put(CCD_CASE_DATA_FIELD, CASE_DATA)
            .build()
    );

    @Autowired
    private MockMvc webClient;

    @MockBean
    EmailClient mockEmailClient;

    @Test
    public void givenCorrectPetitionerDetails_ThenOkResponse() throws Exception {

        CcdCallbackResponse expectedResponse = CcdCallbackResponse.builder().data(CASE_DATA).build();
        when(mockEmailClient.sendEmail(anyString(), anyString(), anyMap(), anyString()))
            .thenReturn(null);

        String inputJson = JSONObject.valueToString(CASE_DETAILS);

        webClient.perform(post(API_URL)
            .header(AUTHORIZATION, AUTH_TOKEN)
            .content(inputJson)
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(MockMvcResultMatchers.content().json(convertObjectToJsonString(expectedResponse)));
    }

    @Test
    public void givenBadRequestBody_thenReturnBadRequest()
        throws Exception {
        webClient.perform(post(API_URL)
            .header(AUTHORIZATION, AUTH_TOKEN)
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isBadRequest());
    }

    @Test
    public void givenEmailServiceReturns500_ThenInternalServerErrorResponse() throws Exception {
        NotificationClientException exception = new NotificationClientException("test exception");
        when(mockEmailClient.sendEmail(anyString(), anyString(), anyMap(), anyString()))
            .thenThrow(exception);

        String inputJson = JSONObject.valueToString(CASE_DETAILS);
        CcdCallbackResponse expectedResponse = CcdCallbackResponse.builder()
            .errors(asList("test exception")).build();

        webClient.perform(post(API_URL)
            .header(AUTHORIZATION, AUTH_TOKEN)
            .content(inputJson)
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(MockMvcResultMatchers.content().json(convertObjectToJsonString(expectedResponse)));
    }
}
