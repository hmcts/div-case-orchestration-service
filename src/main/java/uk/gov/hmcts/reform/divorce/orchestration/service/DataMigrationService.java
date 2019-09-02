package uk.gov.hmcts.reform.divorce.orchestration.service;

import uk.gov.hmcts.reform.divorce.orchestration.event.domain.DataMigrationRequest;

import java.time.LocalDate;

/**
 * Serves data migration involving the Divorce platform.
 */
public interface DataMigrationService {

    /**
     * Migrates the data from cases moved to a given status with a given period to Family Man.
     *
     * @param status    Status in which the case to be migrated have to be.
     * @param date      The period in which the cases must have last come into the given status.
     * @param authToken Authorisation token
     */
    void migrateCasesToFamilyMan(final DataMigrationRequest.Status status, final LocalDate date, final String authToken)
        throws CaseOrchestrationServiceException;

}