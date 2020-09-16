package uk.gov.hmcts.reform.divorce.orchestration.service;

import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CaseDetails;
import uk.gov.hmcts.reform.divorce.orchestration.exception.GeneralOrderServiceException;

public interface GeneralOrderService {

    CaseDetails generateGeneralOrder(CaseDetails caseDetails, String authorisation)
        throws GeneralOrderServiceException;

    CaseDetails generateGeneralOrderDraft(CaseDetails caseDetails, String authorisation)
        throws GeneralOrderServiceException;
}
