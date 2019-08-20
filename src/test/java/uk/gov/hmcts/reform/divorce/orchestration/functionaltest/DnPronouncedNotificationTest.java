package uk.gov.hmcts.reform.divorce.orchestration.functionaltest;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import uk.gov.hmcts.reform.divorce.orchestration.client.EmailClient;
import uk.gov.hmcts.reform.divorce.orchestration.config.EmailTemplatesConfig;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CaseDetails;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CcdCallbackRequest;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CcdCallbackResponse;

import java.util.HashMap;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_CASE_FAMILY_MAN_ID;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_FIRST_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_LAST_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_PETITIONER_EMAIL;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_RESPONDENT_EMAIL;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_STATE;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_USER_EMAIL;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CO_RESP_EMAIL_ADDRESS;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D8_REASON_FOR_DIVORCE_ADULTERY_3RD_PARTY_FNAME;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D8_REASON_FOR_DIVORCE_ADULTERY_3RD_PARTY_LNAME;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D_8_CASE_REFERENCE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D_8_PETITIONER_EMAIL;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D_8_PETITIONER_FIRST_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D_8_PETITIONER_LAST_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.RESPONDENT_EMAIL_ADDRESS;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.RESP_FIRST_NAME_CCD_FIELD;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.RESP_LAST_NAME_CCD_FIELD;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.WHO_PAYS_CCD_CODE_FOR_BOTH;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.WHO_PAYS_CCD_CODE_FOR_CORESPONDENT;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.WHO_PAYS_CCD_CODE_FOR_RESPONDENT;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.WHO_PAYS_COSTS_CCD_FIELD;
import static uk.gov.hmcts.reform.divorce.orchestration.testutil.ObjectMapperTestUtil.convertObjectToJsonString;

@RunWith(SpringRunner.class)
public class DnPronouncedNotificationTest extends MockedFunctionalTest {
    private static final String API_URL = "/dn-pronounced";
    private static final String GENERIC_UPDATE_TEMPLATE_ID = "6ee6ec29-5e88-4516-99cb-2edc30256575";
    private static final String GENERIC_UPDATE_RESPONDENT_TEMPLATE_ID = "dc47109d-95f0-4a55-a11f-de41a5201cbc";

    private Map<String, Object> caseData;
    private CcdCallbackRequest ccdCallbackRequest;

    @MockBean
    private EmailClient emailClient;

    @Autowired
    private EmailTemplatesConfig emailTemplatesConfig;

    @Autowired
    private MockMvc webClient;

    @Before
    public void setup() {
        caseData = new HashMap<>();
        caseData.put(D_8_CASE_REFERENCE, TEST_CASE_FAMILY_MAN_ID);
        caseData.put(D_8_PETITIONER_EMAIL, TEST_PETITIONER_EMAIL);
        caseData.put(RESPONDENT_EMAIL_ADDRESS, TEST_RESPONDENT_EMAIL);
        caseData.put(CO_RESP_EMAIL_ADDRESS, TEST_USER_EMAIL);
        caseData.put(D_8_PETITIONER_FIRST_NAME, TEST_FIRST_NAME);
        caseData.put(D_8_PETITIONER_LAST_NAME, TEST_LAST_NAME);
        caseData.put(RESP_FIRST_NAME_CCD_FIELD, TEST_FIRST_NAME);
        caseData.put(RESP_LAST_NAME_CCD_FIELD, TEST_LAST_NAME);
        caseData.put(D8_REASON_FOR_DIVORCE_ADULTERY_3RD_PARTY_FNAME, TEST_FIRST_NAME);
        caseData.put(D8_REASON_FOR_DIVORCE_ADULTERY_3RD_PARTY_LNAME, TEST_LAST_NAME);

        CaseDetails caseDetails = CaseDetails.builder()
                .caseId(TEST_CASE_ID)
                .state(TEST_STATE)
                .build();

        ccdCallbackRequest = CcdCallbackRequest.builder()
                .caseDetails(caseDetails)
                .build();
    }

