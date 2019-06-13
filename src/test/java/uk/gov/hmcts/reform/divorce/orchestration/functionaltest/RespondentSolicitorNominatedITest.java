package uk.gov.hmcts.reform.divorce.orchestration.functionaltest;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.PropertySource;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import uk.gov.hmcts.reform.divorce.orchestration.OrchestrationServiceApplication;
import uk.gov.hmcts.reform.divorce.orchestration.client.EmailClient;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CcdCallbackRequest;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CcdCallbackResponse;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.idam.Pin;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.idam.PinRequest;
import uk.gov.service.notify.NotificationClientException;

import java.util.Map;

import static com.jayway.jsonpath.matchers.JsonPathMatchers.hasJsonPath;
import static com.jayway.jsonpath.matchers.JsonPathMatchers.isJson;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.core.Is.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.BEARER_AUTH_TOKEN_1;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_LETTER_HOLDER_ID_CODE;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_PIN_CODE;
import static uk.gov.hmcts.reform.divorce.orchestration.testutil.ObjectMapperTestUtil.convertObjectToJsonString;
import static uk.gov.hmcts.reform.divorce.orchestration.testutil.ObjectMapperTestUtil.getJsonFromResourceFile;

@RunWith(SpringRunner.class)
@ContextConfiguration(classes = OrchestrationServiceApplication.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@PropertySource(value = "classpath:application.yml")
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
public class RespondentSolicitorNominatedITest extends IdamTestSupport {

    private static final String API_URL = "/aos-solicitor-nominated";
    private static final String SOLICITOR_AOS_INVIATION_LETTER_ID = "a193f039-2252-425d-861c-6dba255b7e6e";

    @Autowired
    private MockMvc webClient;

    @MockBean
    private EmailClient mockEmailClient;

    @Test
    public void testResponseHasDataAndNoErrors_whenSolicitorIsNominated_thenEmailIsSentToSolicitor() throws Exception {
        final PinRequest pinRequest = PinRequest.builder()
                        .firstName("")
                        .lastName("")
                        .build();

        final Pin pin = Pin.builder().pin(TEST_PIN_CODE).userId(TEST_LETTER_HOLDER_ID_CODE).build();

        stubSignIn();
        stubPinDetailsEndpoint(BEARER_AUTH_TOKEN_1, pinRequest, pin);

        CcdCallbackRequest ccdCallbackRequest = getJsonFromResourceFile(
                "/jsonExamples/payloads/aosSolicitorNominated.json", CcdCallbackRequest.class);
        Map<String, Object> caseData = ccdCallbackRequest.getCaseDetails().getCaseData();
        CcdCallbackResponse expected = CcdCallbackResponse.builder()
                .data(caseData)
                .build();

        webClient.perform(MockMvcRequestBuilders.post(API_URL)
                .content(convertObjectToJsonString(ccdCallbackRequest))
                .contentType(APPLICATION_JSON)
                .accept(APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().json(convertObjectToJsonString(expected)))
                .andExpect(content().string(allOf(
                        isJson(),
                        hasJsonPath("$.errors", nullValue())
                )));

        verify(mockEmailClient).sendEmail(eq(SOLICITOR_AOS_INVIATION_LETTER_ID),
                eq("solicitor@localhost.local"),
                any(), any());
    }

    @Test
    public void testResponseHasErrors_whenSolicitorIsNominated_andSendingEmailFails() throws Exception {
        final PinRequest pinRequest = PinRequest.builder()
                .firstName("")
                .lastName("")
                .build();

        final Pin pin = Pin.builder().pin(TEST_PIN_CODE).userId(TEST_LETTER_HOLDER_ID_CODE).build();
        doThrow(new NotificationClientException("something bad happened")).when(mockEmailClient).sendEmail(any(), any(), any(), any());

        stubSignIn();
        stubPinDetailsEndpoint(BEARER_AUTH_TOKEN_1, pinRequest, pin);

        CcdCallbackRequest ccdCallbackRequest = getJsonFromResourceFile(
                "/jsonExamples/payloads/aosSolicitorNominated.json", CcdCallbackRequest.class);

        webClient.perform(MockMvcRequestBuilders.post(API_URL)
                .content(convertObjectToJsonString(ccdCallbackRequest))
                .contentType(APPLICATION_JSON)
                .accept(APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().string(allOf(
                        isJson(),
                        hasJsonPath("$.data", is(nullValue())),
                        hasJsonPath("$.errors", hasItem("Failed to send e-mail"))
                )));

        verify(mockEmailClient).sendEmail(eq(SOLICITOR_AOS_INVIATION_LETTER_ID),
                eq("solicitor@localhost.local"),
                any(), any());
    }
}
