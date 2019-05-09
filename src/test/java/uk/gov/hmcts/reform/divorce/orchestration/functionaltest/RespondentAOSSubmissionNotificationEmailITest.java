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
import uk.gov.hmcts.reform.divorce.orchestration.OrchestrationServiceApplication;
import uk.gov.hmcts.reform.divorce.orchestration.client.EmailClient;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CcdCallbackRequest;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CcdCallbackResponse;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.RESP_WILL_DEFEND_DIVORCE;
import static uk.gov.hmcts.reform.divorce.orchestration.testutil.ObjectMapperTestUtil.convertObjectToJsonString;
import static uk.gov.hmcts.reform.divorce.orchestration.testutil.ObjectMapperTestUtil.getJsonFromResourceFile;

@RunWith(SpringRunner.class)
@ContextConfiguration(classes = OrchestrationServiceApplication.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@PropertySource(value = "classpath:application.yml")
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
public class RespondentAOSSubmissionNotificationEmailITest {

    private static final String API_URL = "/aos-submitted";
    private static final String DEFENDED_DIVORCE_EMAIL_TEMPLATE_ID = "eac41143-b296-4879-ba60-a0ea6f97c757";
    private static final String UNDEFENDED_DIVORCE_EMAIL_TEMPLATE_ID = "277fd3f3-2fdb-4c79-9354-1b3db8d44cca";

    @Autowired
    private MockMvc webClient;

    @MockBean
    private EmailClient mockClient;

    @Test
    public void testResponseHasDataAndNoErrors_whenEmailCanBeSent_forDefendedDivorce() throws Exception {

        CcdCallbackRequest ccdCallbackRequest = getJsonFromResourceFile(
                "/jsonExamples/payloads/respondentAcknowledgesServiceDefendingDivorce.json", CcdCallbackRequest.class);
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
                )));

        verify(mockClient).sendEmail(eq(DEFENDED_DIVORCE_EMAIL_TEMPLATE_ID),
                eq("respondent@divorce.co.uk"),
                any(), any());
    }

    @Test
    public void testResponseHasDataAndNoErrors_WhenEmailCanBeSent_ForUndefendedDivorce() throws Exception {
        CcdCallbackRequest ccdCallbackRequest = getJsonFromResourceFile(
                "/jsonExamples/payloads/respondentAcknowledgesServiceNotDefendingDivorce.json", CcdCallbackRequest.class);
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
                )));

        verify(mockClient).sendEmail(eq(UNDEFENDED_DIVORCE_EMAIL_TEMPLATE_ID),
                eq("respondent@divorce.co.uk"),
                any(), any());
    }

    @Test
    public void testResponseHasDataAndNoErrors_WhenEmailCanBeSent_ForUndefendedButNoAdmitDivorce() throws Exception {
        CcdCallbackRequest ccdCallbackRequest = getJsonFromResourceFile(
                "/jsonExamples/payloads/respondentAcknowledgesServiceNotDefendingNotAdmittingDivorce.json", CcdCallbackRequest.class);
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
                )));

        verify(mockClient).sendEmail(eq(UNDEFENDED_DIVORCE_EMAIL_TEMPLATE_ID),
                eq("respondent@divorce.co.uk"),
                any(), any());
    }

    @Test
    public void testResponseHasValidationErrors_WhenItIsNotClearIfDivorceWillBeDefended() throws Exception {
        CcdCallbackRequest ccdCallbackRequest = getJsonFromResourceFile(
                "/jsonExamples/payloads/unclearAcknowledgementOfService.json", CcdCallbackRequest.class);

        webClient.perform(post(API_URL)
                .content(convertObjectToJsonString(ccdCallbackRequest))
                .contentType(APPLICATION_JSON)
                .accept(APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().string(allOf(
                        isJson(),
                        hasJsonPath("$.data", is(nullValue())),
                        hasJsonPath("$.errors", hasItem(String.format("%s field doesn't contain a valid value: null",
                            RESP_WILL_DEFEND_DIVORCE)))
                )));
    }

    @Test
    public void testResponseHasValidationErrors_WhenCaseIdIsMissing_ForDefendedDivorce() throws Exception {
        CcdCallbackRequest ccdCallbackRequest = getJsonFromResourceFile(
                "/jsonExamples/payloads/defendedDivorceAOSMissingCaseId.json", CcdCallbackRequest.class);

        webClient.perform(post(API_URL)
                .content(convertObjectToJsonString(ccdCallbackRequest))
                .contentType(APPLICATION_JSON)
                .accept(APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().string(allOf(
                        isJson(),
                        hasJsonPath("$.data", is(nullValue())),
                        hasJsonPath("$.errors",
                                hasItem("Could not evaluate value of mandatory property \"D8caseReference\""))
                )));
    }

    @Test
    public void testResponseHasValidationErrors_WhenFieldsAreMissing_ForDefendedDivorce() throws Exception {
        CcdCallbackRequest ccdCallbackRequest = getJsonFromResourceFile(
                "/jsonExamples/payloads/defendedDivorceAOSMissingFields.json", CcdCallbackRequest.class);

        webClient.perform(post(API_URL)
                .content(convertObjectToJsonString(ccdCallbackRequest))
                .contentType(APPLICATION_JSON)
                .accept(APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().string(allOf(
                        isJson(),
                        hasJsonPath("$.data", is(nullValue())),
                        hasJsonPath("$.errors",
                                hasItem("Could not evaluate value of mandatory property \"D8InferredPetitionerGender\"")
                        )
                )));
    }

    @Test
    public void testResponseHasValidationErrors_WhenFieldsAreMissing_ForUndefendedDivorce() throws Exception {
        CcdCallbackRequest ccdCallbackRequest = getJsonFromResourceFile(
                "/jsonExamples/payloads/undefendedDivorceAOSMissingFields.json", CcdCallbackRequest.class);

        webClient.perform(post(API_URL)
                .content(convertObjectToJsonString(ccdCallbackRequest))
                .contentType(APPLICATION_JSON)
                .accept(APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().string(allOf(
                        isJson(),
                        hasJsonPath("$.data", is(nullValue())),
                        hasJsonPath("$.errors",
                                hasItem("Could not evaluate value of mandatory property \"D8DivorceUnit\"")
                        )
                )));
    }

    @Test
    public void testResponseHasError_IfEmailCannotBeSent() throws Exception {
        CcdCallbackRequest ccdCallbackRequest = getJsonFromResourceFile(
                "/jsonExamples/payloads/respondentAcknowledgesServiceDefendingDivorce.json", CcdCallbackRequest.class);
        doThrow(NotificationClientException.class).when(mockClient).sendEmail(any(), any(), any(), any());

        webClient.perform(post(API_URL)
                .content(convertObjectToJsonString(ccdCallbackRequest))
                .contentType(APPLICATION_JSON)
                .accept(APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().string(allOf(
                        isJson(),
                        hasJsonPath("$.data", is(nullValue())),
                        hasJsonPath("$.errors", hasItem("Failed to send e-mail"))
                )));
    }

}
