package uk.gov.hmcts.reform.divorce.orchestration.tasks.bulk.printing;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.DefaultTaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskException;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.Arrays.asList;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.CcdFields.SERVICE_APPLICATION_DOCUMENTS;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CASE_ID_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.COSTS_ORDER_DOCUMENT_TYPE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D8DOCUMENTS_GENERATED;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DOCUMENT_TYPE_COE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DOCUMENT_TYPE_OTHER;
import static uk.gov.hmcts.reform.divorce.orchestration.tasks.servicejourney.OrderToDispenseGenerationTask.FileMetadata.DOCUMENT_TYPE;
import static uk.gov.hmcts.reform.divorce.orchestration.testutil.CaseDataTestHelper.createCollectionMemberDocumentAsMap;

@RunWith(MockitoJUnitRunner.class)
public class ResendExistingDocumentsPrinterTaskTest {
    private static final String LETTER_TYPE_EXISTING_DOCUMENTS_PACK = "existing-documents-pack";
    private static final String EXCEPTION_MSG = "There are no generated documents to resend for case: %s";
    private static final List<String> DOCUMENT_TYPES_TO_SEND = asList(COSTS_ORDER_DOCUMENT_TYPE, DOCUMENT_TYPE_COE, DOCUMENT_TYPE);

    @Mock
    private BulkPrinterTask bulkPrinterTask;

    @InjectMocks
    private ResendExistingDocumentsPrinterTask classUnderTest;

    @Test
    public void shouldCallBulkPrinterWithSpecificParameters() {
        var caseData = caseData();
        var context = new DefaultTaskContext();

        when(bulkPrinterTask.printSpecifiedDocument(any(), any(), any(), any())).thenReturn(caseData);
        var result = classUnderTest.execute(context, caseData);

        assertThat(result, is(caseData));
        verify(bulkPrinterTask).printSpecifiedDocument(context, caseData, LETTER_TYPE_EXISTING_DOCUMENTS_PACK, DOCUMENT_TYPES_TO_SEND);
    }

    private Map<String, Object> caseData() {
        var caseData = new HashMap<String, Object>();
        caseData.put(D8DOCUMENTS_GENERATED, Arrays.asList(
            createCollectionMemberDocumentAsMap("url", COSTS_ORDER_DOCUMENT_TYPE, COSTS_ORDER_DOCUMENT_TYPE),
            createCollectionMemberDocumentAsMap("url", DOCUMENT_TYPE_COE, DOCUMENT_TYPE_COE),
            createCollectionMemberDocumentAsMap("url", DOCUMENT_TYPE_OTHER, "will be filtered out"),
            createCollectionMemberDocumentAsMap("url", "awesome document type", "also will be filtered out")
        ));
        caseData.put(SERVICE_APPLICATION_DOCUMENTS, Arrays.asList(
            createCollectionMemberDocumentAsMap("url", DOCUMENT_TYPE, DOCUMENT_TYPE)
        ));
        return caseData;
    }

    @Test
    public void shouldThrowTaskExceptionIfNoGeneratedDocumentsPresent() throws TaskException {
        var testId = "123";
        var context = new DefaultTaskContext();
        context.setTransientObject(CASE_ID_JSON_KEY, testId);
        var caseData = new HashMap<String, Object>();

        var exception = assertThrows(TaskException.class, () -> classUnderTest.execute(context, caseData));
        assertThat(exception.getMessage(), is(String.format(EXCEPTION_MSG, testId)));
    }
}
