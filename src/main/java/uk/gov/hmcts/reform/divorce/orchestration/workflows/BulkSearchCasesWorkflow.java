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

@Component
public class BulkSearchCasesWorkflow extends DefaultWorkflow<Map<String, Object>> {

    private final SearchAwaitingPronouncementCases searchAwaitingPronouncementCases;
    private final String query;
//    private final CaseMaintenanceClient caseMaintenanceClient;
//    private final SearchResult searchCasesResult;

    @Autowired
    public BulkSearchCasesWorkflow(SearchAwaitingPronouncementCases searchAwaitingPronouncementCases,
                                   String query) {
        this.searchAwaitingPronouncementCases = searchAwaitingPronouncementCases;
        this.query = query;
    }

    public Map<String, Object> run(String authToken) throws WorkflowException {

        return this.execute(
                new Task[]{
                        searchAwaitingPronouncementCases
                },
                null,
                ImmutablePair.of(AUTH_TOKEN_JSON_KEY, authToken)
        );

    }

}
