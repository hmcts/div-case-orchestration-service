package uk.gov.hmcts.reform.divorce.orchestration.functionaltest;

import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import uk.gov.hmcts.reform.divorce.orchestration.client.EmailClient;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CcdCallbackRequest;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CcdCallbackResponse;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.idam.Pin;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.idam.PinRequest;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.RespondentAosPackPrinter;
import uk.gov.hmcts.reform.divorce.orchestration.workflows.CcdCallbackBulkPrintWorkflow;

import java.util.HashMap;
import java.util.Map;

import static com.jayway.jsonpath.matchers.JsonPathMatchers.hasJsonPath;
import static com.jayway.jsonpath.matchers.JsonPathMatchers.isJson;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.BEARER_AUTH_TOKEN_1;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_LETTER_HOLDER_ID_CODE;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_PIN_CODE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.RECEIVED_AOS_FROM_RESP;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.RECEIVED_AOS_FROM_RESP_DATE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.RESPONDENT_EMAIL_ADDRESS;
import static uk.gov.hmcts.reform.divorce.orchestration.testutil.ObjectMapperTestUtil.convertObjectToJsonString;
import static uk.gov.hmcts.reform.divorce.orchestration.testutil.ObjectMapperTestUtil.getJsonFromResourceFile;

public class RespondentSolicitorNominatedITest extends IdamTestSupport {

    private static final String API_URL = "/aos-solicitor-nominated";

    @Autowired
    private MockMvc webClient;

    @Test
    public void givenRespondentSolicitorNominated_whenCallbackCalled_linkingFieldsAreReset() throws Exception {

        CcdCallbackRequest ccdCallbackRequest = getJsonFromResourceFile(
                "/jsonExamples/payloads/aosSolicitorNominated.json", CcdCallbackRequest.class);
        Map<String, Object> caseData = ccdCallbackRequest.getCaseDetails().getCaseData();
        Map<String, Object> expectedCaseData = new HashMap<>(caseData);
        expectedCaseData.put(RECEIVED_AOS_FROM_RESP, null);
        expectedCaseData.put(RECEIVED_AOS_FROM_RESP_DATE, null);
        expectedCaseData.put(RESPONDENT_EMAIL_ADDRESS, null);

        final PinRequest pinRequest = PinRequest.builder()
            .firstName("")
            .lastName("")
            .build();

        final Pin pin = Pin.builder().pin(TEST_PIN_CODE).userId(TEST_LETTER_HOLDER_ID_CODE).build();

        stubSignIn();
        stubPinDetailsEndpoint(BEARER_AUTH_TOKEN_1, pinRequest, pin);

        CcdCallbackResponse expected = CcdCallbackResponse.builder()
                .data(expectedCaseData)
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
    }
}
