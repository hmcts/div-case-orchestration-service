package uk.gov.hmcts.reform.divorce.orchestration.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.divorce.orchestration.service.SolicitorService;
import uk.gov.hmcts.reform.divorce.orchestration.workflows.IssuePersonalServicePackWorkflow;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class SolicitorServiceImpl implements SolicitorService {

    @Autowired
    public IssuePersonalServicePackWorkflow issuePersonalServicePackWorkflow;

    @Override
    public Map<String, Object> issuePersonalServicePack(Map<String, Object> divorceSession, String authToken, String testCaseId) {
        return issuePersonalServicePackWorkflow.run();
    }
}
