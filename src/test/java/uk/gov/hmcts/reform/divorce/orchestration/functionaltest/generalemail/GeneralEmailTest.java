package uk.gov.hmcts.reform.divorce.orchestration.functionaltest.generalemail;

import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import uk.gov.hmcts.reform.bsp.common.model.document.CtscContactDetails;
import uk.gov.hmcts.reform.divorce.orchestration.client.EmailClient;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.CcdFields;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CaseDetails;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CcdCallbackRequest;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.DocumentLink;
import uk.gov.hmcts.reform.divorce.orchestration.functionaltest.IdamTestSupport;
import uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor.CtscContactDetailsDataProviderService;
import uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor.FullNamesDataExtractor;
import uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.helper.GeneralEmailTaskHelper;

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
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_CO_RESPONDENT_EMAIL;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_CO_RESPONDENT_FIRST_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_CO_RESPONDENT_LAST_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_CO_RESPONDENT_SOLICITOR_EMAIL;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_CO_RESPONDENT_SOLICITOR_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_GENERAL_EMAIL_DETAILS;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_OTHER_PARTY_EMAIL;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_OTHER_PARTY_NAME;
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
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.CcdFields.CO_RESPONDENT_EMAIL_ADDRESS;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.CcdFields.GENERAL_EMAIL_DETAILS;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.CcdFields.GENERAL_EMAIL_PARTIES;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.CcdFields.PETITIONER_EMAIL;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CO_RESPONDENT_GENERAL_EMAIL_SELECTION;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.OTHER_GENERAL_EMAIL_SELECTION;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.PETITIONER_GENERAL_EMAIL_SELECTION;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.PETITIONER_SOLICITOR_EMAIL;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.RESPONDENT_GENERAL_EMAIL_SELECTION;
import static uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor.EmailDataExtractor.CaseDataKeys.CO_RESPONDENT_SOLICITOR_EMAIL;
import static uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor.EmailDataExtractor.CaseDataKeys.OTHER_PARTY_EMAIL;
import static uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor.EmailDataExtractor.CaseDataKeys.RESPONDENT_EMAIL;
import static uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor.EmailDataExtractor.CaseDataKeys.RESPONDENT_SOLICITOR_EMAIL;
import static uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor.FullNamesDataExtractor.CaseDataKeys.CO_RESPONDENT_FIRST_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor.FullNamesDataExtractor.CaseDataKeys.CO_RESPONDENT_LAST_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor.FullNamesDataExtractor.CaseDataKeys.OTHER_PARTY_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor.FullNamesDataExtractor.CaseDataKeys.RESPONDENT_FIRST_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor.FullNamesDataExtractor.CaseDataKeys.RESPONDENT_LAST_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.helper.GeneralEmailTaskHelper.Party.CO_RESPONDENT;
import static uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.helper.GeneralEmailTaskHelper.Party.CO_RESPONDENT_SOLICITOR;
import static uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.helper.GeneralEmailTaskHelper.Party.OTHER;
import static uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.helper.GeneralEmailTaskHelper.Party.PETITIONER;
import static uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.helper.GeneralEmailTaskHelper.Party.PETITIONER_SOLICITOR;
import static uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.helper.GeneralEmailTaskHelper.Party.RESPONDENT;
import static uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.helper.GeneralEmailTaskHelper.Party.RESPONDENT_SOLICITOR;
import static uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.helper.GeneralEmailTaskHelper.getExpectedNotificationTemplateVars;
import static uk.gov.hmcts.reform.divorce.orchestration.testutil.ObjectMapperTestUtil.convertObjectToJsonString;
import static uk.gov.hmcts.reform.divorce.orchestration.testutil.TaskContextHelper.context;

public class GeneralEmailTest extends IdamTestSupport {

    private static final String API_URL = "/create-general-email";

