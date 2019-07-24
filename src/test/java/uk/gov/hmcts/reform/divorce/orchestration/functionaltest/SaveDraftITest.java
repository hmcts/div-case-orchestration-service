package uk.gov.hmcts.reform.divorce.orchestration.functionaltest;

import com.github.tomakehurst.wiremock.client.WireMock;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import uk.gov.hmcts.reform.divorce.orchestration.client.EmailClient;

import java.util.HashMap;
import java.util.Map;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static org.hamcrest.CoreMatchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.MediaType.APPLICATION_JSON_UTF8_VALUE;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_USER_EMAIL;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DIVORCE_SESSION_PETITIONER_EMAIL;
import static uk.gov.hmcts.reform.divorce.orchestration.testutil.ObjectMapperTestUtil.convertObjectToJsonString;

@RunWith(SpringRunner.class)
public class SaveDraftITest extends MockedFunctionalTest {
    private static final String API_URL = "/draftsapi/version/1";
    private static final String CMS_CONTEXT_PATH = "/casemaintenance/version/1/drafts?divorceFormat=true";
    private static final String SAVE_DRAFT_TEMPLATE_ID = "14074c06-87f1-4678-9238-4d71e741eb57";

    private static final String USER_TOKEN = "Some JWT Token";

    private static final Map<String, String> CASE_DATA = new HashMap<>();
    private static final Map<String, Object> CASE_DETAILS = new HashMap<>();

    @MockBean
    private EmailClient emailClient;

    @Autowired
    private MockMvc webClient;

    @Test
    public void givenJWTTokenIsNull_whenSaveDraft_thenReturnBadRequest()
            throws Exception {
        webClient.perform(put(API_URL)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void givenThereIsAConnectionError_whenSaveDraft_thenReturnBadGateway()
            throws Exception {
        final String errorMessage = "some error message";

        stubCmsServerEndpoint(HttpStatus.BAD_GATEWAY, errorMessage);

        CASE_DATA.put("deaftProperty1", "value1");
        CASE_DETAILS.put("case_data", CASE_DATA);

        webClient.perform(put(API_URL)
                .content(convertObjectToJsonString(CASE_DATA))
                .header(AUTHORIZATION, USER_TOKEN)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadGateway())
                .andExpect(content().string(containsString(errorMessage)));

        verify(emailClient, never()).sendEmail(eq(SAVE_DRAFT_TEMPLATE_ID),
                eq(TEST_USER_EMAIL),
                any(), any());
    }

    @Test
    public void givenEverythingWorksAsExpected_whenCmsCalled_thenSaveDraft()
            throws Exception {

        CASE_DATA.put("deaftProperty1", "value1");
        CASE_DATA.put(DIVORCE_SESSION_PETITIONER_EMAIL, TEST_USER_EMAIL);
        CASE_DETAILS.put("case_data", CASE_DATA);

        stubCmsServerEndpoint(HttpStatus.OK, convertObjectToJsonString(CASE_DETAILS));

        webClient.perform(put(API_URL)
                .content(convertObjectToJsonString(CASE_DATA))
                .header(AUTHORIZATION, USER_TOKEN)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().string("{}"));

        verify(emailClient, never()).sendEmail(eq(SAVE_DRAFT_TEMPLATE_ID),
                eq(TEST_USER_EMAIL),
                any(), any());
    }

    @Test
    public void givenEverythingWorksAsExpectedWithSendEmail_whenCmsCalled_thenSaveDraft()
            throws Exception {

        CASE_DATA.put("deaftProperty1", "value1");
        CASE_DATA.put(DIVORCE_SESSION_PETITIONER_EMAIL, TEST_USER_EMAIL);
        CASE_DETAILS.put("case_data", CASE_DATA);

        stubCmsServerEndpoint(HttpStatus.OK, convertObjectToJsonString(CASE_DETAILS));

        webClient.perform(put(API_URL + "?sendEmail=true")
                .content(convertObjectToJsonString(CASE_DATA))
                .header(AUTHORIZATION, USER_TOKEN)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().string("{}"));

        verify(emailClient, times(1)).sendEmail(eq(SAVE_DRAFT_TEMPLATE_ID),
                eq(TEST_USER_EMAIL),
                any(), any());
    }

    private void stubCmsServerEndpoint(HttpStatus status, String body) {
        maintenanceServiceServer.stubFor(WireMock.put(CMS_CONTEXT_PATH)
                .willReturn(aResponse()
                        .withStatus(status.value())
                        .withHeader(CONTENT_TYPE, APPLICATION_JSON_UTF8_VALUE)
                        .withBody(body)));
    }
}

