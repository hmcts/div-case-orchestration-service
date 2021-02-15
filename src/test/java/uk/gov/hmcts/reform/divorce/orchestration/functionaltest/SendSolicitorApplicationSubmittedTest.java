package uk.gov.hmcts.reform.divorce.orchestration.functionaltest;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import uk.gov.hmcts.reform.divorce.orchestration.client.EmailClient;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.CcdFields;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CaseDetails;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CcdCallbackRequest;

import java.util.HashMap;
import java.util.Map;

import static com.jayway.jsonpath.matchers.JsonPathMatchers.hasNoJsonPath;
import static com.jayway.jsonpath.matchers.JsonPathMatchers.isJson;
import static org.hamcrest.Matchers.allOf;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_PETITIONER_EMAIL;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_PETITIONER_FIRST_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_PETITIONER_LAST_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_RESPONDENT_EMAIL;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_RESPONDENT_FIRST_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_RESPONDENT_LAST_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_RESPONDENT_SOLICITOR_EMAIL;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_RESPONDENT_SOLICITOR_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_SOLICITOR_EMAIL;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_SOLICITOR_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.CcdFields.PETITIONER_EMAIL;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.PETITIONER_SOLICITOR_EMAIL;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.RESP_SOL_REPRESENTED;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.YES_VALUE;
import static uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor.EmailDataExtractor.CaseDataKeys.RESPONDENT_EMAIL;
import static uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor.EmailDataExtractor.CaseDataKeys.RESPONDENT_SOLICITOR_EMAIL;
import static uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor.FullNamesDataExtractor.CaseDataKeys.PETITIONER_SOLICITOR_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor.FullNamesDataExtractor.CaseDataKeys.RESPONDENT_FIRST_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor.FullNamesDataExtractor.CaseDataKeys.RESPONDENT_LAST_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor.FullNamesDataExtractor.CaseDataKeys.RESPONDENT_SOLICITOR_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.helper.GeneralEmailTaskHelper.Party.PETITIONER_SOLICITOR;
import static uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.helper.GeneralEmailTaskHelper.getExpectedNotificationTemplateVars;
import static uk.gov.hmcts.reform.divorce.orchestration.testutil.ObjectMapperTestUtil.convertObjectToJsonString;
import static uk.gov.hmcts.reform.divorce.orchestration.testutil.TaskContextHelper.context;


public class SendSolicitorApplicationSubmittedTest extends IdamTestSupport {

    private static final String API_URL = "/solicitor-update";
    private static final String VALID_PRESTATE = "SOTAgreementPayAndSubmitRequired";

    private static final String SOL_APPLICANT_APPLICATION_SUBMITTED = "1e2f2ac5-ce4c-4fce-aca0-47a876eda089";

    @Autowired
    private MockMvc webClient;

    @MockBean
    private EmailClient emailClient;

    @Test
    public void shouldSendApplicationSubmittedEmail_toPetitioner_Solicitor() throws Exception {
        Map<String, Object> caseData = buildInputCaseData();
        CcdCallbackRequest ccdCallbackRequest = buildRequest(caseData);

        setupWebClient(ccdCallbackRequest);

        verify(emailClient).sendEmail(
            eq(SOL_APPLICANT_APPLICATION_SUBMITTED),
            eq(TEST_SOLICITOR_EMAIL),
            eq(getExpectedNotificationTemplateVars(PETITIONER_SOLICITOR, context(), caseData)),
            any()
        );
    }

    private void setupWebClient(CcdCallbackRequest ccdCallbackRequest) throws Exception {
        webClient.perform(post(API_URL)
            .contentType(MediaType.APPLICATION_JSON)
            .header(AUTHORIZATION, AUTH_TOKEN)
            .content(convertObjectToJsonString(ccdCallbackRequest)))
            .andExpect(status().isOk())
            .andExpect(content().string(allOf(isJson(), hasNoJsonPath("$.errors"))));
    }

    public static Map<String, Object> buildInputCaseData() {
        return getPetitionerData(true);
    }

    public static Map<String, Object> getPetitionerData(Boolean isRepresented) {
        Map<String, Object> caseData = new HashMap<>();
        caseData.put(CcdFields.PETITIONER_FIRST_NAME, TEST_PETITIONER_FIRST_NAME);
        caseData.put(CcdFields.PETITIONER_LAST_NAME, TEST_PETITIONER_LAST_NAME);
        caseData.put(PETITIONER_EMAIL, TEST_PETITIONER_EMAIL);

        if (isRepresented) {
            caseData.put(PETITIONER_SOLICITOR_NAME, TEST_SOLICITOR_NAME);
            caseData.put(PETITIONER_SOLICITOR_EMAIL, TEST_SOLICITOR_EMAIL);
            getRespondentData(false);
        }

        return caseData;
    }

    public static Map<String, Object> getRespondentData(Boolean isRepresented) {
        Map<String, Object> caseData = new HashMap<>();
        caseData.put(RESPONDENT_FIRST_NAME, TEST_RESPONDENT_FIRST_NAME);
        caseData.put(RESPONDENT_LAST_NAME, TEST_RESPONDENT_LAST_NAME);
        caseData.put(RESPONDENT_EMAIL, TEST_RESPONDENT_EMAIL);

        if (isRepresented) {
            caseData.put(RESP_SOL_REPRESENTED, YES_VALUE);
            caseData.put(RESPONDENT_SOLICITOR_NAME, TEST_RESPONDENT_SOLICITOR_NAME);
            caseData.put(RESPONDENT_SOLICITOR_EMAIL, TEST_RESPONDENT_SOLICITOR_EMAIL);
            getPetitionerData(false);
        }

        return caseData;
    }

    public static CcdCallbackRequest buildRequest(Map<String, Object> caseData) {
        return new CcdCallbackRequest(
            AUTH_TOKEN,
            "",
            CaseDetails.builder()
                .state(VALID_PRESTATE)
                .caseData(caseData)
                .caseId(TEST_CASE_ID)
                .build()
        );
    }
}
