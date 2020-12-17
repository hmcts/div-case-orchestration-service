package uk.gov.hmcts.reform.divorce.orchestration.service;

import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CaseDetails;

public interface CourtsOrderDocumentsUpdateService {

    void updateExistingCourtOrderDocuments(CaseDetails caseDetails, String authToken);

}