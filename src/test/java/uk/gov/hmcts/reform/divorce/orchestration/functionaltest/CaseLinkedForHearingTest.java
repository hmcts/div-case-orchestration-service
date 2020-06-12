package uk.gov.hmcts.reform.divorce.orchestration.functionaltest;

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
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.AUTHORIZATION_HEADER;
import static uk.gov.hmcts.reform.divorce.orchestration.testutil.ObjectMapperTestUtil.convertObjectToJsonString;
import static uk.gov.hmcts.reform.divorce.orchestration.testutil.ObjectMapperTestUtil.getJsonFromResourceFile;

public class CaseLinkedForHearingTest extends MockedFunctionalTest {

    private static final String API_URL = "/case-linked-for-hearing";
    private static final String PETITIONER_COE_NOTIFICATION_EMAIL_TEMPLATE_ID = "9937c8bc-dc7a-4210-a25b-20aceb82d48d";
    private static final String RESPONDENT_COE_NOTIFICATION_EMAIL_TEMPLATE_ID = "80b986e1-056b-4577-a343-bb2e72e2a3f0";

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
            .header(AUTHORIZATION_HEADER, AUTH_TOKEN)
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
}
