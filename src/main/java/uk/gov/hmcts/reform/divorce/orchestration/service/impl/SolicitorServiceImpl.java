package uk.gov.hmcts.reform.divorce.orchestration.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CcdCallbackRequest;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.WorkflowException;
import uk.gov.hmcts.reform.divorce.orchestration.service.SolicitorService;
import uk.gov.hmcts.reform.divorce.orchestration.workflows.IssuePersonalServicePackWorkflow;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class SolicitorServiceImpl implements SolicitorService {

    private final IssuePersonalServicePackWorkflow issuePersonalServicePackWorkflow;

    @Override
    public Map<String, Object> issuePersonalServicePack(CcdCallbackRequest callbackRequest, String authToken) throws WorkflowException {
        return issuePersonalServicePackWorkflow.run(callbackRequest, authToken);
    }
}
