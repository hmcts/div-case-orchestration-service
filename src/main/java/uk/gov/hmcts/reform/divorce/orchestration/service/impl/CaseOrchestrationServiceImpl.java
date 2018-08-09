package uk.gov.hmcts.reform.divorce.orchestration.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.DivorceSession;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.DefaultWorkflow;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.Workflow;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.WorkflowException;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.Task;
import uk.gov.hmcts.reform.divorce.orchestration.service.CaseOrchestrationService;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.FormatDivorceSessionDataToCaseData;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.SubmitCaseToCCD;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.ValidateCaseData;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
public class CaseOrchestrationServiceImpl implements CaseOrchestrationService {

    @Autowired
    private FormatDivorceSessionDataToCaseData transformSessionDataToCCDData;

    @Autowired
    private ValidateCaseData validateCaseData;

    @Autowired
    private SubmitCaseToCCD submitCaseToCCD;

    @Override
    public Map<String, Object> submit(DivorceSession divorceSession, String authToken) throws WorkflowException {
        Map<String, Object> caseData = new HashMap<>();
        caseData.put("petitionerFirstName", "john");
        Workflow< Map<String, Object>> workflow = new DefaultWorkflow<>();
        transformSessionDataToCCDData.setup(authToken);
        submitCaseToCCD.setup(authToken);
        caseData = workflow.execute(new Task[] {
            transformSessionDataToCCDData,
            validateCaseData,
            submitCaseToCCD
        }, caseData);

        log.info("case id ", caseData.get("id"));
        return caseData;
    }
}
