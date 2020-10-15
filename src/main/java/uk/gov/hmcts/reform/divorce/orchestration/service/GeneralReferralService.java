package uk.gov.hmcts.reform.divorce.orchestration.service;

import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CcdCallbackRequest;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CcdCallbackResponse;

public interface GeneralReferralService {
    CcdCallbackResponse receiveReferral(CcdCallbackRequest ccdCallbackRequest, String authorizationToken) throws CaseOrchestrationServiceException;
}
