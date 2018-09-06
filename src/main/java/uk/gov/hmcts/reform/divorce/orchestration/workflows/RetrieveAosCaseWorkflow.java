package uk.gov.hmcts.reform.divorce.orchestration.workflows;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.CaseDataResponse;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.DefaultWorkflow;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.WorkflowException;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.Task;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.CaseDataToDivorceFormatter;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.RetrieveAosCase;

import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.AUTH_TOKEN_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CHECK_CCD;

@Component
public class RetrieveAosCaseWorkflow extends DefaultWorkflow<CaseDataResponse> {
    private final RetrieveAosCase retrieveAosCase;
    private final CaseDataToDivorceFormatter caseDataToDivorceFormatter;

    @Autowired
    public RetrieveAosCaseWorkflow(RetrieveAosCase retrieveAosCase,
                                   CaseDataToDivorceFormatter caseDataToDivorceFormatter) {
        this.retrieveAosCase = retrieveAosCase;
        this.caseDataToDivorceFormatter = caseDataToDivorceFormatter;
    }

    public CaseDataResponse run(boolean checkCcd,
                                String authToken) throws WorkflowException {
        return this.execute(
            new Task[]{
                retrieveAosCase,
                caseDataToDivorceFormatter
            },
            null,
            new ImmutablePair<>(AUTH_TOKEN_JSON_KEY, authToken),
            new ImmutablePair<>(CHECK_CCD, checkCcd)
        );
    }
}