    private static final String GENERAL_EMAIL_PETITIONER = "9413e5c5-9ac2-4c58-8138-2c6b05008347";
    private static final String GENERAL_EMAIL_PETITIONER_SOLICITOR = "b2c099c0-3d21-4e96-a98a-e3847d4bb348";
    private static final String GENERAL_EMAIL_RESPONDENT = "fcee0c33-ed23-47c2-acbf-ade1fa533615";
    private static final String GENERAL_EMAIL_RESPONDENT_SOLICITOR = "e95c6829-d067-4ad4-9d7a-e5cede2a553c";
    private static final String GENERAL_EMAIL_CO_RESPONDENT = "c570c8df-5638-4adc-8a02-7467aaa4fa4b";
    private static final String GENERAL_EMAIL_CO_RESPONDENT_SOLICITOR = "d887389a-e3d7-4a0d-bca6-113606762373";
    private static final String GENERAL_EMAIL_OTHER_PARTY = "165acb51-407e-449d-90be-9018ebc20028";

    @Autowired
    private MockMvc webClient;

    @Autowired
    private CtscContactDetailsDataProviderService ctscContactDetailsDataProviderService;

    @MockBean
    private EmailClient emailClient;

    @Before
    public void setup() {
        CtscContactDetails ctscContactDetails = ctscContactDetailsDataProviderService.getCtscContactDetails();
        documentGeneratorServiceServer.resetAll();
    }

    @Test
    public void shouldSendGeneralEmailWhen_toPetitioner() throws Exception {
        Map<String, Object> caseData = buildInputCaseData(PETITIONER);
        CcdCallbackRequest ccdCallbackRequest = buildRequest(caseData);

        webClient.perform(post(API_URL)
            .contentType(MediaType.APPLICATION_JSON)
            .header(AUTHORIZATION, AUTH_TOKEN)
            .content(convertObjectToJsonString(ccdCallbackRequest)))
            .andExpect(status().isOk())
            .andExpect(content().string(allOf(isJson(), hasNoJsonPath("$.errors"))));

        verify(emailClient).sendEmail(
            eq(GENERAL_EMAIL_PETITIONER),
            eq(TEST_PETITIONER_EMAIL),
            eq(getExpectedNotificationTemplateVars(GeneralEmailTaskHelper.Party.PETITIONER, context(), caseData)),
            any()
        );
    }

    @Test
    public void shouldSendGeneralEmailWhen_toPetitioner_Solicitor() throws Exception {
        Map<String, Object> caseData = buildInputCaseData(PETITIONER_SOLICITOR);
        CcdCallbackRequest ccdCallbackRequest = buildRequest(caseData);

        webClient.perform(post(API_URL)
            .contentType(MediaType.APPLICATION_JSON)
            .header(AUTHORIZATION, AUTH_TOKEN)
            .content(convertObjectToJsonString(ccdCallbackRequest)))
            .andExpect(status().isOk())
            .andExpect(content().string(allOf(isJson(), hasNoJsonPath("$.errors"))));

        verify(emailClient).sendEmail(
            eq(GENERAL_EMAIL_PETITIONER_SOLICITOR),
            eq(TEST_PETITIONER_EMAIL),
            eq(getExpectedNotificationTemplateVars(PETITIONER_SOLICITOR, context(), caseData)),
            any()
        );
    }

    @Test
    public void shouldSendGeneralEmailWhen_toRespondent() throws Exception {
        Map<String, Object> caseData = buildInputCaseData(RESPONDENT);
        CcdCallbackRequest ccdCallbackRequest = buildRequest(caseData);

        webClient.perform(post(API_URL)
            .contentType(MediaType.APPLICATION_JSON)
            .header(AUTHORIZATION, AUTH_TOKEN)
            .content(convertObjectToJsonString(ccdCallbackRequest)))
            .andExpect(status().isOk())
            .andExpect(content().string(allOf(isJson(), hasNoJsonPath("$.errors"))));

        verify(emailClient).sendEmail(
            eq(GENERAL_EMAIL_RESPONDENT),
            eq(TEST_RESPONDENT_EMAIL),
            eq(getExpectedNotificationTemplateVars(RESPONDENT, context(), caseData)),
            any()
        );
    }

    @Test
    public void shouldSendGeneralEmailWhen_toRespondent_Solicitor() throws Exception {
        Map<String, Object> caseData = buildInputCaseData(RESPONDENT_SOLICITOR);
        CcdCallbackRequest ccdCallbackRequest = buildRequest(caseData);

        webClient.perform(post(API_URL)
            .contentType(MediaType.APPLICATION_JSON)
            .header(AUTHORIZATION, AUTH_TOKEN)
            .content(convertObjectToJsonString(ccdCallbackRequest)))
            .andExpect(status().isOk())
            .andExpect(content().string(allOf(isJson(), hasNoJsonPath("$.errors"))));

        verify(emailClient).sendEmail(
            eq(GENERAL_EMAIL_RESPONDENT_SOLICITOR),
            eq(TEST_RESPONDENT_SOLICITOR_EMAIL),
            eq(getExpectedNotificationTemplateVars(RESPONDENT_SOLICITOR, context(), caseData)),
            any()
        );
    }

