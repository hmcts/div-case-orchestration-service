package uk.gov.hmcts.reform.divorce.orchestration.service;

import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CaseDetails;

import java.util.Map;

public interface CourtOrderDocumentsUpdateService {

    Map<String, Object> updateExistingCourtOrderDocuments(String authToken, CaseDetails caseDetails) throws CaseOrchestrationServiceException;

}