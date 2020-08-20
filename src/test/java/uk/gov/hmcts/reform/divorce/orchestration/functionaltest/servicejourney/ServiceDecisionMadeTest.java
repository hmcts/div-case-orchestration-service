package uk.gov.hmcts.reform.divorce.orchestration.functionaltest.servicejourney;

import com.google.common.collect.ImmutableMap;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultMatcher;
import uk.gov.hmcts.reform.bsp.common.model.document.CtscContactDetails;
import uk.gov.hmcts.reform.divorce.orchestration.client.EmailClient;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CaseDetails;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CcdCallbackRequest;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CollectionMember;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.Document;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.DocumentLink;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.document.ApplicationServiceTypes;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.document.ServiceApplicationRefusalOrder;
import uk.gov.hmcts.reform.divorce.orchestration.functionaltest.IdamTestSupport;
import uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor.CtscContactDetailsDataProviderService;
import uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor.DatesDataExtractor;
import uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor.FullNamesDataExtractor;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.servicejourney.DeemedServiceRefusalOrderTask;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.servicejourney.DispensedServiceRefusalOrderTask;
import uk.gov.hmcts.reform.divorce.utils.DateUtils;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.jayway.jsonpath.matchers.JsonPathMatchers.hasJsonPath;
import static com.jayway.jsonpath.matchers.JsonPathMatchers.hasNoJsonPath;
import static com.jayway.jsonpath.matchers.JsonPathMatchers.isJson;
import static java.util.Collections.singletonMap;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
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
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_PETITIONER_EMAIL;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_PETITIONER_FIRST_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_PETITIONER_LAST_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_RESPONDENT_FIRST_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_RESPONDENT_LAST_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.CcdFields.RECEIVED_SERVICE_APPLICATION_DATE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.CcdFields.SERVICE_APPLICATION_GRANTED;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.CcdFields.SERVICE_APPLICATION_REFUSAL_REASON;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.CcdFields.SERVICE_APPLICATION_TYPE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.CcdFields.SERVICE_REFUSAL_DRAFT;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.CcdStates.SERVICE_APPLICATION_NOT_APPROVED;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CASE_ID_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D8DOCUMENTS_GENERATED;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DOCUMENT_CASE_DETAILS_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D_8_PETITIONER_EMAIL;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NOTIFICATION_PET_NAME;
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
import static uk.gov.hmcts.reform.divorce.orchestration.testutil.ObjectMapperTestUtil.convertObjectToJsonString;
import static uk.gov.hmcts.reform.divorce.orchestration.util.ServiceApplicationRefusalHelper.getServiceApplicationRefusalReason;
import static uk.gov.hmcts.reform.divorce.orchestration.util.ServiceApplicationRefusalHelperTest.TEST_SERVICE_APPLICATION_REFUSAL_REASON;

public class ServiceDecisionMadeTest extends IdamTestSupport {

    private static final String API_URL = "/service-decision-made/final";

    private static final String DEEMED_APPROVED_EMAIL_ID = "00f27db6-2678-4ccd-8cdd-44971b330ca4";
    private static final String DEEMED_NOT_APPROVED_EMAIL_ID = "5140a51a-fcda-42e4-adf4-0b469a1b927a";
    private static final String DISPENSED_APPROVED_EMAIL_ID = "cf03cea1-a155-4f20-a3a6-3ad8fad7742f";
    private static final String DISPENSED_NOT_APPROVED_EMAIL_ID = "e40d8623-e801-4de1-834a-7de101c9d857";

    private CtscContactDetails ctscContactDetails;

    @Autowired
    private MockMvc webClient;

    @Autowired
    private CtscContactDetailsDataProviderService ctscContactDetailsDataProviderService;

    @MockBean
    private EmailClient emailClient;

    @Before
    public void setup() {
        ctscContactDetails = ctscContactDetailsDataProviderService.getCtscContactDetails();
        documentGeneratorServiceServer.resetAll();
    }

