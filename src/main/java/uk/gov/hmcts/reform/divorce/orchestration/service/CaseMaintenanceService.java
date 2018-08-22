package uk.gov.hmcts.reform.divorce.orchestration.service;

import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.WorkflowException;

import java.util.Map;

public interface CaseMaintenanceService {

    /**
     * Submit case.
     */
    public Map<String, Object> submit(Map<String, Object> divorceSession, String authToken) throws WorkflowException;

    /**
     * Update case.
     */
    public Map<String, Object> update(Map<String, Object> divorceSession,
                                      String authToken, String caseId, String eventId) throws WorkflowException;
}
