package uk.gov.hmcts.reform.divorce.orchestration.service.impl;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CaseDetails;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CcdCallbackResponse;
import uk.gov.hmcts.reform.divorce.orchestration.exception.GeneralOrderServiceException;
import uk.gov.hmcts.reform.divorce.orchestration.service.GeneralOrderService;

@Component
public class GeneralOrdersServiceImpl implements GeneralOrderService {

    @Override
    public CcdCallbackResponse generateGeneralOrder(CaseDetails caseDetails, String authorisation)
        throws GeneralOrderServiceException {
        return CcdCallbackResponse.builder().build();
    }

    @Override
    public CcdCallbackResponse generateGeneralOrderDraft(CaseDetails caseDetails, String authorisation)
        throws GeneralOrderServiceException {
        return CcdCallbackResponse.builder().build();
    }
}
