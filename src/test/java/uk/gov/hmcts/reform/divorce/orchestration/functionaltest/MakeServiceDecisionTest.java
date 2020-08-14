package uk.gov.hmcts.reform.divorce.orchestration.functionaltest;

import com.google.common.collect.ImmutableMap;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.CcdStates;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CaseDetails;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CcdCallbackRequest;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CcdCallbackResponse;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.document.ApplicationServiceTypes;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.document.ServiceDecisionOrder;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.documentgeneration.GeneratedDocumentInfo;
import uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor.CtscContactDetailsDataProviderService;
import uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor.DatesDataExtractor;
import uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor.FullNamesDataExtractor;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.servicejourney.DeemedServiceOrderGenerationTask;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.servicejourney.OrderToDispenseGenerationTask;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.servicejourney.ServiceRefusalOrderTask;
import uk.gov.hmcts.reform.divorce.orchestration.testutil.ObjectMapperTestUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.jayway.jsonpath.matchers.JsonPathMatchers.hasNoJsonPath;
import static com.jayway.jsonpath.matchers.JsonPathMatchers.isJson;
import static java.time.LocalDate.now;
import static java.util.Collections.singletonList;
import static java.util.Collections.singletonMap;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.MatcherAssert.assertThat;
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
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.CcdFields.SERVICE_APPLICATION_GRANTED;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.CcdFields.SERVICE_APPLICATION_TYPE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.CcdFields.SERVICE_REFUSAL_DRAFT;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.CcdStates.AWAITING_SERVICE_CONSIDERATION;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D8DOCUMENTS_GENERATED;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DOCUMENT_CASE_DETAILS_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DOCUMENT_COLLECTION;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NO_VALUE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.YES_VALUE;
import static uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor.CaseDataExtractor.CaseDataKeys.CASE_REFERENCE;
import static uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor.CaseDataExtractor.getCaseReference;
import static uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor.FullNamesDataExtractor.CaseDataKeys.PETITIONER_FIRST_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor.FullNamesDataExtractor.CaseDataKeys.PETITIONER_LAST_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor.FullNamesDataExtractor.CaseDataKeys.RESPONDENT_FIRST_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor.FullNamesDataExtractor.CaseDataKeys.RESPONDENT_LAST_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.testutil.CaseDataTestHelper.createCollectionMemberDocumentAsMap;
import static uk.gov.hmcts.reform.divorce.orchestration.testutil.ObjectMapperTestUtil.convertObjectToJsonString;
import static uk.gov.hmcts.reform.divorce.utils.DateUtils.formatDateFromLocalDate;

public class MakeServiceDecisionTest extends IdamTestSupport {

    private static final String API_URL = "/make-service-decision";
    private static final String SERVICE_DECISION_API = "/service-decision-made";

    @Autowired
    private MockMvc webClient;

    @Autowired
    private CtscContactDetailsDataProviderService ctscContactDetailsDataProviderService;

    @Before
    public void setup() {
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
    public void shouldGenerateOderToDispenseAndAddItToResponse() throws Exception {
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
    public void shouldRemoveDraftAndPopulateDocumentsWithDeemedRefusalOrderWhenSubmitted() throws Exception {
        GeneratedDocumentInfo deemedRefusalDraftDocument = generateDocumentInfo(
            ServiceRefusalOrderTask.FileMetadata.DEEMED_DOCUMENT_TYPE,
            ServiceRefusalOrderTask.FileMetadata.DEEMED_TEMPLATE_ID);

        Map<String, Object> refusalOrderData = buildServiceRefusalOrderCaseData(ApplicationServiceTypes.DEEMED, deemedRefusalDraftDocument);
        CcdCallbackRequest ccdCallbackRequest = buildRequest(refusalOrderData);
        ccdCallbackRequest.getCaseDetails().setState(AWAITING_SERVICE_CONSIDERATION);

        stubDocumentGeneratorServiceBaseOnContextPath(
            ServiceRefusalOrderTask.FileMetadata.DEEMED_TEMPLATE_ID,
            singletonMap(DOCUMENT_CASE_DETAILS_JSON_KEY, ObjectMapperTestUtil.convertObject(ccdCallbackRequest.getCaseDetails(), Map.class)),
            ServiceRefusalOrderTask.FileMetadata.DEEMED_DOCUMENT_TYPE
        );

        MvcResult mvcResult = webClient.perform(post(SERVICE_DECISION_API + "/final")
            .header(AUTHORIZATION, AUTH_TOKEN)
            .content(convertObjectToJsonString(ccdCallbackRequest))
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andReturn();

        String responseData = mvcResult.getResponse().getContentAsString();

        assertThat(responseData, isJson());
        assertThat(responseData, hasNoJsonPath("$.ServiceRefusalDraft"));
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
            D8DOCUMENTS_GENERATED,
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
            .ctscContactDetails(ctscContactDetailsDataProviderService.getCtscContactDetails())
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

    private Map<String, Object> buildServiceRefusalOrderCaseData(String serviceApplicationType, GeneratedDocumentInfo serviceRefusalDraft) {
        Map<String, Object> baseData = buildInputCaseData(serviceApplicationType);
        baseData.remove(SERVICE_APPLICATION_GRANTED);

        List<GeneratedDocumentInfo> generatedDocumentInfoList = new ArrayList<>();
        Map<String, Object> payload = ImmutableMap.of(  //TODO: refactor
            SERVICE_APPLICATION_GRANTED, NO_VALUE,
            SERVICE_APPLICATION_TYPE, serviceApplicationType,
            SERVICE_REFUSAL_DRAFT, serviceRefusalDraft,
            DOCUMENT_COLLECTION, generatedDocumentInfoList
        );
        baseData.putAll(payload);

        return baseData;
    }

    private GeneratedDocumentInfo generateDocumentInfo(String templateId, String templateFile) {
        return GeneratedDocumentInfo.builder() //TODO: refactor
            .documentType(templateId)
            .fileName(templateFile)
            .url("test.url")
            .build();
    }

    private Set<GeneratedDocumentInfo> getDocumentCollection(Map<String, Object> returnedCaseData) {
        return (LinkedHashSet<GeneratedDocumentInfo>) returnedCaseData.get(DOCUMENT_COLLECTION);
    }
}
