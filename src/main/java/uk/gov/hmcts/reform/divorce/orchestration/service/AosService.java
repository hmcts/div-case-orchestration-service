package uk.gov.hmcts.reform.divorce.orchestration.service;

import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CcdCallbackRequest;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.WorkflowException;

import java.util.Map;

public interface AosService {

    Map<String, Object> sendPetitionerAOSOverdueNotificationEmail(CcdCallbackRequest ccdCallbackRequest) throws WorkflowException;

}
