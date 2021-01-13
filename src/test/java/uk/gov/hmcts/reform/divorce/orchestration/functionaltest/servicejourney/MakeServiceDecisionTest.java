package uk.gov.hmcts.reform.divorce.orchestration.functionaltest.servicejourney;

import com.google.common.collect.ImmutableMap;
import org.hamcrest.Matcher;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultMatcher;
import uk.gov.hmcts.reform.bsp.common.model.document.CtscContactDetails;
import uk.gov.hmcts.reform.divorce.model.ccd.DocumentLink;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.CcdFields;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CaseDetails;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CcdCallbackRequest;
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

import java.util.ArrayList;
import java.util.Arrays;
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
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_ADDED_DATE;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_CASE_FAMILY_MAN_ID;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_MY_REASON;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_PETITIONER_FIRST_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_PETITIONER_LAST_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_RECEIVED_DATE;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_RESPONDENT_FIRST_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_RESPONDENT_LAST_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_SERVICE_APPLICATION_PAYMENT;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.CcdFields.RECEIVED_SERVICE_ADDED_DATE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.CcdFields.RECEIVED_SERVICE_APPLICATION_DATE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.CcdFields.SERVICE_APPLICATIONS;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.CcdFields.SERVICE_APPLICATION_DECISION_DATE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.CcdFields.SERVICE_APPLICATION_DOCUMENTS;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.CcdFields.SERVICE_APPLICATION_GRANTED;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.CcdFields.SERVICE_APPLICATION_PAYMENT;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.CcdFields.SERVICE_APPLICATION_REFUSAL_REASON;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.CcdFields.SERVICE_APPLICATION_TYPE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.CcdStates.SERVICE_APPLICATION_NOT_APPROVED;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CASE_ID_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DOCUMENT_CASE_DETAILS_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NO_VALUE;
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
import static uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor.ServiceApplicationDataExtractor.getServiceApplicationRefusalReason;
import static uk.gov.hmcts.reform.divorce.orchestration.tasks.servicejourney.ServiceApplicationDataTaskTest.buildCollectionMember;
import static uk.gov.hmcts.reform.divorce.orchestration.testutil.CaseDataTestHelper.createCollectionMemberDocumentAsMap;
import static uk.gov.hmcts.reform.divorce.orchestration.testutil.ObjectMapperTestUtil.convertObjectToJsonString;
import static uk.gov.hmcts.reform.divorce.orchestration.testutil.TaskTestHelper.formatWithCurrentDateTime;
import static uk.gov.hmcts.reform.divorce.utils.DateUtils.formatDateFromLocalDate;
import static uk.gov.hmcts.reform.divorce.utils.DateUtils.formatDateWithCustomerFacingFormat;

public class MakeServiceDecisionTest extends IdamTestSupport {

    private static final String API_URL = "/make-service-decision";
    private static final String anotherServiceDocumentType = "otherDocument";
    public static final String TEST_TYPE = "other type";

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

        webClient.perform(post(API_URL)
            .header(AUTHORIZATION, AUTH_TOKEN)
            .content(convertObjectToJsonString(input))
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(
                content().string(
                    allOf(
                        isJson(),
                        hasNoCcdFieldsThatShouldBeRemoved(),
                        hasJsonPath("$.data.ServiceApplications", hasSize(1)),
                        assertServiceApplicationElement(0, TEST_TYPE),
                        hasNoJsonPath("$.data.ServiceApplicationDocuments")
                    )
                )
            );
    }

