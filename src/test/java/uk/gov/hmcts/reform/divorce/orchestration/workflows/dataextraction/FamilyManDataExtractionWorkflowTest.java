package uk.gov.hmcts.reform.divorce.orchestration.workflows.dataextraction;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.divorce.orchestration.event.domain.DataExtractionRequest;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.WorkflowException;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskException;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.dataextraction.DataExtractionFileCreator;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.dataextraction.ExtractedDataPublisher;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.AUTH_TOKEN_JSON_KEY;

@RunWith(MockitoJUnitRunner.class)
public class FamilyManDataExtractionWorkflowTest {

    private static final String TEST_AUTH_TOKEN = "testAuthToken";

    private static final String FILE_TO_PUBLISH = "fileToPublish";
    private static final String DATE_TO_EXTRACT_KEY = "dateToExtract";

    @Mock
    private DataExtractionFileCreator dataExtractionFileCreatorTask;

    @Mock
    private ExtractedDataPublisher publisherTask;

    @Captor
    private ArgumentCaptor<TaskContext> taskContextArgumentCaptor;

    @InjectMocks
    private FamilyManDataExtractionWorkflow classUnderTest;

    private File fileToPublish;

    @Before
    public void setUp() throws IOException {
        fileToPublish = Files.createTempFile("test", "csv").toFile();
        Files.write(fileToPublish.toPath(), "testFile".getBytes());
    }

    @Test
    public void shouldCallTasks() throws WorkflowException, TaskException {
        when(dataExtractionFileCreatorTask.execute(any(), any())).then(mockInvocation -> {
            Arrays.stream(mockInvocation.getArguments()).filter(o -> o instanceof TaskContext).findFirst()
                .map(TaskContext.class::cast)
                .ifPresent(taskContext -> taskContext.setTransientObject(FILE_TO_PUBLISH, fileToPublish));
            return null;
        });

        LocalDate dateToExtract = LocalDate.now();
        classUnderTest.run(DataExtractionRequest.Status.DA, dateToExtract, TEST_AUTH_TOKEN);

        verify(dataExtractionFileCreatorTask).execute(taskContextArgumentCaptor.capture(), any());
        verify(publisherTask).execute(taskContextArgumentCaptor.capture(), any());

        List<TaskContext> taskContexts = taskContextArgumentCaptor.getAllValues();
        TaskContext dataExtractionFileCreatorTaskContext = taskContexts.get(0);
        assertThat(dataExtractionFileCreatorTaskContext.getTransientObject(AUTH_TOKEN_JSON_KEY), is(TEST_AUTH_TOKEN));
        assertThat(dataExtractionFileCreatorTaskContext.getTransientObject(DATE_TO_EXTRACT_KEY), is(dateToExtract));
        TaskContext publisherTaskContext = taskContexts.get(1);
        assertThat(publisherTaskContext.getTransientObject(FILE_TO_PUBLISH), is(fileToPublish));
    }

}