    @Test
    public void shouldPopulateDocumentsWithDeemedRefusalOrderAndRemoveDraftWhenSubmitted() throws Exception {
        String templateId = DeemedServiceRefusalOrderTask.FileMetadata.TEMPLATE_ID;
        String documentType = DeemedServiceRefusalOrderTask.FileMetadata.DOCUMENT_TYPE;

        CcdCallbackRequest ccdCallbackRequest = buildServiceRefusalOrderFixture(
            templateId, ApplicationServiceTypes.DEEMED, documentType
        );

        webClient.perform(post(API_URL)
            .contentType(MediaType.APPLICATION_JSON)
            .header(AUTHORIZATION, AUTH_TOKEN)
            .content(convertObjectToJsonString(ccdCallbackRequest)))
            .andExpect(status().isOk())
            .andExpect(commonExpectationsForServiceRefusalOrder(documentType));

        verify(emailClient).sendEmail(
            eq(DEEMED_NOT_APPROVED_EMAIL_ID),
            eq(TEST_PETITIONER_EMAIL),
            eq(expectedCitizenEmailVars(ccdCallbackRequest.getCaseDetails().getCaseData())),
            any()
        );
    }

    @Test
    public void shouldPopulateDocumentsWithDispensedRefusalOrderAndRemoveDraftWhenSubmitted() throws Exception {
        String templateId = DispensedServiceRefusalOrderTask.FileMetadata.TEMPLATE_ID;
        String documentType = DispensedServiceRefusalOrderTask.FileMetadata.DOCUMENT_TYPE;

        CcdCallbackRequest ccdCallbackRequest = buildServiceRefusalOrderFixture(
            templateId, ApplicationServiceTypes.DISPENSED, documentType
        );

        webClient.perform(post(API_URL)
            .contentType(MediaType.APPLICATION_JSON)
            .header(AUTHORIZATION, AUTH_TOKEN)
            .content(convertObjectToJsonString(ccdCallbackRequest)))
            .andExpect(status().isOk())
            .andExpect(commonExpectationsForServiceRefusalOrder(documentType));

        verify(emailClient).sendEmail(
            eq(DISPENSED_NOT_APPROVED_EMAIL_ID),
            eq(TEST_PETITIONER_EMAIL),
            eq(expectedCitizenEmailVars(ccdCallbackRequest.getCaseDetails().getCaseData())),
            any()
        );
    }

    @Test
    public void shouldNotGenerateAnyRefusalOrderDocumentOrDraftsWhenServiceApplicationIsGrantedAndSubmitted() throws Exception {
        Map<String, Object> caseData = buildInputCaseData(ApplicationServiceTypes.DISPENSED);
        CcdCallbackRequest ccdCallbackRequest = buildRequest(caseData);

        webClient.perform(post(API_URL)
            .contentType(MediaType.APPLICATION_JSON)
            .header(AUTHORIZATION, AUTH_TOKEN)
            .content(convertObjectToJsonString(ccdCallbackRequest)))
            .andExpect(status().isOk())
            .andExpect(content().string(allOf(
                isJson(),
                hasNoJsonPath("$.data.D8DocumentsGenerated"),
                hasNoJsonPath("$.data.ServiceRefusalDraft"),
                hasNoJsonPath("$.errors")
            )));
    }

    @Test
    public void shouldSendDeemedApprovedEmailWhenServiceApplicationIsGrantedAndDeemed() throws Exception {
        Map<String, Object> caseData = buildInputCaseData(DEEMED);
        CcdCallbackRequest ccdCallbackRequest = buildRequest(caseData);

        webClient.perform(post(API_URL)
            .contentType(MediaType.APPLICATION_JSON)
            .header(AUTHORIZATION, AUTH_TOKEN)
            .content(convertObjectToJsonString(ccdCallbackRequest)))
            .andExpect(status().isOk())
            .andExpect(content().string(allOf(isJson(), hasNoJsonPath("$.errors"))));

        verify(emailClient).sendEmail(
            eq(DEEMED_APPROVED_EMAIL_ID),
            eq(TEST_PETITIONER_EMAIL),
            eq(expectedCitizenEmailVars(caseData)),
            any()
        );
    }

    @Test
    public void shouldSendDispensedApprovedEmailWhenServiceApplicationIsGrantedAndDispensed() throws Exception {
        Map<String, Object> caseData = buildInputCaseData(DISPENSED);
        CcdCallbackRequest ccdCallbackRequest = buildRequest(caseData);

        webClient.perform(post(API_URL)
            .contentType(MediaType.APPLICATION_JSON)
            .header(AUTHORIZATION, AUTH_TOKEN)
            .content(convertObjectToJsonString(ccdCallbackRequest)))
            .andExpect(status().isOk())
            .andExpect(content().string(allOf(isJson(), hasNoJsonPath("$.errors"))));

        verify(emailClient).sendEmail(
            eq(DISPENSED_APPROVED_EMAIL_ID),
            eq(TEST_PETITIONER_EMAIL),
            eq(expectedCitizenEmailVars(caseData)),
            any()
        );
    }

