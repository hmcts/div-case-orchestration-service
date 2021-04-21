package uk.gov.hmcts.reform.divorce.orchestration.functionaltest;

import com.google.common.collect.ImmutableMap;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import uk.gov.hmcts.reform.divorce.orchestration.client.EmailClient;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CaseDetails;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CcdCallbackRequest;

import java.util.Map;

import static java.util.Collections.emptyMap;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_CASE_FAMILY_MAN_ID;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_DIVORCE_UNIT_WEST_MIDLANDS;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_PETITIONER_EMAIL;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_PETITIONER_FIRST_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_PETITIONER_LAST_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.COURT_NAME_TEMPLATE_ID;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DIVORCE_UNIT_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D_8_CASE_REFERENCE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D_8_PETITIONER_EMAIL;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D_8_PETITIONER_FIRST_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D_8_PETITIONER_LAST_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NOTIFICATION_ADDRESSEE_FIRST_NAME_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NOTIFICATION_ADDRESSEE_LAST_NAME_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NOTIFICATION_EMAIL;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NOTIFICATION_REFERENCE_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.testutil.ObjectMapperTestUtil.convertObjectToJsonString;

public class ClarificationSubmittedTest extends MockedFunctionalTest {

    private static final String API_URL = "/clarification-submitted";

    private static final String DECREE_NISI_CLARIFICATION_SUBMISSION_TEMPLATE_ID = "1b59454a-af9a-4444-87a3-67f3f238db35";

    @MockBean
    private EmailClient emailClient;

    @Autowired
    private MockMvc webClient;

    @Test
    public void shouldSendEmailWhenAllGood() throws Exception {
        Map<String, Object> caseData = ImmutableMap.of(
            D_8_PETITIONER_EMAIL, TEST_PETITIONER_EMAIL,
            D_8_PETITIONER_FIRST_NAME, TEST_PETITIONER_FIRST_NAME,
            D_8_PETITIONER_LAST_NAME, TEST_PETITIONER_LAST_NAME,
            DIVORCE_UNIT_JSON_KEY, TEST_DIVORCE_UNIT_WEST_MIDLANDS,
            D_8_CASE_REFERENCE, TEST_CASE_FAMILY_MAN_ID
        );

        webClient.perform(post(API_URL)
            .content(
                convertObjectToJsonString(
                    CcdCallbackRequest.builder()
                        .caseDetails(CaseDetails.builder().caseData(caseData).build())
                        .build()
                )
            )
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk());

        verify(emailClient).sendEmail(
            eq(DECREE_NISI_CLARIFICATION_SUBMISSION_TEMPLATE_ID),
            eq(TEST_PETITIONER_EMAIL),
            eq(expectedEmailVars()),
            any()
        );
    }

    private Map<String, String> expectedEmailVars() {
        return ImmutableMap.of(
            COURT_NAME_TEMPLATE_ID, "West Midlands Regional Divorce Centre",
            NOTIFICATION_EMAIL, TEST_PETITIONER_EMAIL,
            NOTIFICATION_ADDRESSEE_FIRST_NAME_KEY, TEST_PETITIONER_FIRST_NAME,
            NOTIFICATION_ADDRESSEE_LAST_NAME_KEY, TEST_PETITIONER_LAST_NAME,
            NOTIFICATION_REFERENCE_KEY, TEST_CASE_FAMILY_MAN_ID
        );
    }

    @Test
    public void shouldReturnErrorWhenNoMandatoryDataProvided() throws Exception {
        webClient.perform(post(API_URL)
            .content(
                convertObjectToJsonString(
                    CcdCallbackRequest.builder()
                        .caseDetails(CaseDetails.builder().caseData(emptyMap()).build())
                        .build()
                )
            )
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().is5xxServerError());

        verifyNoInteractions(emailClient);
    }
}
