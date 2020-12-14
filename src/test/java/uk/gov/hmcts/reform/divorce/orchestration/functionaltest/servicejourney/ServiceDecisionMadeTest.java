package uk.gov.hmcts.reform.divorce.orchestration.functionaltest.servicejourney;

import com.google.common.collect.ImmutableMap;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import uk.gov.hmcts.reform.divorce.model.ccd.CollectionMember;
import uk.gov.hmcts.reform.divorce.model.ccd.Document;
import uk.gov.hmcts.reform.divorce.model.ccd.DocumentLink;
import uk.gov.hmcts.reform.divorce.orchestration.client.EmailClient;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CaseDetails;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CcdCallbackRequest;
import uk.gov.hmcts.reform.divorce.orchestration.functionaltest.IdamTestSupport;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.jayway.jsonpath.matchers.JsonPathMatchers.hasNoJsonPath;
import static com.jayway.jsonpath.matchers.JsonPathMatchers.isJson;
import static java.util.Arrays.asList;
import static org.hamcrest.Matchers.allOf;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_CASE_FAMILY_MAN_ID;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_MY_REASON;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_PETITIONER_EMAIL;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_PETITIONER_FIRST_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_PETITIONER_FULL_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_PETITIONER_LAST_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_RECEIVED_DATE;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_RESPONDENT_FIRST_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_RESPONDENT_FULL_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_RESPONDENT_LAST_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_SOLICITOR_EMAIL;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_SOLICITOR_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.CcdFields.RECEIVED_SERVICE_APPLICATION_DATE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.CcdFields.SERVICE_APPLICATIONS;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.CcdFields.SERVICE_APPLICATION_DOCUMENTS;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.CcdFields.SERVICE_APPLICATION_GRANTED;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.CcdFields.SERVICE_APPLICATION_REFUSAL_REASON;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.CcdFields.SERVICE_APPLICATION_TYPE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.CcdFields.SERVICE_REFUSAL_DRAFT;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.CcdStates.SERVICE_APPLICATION_NOT_APPROVED;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CASE_ID_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D_8_PETITIONER_EMAIL;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NOTIFICATION_CCD_REFERENCE_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NOTIFICATION_PET_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NOTIFICATION_RESP_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NOTIFICATION_SOLICITOR_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NO_VALUE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.YES_VALUE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.document.ApplicationServiceTypes.DEEMED;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.document.ApplicationServiceTypes.DISPENSED;
import static uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor.CaseDataExtractor.CaseDataKeys.CASE_REFERENCE;
import static uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor.FullNamesDataExtractor.CaseDataKeys.PETITIONER_FIRST_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor.FullNamesDataExtractor.CaseDataKeys.PETITIONER_LAST_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor.FullNamesDataExtractor.CaseDataKeys.RESPONDENT_FIRST_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor.FullNamesDataExtractor.CaseDataKeys.RESPONDENT_LAST_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor.FullNamesDataExtractor.getPetitionerFullName;
import static uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor.ServiceApplicationDataExtractorTest.buildCollectionMember;
import static uk.gov.hmcts.reform.divorce.orchestration.testutil.ObjectMapperTestUtil.convertObjectToJsonString;
import static uk.gov.hmcts.reform.divorce.orchestration.workflows.servicejourney.ServiceDecisionMadeWorkflowTest.petitionerRepresented;

public class ServiceDecisionMadeTest extends IdamTestSupport {

    private static final String API_URL = "/service-decision-made/final";

    private static final String CITIZEN_DEEMED_APPROVED_EMAIL_ID = "00f27db6-2678-4ccd-8cdd-44971b330ca4";
    private static final String PET_SOL_DEEMED_APPROVED_EMAIL_ID = "b762cdc0-fa4d-4699-b60d-1532e912cc3e";

    private static final String CITIZEN_DEEMED_NOT_APPROVED_EMAIL_ID = "5140a51a-fcda-42e4-adf4-0b469a1b927a";
    private static final String SOL_DEEMED_NOT_APPROVED_EMAIL_ID = "919e3780-0776-4219-a30c-72e9d6999414";

