package uk.gov.hmcts.reform.divorce.orchestration.service;

import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CCDCallbackResponse;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CaseDetails;

public interface PetitionIssuedCallBackService {
    CCDCallbackResponse issuePetition(CaseDetails caseDetails, String authToken);
}
