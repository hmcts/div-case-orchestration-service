package uk.gov.hmcts.reform.divorce.orchestration.tasks;

import com.google.common.collect.ImmutableMap;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentMatcher;
import org.mockito.internal.hamcrest.HamcrestArgumentMatcher;
import uk.gov.hmcts.reform.bsp.common.model.document.CtscContactDetails;
import uk.gov.hmcts.reform.divorce.model.documentupdate.GeneratedDocumentInfo;
import uk.gov.hmcts.reform.divorce.orchestration.client.DocumentGeneratorClient;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CaseDetails;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.documentgeneration.DocumentGenerationRequest;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.documentgeneration.GenerateDocumentRequest;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.DefaultTaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskException;
import uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor.CtscContactDetailsDataProviderService;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static java.util.Arrays.asList;
import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.hasItems;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasEntry;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertThrows;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.CcdFields.COURT_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.AUTH_TOKEN_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CASE_DETAILS_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CASE_ID_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.COURT_CONTACT_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CTSC_CONTACT_DETAILS_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DOCUMENT_CASE_DETAILS_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DOCUMENT_COLLECTION;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.constants.TaskContextConstants.DN_COURT_DETAILS;

public class MultipleDocumentGenerationTaskTest {

    private static final String DOCUMENT_GENERATION_REQUESTS_KEY = "documentGenerationRequests";

    private static final String FIRST_DOCUMENT_TEMPLATE_ID = "a";
    private static final String FIRST_DOCUMENT_TYPE = "b";
    private static final String FIRST_DOCUMENT_FILE_NAME = "c";
    private GeneratedDocumentInfo firstExpectedDocument;
    private static final String SECOND_DOCUMENT_TEMPLATE_ID = "d";
    private static final String SECOND_DOCUMENT_TYPE = "e";
    private static final String SECOND_DOCUMENT_FILE_NAME = "f";
    private GeneratedDocumentInfo secondExpectedDocument;

    private Map<String, Object> payload;
    private CaseDetails caseDetails;
    private TaskContext taskContext;

    private DocumentGeneratorClient mockDocumentGeneratorClient;

    private CtscContactDetailsDataProviderService ctscContactDetailsDataProviderService;

    private MultipleDocumentGenerationTask classUnderTest;

    @Before
    public void setUp() {
        mockDocumentGeneratorClient = mock(DocumentGeneratorClient.class);
        ctscContactDetailsDataProviderService = mock(CtscContactDetailsDataProviderService.class);

        DocumentGenerationTask documentGenerationTask =
            new DocumentGenerationTask(mockDocumentGeneratorClient, ctscContactDetailsDataProviderService);

        when(ctscContactDetailsDataProviderService.getCtscContactDetails())
            .thenReturn(getCtscContactDetails());

        classUnderTest = new MultipleDocumentGenerationTask(documentGenerationTask);

        payload = new HashMap<>();
        payload.put("testKey1", "testValue1");
        payload.put(CTSC_CONTACT_DETAILS_KEY, getCtscContactDetails());
        caseDetails = CaseDetails.builder()
            .caseId(TEST_CASE_ID)
            .caseData(payload)
            .build();

        firstExpectedDocument = GeneratedDocumentInfo.builder()
            .documentType(FIRST_DOCUMENT_TYPE)
            .fileName(FIRST_DOCUMENT_FILE_NAME + TEST_CASE_ID)
            .build();

        secondExpectedDocument = GeneratedDocumentInfo.builder()
            .documentType(SECOND_DOCUMENT_TYPE)
            .fileName(SECOND_DOCUMENT_FILE_NAME + TEST_CASE_ID)
            .build();

        taskContext = new DefaultTaskContext();
        taskContext.setTransientObject(CASE_ID_JSON_KEY, TEST_CASE_ID);
        taskContext.setTransientObject(AUTH_TOKEN_JSON_KEY, AUTH_TOKEN);
        taskContext.setTransientObject(CASE_DETAILS_JSON_KEY, caseDetails);

        List<DocumentGenerationRequest> documentGenerationRequests = asList(
            new DocumentGenerationRequest(FIRST_DOCUMENT_TEMPLATE_ID, FIRST_DOCUMENT_TYPE, FIRST_DOCUMENT_FILE_NAME),
            new DocumentGenerationRequest(SECOND_DOCUMENT_TEMPLATE_ID, SECOND_DOCUMENT_TYPE, SECOND_DOCUMENT_FILE_NAME)
        );
        taskContext.setTransientObject(DOCUMENT_GENERATION_REQUESTS_KEY, documentGenerationRequests);
    }

