package uk.gov.hmcts.reform.divorce.orchestration.workflows;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.DefaultWorkflow;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.WorkflowException;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.Task;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.SearchCasesDAOverdueTask;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.UpdateDAOverdueCase;

import java.util.Map;

import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.AUTH_TOKEN_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CASES_OVERDUE_FOR_DA_PROCESSED_COUNT;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CASE_EVENT_ID_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.MAKE_CASE_DA_OVERDUE_EVENT_ID;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.SEARCH_PAGE_KEY;

@Component
@Slf4j
public class UpdateDAOverdueWorkflow extends DefaultWorkflow<Map<String, Object>> {

    private final SearchCasesDAOverdueTask searchCasesDAOverdueTask;
    private final UpdateDAOverdueCase updateDAOverdueCase;

    @Autowired
    public UpdateDAOverdueWorkflow( SearchCasesDAOverdueTask searchCasesDAOverdueTask, UpdateDAOverdueCase updateDAOverdueCase) {

        this.searchCasesDAOverdueTask = searchCasesDAOverdueTask;
        this.updateDAOverdueCase = updateDAOverdueCase;
    }

    public int run(String authToken) throws WorkflowException {
        this.execute(
            new Task[] {
                searchCasesDAOverdueTask,
                updateDAOverdueCase,
            },
            null,
            ImmutablePair.of(AUTH_TOKEN_JSON_KEY, authToken),
            ImmutablePair.of(SEARCH_PAGE_KEY, 0),
            ImmutablePair.of(CASES_OVERDUE_FOR_DA_PROCESSED_COUNT, 0),
            ImmutablePair.of(CASE_EVENT_ID_JSON_KEY, MAKE_CASE_DA_OVERDUE_EVENT_ID)
        );
        return getContext().getTransientObject(CASES_OVERDUE_FOR_DA_PROCESSED_COUNT);
    }
}
