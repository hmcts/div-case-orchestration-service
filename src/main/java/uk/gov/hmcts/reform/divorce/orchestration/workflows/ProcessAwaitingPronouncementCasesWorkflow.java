package uk.gov.hmcts.reform.divorce.orchestration.workflows;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.divorce.orchestration.client.CaseMaintenanceClient;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.SearchResult;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.DefaultWorkflow;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.WorkflowException;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.Task;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.SearchAwaitingPronouncementCases;

import java.util.Map;

import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.AUTH_TOKEN_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.SEARCH_PAGE_KEY;

@Component
public class ProcessAwaitingPronouncementCasesWorkflow extends DefaultWorkflow<SearchResult> {

    private final SearchAwaitingPronouncementCases searchAwaitingPronouncementCases;

    @Autowired
    public ProcessAwaitingPronouncementCasesWorkflow(SearchAwaitingPronouncementCases searchAwaitingPronouncementCases) {
        this.searchAwaitingPronouncementCases = searchAwaitingPronouncementCases;
    }

    public SearchResult run(String authToken) throws WorkflowException {

        return this.execute(
                new Task[]{
                        searchAwaitingPronouncementCases
                },
                null,
                ImmutablePair.of(AUTH_TOKEN_JSON_KEY, authToken),
                ImmutablePair.of(SEARCH_PAGE_KEY, 0)

        );

    }

}
