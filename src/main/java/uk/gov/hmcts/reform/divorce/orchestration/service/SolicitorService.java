package uk.gov.hmcts.reform.divorce.orchestration.service;

import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CcdCallbackRequest;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.WorkflowException;

import java.util.Map;

public interface SolicitorService {
    Map<String, Object> validateForPersonalServicePack(CcdCallbackRequest callbackRequest, String authToken) throws WorkflowException;

    Map<String, Object> solicitorConfirmPersonalService(CcdCallbackRequest callbackRequest) throws WorkflowException;

    Map<String, Object> sendSolicitorPersonalServiceEmail(CcdCallbackRequest callbackRequest) throws WorkflowException;

    Map<String, Object> retrievePbaNumbers(CcdCallbackRequest callbackRequest, String authToken) throws WorkflowException;
}
