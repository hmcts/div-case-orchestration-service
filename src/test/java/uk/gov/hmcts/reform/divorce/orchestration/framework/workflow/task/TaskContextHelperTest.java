package uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task;

import org.junit.Test;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.documentgeneration.GeneratedDocumentInfo;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DOCUMENT_COLLECTION;
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

        assertThat(context.getTransientObject(DOCUMENT_COLLECTION), contains(generatedDocumentInfo));

    }

}