package uk.gov.hmcts.reform.divorce.orchestration.workflows.datamigration;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.divorce.orchestration.event.domain.DataMigrationRequest;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.WorkflowException;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskException;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.datamigration.DataMigrationFileCreator;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.datamigration.MigrationDataPublisher;

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
public class FamilyManDataMigrationWorkflowTest {

    private static final String TEST_AUTH_TOKEN = "testAuthToken";

    private static final String FILE_TO_PUBLISH = "fileToPublish";
    private static final String DATE_TO_MIGRATE_KEY = "dateToMigrate";

    @Mock
    private DataMigrationFileCreator dataMigrationFileCreatorTask;

    @Mock
    private MigrationDataPublisher publisherTask;

    @Captor
    private ArgumentCaptor<TaskContext> taskContextArgumentCaptor;

    @InjectMocks
    private FamilyManDataMigrationWorkflow classUnderTest;

    private File fileToPublish;

    @Before
    public void setUp() throws IOException {
        fileToPublish = Files.createTempFile("test", "csv").toFile();
        Files.write(fileToPublish.toPath(), "testFile".getBytes());
    }

    @Test
    public void shouldCallTasks() throws WorkflowException, TaskException {
        when(dataMigrationFileCreatorTask.execute(any(), any())).then(mockInvocation -> {
            Arrays.stream(mockInvocation.getArguments()).filter(o -> o instanceof TaskContext).findFirst()
                .map(TaskContext.class::cast)
                .ifPresent(taskContext -> taskContext.setTransientObject(FILE_TO_PUBLISH, fileToPublish));
            return null;
        });

        LocalDate dateToMigrate = LocalDate.now();
        classUnderTest.run(DataMigrationRequest.Status.DA, dateToMigrate, TEST_AUTH_TOKEN);

        verify(dataMigrationFileCreatorTask).execute(taskContextArgumentCaptor.capture(), any());
        verify(publisherTask).execute(taskContextArgumentCaptor.capture(), any());

        List<TaskContext> taskContexts = taskContextArgumentCaptor.getAllValues();
        TaskContext dataMigrationFileCreatorTaskContext = taskContexts.get(0);
        assertThat(dataMigrationFileCreatorTaskContext.getTransientObject(AUTH_TOKEN_JSON_KEY), is(TEST_AUTH_TOKEN));
        assertThat(dataMigrationFileCreatorTaskContext.getTransientObject(DATE_TO_MIGRATE_KEY), is(dateToMigrate));
        TaskContext publisherTaskContext = taskContexts.get(1);
        assertThat(publisherTaskContext.getTransientObject(FILE_TO_PUBLISH), is(fileToPublish));
    }

}