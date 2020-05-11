package uk.gov.hmcts.reform.divorce.orchestration.tasks.bulk.printing;

import org.junit.Test;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.documentgeneration.GeneratedDocumentInfo;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.DefaultTaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskContext;

import java.util.ArrayList;
import java.util.List;

import static java.util.Arrays.asList;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class PrepareDataForDocumentGenerationTaskTest {

    @Test
    public void appendAnotherDocumentToBulkPrintCreatesNewListWithOneElementWhenThereWasNoList() {
        TaskContext context = new DefaultTaskContext();

        PrepareDataForDocumentGenerationTask.appendAnotherDocumentToBulkPrint(context, document());

        List<GeneratedDocumentInfo> documents = context.getTransientObject(
            PrepareDataForDocumentGenerationTask.ContextKeys.GENERATED_DOCUMENTS
        );

        assertThat(documents.size(), is(1));
    }

    @Test
    public void appendAnotherDocumentToBulkPrintAddsAnotherDocumentToList() {
        TaskContext context = new DefaultTaskContext();
        List<GeneratedDocumentInfo> existingDocuments = new ArrayList<>(asList(document()));
        context.setTransientObject(PrepareDataForDocumentGenerationTask.ContextKeys.GENERATED_DOCUMENTS, existingDocuments);

        PrepareDataForDocumentGenerationTask.appendAnotherDocumentToBulkPrint(context, document());

        List<GeneratedDocumentInfo> documents = context.getTransientObject(
            PrepareDataForDocumentGenerationTask.ContextKeys.GENERATED_DOCUMENTS
        );

        assertThat(documents.size(), is(2));
    }

    @Test
    public void appendAnotherDocumentToBulkPrintCalledMultipleTimesAddsDocumentEachTime() {
        TaskContext context = new DefaultTaskContext();

        PrepareDataForDocumentGenerationTask.appendAnotherDocumentToBulkPrint(context, document());
        PrepareDataForDocumentGenerationTask.appendAnotherDocumentToBulkPrint(context, document());
        PrepareDataForDocumentGenerationTask.appendAnotherDocumentToBulkPrint(context, document());

        List<GeneratedDocumentInfo> documents = context.getTransientObject(
            PrepareDataForDocumentGenerationTask.ContextKeys.GENERATED_DOCUMENTS
        );

        assertThat(documents.size(), is(3));
    }

    private static GeneratedDocumentInfo document() {
        return GeneratedDocumentInfo.builder().build();
    }
}
