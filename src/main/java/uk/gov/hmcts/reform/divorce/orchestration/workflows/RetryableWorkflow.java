package uk.gov.hmcts.reform.divorce.orchestration.workflows;

import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.WorkflowException;

import java.util.Map;

public interface RetryableWorkflow {

    Map<String, Object> run(Map<String, Object> bulkCaseData, String caseId, String authToken) throws WorkflowException;

}
