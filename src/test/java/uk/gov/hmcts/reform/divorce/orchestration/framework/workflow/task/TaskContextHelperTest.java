package uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task;

import org.junit.Test;
import uk.gov.hmcts.reform.divorce.model.documentupdate.GeneratedDocumentInfo;

import java.util.ArrayList;
import java.util.List;

import static java.util.Collections.singletonList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DOCUMENT_COLLECTION;
import static uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskContextHelper.addAllToContextDocumentCollection;
import static uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskContextHelper.addToContextDocumentCollection;
import static uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskContextHelper.failTask;

public class TaskContextHelperTest {

    private static final String DOCUMENT_TYPE_1 = "docType1";
    private static final String DOCUMENT_TYPE_2 = "docType2";
    private static final String DOCUMENT_URL_1 = "url_1";
    private static final String DOCUMENT_URL_2 = "url_2";
    private static final String TEST_ERROR_KEY = "errorKey";
    private static final String TEST_ERROR_VALUE = "errorValue";

    @Test
    public void failTaskContext() {
        TaskContext context = new DefaultTaskContext();

        failTask(context, TEST_ERROR_KEY, singletonList(TEST_ERROR_VALUE));

        assertThat(context.hasTaskFailed(), is(true));
        assertThat(context.getTransientObject(TEST_ERROR_KEY), not(empty()));
        assertThat(context.getTransientObject(TEST_ERROR_KEY), hasItem(TEST_ERROR_VALUE));
    }

    @Test
    public void addToContextDocumentCollectionAddsGeneratedDocumentInfo() {
        TaskContext context = new DefaultTaskContext();
        GeneratedDocumentInfo generatedDocumentInfo = GeneratedDocumentInfo
            .builder()
            .url(DOCUMENT_URL_1)
            .documentType(DOCUMENT_TYPE_1)
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
    public void addAllToContextDocumentCollectionThrowsWhenAlreadyExisting() {
        TaskContext context = new DefaultTaskContext();
        List<GeneratedDocumentInfo> generatedDocumentInfos = buildGeneratedDocuments();
        generatedDocumentInfos.add(GeneratedDocumentInfo
            .builder()
            .url(DOCUMENT_URL_2)
            .documentType(DOCUMENT_TYPE_2)
            .build());

        addAllToContextDocumentCollection(context, generatedDocumentInfos);
    }

    private List<GeneratedDocumentInfo> buildGeneratedDocuments() {
        List<GeneratedDocumentInfo> generatedDocuments = new ArrayList<>();
        generatedDocuments.add(GeneratedDocumentInfo
            .builder()
            .url(DOCUMENT_URL_1)
            .documentType(DOCUMENT_TYPE_1)
            .build());
        generatedDocuments.add(GeneratedDocumentInfo
            .builder()
            .url(DOCUMENT_URL_2)
            .documentType(DOCUMENT_TYPE_2)
            .build());

        return generatedDocuments;
    }

}