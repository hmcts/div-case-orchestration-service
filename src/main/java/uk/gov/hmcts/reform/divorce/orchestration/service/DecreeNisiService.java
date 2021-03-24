package uk.gov.hmcts.reform.divorce.orchestration.service;

import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CcdCallbackRequest;

import java.util.Map;

/**
 * A service interface for processing cases to be eligible for DA.
 */
public interface DecreeNisiService {

    /**
     * Event to manually pronounce Decree Nisi when bulk pronouncement fails.
     *
     * @param ccdCallbackRequest Callback request containing CCD Case Data
     * @param authToken Authorisation token
     * @return Map of String and Object containing CCD Case Data
     * @throws CaseOrchestrationServiceException if any exception occurs
     */
    Map<String, Object> setDNGrantedManual(CcdCallbackRequest ccdCallbackRequest, String authToken) throws CaseOrchestrationServiceException;

    /**
     * Event to generate Decree Nisi documentation when pronounced by manual event.
     *
     * @param ccdCallbackRequest Callback request containing CCD Case Data
     * @param authToken Authorisation token
     * @throws CaseOrchestrationServiceException if any exception occurs
     */
    Map<String, Object> handleManualDnPronouncementDocumentGeneration(CcdCallbackRequest ccdCallbackRequest, String authToken)
        throws CaseOrchestrationServiceException;
}
