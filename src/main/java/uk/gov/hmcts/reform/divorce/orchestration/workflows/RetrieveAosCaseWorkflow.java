package uk.gov.hmcts.reform.divorce.orchestration.workflows;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.CaseDataResponse;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.DefaultWorkflow;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.WorkflowException;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.Task;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskException;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.AddCourtsToPayload;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.CaseDataToDivorceFormatter;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.RetrieveAosCase;

import java.util.Map;

import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.AUTH_TOKEN_JSON_KEY;

@Component
public class RetrieveAosCaseWorkflow extends DefaultWorkflow<CaseDataResponse> {

    private final RetrieveAosCase retrieveAosCase;
    private final CaseDataToDivorceFormatter caseDataToDivorceFormatter;
    private final AddCourtsToPayload addCourtsToPayload;

    @Autowired
    public RetrieveAosCaseWorkflow(RetrieveAosCase retrieveAosCase,
                                   CaseDataToDivorceFormatter caseDataToDivorceFormatter,
                                   AddCourtsToPayload addCourtsToPayload) {
        this.retrieveAosCase = retrieveAosCase;
        this.caseDataToDivorceFormatter = caseDataToDivorceFormatter;
        this.addCourtsToPayload = addCourtsToPayload;
    }

    public CaseDataResponse run(String authToken) throws WorkflowException {
        CaseDataResponse caseDataResponse = this.execute(
            new Task[] {
                retrieveAosCase,
                caseDataToDivorceFormatter
            },
            null,
            ImmutablePair.of(AUTH_TOKEN_JSON_KEY, authToken)
        );

        try {
            Map<String, Object> modifiedPayload = addCourtsToPayload.execute(getContext(), caseDataResponse.getData());
            caseDataResponse.setData(modifiedPayload);
        } catch (TaskException taskException) {
            throw new WorkflowException(taskException.getMessage(), taskException);
        }

        return caseDataResponse;
    }

}