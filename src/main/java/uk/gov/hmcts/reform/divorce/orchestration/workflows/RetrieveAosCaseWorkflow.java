package uk.gov.hmcts.reform.divorce.orchestration.workflows;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.CaseDataResponse;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.DefaultWorkflow;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.WorkflowException;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.Task;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskException;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.AddCourtsToPayloadTask;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.CaseDataToDivorceFormatterTask;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.RetrieveAosCase;

import java.util.Map;

import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.AUTH_TOKEN_JSON_KEY;

@Component
public class RetrieveAosCaseWorkflow extends DefaultWorkflow<CaseDataResponse> {

    private final RetrieveAosCase retrieveAosCase;
    private final CaseDataToDivorceFormatterTask caseDataToDivorceFormatterTask;
    private final AddCourtsToPayloadTask addCourtsToPayloadTask;

    @Autowired
    public RetrieveAosCaseWorkflow(RetrieveAosCase retrieveAosCase,
                                   CaseDataToDivorceFormatterTask caseDataToDivorceFormatterTask,
                                   AddCourtsToPayloadTask addCourtsToPayloadTask) {
        this.retrieveAosCase = retrieveAosCase;
        this.caseDataToDivorceFormatterTask = caseDataToDivorceFormatterTask;
        this.addCourtsToPayloadTask = addCourtsToPayloadTask;
    }

    public CaseDataResponse run(String authToken) throws WorkflowException {
        CaseDataResponse caseDataResponse = this.execute(
            new Task[]{
                retrieveAosCase,
                caseDataToDivorceFormatterTask
            },
            null,
            ImmutablePair.of(AUTH_TOKEN_JSON_KEY, authToken)
        );

        try {
            Map<String, Object> modifiedPayload = addCourtsToPayloadTask.execute(getContext(), caseDataResponse.getData());
            caseDataResponse.setData(modifiedPayload);
        } catch (TaskException taskException) {
            throw new WorkflowException(taskException.getMessage(), taskException);
        }

        return caseDataResponse;
    }

}