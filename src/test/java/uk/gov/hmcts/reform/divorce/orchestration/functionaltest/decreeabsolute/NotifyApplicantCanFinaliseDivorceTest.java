package uk.gov.hmcts.reform.divorce.orchestration.functionaltest.decreeabsolute;

import org.junit.Test;
import org.mockito.internal.hamcrest.HamcrestArgumentMatcher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import uk.gov.hmcts.reform.divorce.orchestration.client.EmailClient;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CcdCallbackRequest;
import uk.gov.hmcts.reform.divorce.orchestration.functionaltest.MockedFunctionalTest;

import java.util.Map;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.hasEntry;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NOTIFICATION_ADDRESSEE_FIRST_NAME_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NOTIFICATION_ADDRESSEE_LAST_NAME_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NOTIFICATION_CASE_NUMBER_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.testutil.ObjectMapperTestUtil.convertObjectToJsonString;
import static uk.gov.hmcts.reform.divorce.orchestration.testutil.ObjectMapperTestUtil.getJsonFromResourceFile;

public class NotifyApplicantCanFinaliseDivorceTest extends MockedFunctionalTest {

    private static final String API_URL = "/handle-make-case-eligible-for-da-submitted-callback";
    private static final String NOTIFICATION_TEMPLATE_ID = "71fd2e7e-42dc-4dcf-a9bb-007ae9d4b27f";

    @MockBean
    private EmailClient mockClient;

    @Autowired
    private MockMvc webClient;

    @Test
    public void shouldReturnCaseData_WhenCalling_GrantDecreeAbsolute() throws Exception {
        CcdCallbackRequest ccdCallbackRequest = getJsonFromResourceFile(
            "/jsonExamples/payloads/genericPetitionerData.json", CcdCallbackRequest.class);
        Map<String, Object> caseData = ccdCallbackRequest.getCaseDetails().getCaseData();

        webClient.perform(post(API_URL)
            .content(convertObjectToJsonString(ccdCallbackRequest))
            .contentType(APPLICATION_JSON))
            .andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString();

        verify(mockClient).sendEmail(eq(NOTIFICATION_TEMPLATE_ID),
            eq("petitioner@divorce.co.uk"),
            argThat(new HamcrestArgumentMatcher<Map<String, Object>>(allOf(
                hasEntry(NOTIFICATION_CASE_NUMBER_KEY, "LV80D85000"),
                hasEntry(NOTIFICATION_ADDRESSEE_FIRST_NAME_KEY, "Ted"),
                hasEntry(NOTIFICATION_ADDRESSEE_LAST_NAME_KEY, "Jones")
            ))),
            any());
    }

}