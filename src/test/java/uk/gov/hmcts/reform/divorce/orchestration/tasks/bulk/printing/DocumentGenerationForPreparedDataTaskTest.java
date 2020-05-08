package uk.gov.hmcts.reform.divorce.orchestration.tasks.bulk.printing;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.divorce.orchestration.client.DocumentGeneratorClient;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.documentgeneration.GeneratedDocumentInfo;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.Arrays.asList;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.AUTH_TOKEN_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DOCUMENT_FILENAME;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DOCUMENT_TYPE;
import static uk.gov.hmcts.reform.divorce.orchestration.tasks.bulk.printing.DocumentGenerationForPreparedDataTask.ContextKeys.GENERATED_DOCUMENTS;

@RunWith(MockitoJUnitRunner.class)
public class DocumentGenerationForPreparedDataTaskTest {

    @Mock
    private DocumentGeneratorClient documentGeneratorClient;

    @InjectMocks
    private DocumentGenerationForPreparedDataTask documentGenerationForPreparedDataTask;

    private GeneratedDocumentInfo newDocumentGeneratedByTask = GeneratedDocumentInfo.builder().build();

    @Before
    public void setup() {
        when(documentGeneratorClient.generatePDF(any(), eq(AUTH_TOKEN))).thenReturn(newDocumentGeneratedByTask);
    }

    @Test
    public void executeCallsDocumentGeneratorAndPopulatesContextWithTheFirstElement() throws TaskException {
        Map<String, Object> payload = new HashMap<>();
        TaskContext context = prepareTaskContext();

        documentGenerationForPreparedDataTask.execute(context, payload);
        List<GeneratedDocumentInfo> actual = context.getTransientObject(GENERATED_DOCUMENTS);

        assertArrayWithSize(actual, 1);
        assertThat(actual.get(0), is(newDocumentGeneratedByTask));
    }

    @Test
    public void executeCallsDocumentGeneratorAndAppendAnotrherDocumentToList() throws TaskException {
        Map<String, Object> payload = new HashMap<>();
        TaskContext context = prepareTaskContext();
        final List<GeneratedDocumentInfo> existingList = listWithOneDocument();
        context.setTransientObject(GENERATED_DOCUMENTS, listWithOneDocument());

        documentGenerationForPreparedDataTask.execute(context, payload);
        List<GeneratedDocumentInfo> actual = context.getTransientObject(GENERATED_DOCUMENTS);

        assertArrayWithSize(actual, 2);
        assertThat(actual.get(0), is(existingList.get(0)));
        assertThat(actual.get(1), is(newDocumentGeneratedByTask));
    }

    private void assertArrayWithSize(List<GeneratedDocumentInfo> actual, int size) {
        assertThat(actual.size(), is(size));
    }

    private ArrayList<GeneratedDocumentInfo> listWithOneDocument() {
        return new ArrayList<>(asList(GeneratedDocumentInfo.builder().fileName("this-file-exists").build()));
    }

    private TaskContext prepareTaskContext() {
        TaskContext context = PrepareDataForDaGrantedLetterGenerationTaskTest.prepareTaskContext();
        context.setTransientObject(DOCUMENT_TYPE, "type");
        context.setTransientObject(DOCUMENT_FILENAME, "my-file");
        context.setTransientObject(AUTH_TOKEN_JSON_KEY, AUTH_TOKEN);

        return context;
    }
}
