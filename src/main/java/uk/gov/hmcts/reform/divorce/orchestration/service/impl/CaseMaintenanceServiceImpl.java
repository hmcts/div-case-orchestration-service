package uk.gov.hmcts.reform.divorce.orchestration.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.WorkflowException;
import uk.gov.hmcts.reform.divorce.orchestration.service.CaseMaintenanceService;
import uk.gov.hmcts.reform.divorce.orchestration.workflows.SubmitToCCDWorkflow;
import uk.gov.hmcts.reform.divorce.orchestration.workflows.UpdateToCCDWorkflow;

import java.util.Map;

@Slf4j
@Service
public class CaseMaintenanceServiceImpl implements CaseMaintenanceService {

    @Autowired
    private SubmitToCCDWorkflow submitToCCDWorkflow;

    @Autowired
    private UpdateToCCDWorkflow updateToCCDWorkflow;

    @Override
    public Map<String, Object> submit(Map<String, Object> payload, String authToken) throws WorkflowException {
        payload = submitToCCDWorkflow.run(payload, authToken);

        if (submitToCCDWorkflow.errors().isEmpty()) {
            log.info("Case ID is: {}", payload.get("id"));
            return payload;
        } else {
            return submitToCCDWorkflow.errors();
        }
    }

    @Override
    public Map<String, Object> update(Map<String, Object> payload,
                                      String authToken,
                                      String caseId,
                                      String eventId) throws WorkflowException {
        payload = updateToCCDWorkflow.run(payload, authToken, caseId, eventId);

        if (updateToCCDWorkflow.errors().isEmpty()) {
            log.info("Case ID is: {}", payload.get("id"));
            return payload;
        } else {
            return updateToCCDWorkflow.errors();
        }
    }
}