    @Test
    public void shouldGenerateOrderToDispenseAndAddItToResponse() throws Exception {
        String applicationType = ApplicationServiceTypes.DISPENSED;
        String documentType = OrderToDispenseGenerationTask.FileMetadata.DOCUMENT_TYPE;

        Map<String, Object> caseData = buildInputCaseData(applicationType);
        CcdCallbackRequest input = buildRequest(caseData);

        stubDocument(caseData, OrderToDispenseGenerationTask.FileMetadata.TEMPLATE_ID, documentType);

        webClient.perform(post(API_URL)
            .header(AUTHORIZATION, AUTH_TOKEN)
            .content(convertObjectToJsonString(input))
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(
                commonExpectations(applicationType, documentType, documentType)
            );
    }

    @Test
    public void shouldGenerateDeemedServiceOrderAndAddItToResponse() throws Exception {
        String applicationType = ApplicationServiceTypes.DEEMED;
        String documentType = DeemedServiceOrderGenerationTask.FileMetadata.DOCUMENT_TYPE;

        Map<String, Object> caseData = buildInputCaseData(ApplicationServiceTypes.DEEMED);
        CcdCallbackRequest input = buildRequest(caseData);

        stubDocument(caseData, DeemedServiceOrderGenerationTask.FileMetadata.TEMPLATE_ID, documentType);

        webClient.perform(post(API_URL)
            .header(AUTHORIZATION, AUTH_TOKEN)
            .content(convertObjectToJsonString(input))
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(
                commonExpectations(applicationType, documentType, documentType)
            );
    }

    @Test
    public void shouldPopulateDocumentsWithDeemedRefusalOrderAndRemoveDraftWhenSubmitted() throws Exception {
        String templateId = DeemedServiceRefusalOrderTask.FileMetadata.TEMPLATE_ID;
        String documentType = DeemedServiceRefusalOrderTask.FileMetadata.DOCUMENT_TYPE;
        String expectedDocumentFilename = formatWithCurrentDateTime(documentType);

        CcdCallbackRequest ccdCallbackRequest = buildServiceRefusalOrderFixture(
            templateId, ApplicationServiceTypes.DEEMED, documentType
        );

        webClient.perform(post(API_URL)
            .contentType(MediaType.APPLICATION_JSON)
            .header(AUTHORIZATION, AUTH_TOKEN)
            .content(convertObjectToJsonString(ccdCallbackRequest)))
            .andExpect(status().isOk())
            .andExpect(
                commonExpectations(
                    ApplicationServiceTypes.DEEMED, documentType, expectedDocumentFilename
                )
            );
    }

    @Test
    public void shouldPopulateExistingCollectionWithDeemedRefusalOrderAndRemoveDraftWhenSubmitted() throws Exception {
        String templateId = DeemedServiceRefusalOrderTask.FileMetadata.TEMPLATE_ID;
        String documentType = DeemedServiceRefusalOrderTask.FileMetadata.DOCUMENT_TYPE;
        String expectedDocumentFilename = formatWithCurrentDateTime(documentType);

        CcdCallbackRequest ccdCallbackRequest = buildMultipleServiceRefusalOrderFixture(
            templateId, ApplicationServiceTypes.DEEMED, documentType
        );

        webClient.perform(post(API_URL)
            .contentType(MediaType.APPLICATION_JSON)
            .header(AUTHORIZATION, AUTH_TOKEN)
            .content(convertObjectToJsonString(ccdCallbackRequest)))
            .andExpect(status().isOk())
            .andExpect(
                commonExpectationsForMultipleServiceApplications(
                    ApplicationServiceTypes.DEEMED,
                    documentType,
                    expectedDocumentFilename
                )
            );
    }

    @Test
    public void shouldPopulateDocumentsWithDispensedRefusalOrderAndRemoveDraftWhenSubmitted() throws Exception {
        String templateId = DispensedServiceRefusalOrderTask.FileMetadata.TEMPLATE_ID;
        String documentType = DispensedServiceRefusalOrderTask.FileMetadata.DOCUMENT_TYPE;
        String expectedDocumentFilename = formatWithCurrentDateTime(documentType);

        CcdCallbackRequest ccdCallbackRequest = buildServiceRefusalOrderFixture(
            templateId, ApplicationServiceTypes.DISPENSED, documentType
        );

        webClient.perform(post(API_URL)
            .contentType(MediaType.APPLICATION_JSON)
            .header(AUTHORIZATION, AUTH_TOKEN)
            .content(convertObjectToJsonString(ccdCallbackRequest)))
            .andExpect(status().isOk())
            .andExpect(
                commonExpectations(ApplicationServiceTypes.DISPENSED, documentType, expectedDocumentFilename)
            );
    }

    @Test
    public void shouldPopulateExistingCollectionWithDispensedRefusalOrderAndRemoveDraftWhenSubmitted() throws Exception {
        String templateId = DispensedServiceRefusalOrderTask.FileMetadata.TEMPLATE_ID;
        String documentType = DispensedServiceRefusalOrderTask.FileMetadata.DOCUMENT_TYPE;
        String expectedDocumentFilename = formatWithCurrentDateTime(documentType);

        CcdCallbackRequest ccdCallbackRequest = buildMultipleServiceRefusalOrderFixture(
            templateId, ApplicationServiceTypes.DISPENSED, documentType
        );

        webClient.perform(post(API_URL)
            .contentType(MediaType.APPLICATION_JSON)
            .header(AUTHORIZATION, AUTH_TOKEN)
            .content(convertObjectToJsonString(ccdCallbackRequest)))
            .andExpect(status().isOk())
            .andExpect(
                commonExpectationsForMultipleServiceApplications(
                    ApplicationServiceTypes.DISPENSED,
                    documentType,
                    expectedDocumentFilename
                )
            );
    }

    private ResultMatcher commonExpectations(
        String applicationType, String documentType, String documentFileName) {
        return content().string(allOf(
            isJson(),
            hasNoCcdFieldsThatShouldBeRemoved(),
            hasJsonPath("$.data.ServiceApplications", hasSize(1)),
            assertServiceApplicationElement(0, applicationType),
            hasJsonPath("$.data.ServiceApplicationDocuments", hasSize(1)),
            assertServiceApplicationDocumentElement(0, documentType, documentFileName),
            hasNoJsonPath("$.errors")
        ));
    }

    private ResultMatcher commonExpectationsForMultipleServiceApplications(
        String applicationType, String documentType, String documentFileName) {
        return content().string(allOf(
            isJson(),
            hasNoCcdFieldsThatShouldBeRemoved(),
            hasJsonPath("$.data.ServiceApplications", hasSize(2)),
            assertServiceApplicationElement(1, applicationType),
            hasJsonPath("$.data.ServiceApplicationDocuments", hasSize(2)),
            assertServiceApplicationDocumentElement(1, documentType, documentFileName),
            hasNoJsonPath("$.errors")
        ));
    }

    private Matcher<String> assertServiceApplicationDocumentElement(int index, String documentType, String documentFileName) {
        return allOf(
            hasServiceApplicationDocAtIndex(index, "DocumentType", documentType),
            hasServiceApplicationDocAtIndex(index, "DocumentFileName", documentFileName)
        );
    }

    private Matcher<String> assertServiceApplicationElement(int index, String applicationType) {
        return allOf(
            hasServiceApplicationAtIndex(index, "ReceivedDate", TEST_RECEIVED_DATE),
            hasServiceApplicationAtIndex(index, "AddedDate", TEST_ADDED_DATE),
            hasServiceApplicationAtIndex(index, "Type", applicationType),
            hasServiceApplicationAtIndex(index, "Payment", TEST_SERVICE_APPLICATION_PAYMENT),
            hasServiceApplicationAtIndex(index, "DecisionDate", formatDateFromLocalDate(now())),
            hasServiceApplicationAtIndex(index, "RefusalReason", TEST_MY_REASON)
        );
    }

    private Map<String, Object> buildInputCaseData(String applicationType) {
        Map<String, Object> caseData = buildBasicCaseData();

        caseData.put(SERVICE_APPLICATION_TYPE, applicationType);
        caseData.put(SERVICE_APPLICATION_PAYMENT, TEST_SERVICE_APPLICATION_PAYMENT);

        return caseData;
    }

    private Map<String, Object> buildBasicCaseData() {
        Map<String, Object> caseData = new HashMap<>();

        caseData.put(CASE_REFERENCE, TEST_CASE_FAMILY_MAN_ID);

        caseData.put(PETITIONER_FIRST_NAME, TEST_PETITIONER_FIRST_NAME);
        caseData.put(PETITIONER_LAST_NAME, TEST_PETITIONER_LAST_NAME);
        caseData.put(RESPONDENT_FIRST_NAME, TEST_RESPONDENT_FIRST_NAME);
        caseData.put(RESPONDENT_LAST_NAME, TEST_RESPONDENT_LAST_NAME);

        caseData.put(RECEIVED_SERVICE_APPLICATION_DATE, TEST_RECEIVED_DATE);
        caseData.put(SERVICE_APPLICATION_DECISION_DATE, formatDateFromLocalDate(now()));
        caseData.put(RECEIVED_SERVICE_ADDED_DATE, TEST_ADDED_DATE);
        caseData.put(SERVICE_APPLICATION_GRANTED, YES_VALUE);
        caseData.put(SERVICE_APPLICATION_REFUSAL_REASON, TEST_MY_REASON);

        return caseData;
    }

    private void stubDocument(
        Map<String, Object> caseData, String templateId, String documentType) {

        stubDocumentGeneratorService(
            templateId,
            singletonMap(DOCUMENT_CASE_DETAILS_JSON_KEY,
                ImmutableMap.of(
                    "id", getCaseReference(caseData),
                    "case_data", buildPopulatedTemplateModel(caseData)
                )
            ),
            documentType
        );
    }

    private List<Map<String, Object>> buildCollectionWithOneDocument(
        String generatedDocumentId, String documentType) {

        return singletonList(
            createCollectionMemberDocumentAsMap(
                getDocumentStoreTestUrl(generatedDocumentId), documentType, documentType
            )
        );
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
        Map<String, Object> caseData = buildBasicCaseData();
        caseData.put(SERVICE_APPLICATION_TYPE, TEST_TYPE);
        caseData.put(SERVICE_APPLICATION_PAYMENT, TEST_SERVICE_APPLICATION_PAYMENT);

        return buildRequest(caseData);
    }

    private CcdCallbackRequest buildMultipleServiceRefusalOrderFixture(
        String templateId, String serviceType, String documentType) {
        CcdCallbackRequest ccdCallbackRequest = buildServiceRefusalOrderFixture(templateId, serviceType, documentType);

        Map<String, Object> caseData = ccdCallbackRequest.getCaseDetails().getCaseData();

        caseData.put(
            SERVICE_APPLICATION_DOCUMENTS,
            buildCollectionWithOneDocument(UUID.randomUUID().toString(), anotherServiceDocumentType)
        );

        caseData.put(SERVICE_APPLICATION_PAYMENT, TEST_SERVICE_APPLICATION_PAYMENT);
        caseData.put(SERVICE_APPLICATION_TYPE, serviceType);
        caseData.put(SERVICE_APPLICATION_GRANTED, NO_VALUE);
        caseData.put(SERVICE_APPLICATIONS, new ArrayList<>(Arrays.asList(buildCollectionMember())));

        return ccdCallbackRequest;
    }

    private CcdCallbackRequest buildServiceRefusalOrderFixture(
        String templateId, String serviceType, String documentType) {
        DocumentLink refusalDraftDocument = generateDocumentLink(templateId);
        Map<String, Object> refusalOrderData = buildServiceRefusalOrderCaseData(
            serviceType, refusalDraftDocument
        );
        refusalOrderData.put(RECEIVED_SERVICE_ADDED_DATE, TEST_ADDED_DATE);
        refusalOrderData.put(SERVICE_APPLICATION_PAYMENT, TEST_SERVICE_APPLICATION_PAYMENT);
        refusalOrderData.remove(SERVICE_APPLICATIONS);

        CcdCallbackRequest ccdCallbackRequest = buildRefusalRequest(refusalOrderData);
        ccdCallbackRequest.getCaseDetails().setState(SERVICE_APPLICATION_NOT_APPROVED);

        stubDocumentGeneratorService(
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
            .documentIssuedOn(formatDateWithCustomerFacingFormat(now()))
            .build();
    }

    private Matcher<? super Object> hasServiceApplicationDocAtIndex(int index, String field, String value) {
        String pattern = "$.data.ServiceApplicationDocuments[%s].value.%s";
        return hasJsonPath(String.format(pattern, index, field), is(value));
    }

    private Matcher<? super Object> hasServiceApplicationAtIndex(int index, String field, String value) {
        String pattern = "$.data.ServiceApplications[%s].value.%s";
        return hasJsonPath(String.format(pattern, index, field), is(value));
    }

    private Matcher<? super Object> hasNoCcdFieldsThatShouldBeRemoved() {
        String pathPattern = "$.data.%s";
        return allOf(
            hasNoJsonPath(String.format(pathPattern, CcdFields.SERVICE_REFUSAL_DRAFT)),
            hasNoJsonPath(String.format(pathPattern, CcdFields.RECEIVED_SERVICE_APPLICATION_DATE)),
            hasNoJsonPath(String.format(pathPattern, CcdFields.RECEIVED_SERVICE_ADDED_DATE)),
            hasNoJsonPath(String.format(pathPattern, CcdFields.SERVICE_APPLICATION_TYPE)),
            hasNoJsonPath(String.format(pathPattern, CcdFields.SERVICE_APPLICATION_PAYMENT)),
            hasNoJsonPath(String.format(pathPattern, CcdFields.SERVICE_APPLICATION_GRANTED)),
            hasNoJsonPath(String.format(pathPattern, CcdFields.SERVICE_APPLICATION_DECISION_DATE)),
            hasNoJsonPath(String.format(pathPattern, CcdFields.SERVICE_APPLICATION_REFUSAL_REASON))
        );
    }
}
