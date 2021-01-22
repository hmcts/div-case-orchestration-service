package uk.gov.hmcts.reform.divorce.orchestration.framework.workflow;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CaseDetails;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.Task;

import java.util.Map;

/**
 * This type of workflow provides an entry point method and encapsulates any exceptions thrown within that method into a WorkflowException.
 */
@Slf4j
public abstract class StandardisedWorkflow extends Workflow<Map<String, Object>> {

    public Map<String, Object> run(CaseDetails caseDetails, String authToken) throws WorkflowException {
        String caseId = caseDetails.getCaseId();
        log.info("CaseID: {} StandardisedWorkflow is going to be executed.", caseId);

        try {

            Task<Map<String, Object>>[] tasksToExecute = getTasksToExecute(caseDetails);
            Pair<String, Object>[] contextVariables = prepareContextVariables(caseDetails, authToken);
            return execute(tasksToExecute,
                caseDetails.getCaseData(),
                contextVariables);
        } catch (Exception exception) {
            String errorMessage = "CaseID: " + caseId + ". Failed to execute workflow.";
            log.error(errorMessage, exception);

            if (exception instanceof WorkflowException) {
                //Exception is already a WorkflowException
                throw exception;
            } else {
                //Wrap exception in a WorkflowException
                throw new WorkflowException(errorMessage, exception);
            }
        }
    }

    protected abstract Task<Map<String, Object>>[] getTasksToExecute(CaseDetails caseDetails);

    protected abstract Pair<String, Object>[] prepareContextVariables(CaseDetails caseDetails, String authToken);

}