package uk.gov.hmcts.reform.divorce.orchestration.service;

import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CcdCallbackRequest;
import uk.gov.hmcts.reform.divorce.orchestration.exception.JudgeServiceException;

import java.util.Map;

public interface JudgeService {

    Map<String, Object> judgeCostsDecision(CcdCallbackRequest ccdCallbackRequest) throws JudgeServiceException;
}