    @Test
    public void shouldSendGeneralEmailWhen_toCoRespondent() throws Exception {
        Map<String, Object> caseData = buildInputCaseData(CO_RESPONDENT);
        CcdCallbackRequest ccdCallbackRequest = buildRequest(caseData);

        webClient.perform(post(API_URL)
            .contentType(MediaType.APPLICATION_JSON)
            .header(AUTHORIZATION, AUTH_TOKEN)
            .content(convertObjectToJsonString(ccdCallbackRequest)))
            .andExpect(status().isOk())
            .andExpect(content().string(allOf(isJson(), hasNoJsonPath("$.errors"))));

        verify(emailClient).sendEmail(
            eq(GENERAL_EMAIL_CO_RESPONDENT),
            eq(TEST_CO_RESPONDENT_EMAIL),
            eq(getExpectedNotificationTemplateVars(CO_RESPONDENT, context(), caseData)),
            any()
        );
    }

    @Test
    public void shouldSendGeneralEmailWhen_toCoRespondent_Solicitor() throws Exception {
        Map<String, Object> caseData = buildInputCaseData(CO_RESPONDENT_SOLICITOR);
        CcdCallbackRequest ccdCallbackRequest = buildRequest(caseData);

        webClient.perform(post(API_URL)
            .contentType(MediaType.APPLICATION_JSON)
            .header(AUTHORIZATION, AUTH_TOKEN)
            .content(convertObjectToJsonString(ccdCallbackRequest)))
            .andExpect(status().isOk())
            .andExpect(content().string(allOf(isJson(), hasNoJsonPath("$.errors"))));

        verify(emailClient).sendEmail(
            eq(GENERAL_EMAIL_CO_RESPONDENT_SOLICITOR),
            eq(TEST_CO_RESPONDENT_SOLICITOR_EMAIL),
            eq(getExpectedNotificationTemplateVars(CO_RESPONDENT_SOLICITOR, context(), caseData)),
            any()
        );
    }

    @Test
    public void shouldSendGeneralEmailWhen_toOther() throws Exception {
        Map<String, Object> caseData = buildInputCaseData(OTHER);
        CcdCallbackRequest ccdCallbackRequest = buildRequest(caseData);

        webClient.perform(post(API_URL)
            .contentType(MediaType.APPLICATION_JSON)
            .header(AUTHORIZATION, AUTH_TOKEN)
            .content(convertObjectToJsonString(ccdCallbackRequest)))
            .andExpect(status().isOk())
            .andExpect(content().string(allOf(isJson(), hasNoJsonPath("$.errors"))));

        verify(emailClient).sendEmail(
            eq(GENERAL_EMAIL_OTHER_PARTY),
            eq(TEST_OTHER_PARTY_EMAIL),
            eq(getExpectedNotificationTemplateVars(OTHER, context(), caseData)),
            any()
        );
    }

    public static Map<String, Object> buildInputCaseData(GeneralEmailTaskHelper.Party party) {
        switch (party) {
            case PETITIONER:
                return getPetitionerData(false);
            case PETITIONER_SOLICITOR:
                return getPetitionerData(true);
            case RESPONDENT:
                return getRespondentData(false);
            case RESPONDENT_SOLICITOR:
                return getRespondentData(true);
            case CO_RESPONDENT:
                return getCoRespondentData(false);
            case CO_RESPONDENT_SOLICITOR:
                return getCoRespondentData(true);
            default:
                return getOtherPartyData();
        }
    }

