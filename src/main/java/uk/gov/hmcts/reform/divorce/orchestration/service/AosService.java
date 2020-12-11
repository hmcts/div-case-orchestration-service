package uk.gov.hmcts.reform.divorce.orchestration.service;

import uk.gov.hmcts.reform.divorce.model.parties.DivorceParty;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CaseDetails;

import java.util.Map;

public interface AosService {

    Map<String, Object> issueAosPackOffline(String authToken, CaseDetails caseDetails, DivorceParty divorceParty)
        throws CaseOrchestrationServiceException;

    Map<String, Object> processAosPackOfflineAnswers(String authToken, CaseDetails caseDetails, DivorceParty divorceParty)
        throws CaseOrchestrationServiceException;

    void findCasesForWhichAosIsOverdue(String authToken) throws CaseOrchestrationServiceException;

    void makeCaseAosOverdue(String authToken, String caseId) throws CaseOrchestrationServiceException;

    Map<String, Object> prepareAosNotReceivedEventForSubmission(String authToken, CaseDetails caseDetails) throws CaseOrchestrationServiceException;

    void markAosNotReceivedForProcessServerCase(String authToken, String caseId) throws CaseOrchestrationServiceException;

    void markAosNotReceivedForAlternativeMethodCase(String authToken, String caseId) throws CaseOrchestrationServiceException;

}
