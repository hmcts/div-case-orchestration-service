package uk.gov.hmcts.reform.divorce.orchestration.workflows.datamigration;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.divorce.orchestration.event.domain.DataMigrationRequest.Status;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.DefaultWorkflow;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.WorkflowException;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.DefaultTaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.Task;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.datamigration.DataMigrationFileCreator;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.datamigration.MigrationDataPublisher;

import java.time.LocalDate;

import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.AUTH_TOKEN_JSON_KEY;

@Component
public class FamilyManDataMigrationWorkflow extends DefaultWorkflow<Void> {

    public static final String FILE_TO_PUBLISH = "fileToPublish";
    public static final String DATE_TO_MIGRATE_KEY = "dateToMigrate";

    @Autowired
    private DataMigrationFileCreator dataMigrationFileCreator;

    @Autowired
    private MigrationDataPublisher publisherTask;

    public void run(Status status, LocalDate dateToMigrate, String authToken) throws WorkflowException {
        execute(
            new Task[] {
                dataMigrationFileCreator,
                publisherTask
            },
            new DefaultTaskContext(),
            null,
            ImmutablePair.of(AUTH_TOKEN_JSON_KEY, authToken),
            ImmutablePair.of(DATE_TO_MIGRATE_KEY, dateToMigrate)
        );
    }

}