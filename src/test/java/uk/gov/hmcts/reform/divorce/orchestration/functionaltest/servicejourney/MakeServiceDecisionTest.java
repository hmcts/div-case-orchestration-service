package uk.gov.hmcts.reform.divorce.orchestration.functionaltest.servicejourney;

import com.google.common.collect.ImmutableMap;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultMatcher;
import uk.gov.hmcts.reform.bsp.common.model.document.CtscContactDetails;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.CcdStates;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CaseDetails;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CcdCallbackRequest;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CcdCallbackResponse;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.DocumentLink;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.document.ApplicationServiceTypes;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.document.ServiceApplicationRefusalOrder;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.document.ServiceDecisionOrder;
import uk.gov.hmcts.reform.divorce.orchestration.functionaltest.IdamTestSupport;
import uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor.CtscContactDetailsDataProviderService;
import uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor.DatesDataExtractor;
import uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor.FullNamesDataExtractor;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.servicejourney.DeemedServiceOrderGenerationTask;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.servicejourney.DeemedServiceRefusalOrderTask;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.servicejourney.DispensedServiceRefusalOrderTask;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.servicejourney.OrderToDispenseGenerationTask;
import uk.gov.hmcts.reform.divorce.utils.DateUtils;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static com.jayway.jsonpath.matchers.JsonPathMatchers.hasJsonPath;
import static com.jayway.jsonpath.matchers.JsonPathMatchers.hasNoJsonPath;
import static com.jayway.jsonpath.matchers.JsonPathMatchers.isJson;
import static java.time.LocalDate.now;
import static java.util.Collections.singletonList;
import static java.util.Collections.singletonMap;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_CASE_FAMILY_MAN_ID;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_PETITIONER_FIRST_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_PETITIONER_LAST_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_RESPONDENT_FIRST_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_RESPONDENT_LAST_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.CcdFields.RECEIVED_SERVICE_APPLICATION_DATE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.CcdFields.SERVICE_APPLICATION_DECISION_DATE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.CcdFields.SERVICE_APPLICATION_DOCUMENTS;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.CcdFields.SERVICE_APPLICATION_GRANTED;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.CcdFields.SERVICE_APPLICATION_TYPE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.CcdStates.SERVICE_APPLICATION_NOT_APPROVED;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CASE_ID_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DOCUMENT_CASE_DETAILS_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.YES_VALUE;
import static uk.gov.hmcts.reform.divorce.orchestration.functionaltest.servicejourney.ServiceDecisionMadeTest.buildRefusalRequest;
import static uk.gov.hmcts.reform.divorce.orchestration.functionaltest.servicejourney.ServiceDecisionMadeTest.buildServiceRefusalOrderCaseData;
import static uk.gov.hmcts.reform.divorce.orchestration.functionaltest.servicejourney.ServiceDecisionMadeTest.generateDocumentLink;
import static uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor.CaseDataExtractor.CaseDataKeys.CASE_REFERENCE;
import static uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor.CaseDataExtractor.getCaseReference;
import static uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor.FullNamesDataExtractor.CaseDataKeys.PETITIONER_FIRST_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor.FullNamesDataExtractor.CaseDataKeys.PETITIONER_LAST_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor.FullNamesDataExtractor.CaseDataKeys.RESPONDENT_FIRST_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor.FullNamesDataExtractor.CaseDataKeys.RESPONDENT_LAST_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor.FullNamesDataExtractor.getPetitionerFullName;
import static uk.gov.hmcts.reform.divorce.orchestration.testutil.CaseDataTestHelper.createCollectionMemberDocumentAsMap;
import static uk.gov.hmcts.reform.divorce.orchestration.testutil.ObjectMapperTestUtil.convertObjectToJsonString;
import static uk.gov.hmcts.reform.divorce.orchestration.testutil.TaskTestHelper.formatWithCurrentDate;
import static uk.gov.hmcts.reform.divorce.orchestration.util.ServiceApplicationRefusalHelper.getServiceApplicationRefusalReason;
import static uk.gov.hmcts.reform.divorce.utils.DateUtils.formatDateFromLocalDate;

public class MakeServiceDecisionTest extends IdamTestSupport {

    private static final String API_URL = "/make-service-decision";
    private static final String anotherServiceDocumentType = "otherDocument";

