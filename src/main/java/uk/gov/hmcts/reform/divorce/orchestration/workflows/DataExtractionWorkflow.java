package uk.gov.hmcts.reform.divorce.orchestration.workflows;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.DefaultWorkflow;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.WorkflowException;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.Task;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.dataextraction.DataExtractionTask;

import java.util.Map;

@Component
@Slf4j
public class DataExtractionWorkflow extends DefaultWorkflow<Map<String, Object>> {

    private final DataExtractionTask dataExtractionTask;

    @Autowired
    public DataExtractionWorkflow(DataExtractionTask dataExtractionTask) {
        this.dataExtractionTask = dataExtractionTask;
    }

    public void run() throws WorkflowException {
        this.execute(
            new Task[] {
                dataExtractionTask
            },
            null
        );
    }
}
