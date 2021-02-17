package uk.gov.hmcts.reform.divorce.orchestration.functionaltest.alternativeservice;

import com.google.common.collect.ImmutableMap;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import uk.gov.hmcts.reform.divorce.orchestration.client.EmailClient;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CaseDetails;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CcdCallbackRequest;
import uk.gov.hmcts.reform.divorce.orchestration.functionaltest.IdamTestSupport;
import uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor.AddresseeDataExtractorTest;

import java.util.HashMap;
import java.util.Map;

import static com.jayway.jsonpath.matchers.JsonPathMatchers.hasNoJsonPath;
import static com.jayway.jsonpath.matchers.JsonPathMatchers.isJson;
import static org.hamcrest.Matchers.allOf;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_CASE_FAMILY_MAN_ID;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_PETITIONER_EMAIL;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_PETITIONER_FIRST_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_PETITIONER_LAST_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_RELATIONSHIP_HUSBAND;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_RESPONDENT_FIRST_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_RESPONDENT_LAST_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_SOLICITOR_EMAIL;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_SOLICITOR_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.CcdFields.SERVED_BY_PROCESS_SERVER;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D_8_CASE_REFERENCE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D_8_INFERRED_PETITIONER_GENDER;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NOTIFICATION_CASE_NUMBER_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NOTIFICATION_CCD_REFERENCE_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NOTIFICATION_HUSBAND_OR_WIFE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NOTIFICATION_PET_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NOTIFICATION_RESP_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NOTIFICATION_SOLICITOR_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.PETITIONER_SOLICITOR_EMAIL;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.PETITIONER_SOLICITOR_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.YES_VALUE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.Gender.MALE;
import static uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor.EmailDataExtractor.CaseDataKeys.PETITIONER_EMAIL;
import static uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor.FullNamesDataExtractor.CaseDataKeys.PETITIONER_FIRST_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor.FullNamesDataExtractor.CaseDataKeys.PETITIONER_LAST_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor.FullNamesDataExtractor.CaseDataKeys.RESPONDENT_FIRST_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor.FullNamesDataExtractor.CaseDataKeys.RESPONDENT_LAST_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.testutil.ObjectMapperTestUtil.convertObjectToJsonString;

public class AosNotReceivedForProcessServerTest extends IdamTestSupport {

    private static final String API_URL = "/aos-not-received-for-process-server";

    private static final String CITIZEN_AWAITING_DN_SERVED_BY_PROCESS = "7564ad2a-e746-48f3-9f6b-12bd7b5cfccf";
    private static final String PET_SOL_AWAITING_DN_SERVED_BY_PROCESS = "0560da04-0c7b-460a-8909-1e693ae78fe5";

    @MockBean
    private EmailClient emailClient;

    @Autowired
    private MockMvc webClient;

    @Test
    public void petitionerRepresentedThenSendEmailToPetitionerSolicitor() throws Exception {
        callEndpointWithData(buildCaseDataForSolicitor(SERVED_BY_PROCESS_SERVER));

        verify(emailClient).sendEmail(
            eq(PET_SOL_AWAITING_DN_SERVED_BY_PROCESS),
            eq(TEST_SOLICITOR_EMAIL),
            eq(getExpectedSolicitorTemplateVars()),
            any()
        );
    }

    @Test
    public void petitionerNotRepresentedThenSendEmailToPetitioner() throws Exception {
        callEndpointWithData(buildCaseDataForPetitioner(SERVED_BY_PROCESS_SERVER));

        verify(emailClient).sendEmail(
            eq(CITIZEN_AWAITING_DN_SERVED_BY_PROCESS),
            eq(TEST_PETITIONER_EMAIL),
            eq(getExpectedPetitionerTemplateVars()),
            any()
        );
    }

    @Test
    public void servedByProcessFieldIsNotYesThenDontSendAnyEmail() throws Exception {
        callEndpointWithData(new HashMap<>());

        verifyNoInteractions(emailClient);
    }

    private void callEndpointWithData(Map<String, Object> stringObjectMap) throws Exception {
        CcdCallbackRequest input = new CcdCallbackRequest(
            AUTH_TOKEN,
            "aosNotReceivedForProcessServer",
            CaseDetails.builder().caseData(stringObjectMap).caseId(TEST_CASE_ID).build()
        );

        webClient.perform(post(API_URL)
            .content(convertObjectToJsonString(input))
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content().string(allOf(
                isJson(),
                hasNoJsonPath("$.errors"),
                hasNoJsonPath("$.warnings")
            )));
    }

    static Map<String, Object> buildCaseDataForSolicitor(String field) {
        Map<String, Object> caseData = buildCommonCaseData(field);

        caseData.put(PETITIONER_SOLICITOR_NAME, TEST_SOLICITOR_NAME);
        caseData.put(PETITIONER_SOLICITOR_EMAIL, TEST_SOLICITOR_EMAIL);

        return caseData;
    }

    static Map<String, Object> buildCaseDataForPetitioner(String field) {
        Map<String, Object> caseData = buildCommonCaseData(field);

        caseData.put(D_8_CASE_REFERENCE, TEST_CASE_FAMILY_MAN_ID);
        caseData.put(D_8_INFERRED_PETITIONER_GENDER, MALE.getValue());

        caseData.put(PETITIONER_EMAIL, TEST_PETITIONER_EMAIL);

        return caseData;
    }

    static Map<String, Object> buildCommonCaseData(String field) {
        Map<String, Object> caseData = AddresseeDataExtractorTest.buildCaseDataWithRespondent();

        caseData.put(field, YES_VALUE);

        caseData.put(PETITIONER_FIRST_NAME, TEST_PETITIONER_FIRST_NAME);
        caseData.put(PETITIONER_LAST_NAME, TEST_PETITIONER_LAST_NAME);
        caseData.put(RESPONDENT_FIRST_NAME, TEST_RESPONDENT_FIRST_NAME);
        caseData.put(RESPONDENT_LAST_NAME, TEST_RESPONDENT_LAST_NAME);

        return caseData;
    }

    static Map<String, String> getExpectedSolicitorTemplateVars() {
        return ImmutableMap.of(
            NOTIFICATION_PET_NAME, TEST_PETITIONER_FIRST_NAME + " " + TEST_PETITIONER_LAST_NAME,
            NOTIFICATION_RESP_NAME, TEST_RESPONDENT_FIRST_NAME + " " + TEST_RESPONDENT_LAST_NAME,
            NOTIFICATION_CCD_REFERENCE_KEY, TEST_CASE_ID,
            NOTIFICATION_SOLICITOR_NAME, TEST_SOLICITOR_NAME
        );
    }

    static Map<String, String> getExpectedPetitionerTemplateVars() {
        return ImmutableMap.of(
            NOTIFICATION_PET_NAME, TEST_PETITIONER_FIRST_NAME + " " + TEST_PETITIONER_LAST_NAME,
            NOTIFICATION_CASE_NUMBER_KEY, TEST_CASE_FAMILY_MAN_ID,
            NOTIFICATION_HUSBAND_OR_WIFE, TEST_RELATIONSHIP_HUSBAND
        );
    }
}
