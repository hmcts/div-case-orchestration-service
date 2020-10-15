package uk.gov.hmcts.reform.divorce.orchestration.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CcdCallbackRequest;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CcdCallbackResponse;
import uk.gov.hmcts.reform.divorce.orchestration.service.CaseOrchestrationServiceException;
import uk.gov.hmcts.reform.divorce.orchestration.service.GeneralReferralService;

@Component
@RequiredArgsConstructor
public class GeneralReferralServiceImpl implements GeneralReferralService {

    private final GeneralReferralWorkflow generalReferralWorkflow;

    @Override
    public CcdCallbackResponse receiveReferral(CcdCallbackRequest ccdCallbackRequest, String authorizationToken)
        throws CaseOrchestrationServiceException {
        return CcdCallbackResponse.builder().build();
    }
}
