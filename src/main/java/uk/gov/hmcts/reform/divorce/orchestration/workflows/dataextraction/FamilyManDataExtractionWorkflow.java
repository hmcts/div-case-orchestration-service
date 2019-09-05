package uk.gov.hmcts.reform.divorce.orchestration.workflows.dataextraction;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.divorce.orchestration.event.domain.DataExtractionRequest.Status;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.DefaultWorkflow;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.WorkflowException;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.DefaultTaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.Task;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.dataextraction.DataExtractionFileCreator;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.dataextraction.ExtractedDataPublisher;

import java.time.LocalDate;

import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.AUTH_TOKEN_JSON_KEY;

@Component
public class FamilyManDataExtractionWorkflow extends DefaultWorkflow<Void> {

    public static final String FILE_TO_PUBLISH = "fileToPublish";
    public static final String DATE_TO_EXTRACT_KEY = "dateToExtract";

    @Autowired
    private DataExtractionFileCreator dataExtractionFileCreator;

    @Autowired
    private ExtractedDataPublisher publisherTask;

    public void run(Status status, LocalDate dateToExtract, String authToken) throws WorkflowException {
        execute(
            new Task[] {
                dataExtractionFileCreator,
                publisherTask
            },
            new DefaultTaskContext(),
            null,
            ImmutablePair.of(AUTH_TOKEN_JSON_KEY, authToken),
            ImmutablePair.of(DATE_TO_EXTRACT_KEY, dateToExtract)
        );
    }

}