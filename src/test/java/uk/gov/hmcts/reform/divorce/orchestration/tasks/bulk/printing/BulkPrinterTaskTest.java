package uk.gov.hmcts.reform.divorce.orchestration.tasks.bulk.printing;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CaseDetails;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.documentgeneration.GeneratedDocumentInfo;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.DefaultTaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.service.BulkPrintService;

import java.util.HashMap;
import java.util.Map;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyMap;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.BULK_PRINT_ERROR_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CASE_DETAILS_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DOCUMENTS_GENERATED;
import static uk.gov.hmcts.reform.divorce.orchestration.tasks.bulk.printing.BulkPrinterTask.BULK_PRINT_LETTER_TYPE;
import static uk.gov.hmcts.reform.divorce.orchestration.tasks.bulk.printing.BulkPrinterTask.DOCUMENT_TYPES_TO_PRINT;

@RunWith(MockitoJUnitRunner.class)
public class BulkPrinterTaskTest {

    private static final String TEST_CASE_ID = "case-id";
    private static final String TEST_LETTER_TYPE = "test-letter-type";
    private static final String TEST_FIRST_DOCUMENT_TYPE = "test_first_document_type";
    private static final String TEST_SECOND_DOCUMENT_TYPE = "test_second_document_type";

    private DefaultTaskContext context;

    @Mock
    private BulkPrintService bulkPrintService;

    @InjectMocks
    private BulkPrinterTask classUnderTest;

    @Before
    public void setUp() {
        context = new DefaultTaskContext();
        context.setTransientObject(BULK_PRINT_LETTER_TYPE, TEST_LETTER_TYPE);
        context.setTransientObject(DOCUMENT_TYPES_TO_PRINT, asList(TEST_FIRST_DOCUMENT_TYPE, TEST_SECOND_DOCUMENT_TYPE));

        final CaseDetails caseDetails = CaseDetails.builder().caseId(TEST_CASE_ID).build();
        context.setTransientObject(CASE_DETAILS_JSON_KEY, caseDetails);
    }

    @Test
    public void happyPath() {
        final GeneratedDocumentInfo secondDocument = mock(GeneratedDocumentInfo.class);
        final GeneratedDocumentInfo firstDocument = mock(GeneratedDocumentInfo.class);
        final Map<String, GeneratedDocumentInfo> generatedDocuments = new HashMap<>();
        generatedDocuments.put(TEST_SECOND_DOCUMENT_TYPE, secondDocument);
        generatedDocuments.put(TEST_FIRST_DOCUMENT_TYPE, firstDocument);
        context.setTransientObject(DOCUMENTS_GENERATED, generatedDocuments);

        final Map<String, Object> result = classUnderTest.execute(context, emptyMap());

        assertThat(result, is(emptyMap()));
        verify(bulkPrintService).send(TEST_CASE_ID, TEST_LETTER_TYPE, asList(firstDocument, secondDocument));
    }

    @Test
    public void shouldAddNecessaryInfoToContextWhenPrintingSpecifiedDocuments() {
        final GeneratedDocumentInfo firstDocument = mock(GeneratedDocumentInfo.class);
        final GeneratedDocumentInfo secondDocument = mock(GeneratedDocumentInfo.class);
        final GeneratedDocumentInfo thirdDocument = mock(GeneratedDocumentInfo.class);
        final GeneratedDocumentInfo fourthDocument = mock(GeneratedDocumentInfo.class);
        final Map<String, GeneratedDocumentInfo> generatedDocuments = new HashMap<>();
        generatedDocuments.put(TEST_FIRST_DOCUMENT_TYPE, firstDocument);
        generatedDocuments.put(TEST_SECOND_DOCUMENT_TYPE, secondDocument);
        generatedDocuments.put("differentDocumentType1", thirdDocument);
        generatedDocuments.put("differentDocumentType2", fourthDocument);
        context.setTransientObject(DOCUMENTS_GENERATED, generatedDocuments);

        Map<String, Object> returnedPayload = classUnderTest.printSpecifiedDocument(context, emptyMap(),
            "differentLetterType",
            asList("differentDocumentType1", "differentDocumentType2"));
        assertThat(returnedPayload, is(emptyMap()));
        verify(bulkPrintService).send(TEST_CASE_ID, "differentLetterType", asList(thirdDocument, fourthDocument));

        returnedPayload = classUnderTest.execute(context, emptyMap());
        assertThat(returnedPayload, is(emptyMap()));
        verify(bulkPrintService).send(TEST_CASE_ID, TEST_LETTER_TYPE, asList(firstDocument, secondDocument));
    }

    @Test
    public void errorsFromBulkPrintServiceAreReported() {
        final GeneratedDocumentInfo miniPetition = mock(GeneratedDocumentInfo.class);
        final GeneratedDocumentInfo respondentAosLetter = mock(GeneratedDocumentInfo.class);
        final Map<String, GeneratedDocumentInfo> generatedDocuments = new HashMap<>();
        generatedDocuments.put(TEST_SECOND_DOCUMENT_TYPE, miniPetition);
        generatedDocuments.put(TEST_FIRST_DOCUMENT_TYPE, respondentAosLetter);
        context.setTransientObject(DOCUMENTS_GENERATED, generatedDocuments);

        doThrow(new RuntimeException()).when(bulkPrintService).send(anyString(), anyString(), anyList());

        classUnderTest.execute(context, emptyMap());

        assertThat(context.hasTaskFailed(), is(true));
        assertThat(context.getTransientObject(BULK_PRINT_ERROR_KEY), is("Bulk print failed for " + TEST_LETTER_TYPE));
    }

    @Test
    public void packCannotBePrintedWithoutTheFirstDocument() {
        final GeneratedDocumentInfo miniPetition = mock(GeneratedDocumentInfo.class);
        final Map<String, GeneratedDocumentInfo> generatedDocuments = new HashMap<>();
        generatedDocuments.put(TEST_SECOND_DOCUMENT_TYPE, miniPetition);
        context.setTransientObject(DOCUMENTS_GENERATED, generatedDocuments);

        classUnderTest.execute(context, emptyMap());

        verifyZeroInteractions(bulkPrintService);
    }

    @Test
    public void packCannotBePrintedWithoutTheSecondDocument() {
        final GeneratedDocumentInfo respondentAosLetter = mock(GeneratedDocumentInfo.class);
        final Map<String, GeneratedDocumentInfo> generatedDocuments = new HashMap<>();
        generatedDocuments.put(TEST_FIRST_DOCUMENT_TYPE, respondentAosLetter);
        context.setTransientObject(DOCUMENTS_GENERATED, generatedDocuments);

        classUnderTest.execute(context, emptyMap());

        verifyZeroInteractions(bulkPrintService);
    }

}