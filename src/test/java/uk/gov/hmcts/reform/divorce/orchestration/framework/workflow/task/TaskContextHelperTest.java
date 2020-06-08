package uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task;

import org.junit.Test;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.documentgeneration.GeneratedDocumentInfo;

import java.util.Arrays;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.hasSize;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DOCUMENT_COLLECTION;
import static uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskContextHelper.addAllToContextDocumentCollection;
import static uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskContextHelper.addToContextDocumentCollection;

public class TaskContextHelperTest {

    @Test
    public void addToContextDocumentCollectionAddsGeneratedDocumentInfo() {
        TaskContext context = new DefaultTaskContext();
        GeneratedDocumentInfo generatedDocumentInfo = GeneratedDocumentInfo
            .builder()
            .url("url")
            .documentType("docType")
            .build();

        addToContextDocumentCollection(context, generatedDocumentInfo);

        assertThat(context.getTransientObject(DOCUMENT_COLLECTION), hasSize(1));
        assertThat(context.getTransientObject(DOCUMENT_COLLECTION), contains(generatedDocumentInfo));

    }

    @Test
    public void addAllToContextDocumentCollectionAddsGeneratedDocumentInfoWithoutDuplicates() {
        TaskContext context = new DefaultTaskContext();

        List<GeneratedDocumentInfo> generatedDocumentInfos = buildGeneratedDocuments();

        addAllToContextDocumentCollection(context, generatedDocumentInfos);

        assertThat(context.getTransientObject(DOCUMENT_COLLECTION), hasSize(2));
        assertThat(context.getTransientObject(DOCUMENT_COLLECTION), containsInAnyOrder(generatedDocumentInfos.get(0), generatedDocumentInfos.get(1)));
    }

    @Test(expected = InvalidDataForTaskException.class)
    public void addAllToContextDocumentCollectionThrowsWhenAddingAlreadyExisting() {
        TaskContext context = new DefaultTaskContext();

        List<GeneratedDocumentInfo> generatedDocumentInfos = buildGeneratedDocuments();
        generatedDocumentInfos.add(GeneratedDocumentInfo
            .builder()
            .url("url")
            .documentType("docType2")
            .build());

        addAllToContextDocumentCollection(context, generatedDocumentInfos);
    }

    private List<GeneratedDocumentInfo> buildGeneratedDocuments() {
        GeneratedDocumentInfo generatedDocumentInfo1 = GeneratedDocumentInfo
            .builder()
            .url("url")
            .documentType("docType1")
            .build();
        GeneratedDocumentInfo generatedDocumentInfo2 = GeneratedDocumentInfo
            .builder()
            .url("url")
            .documentType("docType2")
            .build();

        return Arrays.asList(generatedDocumentInfo1, generatedDocumentInfo2);
    }

}