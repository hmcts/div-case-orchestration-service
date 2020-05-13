package uk.gov.hmcts.reform.divorce.orchestration.tasks.bulk.printing;

import com.google.common.collect.ImmutableMap;
import org.junit.Test;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.documentgeneration.GeneratedDocumentInfo;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.DefaultTaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskContext;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class PrepareDataForDocumentGenerationTaskTest {

    @Test
    public void appendAnotherDocumentToBulkPrintCreatesNewListWithOneElementWhenThereWasNoList() {
        TaskContext context = new DefaultTaskContext();

        PrepareDataForDocumentGenerationTask.appendAnotherDocumentToBulkPrint(context, document());

        Map<String, GeneratedDocumentInfo> documents = context.getTransientObject(
            PrepareDataForDocumentGenerationTask.ContextKeys.GENERATED_DOCUMENTS
        );

        assertThat(documents.size(), is(1));
    }

    @Test
    public void appendAnotherDocumentToBulkPrintAddsAnotherDocumentToList() {
        TaskContext context = new DefaultTaskContext();
        Map<String, GeneratedDocumentInfo> existingDocuments = buildExistingDocumentsMap();
        context.setTransientObject(PrepareDataForDocumentGenerationTask.ContextKeys.GENERATED_DOCUMENTS, existingDocuments);

        PrepareDataForDocumentGenerationTask.appendAnotherDocumentToBulkPrint(context, document());

        Map<String, GeneratedDocumentInfo> documents = context.getTransientObject(
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

        Map<String, GeneratedDocumentInfo> documents = context.getTransientObject(
            PrepareDataForDocumentGenerationTask.ContextKeys.GENERATED_DOCUMENTS
        );

        assertThat(documents.size(), is(3));
    }

    @Test
    public void getDocumentToBulkPrintReturnsEmptyMapWhenNoDocumentsProvided() {
        TaskContext context = new DefaultTaskContext();

        Map<String, GeneratedDocumentInfo> documents = PrepareDataForDocumentGenerationTask.getDocumentToBulkPrint(context);

        assertThat(documents.size(), is(0));
    }

    @Test
    public void getDocumentToBulkPrintReturnsMapWithAllDocumentsAdded() {
        TaskContext context = new DefaultTaskContext();

        PrepareDataForDocumentGenerationTask.appendAnotherDocumentToBulkPrint(context, document());
        PrepareDataForDocumentGenerationTask.appendAnotherDocumentToBulkPrint(context, document());
        PrepareDataForDocumentGenerationTask.appendAnotherDocumentToBulkPrint(context, document());

        Map<String, GeneratedDocumentInfo> documents = PrepareDataForDocumentGenerationTask.getDocumentToBulkPrint(context);

        assertThat(documents.size(), is(3));
        documents.values().forEach(documentInfo -> assertThat(documentInfo, instanceOf(GeneratedDocumentInfo.class)));
    }

    private Map<String, GeneratedDocumentInfo> buildExistingDocumentsMap() {
        GeneratedDocumentInfo doc = document();

        return new HashMap<>(ImmutableMap.of(doc.getDocumentType(), document()));
    }

    static GeneratedDocumentInfo document() {
        return GeneratedDocumentInfo.builder().documentType(randomString()).build();
    }

    private static String randomString() {
        byte[] array = new byte[7];
        new Random().nextBytes(array);

        return new String(array, StandardCharsets.UTF_8);
    }
}