    @Test
    public void callsDocumentGeneratorAndStoresGeneratedDocument() throws TaskException {
        final GeneratedDocumentInfo firstExpectedDocument = GeneratedDocumentInfo.builder()
            .documentType(FIRST_DOCUMENT_TYPE)
            .fileName(FIRST_DOCUMENT_FILE_NAME + TEST_CASE_ID)
            .build();

        final GeneratedDocumentInfo secondExpectedDocument = GeneratedDocumentInfo.builder()
            .documentType(FIRST_DOCUMENT_TYPE)
            .fileName(FIRST_DOCUMENT_FILE_NAME + TEST_CASE_ID)
            .build();

        //given
        when(mockDocumentGeneratorClient.generatePDF(
            argThat(matchesDocumentInputParameters(FIRST_DOCUMENT_TEMPLATE_ID, caseDetails)), eq(AUTH_TOKEN))).thenReturn(firstExpectedDocument);
        when(mockDocumentGeneratorClient.generatePDF(
            argThat(matchesDocumentInputParameters(SECOND_DOCUMENT_TEMPLATE_ID, caseDetails)), eq(AUTH_TOKEN))).thenReturn(secondExpectedDocument);

        //when
        Map<String, Object> returnedPayload = classUnderTest.execute(taskContext, payload);

        //then
        assertThat(returnedPayload, hasEntry("testKey1", "testValue1"));
        final Set<GeneratedDocumentInfo> documentCollection = taskContext.getTransientObject(DOCUMENT_COLLECTION);
        assertThat(documentCollection, hasSize(2));
        assertThat(documentCollection, hasItems(firstExpectedDocument, secondExpectedDocument));

        verify(mockDocumentGeneratorClient).generatePDF(
            argThat(matchesDocumentInputParameters(FIRST_DOCUMENT_TEMPLATE_ID, caseDetails)), eq(AUTH_TOKEN));
        verify(mockDocumentGeneratorClient).generatePDF(
            argThat(matchesDocumentInputParameters(SECOND_DOCUMENT_TEMPLATE_ID, caseDetails)), eq(AUTH_TOKEN));
    }

    @Test
    public void callsDocumentGeneratorAndStoresGeneratedDocumentWithDnCourtDetails() throws TaskException {
        Map<String, Object> dnCourtDetails = ImmutableMap.of(
            COURT_NAME, "TestCourt",
            COURT_CONTACT_JSON_KEY, "TestContact"
        );
        taskContext.setTransientObject(DN_COURT_DETAILS, dnCourtDetails);
        Map<String, Object> caseData = new HashMap<>(payload);
        caseData.putAll(dnCourtDetails);
        caseData.put(CTSC_CONTACT_DETAILS_KEY, getCtscContactDetails());

        CaseDetails dnCaseDetails = CaseDetails.builder()
            .caseId(TEST_CASE_ID)
            .caseData(caseData)
            .build();

        //given
        when(mockDocumentGeneratorClient.generatePDF(
            argThat(matchesDocumentInputParameters(FIRST_DOCUMENT_TEMPLATE_ID, dnCaseDetails)), eq(AUTH_TOKEN))).thenReturn(firstExpectedDocument);
        when(mockDocumentGeneratorClient.generatePDF(
            argThat(matchesDocumentInputParameters(SECOND_DOCUMENT_TEMPLATE_ID, dnCaseDetails)), eq(AUTH_TOKEN))).thenReturn(secondExpectedDocument);

        //when
        Map<String, Object> returnedPayload = classUnderTest.execute(taskContext, payload);

        //then
        assertThat(returnedPayload, hasEntry("testKey1", "testValue1"));
        final LinkedHashSet<GeneratedDocumentInfo> documentCollection = taskContext.getTransientObject(DOCUMENT_COLLECTION);
        assertThat(documentCollection, hasSize(2));
        assertThat(documentCollection, hasItems(firstExpectedDocument, secondExpectedDocument));

        verify(mockDocumentGeneratorClient).generatePDF(
            argThat(matchesDocumentInputParameters(FIRST_DOCUMENT_TEMPLATE_ID, dnCaseDetails)), eq(AUTH_TOKEN));
        verify(mockDocumentGeneratorClient).generatePDF(
            argThat(matchesDocumentInputParameters(SECOND_DOCUMENT_TEMPLATE_ID, dnCaseDetails)), eq(AUTH_TOKEN));
    }

    @Test
    public void shouldThrowTaskExceptionIfDocumentListIsNotPassed() throws TaskException {
        DefaultTaskContext context = new DefaultTaskContext();
        HashMap<String, Object> caseData = new HashMap<>();

        TaskException exception = assertThrows(TaskException.class,
            () -> classUnderTest.execute(context, caseData));
        assertThat(exception.getMessage(), is("Could not find a list of document generation requests"));
    }

    @Test
    public void shouldThrowTaskExceptionIfDocumentListIsEmpty() throws TaskException {
        DefaultTaskContext context = new DefaultTaskContext();
        context.setTransientObject(DOCUMENT_GENERATION_REQUESTS_KEY, new ArrayList<>());
        HashMap<String, Object> caseData = new HashMap<>();

        TaskException exception = assertThrows(TaskException.class,
            () -> classUnderTest.execute(context, caseData));
        assertThat(exception.getMessage(), is("Could not find a list of document generation requests"));
    }

    protected static ArgumentMatcher<GenerateDocumentRequest> matchesDocumentInputParameters(String documentTemplateId, CaseDetails caseDetails) {
        return new HamcrestArgumentMatcher<>(allOf(
            hasProperty("template", is(documentTemplateId)),
            hasProperty("values",
                hasEntry(equalTo(DOCUMENT_CASE_DETAILS_JSON_KEY), hasProperty("caseData", equalTo(caseDetails.getCaseData())))
            )
        ));
    }

    protected CtscContactDetails getCtscContactDetails() {
        return CtscContactDetails
            .builder()
            .serviceCentre("Courts and Tribunals Service Centre")
            .careOf("c/o HMCTS Digital Divorce")
            .centreName("HMCTS Digital Divorce")
            .poBox("PO Box 12706")
            .town("Harlow")
            .postcode("CM20 9QT")
            .emailAddress("divorcecase@justice.gov.uk")
            .phoneNumber("0300 303 0642")
            .openingHours("8am to 6pm, Monday to Friday")
            .build();
    }
}