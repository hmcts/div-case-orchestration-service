package uk.gov.hmcts.reform.divorce.orchestration.tasks.servicejourney;

import com.google.common.collect.ImmutableMap;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.bulk.print.DocmosisTemplateVars;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CollectionMember;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.Document;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.DocumentLink;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.document.ApplicationServiceTypes;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.document.ServiceApplicationRefusalOrder;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.documentgeneration.GeneratedDocumentInfo;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.DefaultTaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.PdfDocumentGenerationService;
import uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor.CtscContactDetailsDataProviderService;
import uk.gov.hmcts.reform.divorce.orchestration.util.CcdUtil;
import uk.gov.hmcts.reform.divorce.orchestration.util.mapper.CcdMappers;
import uk.gov.hmcts.reform.divorce.utils.DateUtils;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_PETITIONER_FIRST_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_PETITIONER_FULL_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_PETITIONER_LAST_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_RESPONDENT_FIRST_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_RESPONDENT_FULL_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_RESPONDENT_LAST_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.CcdFields.RECEIVED_SERVICE_APPLICATION_DATE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.CcdFields.SERVICE_APPLICATION_GRANTED;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.CcdFields.SERVICE_APPLICATION_REFUSAL_REASON;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.CcdFields.SERVICE_APPLICATION_TYPE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.CcdFields.SERVICE_REFUSAL_DRAFT;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.AUTH_TOKEN_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CASE_ID_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D8DOCUMENTS_GENERATED;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NO_VALUE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.YES_VALUE;
import static uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor.FullNamesDataExtractor.CaseDataKeys.PETITIONER_FIRST_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor.FullNamesDataExtractor.CaseDataKeys.PETITIONER_LAST_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor.FullNamesDataExtractor.CaseDataKeys.RESPONDENT_FIRST_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor.FullNamesDataExtractor.CaseDataKeys.RESPONDENT_LAST_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.tasks.bulk.printing.BulkPrintTestData.CTSC_CONTACT;
import static uk.gov.hmcts.reform.divorce.orchestration.tasks.servicejourney.ServiceDecisionOrderGenerationTaskTest.TEST_RECEIVED_DATE;
import static uk.gov.hmcts.reform.divorce.orchestration.tasks.servicejourney.ServiceRefusalOrderTask.DRAFT_DECISION;
import static uk.gov.hmcts.reform.divorce.orchestration.tasks.servicejourney.ServiceRefusalOrderTask.FINAL_DECISION;
import static uk.gov.hmcts.reform.divorce.orchestration.testutil.TaskTestHelper.createGeneratedDocument;
import static uk.gov.hmcts.reform.divorce.orchestration.workflows.servicejourney.ServiceRefusalOrderWorkflow.SERVICE_DECISION;

@RunWith(MockitoJUnitRunner.class)
public class ServiceRefusalOrderTaskTest {

    private static final String TEST_URL = "https://ds.url.com";
    private static final String TEST_FILE_NAME = "file_name";
    private static final String TEST_REFUSAL_REASON = "Lorem ipsum dolor sit amet, consectetur adipiscing elit.";

    @InjectMocks
    private ServiceRefusalOrderTask serviceRefusalOrderTask;

    @Mock
    protected CtscContactDetailsDataProviderService ctscContactDetailsDataProviderService;

    @Mock
    private PdfDocumentGenerationService pdfDocumentGenerationService;

    @Mock
    private CcdUtil ccdUtil;

    @Captor
    private ArgumentCaptor<List<GeneratedDocumentInfo>> documentsCaptor;

    @Captor
    private ArgumentCaptor<DocmosisTemplateVars> templateVarsArgumentCaptor;

    @Before
    public void setup() {
        when(ctscContactDetailsDataProviderService.getCtscContactDetails()).thenReturn(CTSC_CONTACT);
    }

    @Test
    public void shouldGenerateRefusalOrderDocumentIfServiceRefusalIsDeemedAndFinal() {
        String serviceType = ApplicationServiceTypes.DEEMED;
        String documentType = ServiceRefusalOrderTask.FileMetadata.DEEMED_DOCUMENT_TYPE;
        String templateId = ServiceRefusalOrderTask.FileMetadata.DEEMED_TEMPLATE_ID;

        Map<String, Object> caseData = setUpFixturesAndReturnTestDataWith(serviceType, documentType);

        Map<String, Object> returnedCaseData = serviceRefusalOrderTask.execute(prepareTaskContext(FINAL_DECISION), caseData);

        runCommonDocumentAssertions(returnedCaseData);
        runCommonDocumentVerifications(templateId);

        verifyDocumentTemplateVariables(templateVarsArgumentCaptor);
        verifyGeneratedDocument(documentsCaptor, documentType,  getGeneratedDocumentFileName(returnedCaseData));
    }

