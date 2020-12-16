package uk.gov.hmcts.reform.divorce.orchestration.service;

import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CaseDetails;

public interface AlternativeServiceService {

    CaseDetails confirmAlternativeService(CaseDetails caseDetails)
        throws CaseOrchestrationServiceException;

    CaseDetails alternativeServiceConfirmed(CaseDetails caseDetails)
        throws CaseOrchestrationServiceException;

    CaseDetails aosNotReceivedForProcessServer(CaseDetails caseDetails)
        throws CaseOrchestrationServiceException;

    CaseDetails confirmProcessServerService(CaseDetails caseDetails)
        throws CaseOrchestrationServiceException;
}
