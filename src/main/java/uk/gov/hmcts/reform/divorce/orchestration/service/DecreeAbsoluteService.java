package uk.gov.hmcts.reform.divorce.orchestration.service;

import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CcdCallbackRequest;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.WorkflowException;

import java.util.Map;

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

    /** Notify Respondents that the applicant has requested Decree Absolute.
     *
     * @param ccdCallbackRequest Callback request containing CCD Case Data
     * @param authToken Authorisation Token
     * @return Map of String and Object containing CCD Case Data
     * @throws WorkflowException when workflow fails
     */
    Map<String, Object> notifyRespondentOfDARequested(CcdCallbackRequest ccdCallbackRequest, String authToken) throws WorkflowException;

}
