package uk.gov.hmcts.reform.divorce.orchestration.workflows;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.DefaultWorkflow;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.WorkflowException;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.Task;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.AddCourtsToPayloadTask;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.CaseDataToDivorceFormatter;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.RetrieveAosCase;

import java.util.Map;

import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.AUTH_TOKEN_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.constants.TaskContextConstants.CASE_ID_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.constants.TaskContextConstants.CASE_STATE_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.constants.TaskContextConstants.COURT_KEY;

@Component
public class RetrieveAosCaseWorkflow extends DefaultWorkflow<Map<String, Object>> {

    private final RetrieveAosCase retrieveAosCase;
    private final CaseDataToDivorceFormatter caseDataToDivorceFormatter;
    private final AddCourtsToPayloadTask addCourtsToPayloadTask;

    @Autowired
    public RetrieveAosCaseWorkflow(RetrieveAosCase retrieveAosCase,
                                   CaseDataToDivorceFormatter caseDataToDivorceFormatter,
                                   AddCourtsToPayloadTask addCourtsToPayloadTask) {
        this.retrieveAosCase = retrieveAosCase;
        this.caseDataToDivorceFormatter = caseDataToDivorceFormatter;
        this.addCourtsToPayloadTask = addCourtsToPayloadTask;
    }

    public Map<String, Object> run(String authToken) throws WorkflowException {
        return this.execute(
            new Task[] {
                retrieveAosCase,
                caseDataToDivorceFormatter,
                addCourtsToPayloadTask
            },
            null,
            ImmutablePair.of(AUTH_TOKEN_JSON_KEY, authToken)
        );
    }

    public String getCaseId() {
        return getContext().getTransientObject(CASE_ID_KEY);
    }

    public String getCaseState() {
        return getContext().getTransientObject(CASE_STATE_KEY);
    }

    public String getCourt() {
        return getContext().getTransientObject(COURT_KEY);
    }

}