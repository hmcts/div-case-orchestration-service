package uk.gov.hmcts.reform.divorce.orchestration.service;

import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CaseDetails;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CcdCallbackResponse;
import uk.gov.hmcts.reform.divorce.orchestration.exception.GeneralOrderServiceException;

import java.util.Map;

public interface GeneralOrderService {

    CcdCallbackResponse generateGeneralOrder(CaseDetails caseDetails, String authorisation)
        throws GeneralOrderServiceException;

    CcdCallbackResponse generateGeneralOrderDraft(CaseDetails caseDetails, String authorisation)
        throws GeneralOrderServiceException;
}
