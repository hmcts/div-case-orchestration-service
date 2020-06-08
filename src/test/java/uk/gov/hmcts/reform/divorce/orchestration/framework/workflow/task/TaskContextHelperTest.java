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

public class TaskContextHelperTest  {

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
        GeneratedDocumentInfo generatedDocumentInfo3 = GeneratedDocumentInfo
            .builder()
            .url("url")
            .documentType("docType2")
            .build();

        List<GeneratedDocumentInfo> generatedDocumentInfos = Arrays.asList(generatedDocumentInfo1, generatedDocumentInfo2, generatedDocumentInfo3);

        addAllToContextDocumentCollection(context, generatedDocumentInfos);

        assertThat(context.getTransientObject(DOCUMENT_COLLECTION), hasSize(2));
        assertThat(context.getTransientObject(DOCUMENT_COLLECTION), containsInAnyOrder(generatedDocumentInfo1,generatedDocumentInfo2));

    }

}