    public static Map<String, Object> getPetitionerData(Boolean isRepresented) {
        Map<String, Object> caseData = new HashMap<>();
        caseData.put(GENERAL_EMAIL_PARTIES, PETITIONER_GENERAL_EMAIL_SELECTION);
        caseData.put(CcdFields.PETITIONER_FIRST_NAME, TEST_PETITIONER_FIRST_NAME);
        caseData.put(CcdFields.PETITIONER_LAST_NAME, TEST_PETITIONER_LAST_NAME);
        caseData.put(PETITIONER_EMAIL, TEST_PETITIONER_EMAIL);
        caseData.put(GENERAL_EMAIL_DETAILS, TEST_GENERAL_EMAIL_DETAILS);

        if (isRepresented) {
            caseData.put(FullNamesDataExtractor.CaseDataKeys.PETITIONER_SOLICITOR_NAME, TEST_SOLICITOR_NAME);
            caseData.put(PETITIONER_SOLICITOR_EMAIL, TEST_SOLICITOR_EMAIL);
        }

        return caseData;
    }

    public static Map<String, Object> getRespondentData(Boolean isRepresented) {
        Map<String, Object> caseData = new HashMap<>();
        caseData.put(GENERAL_EMAIL_PARTIES, RESPONDENT_GENERAL_EMAIL_SELECTION);
        caseData.put(RESPONDENT_FIRST_NAME, TEST_RESPONDENT_FIRST_NAME);
        caseData.put(RESPONDENT_LAST_NAME, TEST_RESPONDENT_LAST_NAME);
        caseData.put(RESPONDENT_EMAIL, TEST_RESPONDENT_EMAIL);
        caseData.put(GENERAL_EMAIL_DETAILS, TEST_GENERAL_EMAIL_DETAILS);

        if (isRepresented) {
            caseData.put(FullNamesDataExtractor.CaseDataKeys.RESPONDENT_SOLICITOR_NAME, TEST_RESPONDENT_SOLICITOR_NAME);
            caseData.put(RESPONDENT_SOLICITOR_EMAIL, TEST_RESPONDENT_SOLICITOR_EMAIL);
        }

        return caseData;
    }

    public static Map<String, Object> getCoRespondentData(Boolean isRepresented) {
        Map<String, Object> caseData = new HashMap<>();
        caseData.put(GENERAL_EMAIL_PARTIES, CO_RESPONDENT_GENERAL_EMAIL_SELECTION);
        caseData.put(CO_RESPONDENT_FIRST_NAME, TEST_CO_RESPONDENT_FIRST_NAME);
        caseData.put(CO_RESPONDENT_LAST_NAME, TEST_CO_RESPONDENT_LAST_NAME);
        caseData.put(CO_RESPONDENT_EMAIL_ADDRESS, TEST_CO_RESPONDENT_LAST_NAME);
        caseData.put(GENERAL_EMAIL_DETAILS, TEST_GENERAL_EMAIL_DETAILS);

        if (isRepresented) {
            caseData.put(FullNamesDataExtractor.CaseDataKeys.CO_RESPONDENT_SOLICITOR_NAME, TEST_CO_RESPONDENT_SOLICITOR_NAME);
            caseData.put(CO_RESPONDENT_SOLICITOR_EMAIL, TEST_CO_RESPONDENT_SOLICITOR_EMAIL);
        }

        return caseData;
    }

    public static Map<String, Object> getOtherPartyData() {
        Map<String, Object> caseData = new HashMap<>();
        caseData.put(GENERAL_EMAIL_PARTIES, OTHER_GENERAL_EMAIL_SELECTION);
        caseData.put(OTHER_PARTY_NAME, TEST_OTHER_PARTY_NAME);
        caseData.put(OTHER_PARTY_EMAIL, TEST_OTHER_PARTY_EMAIL);
        caseData.put(GENERAL_EMAIL_DETAILS, TEST_GENERAL_EMAIL_DETAILS);

        return caseData;
    }

    public static CcdCallbackRequest buildRequest(Map<String, Object> caseData) {
        return new CcdCallbackRequest(
            AUTH_TOKEN,
            "",
            CaseDetails.builder()
                .state("*")
                .caseData(caseData)
                .caseId(TEST_CASE_ID)
                .build()
        );
    }

    public static DocumentLink generateDocumentLink(String templateFile) {
        DocumentLink documentLink = new DocumentLink();
        documentLink.setDocumentUrl("test.url");
        documentLink.setDocumentFilename(templateFile);
        documentLink.setDocumentBinaryUrl("binary_url");

        return documentLink;
    }
}