    @Test
    public void shouldGenerateRefusalOrderDocumentIfServiceRefusalIsDispensedAndFinal() {
        String serviceType = ApplicationServiceTypes.DISPENSED;
        String documentType = ServiceRefusalOrderTask.FileMetadata.DISPENSE_DOCUMENT_TYPE;
        String templateId = ServiceRefusalOrderTask.FileMetadata.DISPENSE_TEMPLATE_ID;

        Map<String, Object> caseData = setUpFixturesAndReturnTestDataWith(serviceType, documentType);

        Map<String, Object> returnedCaseData = serviceRefusalOrderTask.execute(prepareTaskContext(FINAL_DECISION), caseData);

        runCommonDocumentAssertions(returnedCaseData);
        runCommonDocumentVerifications(templateId);

        verifyDocumentTemplateVariables(templateVarsArgumentCaptor);
        verifyGeneratedDocument(documentsCaptor, documentType,  getGeneratedDocumentFileName(returnedCaseData));
    }

    @Test
    public void shouldGenerateDraftDocumentWhenServiceRefusalIsDeemedAndDraft() {
        String serviceType = ApplicationServiceTypes.DEEMED;
        String documentType = ServiceRefusalOrderTask.FileMetadata.DEEMED_DOCUMENT_TYPE;
        String templateId = ServiceRefusalOrderTask.FileMetadata.DEEMED_TEMPLATE_ID;

        Map<String, Object> caseData = setUpFixturesForDraftAndReturnTestDataWith(serviceType, documentType);

        Map<String, Object> returnedCaseData = serviceRefusalOrderTask.execute(prepareTaskContext(DRAFT_DECISION), caseData);

        runCommonDraftDocumentAssertions(returnedCaseData);
        runCommonDraftDocumentVerifications(templateId);
    }

    @Test
    public void shouldGenerateDraftDocumentWhenServiceRefusalIsDispensedAndDraft() {
        String serviceType = ApplicationServiceTypes.DISPENSED;
        String documentType = ServiceRefusalOrderTask.FileMetadata.DISPENSE_DOCUMENT_TYPE;
        String templateId = ServiceRefusalOrderTask.FileMetadata.DISPENSE_TEMPLATE_ID;

        Map<String, Object> caseData = setUpFixturesForDraftAndReturnTestDataWith(serviceType, documentType);

        Map<String, Object> returnedCaseData = serviceRefusalOrderTask.execute(prepareTaskContext(DRAFT_DECISION), caseData);

        runCommonDraftDocumentAssertions(returnedCaseData);
        runCommonDraftDocumentVerifications(templateId);
    }

    @Test
    public void shouldNotGenerateAnyDocumentOrDraftWhenServiceApplicationIsGrantedAndMidEventIsTriggered() {
        Map<String, Object> caseData = buildServiceApplicationGrantedCaseData();

        Map<String, Object> returnedCaseData = serviceRefusalOrderTask.execute(prepareTaskContext(DRAFT_DECISION), caseData);

        assertThat(returnedCaseData, is(caseData));
        runCommonVerificationsWhenNothingIsExpected();
    }

    @Test
    public void shouldNotGenerateAnyDocumentOrDraftWhenServiceApplicationIsGrantedAndSubmitIsTriggered() {
        Map<String, Object> caseData = buildServiceApplicationGrantedCaseData();

        Map<String, Object> returnedCaseData = serviceRefusalOrderTask.execute(prepareTaskContext(FINAL_DECISION), caseData);

        assertThat(returnedCaseData, is(caseData));
        runCommonVerificationsWhenNothingIsExpected();
    }

    @Test
    public void shouldReturnDeemedDocumentTemplateIdValue() {
        String expectedTemplateId = ServiceRefusalOrderTask.FileMetadata.DEEMED_TEMPLATE_ID;
        Map<String, Object> data = ImmutableMap.of(SERVICE_APPLICATION_TYPE, ApplicationServiceTypes.DEEMED);

        String deemedRefusalOrderTemplateId = serviceRefusalOrderTask.getRefusalOrderTemplateId(data);

        assertThat(expectedTemplateId, is(deemedRefusalOrderTemplateId));
    }

