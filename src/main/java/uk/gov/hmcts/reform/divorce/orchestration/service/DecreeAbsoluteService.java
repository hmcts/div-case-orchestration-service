package uk.gov.hmcts.reform.divorce.orchestration.service;

import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.WorkflowException;


/**
 * A service interface for processing cases to be eligible for DA.
 */
public interface DecreeAbsoluteService {

    /**
     * Process cases which are eligible for Decree Absolute.
     *
     * @param authToken Authorisation token
     * @return a number of cases are processed
     * @throws WorkflowException if any exception occurs
     */
    int enableCaseEligibleForDecreeAbsolute(String authToken) throws WorkflowException;

}