    private CtscContactDetails ctscContactDetails;

    @Autowired
    private MockMvc webClient;

    @Autowired
    private CtscContactDetailsDataProviderService ctscContactDetailsDataProviderService;

    @Before
    public void setup() {
        ctscContactDetails = ctscContactDetailsDataProviderService.getCtscContactDetails();
        documentGeneratorServiceServer.resetAll();
    }

    @Test
    public void shouldPopulateReceivedServiceAddedDateInResponse() throws Exception {
        CcdCallbackRequest input = buildRequest();

        Map<String, Object> expectedCaseData = addServiceApplicationDecisionDate(input.getCaseDetails().getCaseData());

        webClient.perform(post(API_URL)
            .header(AUTHORIZATION, AUTH_TOKEN)
            .content(convertObjectToJsonString(input))
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content().string(containsString(convertObjectToJsonString(expectedCaseData))));
    }

    @Test
    public void shouldGenerateOrderToDispenseAndAddItToResponse() throws Exception {
        Map<String, Object> caseData = buildInputCaseData(ApplicationServiceTypes.DISPENSED);
        CcdCallbackRequest input = buildRequest(caseData);

        CcdCallbackResponse expectedResponse = buildExpectedResponse(
            caseData,
            OrderToDispenseGenerationTask.FileMetadata.TEMPLATE_ID,
            OrderToDispenseGenerationTask.FileMetadata.DOCUMENT_TYPE
        );

        webClient.perform(post(API_URL)
            .header(AUTHORIZATION, AUTH_TOKEN)
            .content(convertObjectToJsonString(input))
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content().json(convertObjectToJsonString(expectedResponse)));
    }

    @Test
    public void shouldGenerateDeemedServiceOrderAndAddItToResponse() throws Exception {
        Map<String, Object> caseData = buildInputCaseData(ApplicationServiceTypes.DEEMED);
        CcdCallbackRequest input = buildRequest(caseData);

        CcdCallbackResponse expectedResponse = buildExpectedResponse(
            caseData,
            DeemedServiceOrderGenerationTask.FileMetadata.TEMPLATE_ID,
            DeemedServiceOrderGenerationTask.FileMetadata.DOCUMENT_TYPE
        );

        webClient.perform(post(API_URL)
            .header(AUTHORIZATION, AUTH_TOKEN)
            .content(convertObjectToJsonString(input))
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content().json(convertObjectToJsonString(expectedResponse)));
    }

    @Test
    public void shouldPopulateDocumentsWithDeemedRefusalOrderAndRemoveDraftWhenSubmitted() throws Exception {
        String templateId = DeemedServiceRefusalOrderTask.FileMetadata.TEMPLATE_ID;
        String documentType = DeemedServiceRefusalOrderTask.FileMetadata.DOCUMENT_TYPE;
        String expectedDocumentFilename = formatWithCurrentDate(documentType);

        CcdCallbackRequest ccdCallbackRequest = buildServiceRefusalOrderFixture(
            templateId, ApplicationServiceTypes.DEEMED, documentType
        );

        webClient.perform(post(API_URL)
            .contentType(MediaType.APPLICATION_JSON)
            .header(AUTHORIZATION, AUTH_TOKEN)
            .content(convertObjectToJsonString(ccdCallbackRequest)))
            .andExpect(status().isOk())
            .andExpect(commonExpectationsForRefusalOrderDocuments(documentType, expectedDocumentFilename));
    }

    @Test
    public void shouldPopulateExistingCollectionWithDeemedRefusalOrderAndRemoveDraftWhenSubmitted() throws Exception {
        String templateId = DeemedServiceRefusalOrderTask.FileMetadata.TEMPLATE_ID;
        String documentType = DeemedServiceRefusalOrderTask.FileMetadata.DOCUMENT_TYPE;
        String expectedDocumentFilename = formatWithCurrentDate(documentType);

        CcdCallbackRequest ccdCallbackRequest = buildMultipleServiceRefusalOrderFixture(
            templateId, ApplicationServiceTypes.DEEMED, documentType
        );

        webClient.perform(post(API_URL)
            .contentType(MediaType.APPLICATION_JSON)
            .header(AUTHORIZATION, AUTH_TOKEN)
            .content(convertObjectToJsonString(ccdCallbackRequest)))
            .andExpect(status().isOk())
            .andExpect(commonExpectationsForMultipleRefusalOrder(documentType, expectedDocumentFilename));
    }

