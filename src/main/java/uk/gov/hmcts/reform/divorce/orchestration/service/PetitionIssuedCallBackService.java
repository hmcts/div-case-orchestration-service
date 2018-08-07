package uk.gov.hmcts.reform.divorce.orchestration.service;

import uk.gov.hmcts.reform.divorce.orchestration.domian.model.ccd.CCDCallbackResponse;

import java.util.Map;

public interface PetitionIssuedCallBackService {
    CCDCallbackResponse issuePetition(Map<String, Object> caseData, String authToken);
}
