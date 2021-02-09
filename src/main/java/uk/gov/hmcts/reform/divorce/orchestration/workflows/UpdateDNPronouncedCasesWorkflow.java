package uk.gov.hmcts.reform.divorce.orchestration.workflows;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.DefaultWorkflow;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.WorkflowException;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.Task;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.SearchDNPronouncedCasesTask;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.UpdateDNPronouncedCase;

import java.util.Map;

import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.AUTH_TOKEN_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.AWAITING_DA_PERIOD_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CASES_ELIGIBLE_FOR_DA_PROCESSED_COUNT;


@Component
@Slf4j
public class UpdateDNPronouncedCasesWorkflow extends DefaultWorkflow<Map<String, Object>> {

    private final SearchDNPronouncedCasesTask searchDNPronouncedCasesTask;
    private final UpdateDNPronouncedCase updateDNPronouncedCase;

    @Value("${case.event.awaiting-da-period:43d}")
    private String awaitingDAPeriod;

    @Autowired
    public UpdateDNPronouncedCasesWorkflow(SearchDNPronouncedCasesTask searchDNPronouncedCasesTask, UpdateDNPronouncedCase updateDNPronouncedCase) {
        this.searchDNPronouncedCasesTask = searchDNPronouncedCasesTask;
        this.updateDNPronouncedCase = updateDNPronouncedCase;
    }

    public int run(String authToken) throws WorkflowException {
        this.execute(
            new Task[] {
                searchDNPronouncedCasesTask,
                updateDNPronouncedCase
            },
            null,
            ImmutablePair.of(AUTH_TOKEN_JSON_KEY, authToken),
            ImmutablePair.of(AWAITING_DA_PERIOD_KEY, awaitingDAPeriod),
            ImmutablePair.of(CASES_ELIGIBLE_FOR_DA_PROCESSED_COUNT, 0)
        );
        return getContext().getTransientObject(CASES_ELIGIBLE_FOR_DA_PROCESSED_COUNT);
    }
}