    private static final String CITIZEN_DISPENSED_APPROVED_EMAIL_ID = "cf03cea1-a155-4f20-a3a6-3ad8fad7742f";
    private static final String SOL_DISPENSED_APPROVED_EMAIL_ID = "2cb5e2c4-8090-4f7e-b0ae-574491cd8680";

    private static final String CITIZEN_DISPENSED_NOT_APPROVED_EMAIL_ID = "e40d8623-e801-4de1-834a-7de101c9d857";
    private static final String SOL_DISPENSED_NOT_APPROVED_EMAIL_ID = "d4de177b-b5b9-409c-95bc-cc8f85afd136";

    @Autowired
    private MockMvc webClient;

    @MockBean
    private EmailClient emailClient;

    @Before
    public void setup() {
        documentGeneratorServiceServer.resetAll();
    }

    @Test
    public void shouldSendDeemedApprovedEmailWhenServiceApplicationIsGrantedAndDeemed() throws Exception {
        Map<String, Object> caseData = buildInputCaseData(YES_VALUE, DEEMED);
        CcdCallbackRequest ccdCallbackRequest = buildRefusalRequest(caseData);

        webClient.perform(post(API_URL)
            .contentType(MediaType.APPLICATION_JSON)
            .header(AUTHORIZATION, AUTH_TOKEN)
            .content(convertObjectToJsonString(ccdCallbackRequest)))
            .andExpect(status().isOk())
            .andExpect(content().string(allOf(isJson(), hasNoJsonPath("$.errors"))));

        verify(emailClient).sendEmail(
            eq(CITIZEN_DEEMED_APPROVED_EMAIL_ID),
            eq(TEST_PETITIONER_EMAIL),
            eq(expectedCitizenEmailVars(caseData)),
            any()
        );
    }

    @Test
    public void shouldSendSolicitorDeemedApprovedEmailWhenServiceApplicationIsGrantedAndDeemedAndPetRepresented() throws Exception {
        Map<String, Object> caseData = petitionerRepresented(buildInputCaseData(YES_VALUE, DEEMED));
        CcdCallbackRequest ccdCallbackRequest = buildRefusalRequest(caseData);

        webClient.perform(post(API_URL)
            .contentType(MediaType.APPLICATION_JSON)
            .header(AUTHORIZATION, AUTH_TOKEN)
            .content(convertObjectToJsonString(ccdCallbackRequest)))
            .andExpect(status().isOk())
            .andExpect(content().string(allOf(isJson(), hasNoJsonPath("$.errors"))));

        verify(emailClient).sendEmail(
            eq(PET_SOL_DEEMED_APPROVED_EMAIL_ID),
            eq(TEST_SOLICITOR_EMAIL),
            eq(expectedSolicitorEmailVars()),
            any()
        );
    }

    @Test
    public void shouldSendDispensedApprovedEmailToCitizenWhenServiceApplicationIsGrantedAndDispensed() throws Exception {
        Map<String, Object> caseData = buildInputCaseData(YES_VALUE, DISPENSED);
        CcdCallbackRequest ccdCallbackRequest = buildRefusalRequest(caseData);

        webClient.perform(post(API_URL)
            .contentType(MediaType.APPLICATION_JSON)
            .header(AUTHORIZATION, AUTH_TOKEN)
            .content(convertObjectToJsonString(ccdCallbackRequest)))
            .andExpect(status().isOk())
            .andExpect(content().string(allOf(isJson(), hasNoJsonPath("$.errors"))));

        verify(emailClient).sendEmail(
            eq(CITIZEN_DISPENSED_APPROVED_EMAIL_ID),
            eq(TEST_PETITIONER_EMAIL),
            eq(expectedCitizenEmailVars(caseData)),
            any()
        );
    }

