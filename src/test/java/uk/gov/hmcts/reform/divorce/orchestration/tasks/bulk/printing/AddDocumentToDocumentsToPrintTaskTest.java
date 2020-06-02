package uk.gov.hmcts.reform.divorce.orchestration.tasks.bulk.printing;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.documentgeneration.GeneratedDocumentInfo;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.DefaultTaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.DocumentContentFetcherService;

import java.util.Map;

import static java.util.Arrays.asList;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor.DocumentDataExtractorTest.buildCaseDataWithDocumentsGeneratedList;
import static uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor.DocumentDataExtractorTest.buildCollectionMemberWithDocumentType;
import static uk.gov.hmcts.reform.divorce.orchestration.tasks.bulk.printing.AddDocumentToDocumentsToPrintTask.DOCUMENT_TYPE_TO_ADD_TO_PRINT;
import static uk.gov.hmcts.reform.divorce.orchestration.tasks.bulk.printing.PrepareDataForDocumentGenerationTask.appendAnotherDocumentToBulkPrint;
import static uk.gov.hmcts.reform.divorce.orchestration.tasks.bulk.printing.PrepareDataForDocumentGenerationTaskTest.document;

@RunWith(MockitoJUnitRunner.class)
public class AddDocumentToDocumentsToPrintTaskTest {

    @Mock
    private DocumentContentFetcherService documentContentFetcherService;

    @InjectMocks
    private AddDocumentToDocumentsToPrintTask addDocumentToDocumentsToPrintTask;

    private String TEST_DOCUMENT_TYPE;

    @Before
    public void setup() {
        TEST_DOCUMENT_TYPE = OrchestrationConstants.DECREE_ABSOLUTE_DOCUMENT_TYPE;
        when(documentContentFetcherService.fetchPrintContent(any(GeneratedDocumentInfo.class)))
            .thenReturn(GeneratedDocumentInfo.builder().documentType(TEST_DOCUMENT_TYPE).build());
    }

    @Test
    public void executeAddsFirstDocumentToContext() {
        TaskContext context = new DefaultTaskContext();
        context.setTransientObject(DOCUMENT_TYPE_TO_ADD_TO_PRINT, TEST_DOCUMENT_TYPE);

        Map<String, Object> caseData = buildCaseDataWithDocumentsGeneratedList(
            asList(
                buildCollectionMemberWithDocumentType(TEST_DOCUMENT_TYPE)
            )
        );

        addDocumentToDocumentsToPrintTask.execute(context, caseData);

        Map<String, GeneratedDocumentInfo> documentsToBulkPrint = PrepareDataForDocumentGenerationTask.getDocumentsToBulkPrint(context);
        GeneratedDocumentInfo document = documentsToBulkPrint.get(TEST_DOCUMENT_TYPE);

        assertThat(documentsToBulkPrint.size(), is(1));
        assertThat(document, instanceOf(GeneratedDocumentInfo.class));
        assertThat(document.getDocumentType(), is(TEST_DOCUMENT_TYPE));
    }

    @Test
    public void executeAddsAnotherDocumentToContext() {
        TaskContext context = new DefaultTaskContext();
        context.setTransientObject(DOCUMENT_TYPE_TO_ADD_TO_PRINT, TEST_DOCUMENT_TYPE);
        final GeneratedDocumentInfo existingDocument = document();
        appendAnotherDocumentToBulkPrint(context, existingDocument);

        Map<String, Object> caseData = buildCaseDataWithDocumentsGeneratedList(
            asList(
                buildCollectionMemberWithDocumentType(TEST_DOCUMENT_TYPE)
            )
        );

        addDocumentToDocumentsToPrintTask.execute(context, caseData);

        Map<String, GeneratedDocumentInfo> documentsToBulkPrint = PrepareDataForDocumentGenerationTask.getDocumentsToBulkPrint(context);
        final GeneratedDocumentInfo document = documentsToBulkPrint.get(TEST_DOCUMENT_TYPE);

        assertThat(documentsToBulkPrint.size(), is(2));
        assertThat(documentsToBulkPrint.get(existingDocument.getDocumentType()), instanceOf(GeneratedDocumentInfo.class));
        assertThat(documentsToBulkPrint.get(existingDocument.getDocumentType()).getDocumentType(), is(existingDocument.getDocumentType()));
        assertThat(document, instanceOf(GeneratedDocumentInfo.class));
        assertThat(document.getDocumentType(), is(TEST_DOCUMENT_TYPE));
    }
}
