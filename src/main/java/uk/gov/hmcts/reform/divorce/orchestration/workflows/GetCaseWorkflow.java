package uk.gov.hmcts.reform.divorce.orchestration.workflows;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.CaseDataResponse;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.DefaultWorkflow;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.WorkflowException;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.Task;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.CaseDataToDivorceFormatter;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.GetCase;

import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.AUTH_TOKEN_JSON_KEY;

@Component
public class GetCaseWorkflow extends DefaultWorkflow<CaseDataResponse> {
    private final GetCase getCase;
    private final CaseDataToDivorceFormatter caseDataToDivorceFormatter;

    @Autowired
    public GetCaseWorkflow(GetCase getCase,
                           CaseDataToDivorceFormatter caseDataToDivorceFormatter) {
        this.getCase = getCase;
        this.caseDataToDivorceFormatter = caseDataToDivorceFormatter;
    }

    public CaseDataResponse run(String authToken) throws WorkflowException {
        return this.execute(
            new Task[]{
                getCase,
                caseDataToDivorceFormatter
            },
            null,
            ImmutablePair.of(AUTH_TOKEN_JSON_KEY, authToken)
        );
    }
}