    @Test
    public void shouldSendDispensedApprovedEmailToSolicitorWhenServiceApplicationIsGrantedAndDispensed() throws Exception {
        Map<String, Object> caseData = petitionerRepresented(buildInputCaseData(YES_VALUE, DISPENSED));
        CcdCallbackRequest ccdCallbackRequest = buildRefusalRequest(caseData);

        webClient.perform(post(API_URL)
            .contentType(MediaType.APPLICATION_JSON)
            .header(AUTHORIZATION, AUTH_TOKEN)
            .content(convertObjectToJsonString(ccdCallbackRequest)))
            .andExpect(status().isOk())
            .andExpect(content().string(allOf(isJson(), hasNoJsonPath("$.errors"))));

        verify(emailClient).sendEmail(
            eq(SOL_DISPENSED_APPROVED_EMAIL_ID),
            eq(TEST_SOLICITOR_EMAIL),
            eq(expectedSolicitorEmailVars()),
            any()
        );
    }

    @Test
    public void shouldSendDispensedNotApprovedEmail_ToSolicitor_WhenServiceApplicationIsNotGrantedAndDispensed() throws Exception {
        Map<String, Object> caseData = petitionerRepresented(buildInputCaseData(NO_VALUE, DISPENSED));
        CcdCallbackRequest ccdCallbackRequest = buildRefusalRequest(caseData);

        webClient.perform(post(API_URL)
            .contentType(MediaType.APPLICATION_JSON)
            .header(AUTHORIZATION, AUTH_TOKEN)
            .content(convertObjectToJsonString(ccdCallbackRequest)))
            .andExpect(status().isOk())
            .andExpect(content().string(allOf(isJson(), hasNoJsonPath("$.errors"))));

        verify(emailClient).sendEmail(
            eq(SOL_DISPENSED_NOT_APPROVED_EMAIL_ID),
            eq(TEST_SOLICITOR_EMAIL),
            eq(expectedSolicitorEmailVars()),
            any()
        );
    }

    @Test
    public void shouldSendDispensedNotApprovedEmail_ToCitizen_WhenServiceApplicationIsNotGrantedAndDispensed() throws Exception {
        Map<String, Object> caseData = buildInputCaseData(NO_VALUE, DISPENSED);
        CcdCallbackRequest ccdCallbackRequest = buildRefusalRequest(caseData);

        webClient.perform(post(API_URL)
            .contentType(MediaType.APPLICATION_JSON)
            .header(AUTHORIZATION, AUTH_TOKEN)
            .content(convertObjectToJsonString(ccdCallbackRequest)))
            .andExpect(status().isOk())
            .andExpect(content().string(allOf(isJson(), hasNoJsonPath("$.errors"))));

        verify(emailClient).sendEmail(
            eq(CITIZEN_DISPENSED_NOT_APPROVED_EMAIL_ID),
            eq(TEST_PETITIONER_EMAIL),
            eq(expectedCitizenEmailVars(caseData)),
            any()
        );
    }

    @Test
    public void shouldSendDeemedNotApprovedEmail_ToCitizen_WhenServiceApplicationIsNotGrantedAndDeemed() throws Exception {
        Map<String, Object> caseData = buildInputCaseData(NO_VALUE, DEEMED);
        CcdCallbackRequest ccdCallbackRequest = buildRefusalRequest(caseData);

        webClient.perform(post(API_URL)
            .contentType(MediaType.APPLICATION_JSON)
            .header(AUTHORIZATION, AUTH_TOKEN)
            .content(convertObjectToJsonString(ccdCallbackRequest)))
            .andExpect(status().isOk())
            .andExpect(content().string(allOf(isJson(), hasNoJsonPath("$.errors"))));

        verify(emailClient).sendEmail(
            eq(CITIZEN_DEEMED_NOT_APPROVED_EMAIL_ID),
            eq(TEST_PETITIONER_EMAIL),
            eq(expectedCitizenEmailVars(caseData)),
            any()
        );
    }

