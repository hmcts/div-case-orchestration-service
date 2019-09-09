package uk.gov.hmcts.reform.divorce.orchestration.service;

import uk.gov.hmcts.reform.divorce.orchestration.event.domain.DataExtractionRequest;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.WorkflowException;

import java.time.LocalDate;

/**
 * Serves data extraction involving the Divorce platform.
 */
public interface DataExtractionService {

    /**
     * Process case statuses along with yesterday's date.
     *
     * @throws WorkflowException if any exception occurs
     */
    void requestDataExtraction() throws WorkflowException;
  
    /**
     * Extracts the data from cases moved to a given status with a given period to Family Man.
     *
     * @param status    Status in which the case to be extracted have to be.
     * @param date      The period in which the cases must have last come into the given status.
     * @param authToken Authorisation token
     */
    void extractCasesToFamilyMan(final DataExtractionRequest.Status status, final LocalDate date, final String authToken)
        throws CaseOrchestrationServiceException;

}