    private Map<String, String> expectedCitizenEmailVars(Map<String, Object> caseData) {
        return ImmutableMap.of(NOTIFICATION_PET_NAME, getPetitionerFullName(caseData));
    }

    private ResultMatcher commonExpectationsForServiceRefusalOrder(String documentType) {
        return content().string(allOf(
            isJson(),
            hasNoJsonPath("$.data.ServiceRefusalDraft"),
            hasJsonPath("$.data.D8DocumentsGenerated", hasSize(1)),
            hasJsonPath("$.data.D8DocumentsGenerated[0].value.DocumentType",
                is(documentType)),
            hasNoJsonPath("$.errors")
        ));
    }

    private CcdCallbackRequest buildServiceRefusalOrderFixture(
        String templateId, String serviceType, String documentType) {
        DocumentLink refusalDraftDocument = generateDocumentLink(templateId);
        Map<String, Object> refusalOrderData = buildServiceRefusalOrderCaseData(
            serviceType, refusalDraftDocument
        );

        CcdCallbackRequest ccdCallbackRequest = buildRequest(refusalOrderData);
        ccdCallbackRequest.getCaseDetails().setState(SERVICE_APPLICATION_NOT_APPROVED);

        stubDocumentGeneratorServiceBaseOnContextPath(
            templateId,
            singletonMap(DOCUMENT_CASE_DETAILS_JSON_KEY,
                ImmutableMap.of(
                    "id", refusalOrderData.get(CASE_ID_JSON_KEY),
                    "case_data", buildPopulatedServiceRefusalOrderTemplateModel(refusalOrderData)
                )),
            documentType
        );
        return ccdCallbackRequest;
    }

    public static Map<String, Object> buildInputCaseData(String applicationType) {
        Map<String, Object> caseData = new HashMap<>();

        caseData.put(CASE_REFERENCE, TEST_CASE_FAMILY_MAN_ID);

        caseData.put(D_8_PETITIONER_EMAIL, TEST_PETITIONER_EMAIL);
        caseData.put(PETITIONER_FIRST_NAME, TEST_PETITIONER_FIRST_NAME);
        caseData.put(PETITIONER_LAST_NAME, TEST_PETITIONER_LAST_NAME);
        caseData.put(RESPONDENT_FIRST_NAME, TEST_RESPONDENT_FIRST_NAME);
        caseData.put(RESPONDENT_LAST_NAME, TEST_RESPONDENT_LAST_NAME);

        caseData.put(RECEIVED_SERVICE_APPLICATION_DATE, "2010-10-10");
        caseData.put(SERVICE_APPLICATION_GRANTED, YES_VALUE);
        caseData.put(SERVICE_APPLICATION_TYPE, applicationType);

        return caseData;
    }

    private ServiceApplicationRefusalOrder buildPopulatedServiceRefusalOrderTemplateModel(Map<String, Object> caseData) {
        return ServiceApplicationRefusalOrder.serviceApplicationRefusalOrderBuilder()
            .ctscContactDetails(ctscContactDetails)
            .petitionerFullName(getPetitionerFullName(caseData))
            .respondentFullName(FullNamesDataExtractor.getRespondentFullName(caseData))
            .caseReference((String) caseData.get(CASE_ID_JSON_KEY))
            .receivedServiceApplicationDate(DatesDataExtractor.getReceivedServiceApplicationDate(caseData))
            .serviceApplicationRefusalReason(getServiceApplicationRefusalReason(caseData))
            .documentIssuedOn(DateUtils.formatDateWithCustomerFacingFormat(LocalDate.now()))
            .build();
    }

    public static CcdCallbackRequest buildRequest(Map<String, Object> caseData) {
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
        Map<String, Object> baseData = buildInputCaseData(serviceApplicationType);
        baseData.put(CASE_ID_JSON_KEY, TEST_CASE_ID);

        List<CollectionMember<Document>> generatedDocumentInfoList = new ArrayList<>();

        Map<String, Object> payload = ImmutableMap.of(
            SERVICE_APPLICATION_GRANTED, NO_VALUE,
            SERVICE_REFUSAL_DRAFT, serviceRefusalDraft,
            D8DOCUMENTS_GENERATED, generatedDocumentInfoList,
            SERVICE_APPLICATION_REFUSAL_REASON, TEST_SERVICE_APPLICATION_REFUSAL_REASON
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