    @Test
    public void shouldPopulateDocumentsWithDispensedRefusalOrderAndRemoveDraftWhenSubmitted() throws Exception {
        String templateId = DispensedServiceRefusalOrderTask.FileMetadata.TEMPLATE_ID;
        String documentType = DispensedServiceRefusalOrderTask.FileMetadata.DOCUMENT_TYPE;
        String expectedDocumentFilename = formatWithCurrentDate(documentType);

        CcdCallbackRequest ccdCallbackRequest = buildServiceRefusalOrderFixture(
            templateId, ApplicationServiceTypes.DISPENSED, documentType
        );

        webClient.perform(post(API_URL)
            .contentType(MediaType.APPLICATION_JSON)
            .header(AUTHORIZATION, AUTH_TOKEN)
            .content(convertObjectToJsonString(ccdCallbackRequest)))
            .andExpect(status().isOk())
            .andExpect(commonExpectationsForRefusalOrderDocuments(documentType, expectedDocumentFilename));
    }

    @Test
    public void shouldPopulateExistingCollectionWithDispensedRefusalOrderAndRemoveDraftWhenSubmitted() throws Exception {
        String templateId = DispensedServiceRefusalOrderTask.FileMetadata.TEMPLATE_ID;
        String documentType = DispensedServiceRefusalOrderTask.FileMetadata.DOCUMENT_TYPE;
        String expectedDocumentFilename = formatWithCurrentDate(documentType);

        CcdCallbackRequest ccdCallbackRequest = buildMultipleServiceRefusalOrderFixture(
            templateId, ApplicationServiceTypes.DISPENSED, documentType
        );

        webClient.perform(post(API_URL)
            .contentType(MediaType.APPLICATION_JSON)
            .header(AUTHORIZATION, AUTH_TOKEN)
            .content(convertObjectToJsonString(ccdCallbackRequest)))
            .andExpect(status().isOk())
            .andExpect(commonExpectationsForMultipleRefusalOrder(documentType, expectedDocumentFilename));
    }

    private ResultMatcher commonExpectationsForRefusalOrderDocuments(String documentType, String documentFileName) {
        return content().string(allOf(
            isJson(),
            hasNoJsonPath("$.data.ServiceRefusalDraft"),
            hasJsonPath("$.data.ServiceApplicationDocuments", hasSize(1)),
            hasJsonPath("$.data.ServiceApplicationDocuments[0].value.DocumentType",
                is(documentType)),
            hasJsonPath("$.data.ServiceApplicationDocuments[0].value.DocumentFileName",
                is(documentFileName)),
            hasNoJsonPath("$.errors")
        ));
    }

    private ResultMatcher commonExpectationsForMultipleRefusalOrder(String documentType, String documentFileName) {
        return content().string(allOf(
            isJson(),
            hasNoJsonPath("$.data.ServiceRefusalDraft"),
            hasJsonPath("$.data.ServiceApplicationDocuments", hasSize(2)),
            hasJsonPath("$.data.ServiceApplicationDocuments[1].value.DocumentType",
                is(documentType)),
            hasJsonPath("$.data.ServiceApplicationDocuments[1].value.DocumentFileName",
                is(documentFileName)),
            hasNoJsonPath("$.errors")
        ));
    }

    private Map<String, Object> buildInputCaseData(String applicationType) {
        Map<String, Object> caseData = new HashMap<>();

        caseData.put(CASE_REFERENCE, TEST_CASE_FAMILY_MAN_ID);

        caseData.put(PETITIONER_FIRST_NAME, TEST_PETITIONER_FIRST_NAME);
        caseData.put(PETITIONER_LAST_NAME, TEST_PETITIONER_LAST_NAME);
        caseData.put(RESPONDENT_FIRST_NAME, TEST_RESPONDENT_FIRST_NAME);
        caseData.put(RESPONDENT_LAST_NAME, TEST_RESPONDENT_LAST_NAME);

        caseData.put(RECEIVED_SERVICE_APPLICATION_DATE, "2010-10-10");
        caseData.put(SERVICE_APPLICATION_GRANTED, YES_VALUE);
        caseData.put(SERVICE_APPLICATION_TYPE, applicationType);

        return caseData;
    }

