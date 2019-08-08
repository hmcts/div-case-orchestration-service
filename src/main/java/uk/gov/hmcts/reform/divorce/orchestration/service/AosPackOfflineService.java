package uk.gov.hmcts.reform.divorce.orchestration.service;

import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CaseDetails;

import java.util.Map;

public interface AosPackOfflineService {

    Map<String, Object> issueAosPackOffline(String authToken, CaseDetails caseDetails) throws CaseOrchestrationServiceException;

}
