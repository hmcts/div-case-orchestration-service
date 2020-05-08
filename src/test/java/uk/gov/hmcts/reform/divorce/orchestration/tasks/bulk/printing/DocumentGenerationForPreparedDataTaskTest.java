package uk.gov.hmcts.reform.divorce.orchestration.tasks.bulk.printing;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.divorce.orchestration.client.DocumentGeneratorClient;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.documentgeneration.GeneratedDocumentInfo;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskException;

import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.AUTH_TOKEN_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DOCUMENT_FILENAME;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DOCUMENT_TYPE;
import static uk.gov.hmcts.reform.divorce.orchestration.tasks.bulk.printing.DocumentGenerationForPreparedDataTask.ContextKeys.GENERATED_DOCUMENT;

@RunWith(MockitoJUnitRunner.class)
public class DocumentGenerationForPreparedDataTaskTest {

    @Mock
    private DocumentGeneratorClient documentGeneratorClient;

    @InjectMocks
    private DocumentGenerationForPreparedDataTask documentGenerationForPreparedDataTask;

    @Test
    public void executeCallsDocumentGeneratorAndPopulatesContext() throws TaskException {
        Map<String, Object> payload = new HashMap<>();
        TaskContext context = prepareTaskContext();
        GeneratedDocumentInfo documentInfo = GeneratedDocumentInfo.builder().build();

        when(documentGeneratorClient.generatePDF(any(), eq(AUTH_TOKEN))).thenReturn(documentInfo);

        documentGenerationForPreparedDataTask.execute(context, payload);

        assertThat(context.getTransientObject(GENERATED_DOCUMENT), is(documentInfo));
    }

    private TaskContext prepareTaskContext() {
        TaskContext context = PrepareDataForDaGrantedLetterGenerationTaskTest.prepareTaskContext();
        context.setTransientObject(DOCUMENT_TYPE, "type");
        context.setTransientObject(DOCUMENT_FILENAME, "my-file");
        context.setTransientObject(AUTH_TOKEN_JSON_KEY, AUTH_TOKEN);

        return context;
    }
}