    private CcdCallbackResponse buildExpectedResponse(
        Map<String, Object> caseData, String templateId, String documentType) {
        Map<String, Object> caseDataBeforeGeneratingPdf = new HashMap<>(caseData);
        addServiceApplicationDecisionDate(caseDataBeforeGeneratingPdf);

        String generatedDocumentId = stubDocumentGeneratorServiceBaseOnContextPath(
            templateId,
            singletonMap(DOCUMENT_CASE_DETAILS_JSON_KEY,
                ImmutableMap.of(
                    "id", getCaseReference(caseDataBeforeGeneratingPdf),
                    "case_data", buildPopulatedTemplateModel(caseDataBeforeGeneratingPdf)
                )
            ),
            documentType
        );

        Map<String, Object> expectedCaseData = new HashMap<>(caseDataBeforeGeneratingPdf);

        expectedCaseData.put(
            SERVICE_APPLICATION_DOCUMENTS,
            buildCollectionWithOneDocument(generatedDocumentId, documentType)
        );

        return CcdCallbackResponse.builder()
            .state(CcdStates.AWAITING_DECREE_NISI)
            .data(expectedCaseData)
            .build();
    }

    private List<Map<String, Object>> buildCollectionWithOneDocument(
        String generatedDocumentId, String documentType) {

        return singletonList(
            createCollectionMemberDocumentAsMap(
                getDocumentStoreTestUrl(generatedDocumentId), documentType, documentType
            )
        );
    }

    private Map<String, Object> addServiceApplicationDecisionDate(Map<String, Object> caseDataBeforeGeneratingPdf) {
        caseDataBeforeGeneratingPdf.put(SERVICE_APPLICATION_DECISION_DATE, formatDateFromLocalDate(now()));

        return caseDataBeforeGeneratingPdf;
    }

    private ServiceDecisionOrder buildPopulatedTemplateModel(Map<String, Object> caseData) {
        return ServiceDecisionOrder.serviceDecisionOrderBuilder()
            .caseReference(TEST_CASE_FAMILY_MAN_ID)
            .ctscContactDetails(ctscContactDetails)
            .petitionerFullName(FullNamesDataExtractor.getPetitionerFullName(caseData))
            .respondentFullName(FullNamesDataExtractor.getRespondentFullName(caseData))
            .documentIssuedOn(DatesDataExtractor.getLetterDate())
            .receivedServiceApplicationDate(DatesDataExtractor.getReceivedServiceApplicationDate(caseData))
            .serviceApplicationDecisionDate(DatesDataExtractor.getServiceApplicationDecisionDate(caseData))
            .build();
    }

    private CcdCallbackRequest buildRequest(Map<String, Object> caseData) {
        return new CcdCallbackRequest(
            AUTH_TOKEN,
            "makeServiceDecision",
            CaseDetails.builder().caseData(caseData).caseId(TEST_CASE_ID).build()
        );
    }

    private CcdCallbackRequest buildRequest() {
        return buildRequest(new HashMap<>());
    }

    private CcdCallbackRequest buildMultipleServiceRefusalOrderFixture(String templateId, String serviceType, String documentType) {
        CcdCallbackRequest ccdCallbackRequest = buildServiceRefusalOrderFixture(templateId, serviceType, documentType);

        Map<String, Object> caseData = ccdCallbackRequest.getCaseDetails().getCaseData();

        caseData.put(
            SERVICE_APPLICATION_DOCUMENTS,
            buildCollectionWithOneDocument(UUID.randomUUID().toString(), anotherServiceDocumentType)
        );

        return ccdCallbackRequest;
    }

    private CcdCallbackRequest buildServiceRefusalOrderFixture(
        String templateId, String serviceType, String documentType) {
        DocumentLink refusalDraftDocument = generateDocumentLink(templateId);
        Map<String, Object> refusalOrderData = buildServiceRefusalOrderCaseData(
            serviceType, refusalDraftDocument
        );

        CcdCallbackRequest ccdCallbackRequest = buildRefusalRequest(refusalOrderData);
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
}

