package uk.gov.hmcts.reform.divorce.orchestration.tasks.datamigration;

import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.divorce.orchestration.client.CaseMaintenanceClient;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.Task;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskException;
import uk.gov.hmcts.reform.divorce.orchestration.util.CMSHelper;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.time.LocalDate;
import java.util.List;

import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.AUTH_TOKEN_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DA_REQUESTED;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DIVORCE_GRANTED;
import static uk.gov.hmcts.reform.divorce.orchestration.workflows.datamigration.FamilyManDataMigrationWorkflow.DATE_TO_MIGRATE_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.workflows.datamigration.FamilyManDataMigrationWorkflow.FILE_TO_PUBLISH;

@Component
public class DataMigrationFileCreator implements Task {

    private final CMSHelper cmsHelper;

    @Autowired
    public DataMigrationFileCreator(CaseMaintenanceClient caseMaintenanceClient, DecreeAbsoluteDataExtractor caseDetailsMapper) {
        cmsHelper = new CMSHelper(caseMaintenanceClient, caseDetailsMapper);
    }

    @Override
    public Object execute(TaskContext context, Object payload) throws TaskException {
        LocalDate lastModifiedDate = context.getTransientObject(DATE_TO_MIGRATE_KEY);
        String authToken = context.getTransientObject(AUTH_TOKEN_JSON_KEY);

        QueryBuilder[] queryBuilders = {
            QueryBuilders.termQuery("last_modified", lastModifiedDate),
            QueryBuilders.termsQuery("state", DA_REQUESTED.toLowerCase(), DIVORCE_GRANTED.toLowerCase())
        };
        List<String> csvBodyLines = cmsHelper.searchCMSCases(0, 50, authToken, queryBuilders);

        StringBuilder csvFileContent = new StringBuilder();
        csvFileContent.append("CaseReferenceNumber,DAApplicationDate,DNPronouncementDate,PartyApplyingForDA");
        csvBodyLines.stream().forEach(csvFileContent::append);

        File csvFile = createFile(csvFileContent.toString());
        context.setTransientObject(FILE_TO_PUBLISH, csvFile);

        return null;
    }

    private File createFile(String csvFileContent) throws TaskException {
        File csvFile;

        try {
            csvFile = Files.createTempFile("DA_family_man_data_migration", ".csv").toFile();
            Files.write(csvFile.toPath(), csvFileContent.getBytes());
            csvFile.deleteOnExit();
        } catch (IOException e) {
            throw new TaskException("Problems creating CSV file.", e);
        }

        return csvFile;
    }

}