    @Test
    public void shouldReturnDispensedDocumentTemplateIdValue() {
        String expectedTemplateId = ServiceRefusalOrderTask.FileMetadata.DISPENSE_TEMPLATE_ID;
        Map<String, Object> data = ImmutableMap.of(SERVICE_APPLICATION_TYPE, ApplicationServiceTypes.DISPENSED);

        String dispensedRefusalOrderTemplateId = serviceRefusalOrderTask.getRefusalOrderTemplateId(data);

        assertThat(expectedTemplateId, is(dispensedRefusalOrderTemplateId));
    }

    private void runCommonDocumentVerifications(String templateId) {
        verify(pdfDocumentGenerationService).generatePdf(templateVarsArgumentCaptor.capture(), eq(templateId), eq(AUTH_TOKEN));
        verify(ccdUtil).addNewDocumentsToCaseData(anyMap(), documentsCaptor.capture());
    }

    private void runCommonDocumentAssertions(Map<String, Object> returnedCaseData) {
        assertThat(returnedCaseData, notNullValue());
        assertThat(getDocumentCollection(returnedCaseData), hasSize(1));
        assertThat(returnedCaseData.get(SERVICE_REFUSAL_DRAFT), is(nullValue()));
    }

    private void runCommonDraftDocumentAssertions(Map<String, Object> returnedCaseData) {
        DocumentLink documentLink = (DocumentLink) returnedCaseData.get(SERVICE_REFUSAL_DRAFT);
        assertThat(returnedCaseData, notNullValue());
        assertThat(getDocumentCollection(returnedCaseData), hasSize(0));
        assertThat(documentLink.getDocumentFilename(), is(TEST_FILE_NAME + ".pdf"));
        assertThat(documentLink.getDocumentBinaryUrl(), is(TEST_URL + "/binary"));
        assertThat(documentLink.getDocumentUrl(), is(TEST_URL));
    }

    private void runCommonDraftDocumentVerifications(String templateId) {
        verify(pdfDocumentGenerationService).generatePdf(templateVarsArgumentCaptor.capture(), eq(templateId), eq(AUTH_TOKEN));
        verify(ccdUtil, never()).addNewDocumentsToCaseData(anyMap(), anyList());
    }

    private void runCommonVerificationsWhenNothingIsExpected() {
        verify(pdfDocumentGenerationService, never()).generatePdf(any(), anyString(), eq(AUTH_TOKEN));
        verify(ccdUtil, never()).addNewDocumentsToCaseData(anyMap(), anyList());
    }

    private void verifyGeneratedDocument(ArgumentCaptor<List<GeneratedDocumentInfo>> documentsCaptor, String documentType, String fileName) {
        List<GeneratedDocumentInfo> documentInfoList = documentsCaptor.getValue();
        GeneratedDocumentInfo generatedDocumentInfo = documentInfoList.get(0);

        assertThat(documentInfoList, hasSize(1));
        assertThat(generatedDocumentInfo.getDocumentType(), is(documentType));
        assertThat(generatedDocumentInfo.getFileName(), is(fileName));
    }

    private void verifyDocumentTemplateVariables(ArgumentCaptor<DocmosisTemplateVars> templateVarsArgumentCaptor) {
        DocmosisTemplateVars docmosisTemplateVars = templateVarsArgumentCaptor.getValue();
        assertEquals(getServiceDecisionRefusalOrder(), docmosisTemplateVars);
    }

    private Map<String, Object> buildRefusalOrderData(String serviceType) {
        List<CollectionMember<Document>> documentCollection = new ArrayList<>();
        DocumentLink documentLink = getDocumentLink();

        Map<String, Object> payload = new HashMap<>();
        payload.put(CASE_ID_JSON_KEY, TEST_CASE_ID);
        payload.put(PETITIONER_FIRST_NAME, TEST_PETITIONER_FIRST_NAME);
        payload.put(PETITIONER_LAST_NAME, TEST_PETITIONER_LAST_NAME);
        payload.put(RESPONDENT_FIRST_NAME, TEST_RESPONDENT_FIRST_NAME);
        payload.put(RESPONDENT_LAST_NAME, TEST_RESPONDENT_LAST_NAME);

        payload.put(RECEIVED_SERVICE_APPLICATION_DATE, TEST_RECEIVED_DATE);
        payload.put(SERVICE_APPLICATION_REFUSAL_REASON, TEST_REFUSAL_REASON);

        payload.put(SERVICE_APPLICATION_GRANTED, NO_VALUE);
        payload.put(SERVICE_APPLICATION_TYPE, serviceType);
        payload.put(SERVICE_REFUSAL_DRAFT, documentLink);

        payload.put(D8DOCUMENTS_GENERATED, documentCollection);

        return payload;
    }