    @Test
    public void shouldSendDeemedNotApprovedEmail_ToSolicitor_WhenServiceApplicationIsNotGrantedAndDeemed() throws Exception {
        Map<String, Object> caseData = petitionerRepresented(buildInputCaseData(NO_VALUE, DEEMED));
        CcdCallbackRequest ccdCallbackRequest = buildRefusalRequest(caseData);

        webClient.perform(post(API_URL)
            .contentType(MediaType.APPLICATION_JSON)
            .header(AUTHORIZATION, AUTH_TOKEN)
            .content(convertObjectToJsonString(ccdCallbackRequest)))
            .andExpect(status().isOk())
            .andExpect(content().string(allOf(isJson(), hasNoJsonPath("$.errors"))));

        verify(emailClient).sendEmail(
            eq(SOL_DEEMED_NOT_APPROVED_EMAIL_ID),
            eq(TEST_SOLICITOR_EMAIL),
            eq(expectedSolicitorEmailVars()),
            any()
        );
    }

    private Map<String, String> expectedCitizenEmailVars(Map<String, Object> caseData) {
        return ImmutableMap.of(NOTIFICATION_PET_NAME, getPetitionerFullName(caseData));
    }

    private Map<String, String> expectedSolicitorEmailVars() {
        return ImmutableMap.of(
            NOTIFICATION_PET_NAME, TEST_PETITIONER_FULL_NAME,
            NOTIFICATION_RESP_NAME, TEST_RESPONDENT_FULL_NAME,
            NOTIFICATION_CCD_REFERENCE_KEY, TEST_CASE_ID,
            NOTIFICATION_SOLICITOR_NAME, TEST_SOLICITOR_NAME
        );
    }

    public static Map<String, Object> buildInputCaseData(String isApplicationGranted, String applicationType) {
        Map<String, Object> caseData = new HashMap<>();

        caseData.put(CASE_REFERENCE, TEST_CASE_FAMILY_MAN_ID);

        caseData.put(D_8_PETITIONER_EMAIL, TEST_PETITIONER_EMAIL);
        caseData.put(PETITIONER_FIRST_NAME, TEST_PETITIONER_FIRST_NAME);
        caseData.put(PETITIONER_LAST_NAME, TEST_PETITIONER_LAST_NAME);
        caseData.put(RESPONDENT_FIRST_NAME, TEST_RESPONDENT_FIRST_NAME);
        caseData.put(RESPONDENT_LAST_NAME, TEST_RESPONDENT_LAST_NAME);
        caseData.put(RECEIVED_SERVICE_APPLICATION_DATE, TEST_RECEIVED_DATE);

        caseData.put(SERVICE_APPLICATIONS, asList(buildCollectionMember(isApplicationGranted, applicationType)));

        return caseData;
    }

    public static CcdCallbackRequest buildRefusalRequest(Map<String, Object> caseData) {
        return new CcdCallbackRequest(
            AUTH_TOKEN,
            "",
            CaseDetails.builder()
                .state(SERVICE_APPLICATION_NOT_APPROVED)
                .caseData(caseData)
                .caseId(TEST_CASE_ID)
                .build()
        );
    }

    public static Map<String, Object> buildServiceRefusalOrderCaseData(
        String serviceApplicationType, DocumentLink serviceRefusalDraft
    ) {
        Map<String, Object> baseData = buildInputCaseData(YES_VALUE, serviceApplicationType);
        baseData.put(CASE_ID_JSON_KEY, TEST_CASE_ID);

        List<CollectionMember<Document>> generatedDocumentInfoList = new ArrayList<>();

        Map<String, Object> payload = ImmutableMap.of(
            SERVICE_APPLICATION_GRANTED, NO_VALUE,
            SERVICE_APPLICATION_TYPE, serviceApplicationType,
            SERVICE_REFUSAL_DRAFT, serviceRefusalDraft,
            SERVICE_APPLICATION_DOCUMENTS, generatedDocumentInfoList,
            SERVICE_APPLICATION_REFUSAL_REASON, TEST_MY_REASON
        );
        baseData.putAll(payload);

        return baseData;
    }

    public static DocumentLink generateDocumentLink(String templateFile) {
        DocumentLink documentLink = new DocumentLink();
        documentLink.setDocumentUrl("test.url");
        documentLink.setDocumentFilename(templateFile);
        documentLink.setDocumentBinaryUrl("binary_url");

        return documentLink;
    }
}
