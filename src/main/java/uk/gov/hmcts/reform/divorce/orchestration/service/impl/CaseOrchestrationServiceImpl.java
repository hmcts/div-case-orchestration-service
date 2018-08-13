package uk.gov.hmcts.reform.divorce.orchestration.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.WorkflowException;
import uk.gov.hmcts.reform.divorce.orchestration.service.CaseOrchestrationService;
import uk.gov.hmcts.reform.divorce.orchestration.workflows.SubmitToCCDWorkflow;

import java.util.Map;

@Slf4j
@Service
public class CaseOrchestrationServiceImpl implements CaseOrchestrationService {

    @Autowired
    private SubmitToCCDWorkflow submitToCCDWorkflow;

    @Override
    public Map<String, Object> submit(Map<String, Object> payLoad, String authToken) throws WorkflowException {
        payLoad = submitToCCDWorkflow.run(payLoad, authToken);

        if (submitToCCDWorkflow.errors().isEmpty()) {
            log.info("Case ID is: {}", payLoad.get("id"));
            return payLoad;
        } else {
            return submitToCCDWorkflow.errors();
        }
    }
}