    private Map<String, Object> buildServiceApplicationGrantedCaseData() {
        Map<String, Object> caseData = ImmutableMap.of(
            SERVICE_APPLICATION_GRANTED, YES_VALUE,
            SERVICE_APPLICATION_TYPE, ApplicationServiceTypes.DEEMED
        );
        return caseData;
    }

    private Map<String, Object> setUpFixturesAndReturnTestDataWith(String serviceType, String documentType) {
        Map<String, Object> caseData = buildRefusalOrderData(serviceType);
        GeneratedDocumentInfo generatedDocument = createGeneratedDocument(TEST_URL, documentType, TEST_FILE_NAME);
        Map<String, Object> modifiedCaseData = getModifiedCaseDataWithNewDocument(caseData, generatedDocument);

        when(pdfDocumentGenerationService.generatePdf(any(DocmosisTemplateVars.class), any(), eq(AUTH_TOKEN))).thenReturn(generatedDocument);
        when(ccdUtil.addNewDocumentsToCaseData(anyMap(), anyList())).thenReturn(modifiedCaseData);

        return caseData;
    }

    private Map<String, Object> setUpFixturesForDraftAndReturnTestDataWith(String serviceType, String documentType) {
        Map<String, Object> caseData = buildRefusalOrderData(serviceType);
        GeneratedDocumentInfo generatedDocument = createGeneratedDocument(TEST_URL, documentType, TEST_FILE_NAME);

        when(pdfDocumentGenerationService.generatePdf(any(DocmosisTemplateVars.class), any(), eq(AUTH_TOKEN))).thenReturn(generatedDocument);

        return caseData;
    }

    private DocumentLink getDocumentLink() {
        DocumentLink documentLink = new DocumentLink();
        documentLink.setDocumentBinaryUrl("binary_url");
        documentLink.setDocumentFilename("file_name");
        documentLink.setDocumentUrl("url");
        return documentLink;
    }

    private List<CollectionMember<Document>> getDocumentCollection(Map<String, Object> returnedCaseData) {
        return (List<CollectionMember<Document>>) returnedCaseData.get(D8DOCUMENTS_GENERATED);
    }

    private Map<String, Object> getModifiedCaseDataWithNewDocument(Map<String, Object> caseData, GeneratedDocumentInfo generatedDocument) {
        Map<String, Object> modifiedCaseData = new HashMap();
        modifiedCaseData.putAll(caseData);
        getDocumentCollection(modifiedCaseData).add(CcdMappers.mapDocumentInfoToCcdDocument(generatedDocument));

        return modifiedCaseData;
    }

    private static TaskContext prepareTaskContext(String decision) {
        TaskContext context = new DefaultTaskContext();
        context.setTransientObject(CASE_ID_JSON_KEY, TEST_CASE_ID);
        context.setTransientObject(SERVICE_DECISION, decision);
        context.setTransientObject(AUTH_TOKEN_JSON_KEY, AUTH_TOKEN);
        return context;
    }

    private ServiceApplicationRefusalOrder getServiceDecisionRefusalOrder() {
        return ServiceApplicationRefusalOrder.serviceApplicationRefusalOrderBuilder()
            .ctscContactDetails(CTSC_CONTACT)
            .petitionerFullName(TEST_PETITIONER_FULL_NAME)
            .respondentFullName(TEST_RESPONDENT_FULL_NAME)
            .caseReference(TEST_CASE_ID)
            .receivedServiceApplicationDate(DateUtils.formatDateWithCustomerFacingFormat(TEST_RECEIVED_DATE))
            .serviceApplicationRefusalReason(TEST_REFUSAL_REASON)
            .documentIssuedOn(DateUtils.formatDateWithCustomerFacingFormat(LocalDate.now()))
            .build();
    }

    private String getGeneratedDocumentFileName(Map<String, Object> returnedCaseData) {
        List<CollectionMember<Document>> documentCollection = getDocumentCollection(returnedCaseData);
        CollectionMember<Document> documentCollectionMember = documentCollection.get(0);
        return documentCollectionMember.getValue().getDocumentFileName();
    }
}