package uk.gov.hmcts.reform.divorce.orchestration.service;

import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CaseDetails;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.parties.DivorceParty;

import java.util.Map;

public interface AosService {

    Map<String, Object> issueAosPackOffline(String authToken, CaseDetails caseDetails, DivorceParty divorceParty)
        throws CaseOrchestrationServiceException;

    Map<String, Object> processAosPackOfflineAnswers(String authToken, CaseDetails caseDetails, DivorceParty divorceParty)
        throws CaseOrchestrationServiceException;

    void markCasesToBeMovedToAosOverdue(String authToken) throws CaseOrchestrationServiceException;

    void makeCaseAosOverdue(String authToken, String caseId);

}
