package uk.gov.hmcts.reform.divorce.orchestration.service;

import uk.gov.hmcts.reform.divorce.orchestration.event.domain.DataExtractionRequest;

import java.time.LocalDate;

/**
 * Serves data extraction involving the Divorce platform.
 */
public interface DataExtractionService {

    /**
     * Process case statuses along with yesterday's date.
     *
     * @throws CaseOrchestrationServiceException if any exception occurs
     */
    void requestDataExtractionForPreviousDay() throws CaseOrchestrationServiceException;

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
