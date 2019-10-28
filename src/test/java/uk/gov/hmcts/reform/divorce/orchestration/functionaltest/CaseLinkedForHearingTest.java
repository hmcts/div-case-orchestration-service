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
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CO_RESP_EMAIL_ADDRESS;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D8_REASON_FOR_DIVORCE_ADULTERY_3RD_PARTY_FNAME;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D8_REASON_FOR_DIVORCE_ADULTERY_3RD_PARTY_LNAME;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D_8_REASON_FOR_DIVORCE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.facts.DivorceFacts.ADULTERY;
import static uk.gov.hmcts.reform.divorce.orchestration.testutil.ObjectMapperTestUtil.convertObjectToJsonString;
import static uk.gov.hmcts.reform.divorce.orchestration.testutil.ObjectMapperTestUtil.getJsonFromResourceFile;

public class CaseLinkedForHearingTest extends MockedFunctionalTest {

    private static final String API_URL = "/case-linked-for-hearing";
    private static final String PETITIONER_COE_NOTIFICATION_EMAIL_TEMPLATE_ID = "9937c8bc-dc7a-4210-a25b-20aceb82d48d";
    private static final String RESPONDENT_COE_NOTIFICATION_EMAIL_TEMPLATE_ID = "80b986e1-056b-4577-a343-bb2e72e2a3f0";
    private static final String CO_RESPONDENT_COE_NOTIFICATION_EMAIL_TEMPLATE_ID = "b506dfcb-922c-4ce5-8f4c-841a776baaf9";

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
            .content(convertObjectToJsonString(ccdCallbackRequest))
            .contentType(APPLICATION_JSON)
            .accept(APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content().json(convertObjectToJsonString(expectedResponse)))
            .andExpect(content().string(allOf(
                isJson(),
                hasJsonPath("$.errors", nullValue())
            )));

        verify(mockClient).sendEmail(eq(PETITIONER_COE_NOTIFICATION_EMAIL_TEMPLATE_ID),
            eq("petitioner@justice.uk"),
            any(), any());

        verify(mockClient).sendEmail(eq(RESPONDENT_COE_NOTIFICATION_EMAIL_TEMPLATE_ID),
                eq("respondent@justice.uk"),
                any(), any());

        verify(mockClient, never()).sendEmail(eq(CO_RESPONDENT_COE_NOTIFICATION_EMAIL_TEMPLATE_ID),
            any(), any(), any());
    }

    @Test
    public void whenCallbackEndpointIsCalledWithCoRespondent_ThenResponseIsSuccessful() throws Exception {
        CcdCallbackRequest ccdCallbackRequest = getJsonFromResourceFile(
            "/jsonExamples/payloads/caseListedForHearing.json", CcdCallbackRequest.class);
        Map<String, Object> caseData = ccdCallbackRequest.getCaseDetails().getCaseData();
        caseData.put(D_8_REASON_FOR_DIVORCE, ADULTERY);
        caseData.put(CO_RESP_EMAIL_ADDRESS, "co-respondent@justice.uk");
        caseData.put(D8_REASON_FOR_DIVORCE_ADULTERY_3RD_PARTY_FNAME, "Alphonse");
        caseData.put(D8_REASON_FOR_DIVORCE_ADULTERY_3RD_PARTY_LNAME, "Mango");
        
        CcdCallbackResponse expectedResponse = CcdCallbackResponse.builder()
            .data(caseData)
            .build();

        webClient.perform(post(API_URL)
            .content(convertObjectToJsonString(ccdCallbackRequest))
            .contentType(APPLICATION_JSON)
            .accept(APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content().json(convertObjectToJsonString(expectedResponse)))
            .andExpect(content().string(allOf(
                isJson(),
                hasJsonPath("$.errors", nullValue())
            )));

        verify(mockClient).sendEmail(eq(PETITIONER_COE_NOTIFICATION_EMAIL_TEMPLATE_ID),
            eq("petitioner@justice.uk"),
            any(), any());

        verify(mockClient).sendEmail(eq(RESPONDENT_COE_NOTIFICATION_EMAIL_TEMPLATE_ID),
            eq("respondent@justice.uk"),
            any(), any());

        verify(mockClient).sendEmail(eq(CO_RESPONDENT_COE_NOTIFICATION_EMAIL_TEMPLATE_ID),
            eq("co-respondent@justice.uk"),
            any(), any());
    }

}