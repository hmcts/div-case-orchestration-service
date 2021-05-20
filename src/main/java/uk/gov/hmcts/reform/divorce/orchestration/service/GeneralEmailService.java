package uk.gov.hmcts.reform.divorce.orchestration.service;

import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CaseDetails;

import java.util.Map;

public interface GeneralEmailService {

    Map<String, Object> clearGeneralEmailFields(CaseDetails caseDetails) throws CaseOrchestrationServiceException;

    Map<String, Object> createGeneralEmail(CaseDetails caseDetails) throws CaseOrchestrationServiceException;
}
