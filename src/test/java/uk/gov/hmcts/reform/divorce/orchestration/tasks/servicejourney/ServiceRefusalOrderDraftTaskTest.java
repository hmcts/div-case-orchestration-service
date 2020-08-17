package uk.gov.hmcts.reform.divorce.orchestration.tasks.servicejourney;

import com.google.common.collect.ImmutableMap;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.bulk.print.DocmosisTemplateVars;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CollectionMember;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.Document;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.DocumentLink;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.document.ApplicationServiceTypes;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.documentgeneration.GeneratedDocumentInfo;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.DefaultTaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.PdfDocumentGenerationService;
import uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor.CtscContactDetailsDataProviderService;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasKey;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.notNullValue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_PETITIONER_FIRST_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_PETITIONER_LAST_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_RESPONDENT_FIRST_NAME;
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
import static uk.gov.hmcts.reform.divorce.orchestration.tasks.servicejourney.ServiceRefusalOrderGenerationTaskTest.TEST_RECEIVED_DATE;
import static uk.gov.hmcts.reform.divorce.orchestration.testutil.TaskTestHelper.createGeneratedDocument;
import static uk.gov.hmcts.reform.divorce.orchestration.util.ServiceApplicationRefusalHelperTest.TEST_SERVICE_APPLICATION_REFUSAL_REASON;

public abstract class ServiceRefusalOrderDraftTaskTest {

    private static final String TEST_URL = "https://ds.url.com";
    private static final String TEST_FILE_NAME = "file_name";

    @Mock
    protected CtscContactDetailsDataProviderService ctscContactDetailsDataProviderService;

    @Mock
    private PdfDocumentGenerationService pdfDocumentGenerationService;

    @Captor
    private ArgumentCaptor<DocmosisTemplateVars> templateVarsArgumentCaptor;

    protected abstract ServiceRefusalOrderDraftTask getTask();

    protected abstract String getTemplateId();

    protected abstract String documentType();

    protected abstract String getServiceApplicationType();

    protected void shouldGenerateAndAddDraftDocument() {
        Map<String, Object> caseData = setUpFixturesForDraftAndReturnTestDataWith(getServiceApplicationType(), documentType());

        Map<String, Object> returnedCaseData = getTask().execute(prepareTaskContext(), caseData);

        runCommonDraftDocumentAssertions(returnedCaseData);
        runCommonDraftDocumentVerifications(getTemplateId());
    }

    protected void shouldNotGenerateOrAddDraftDocument() {
        Map<String, Object> caseData = buildServiceApplicationGrantedCaseData();

        Map<String, Object> returnedCaseData = getTask().execute(prepareTaskContext(), caseData);

        assertThat(returnedCaseData, is(caseData));
        runCommonVerificationsWhenNothingIsExpected();
        runCommonAssertionsWhenNothingIsExpected(returnedCaseData);
    }

    private Map<String, Object> setUpFixturesForDraftAndReturnTestDataWith(String serviceType, String documentType) {
        Map<String, Object> caseData = buildRefusalOrderData(serviceType);
        GeneratedDocumentInfo generatedDocument = createGeneratedDocument(TEST_URL, documentType, TEST_FILE_NAME);

        when(ctscContactDetailsDataProviderService.getCtscContactDetails()).thenReturn(CTSC_CONTACT);
        when(pdfDocumentGenerationService.generatePdf(any(DocmosisTemplateVars.class), any(), eq(AUTH_TOKEN))).thenReturn(generatedDocument);

        return caseData;
    }

    private void runCommonVerificationsWhenNothingIsExpected() {
        verify(pdfDocumentGenerationService, never()).generatePdf(any(), anyString(), eq(AUTH_TOKEN));
        verify(ctscContactDetailsDataProviderService, never()).getCtscContactDetails();
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
        payload.put(SERVICE_APPLICATION_REFUSAL_REASON, TEST_SERVICE_APPLICATION_REFUSAL_REASON);

        payload.put(SERVICE_APPLICATION_GRANTED, NO_VALUE);
        payload.put(SERVICE_APPLICATION_TYPE, serviceType);
        payload.put(SERVICE_REFUSAL_DRAFT, documentLink);

        payload.put(D8DOCUMENTS_GENERATED, documentCollection);

        return payload;
    }

    private Map<String, Object> buildServiceApplicationGrantedCaseData() {
        Map<String, Object> caseData = ImmutableMap.of(
            CASE_ID_JSON_KEY, TEST_CASE_ID,
            SERVICE_APPLICATION_GRANTED, YES_VALUE,
            SERVICE_APPLICATION_TYPE, ApplicationServiceTypes.DEEMED
        );
        return caseData;
    }

    private void runCommonDraftDocumentVerifications(String templateId) {
        verify(ctscContactDetailsDataProviderService).getCtscContactDetails();
        verify(pdfDocumentGenerationService).generatePdf(templateVarsArgumentCaptor.capture(), eq(templateId), eq(AUTH_TOKEN));
    }

    private void runCommonDraftDocumentAssertions(Map<String, Object> returnedCaseData) {
        assertThat(returnedCaseData, notNullValue());
        assertThat(returnedCaseData, hasKey(SERVICE_REFUSAL_DRAFT));
        assertThat(getDocumentCollection(returnedCaseData), hasSize(0));

        DocumentLink documentLink = (DocumentLink) returnedCaseData.get(SERVICE_REFUSAL_DRAFT);
        assertThat(documentLink.getDocumentFilename(), is(TEST_FILE_NAME + ".pdf"));
        assertThat(documentLink.getDocumentBinaryUrl(), is(TEST_URL + "/binary"));
        assertThat(documentLink.getDocumentUrl(), is(TEST_URL));
    }

    private void runCommonAssertionsWhenNothingIsExpected(Map<String, Object> returnedCaseData) {
        assertThat(returnedCaseData, notNullValue());
        assertThat(returnedCaseData, not(hasKey(SERVICE_REFUSAL_DRAFT)));
    }

    private List<CollectionMember<Document>> getDocumentCollection(Map<String, Object> returnedCaseData) {
        return (List<CollectionMember<Document>>) returnedCaseData.get(D8DOCUMENTS_GENERATED);
    }

    private DocumentLink getDocumentLink() {
        DocumentLink documentLink = new DocumentLink();
        documentLink.setDocumentBinaryUrl("binary_url");
        documentLink.setDocumentFilename("file_name");
        documentLink.setDocumentUrl("url");
        return documentLink;
    }

    private static TaskContext prepareTaskContext() {
        TaskContext context = new DefaultTaskContext();
        context.setTransientObject(CASE_ID_JSON_KEY, TEST_CASE_ID);
        context.setTransientObject(AUTH_TOKEN_JSON_KEY, AUTH_TOKEN);
        return context;
    }

}