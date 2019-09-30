package uk.gov.hmcts.reform.divorce.orchestration.tasks.dataextraction;

import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CaseDetails;
import uk.gov.hmcts.reform.divorce.orchestration.event.domain.DataExtractionRequest.Status;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.Task;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskException;
import uk.gov.hmcts.reform.divorce.orchestration.util.CMSElasticSearchSupport;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.AUTH_TOKEN_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.workflows.dataextraction.FamilyManDataExtractionWorkflow.DATE_TO_EXTRACT_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.workflows.dataextraction.FamilyManDataExtractionWorkflow.FILE_TO_PUBLISH;
import static uk.gov.hmcts.reform.divorce.orchestration.workflows.dataextraction.FamilyManDataExtractionWorkflow.STATUS_KEY;

@Component
@Slf4j
public class DataExtractionFileCreator implements Task<Void> {

    private final CMSElasticSearchSupport cmsElasticSearchSupport;
    private final CSVExtractorFactory csvExtractorFactory;

    @Autowired
    public DataExtractionFileCreator(CMSElasticSearchSupport cmsElasticSearchSupport, CSVExtractorFactory csvExtractorFactory) {
        this.cmsElasticSearchSupport = cmsElasticSearchSupport;
        this.csvExtractorFactory = csvExtractorFactory;
    }

    @Override
    public Void execute(TaskContext context, Void payload) throws TaskException {
        Status status = context.getTransientObject(STATUS_KEY);
        CSVExtractor csvExtractor = csvExtractorFactory.getCSVExtractorForStatus(status);
        LocalDate lastModifiedDate = context.getTransientObject(DATE_TO_EXTRACT_KEY);
        String authToken = context.getTransientObject(AUTH_TOKEN_JSON_KEY);

        List<String> relevantStates = csvExtractor.getRelevantCaseStates()
            .map(String::toLowerCase)
            .collect(Collectors.toList());
        QueryBuilder[] queryBuilders = {
            QueryBuilders.termQuery("last_modified", lastModifiedDate),
            QueryBuilders.termsQuery("state", relevantStates)
        };

        StringBuilder csvFileContent = new StringBuilder();
        csvFileContent.append(csvExtractor.getHeaderLine());
        List<CaseDetails> casesDetails = cmsElasticSearchSupport.searchCMSCases(0, 50, authToken, queryBuilders).collect(Collectors.toList());
        for (CaseDetails caseDetails : casesDetails) {
            csvExtractor.mapCaseData(caseDetails)
                .map(line -> System.lineSeparator() + line)
                .ifPresent(csvFileContent::append);
        }

        File csvFile = createFile(csvFileContent.toString());
        context.setTransientObject(FILE_TO_PUBLISH, csvFile);
        log.info("Created csv file with {} lines of case data for {}", casesDetails.size(), status.name());

        return payload;
    }

    private File createFile(String csvFileContent) throws TaskException {
        File csvFile;

        try {
            csvFile = Files.createTempFile("data_extraction", ".csv").toFile();
            Files.write(csvFile.toPath(), csvFileContent.getBytes());
            csvFile.deleteOnExit();
        } catch (IOException e) {
            throw new TaskException("Problems creating CSV file.", e);
        }

        return csvFile;
    }
}