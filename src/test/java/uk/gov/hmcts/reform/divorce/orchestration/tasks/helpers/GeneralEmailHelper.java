package uk.gov.hmcts.reform.divorce.orchestration.tasks.helpers;

import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.DefaultTaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskContext;

import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CASE_ID_JSON_KEY;

public class GeneralEmailHelper {

    public static TaskContext getTaskContext() {
        TaskContext context = new DefaultTaskContext();
        context.setTransientObject(CASE_ID_JSON_KEY, TEST_CASE_ID);

        return context;
    }
}
