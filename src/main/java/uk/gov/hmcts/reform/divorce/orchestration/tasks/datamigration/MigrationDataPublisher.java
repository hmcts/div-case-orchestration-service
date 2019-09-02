package uk.gov.hmcts.reform.divorce.orchestration.tasks.datamigration;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.divorce.orchestration.datamigration.DataMigrationEmailClient;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.Task;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskException;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import javax.mail.MessagingException;

import static uk.gov.hmcts.reform.divorce.orchestration.workflows.datamigration.FamilyManDataMigrationWorkflow.DATE_TO_MIGRATE_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.workflows.datamigration.FamilyManDataMigrationWorkflow.FILE_TO_PUBLISH;

@Component
public class MigrationDataPublisher implements Task {

    private static final DateTimeFormatter DATA_MIGRATION_DATE_FORMAT_FOR_FILE_NAME = DateTimeFormatter.ofPattern("ddMMyyyy000000");

    @Autowired
    private DataMigrationEmailClient emailClient;

    @Override
    public Object execute(TaskContext context, Object payload) throws TaskException {

        LocalDate dateToMigrate = context.getTransientObject(DATE_TO_MIGRATE_KEY);
        String attachmentFileName = String.format("DA_%s.csv", dateToMigrate.format(DATA_MIGRATION_DATE_FORMAT_FOR_FILE_NAME));

        try {
            emailClient.sendEmailWithAttachment(attachmentFileName, context.getTransientObject(FILE_TO_PUBLISH));
        } catch (MessagingException e) {
            throw new TaskException("Error sending e-mail with data migration file.", e);
        }

        return null;
    }

}