    @Test
    public void givenCaseDataWithNoPaysCosts_whenDnPronounced_thenSendGenericNotifications() throws Exception {
        ccdCallbackRequest.getCaseDetails().setCaseData(caseData);

        CcdCallbackResponse expected = CcdCallbackResponse.builder()
                .data(caseData)
                .build();

        webClient.perform(post(API_URL)
                .content(convertObjectToJsonString(ccdCallbackRequest))
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().json(convertObjectToJsonString(expected)));

        verify(emailClient, times(1)).sendEmail(eq(GENERIC_UPDATE_TEMPLATE_ID),
                eq(TEST_PETITIONER_EMAIL),
                any(), any());
        verify(emailClient, times(1)).sendEmail(eq(GENERIC_UPDATE_RESPONDENT_TEMPLATE_ID),
                eq(TEST_RESPONDENT_EMAIL),
                any(), any());
        verify(emailClient, never()).sendEmail(eq(GENERIC_UPDATE_RESPONDENT_TEMPLATE_ID),
                eq(TEST_USER_EMAIL),
                any(), any());
    }

    @Test
    public void givenCaseDataWithRespondentPaysCosts_whenDnPronounced_thenSendGenericNotifications() throws Exception {
        caseData.put(WHO_PAYS_COSTS_CCD_FIELD, WHO_PAYS_CCD_CODE_FOR_RESPONDENT);
        ccdCallbackRequest.getCaseDetails().setCaseData(caseData);

        CcdCallbackResponse expected = CcdCallbackResponse.builder()
                .data(caseData)
                .build();

        webClient.perform(post(API_URL)
                .content(convertObjectToJsonString(ccdCallbackRequest))
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().json(convertObjectToJsonString(expected)));

        verify(emailClient, times(1)).sendEmail(eq(GENERIC_UPDATE_TEMPLATE_ID),
                eq(TEST_PETITIONER_EMAIL),
                any(), any());
        verify(emailClient, times(1)).sendEmail(eq(GENERIC_UPDATE_RESPONDENT_TEMPLATE_ID),
                eq(TEST_RESPONDENT_EMAIL),
                any(), any());
        verify(emailClient, never()).sendEmail(eq(GENERIC_UPDATE_RESPONDENT_TEMPLATE_ID),
                eq(TEST_USER_EMAIL),
                any(), any());
    }

    @Test
    public void givenCaseDataWithCoRespondentPaysCosts_whenDnPronounced_thenSendGenericNotifications() throws Exception {
        caseData.put(WHO_PAYS_COSTS_CCD_FIELD, WHO_PAYS_CCD_CODE_FOR_CORESPONDENT);
        ccdCallbackRequest.getCaseDetails().setCaseData(caseData);

        CcdCallbackResponse expected = CcdCallbackResponse.builder()
                .data(caseData)
                .build();

        webClient.perform(post(API_URL)
                .content(convertObjectToJsonString(ccdCallbackRequest))
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().json(convertObjectToJsonString(expected)));

        verify(emailClient, times(1)).sendEmail(eq(GENERIC_UPDATE_TEMPLATE_ID),
                eq(TEST_PETITIONER_EMAIL),
                any(), any());
        verify(emailClient, times(1)).sendEmail(eq(GENERIC_UPDATE_RESPONDENT_TEMPLATE_ID),
                eq(TEST_RESPONDENT_EMAIL),
                any(), any());
        verify(emailClient, times(1)).sendEmail(eq(GENERIC_UPDATE_RESPONDENT_TEMPLATE_ID),
                eq(TEST_USER_EMAIL),
                any(), any());
    }

    @Test
    public void givenCaseDataWithBothPaysCosts_whenDnPronounced_thenSendGenericNotifications() throws Exception {
        caseData.put(WHO_PAYS_COSTS_CCD_FIELD, WHO_PAYS_CCD_CODE_FOR_BOTH);
        ccdCallbackRequest.getCaseDetails().setCaseData(caseData);

        CcdCallbackResponse expected = CcdCallbackResponse.builder()
                .data(caseData)
                .build();

        webClient.perform(post(API_URL)
                .content(convertObjectToJsonString(ccdCallbackRequest))
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().json(convertObjectToJsonString(expected)));

        verify(emailClient, times(1)).sendEmail(eq(GENERIC_UPDATE_TEMPLATE_ID),
                eq(TEST_PETITIONER_EMAIL),
                any(), any());
        verify(emailClient, times(1)).sendEmail(eq(GENERIC_UPDATE_RESPONDENT_TEMPLATE_ID),
                eq(TEST_RESPONDENT_EMAIL),
                any(), any());
        verify(emailClient, times(1)).sendEmail(eq(GENERIC_UPDATE_RESPONDENT_TEMPLATE_ID),
                eq(TEST_USER_EMAIL),
                any(), any());
    }
}
