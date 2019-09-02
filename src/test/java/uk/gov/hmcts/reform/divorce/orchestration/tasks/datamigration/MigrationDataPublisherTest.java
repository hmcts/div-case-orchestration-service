package uk.gov.hmcts.reform.divorce.orchestration.tasks.datamigration;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.divorce.orchestration.datamigration.DataMigrationEmailClient;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.DefaultTaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskException;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.time.LocalDate;
import java.time.Month;

import javax.mail.MessagingException;

import static org.hamcrest.Matchers.instanceOf;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.reform.divorce.orchestration.workflows.datamigration.FamilyManDataMigrationWorkflow.DATE_TO_MIGRATE_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.workflows.datamigration.FamilyManDataMigrationWorkflow.FILE_TO_PUBLISH;

@RunWith(MockitoJUnitRunner.class)
public class MigrationDataPublisherTest {

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Mock
    private DataMigrationEmailClient emailClient;

    @InjectMocks
    private MigrationDataPublisher classUnderTest;

    private DefaultTaskContext taskContext;

    @Before
    public void setUp() {
        taskContext = new DefaultTaskContext();
        LocalDate testDateToProcess = LocalDate.of(2019, Month.JULY, 15);
        taskContext.setTransientObject(DATE_TO_MIGRATE_KEY, testDateToProcess);
    }

    @Test
    public void shouldCallEmailClientWithFileToAttach() throws TaskException, IOException, MessagingException {
        File file = Files.createTempFile("test", "file").toFile();
        Files.write(file.toPath(), "my test file".getBytes());
        taskContext.setTransientObject(FILE_TO_PUBLISH, file);

        classUnderTest.execute(taskContext, null);

        verify(emailClient).sendEmailWithAttachment(eq("DA_15072019000000.csv"), eq(file));
    }

    @Test
    public void shouldThrowTaskException_WhenEmailFails() throws TaskException, MessagingException {
        expectedException.expect(TaskException.class);
        expectedException.expectCause(instanceOf(MessagingException.class));

        doThrow(MessagingException.class).when(emailClient).sendEmailWithAttachment(any(), any());

        classUnderTest.execute(taskContext, null);
    }

}