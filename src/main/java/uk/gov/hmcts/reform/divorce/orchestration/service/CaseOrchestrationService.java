package uk.gov.hmcts.reform.divorce.orchestration.service;

import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.DivorceSession;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.WorkflowException;

import java.util.Map;

public interface CaseOrchestrationService {

    /**
     * Submit case.
     */
    public Map<String, Object> submit(DivorceSession divorceSession, String authToken) throws WorkflowException;
}
