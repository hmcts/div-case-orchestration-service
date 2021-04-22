package uk.gov.hmcts.reform.divorce.orchestration.service.impl;

import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CcdCallbackRequest;
import uk.gov.hmcts.reform.divorce.orchestration.exception.JudgeServiceException;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.WorkflowException;
import uk.gov.hmcts.reform.divorce.orchestration.service.JudgeService;
import uk.gov.hmcts.reform.divorce.orchestration.workflows.JudgeCostsDecisionWorkflow;

import java.util.Map;

public class JudgeServiceImpl implements JudgeService {

    private final JudgeCostsDecisionWorkflow judgeCostsDecisionWorkflow;

    public JudgeServiceImpl(JudgeCostsDecisionWorkflow judgeCostsDecisionWorkflow) {
        this.judgeCostsDecisionWorkflow = judgeCostsDecisionWorkflow;
    }

    @Override
    public Map<String, Object> judgeCostsDecision(CcdCallbackRequest ccdCallbackRequest)
            throws JudgeServiceException {
        try {
            return judgeCostsDecisionWorkflow.run(ccdCallbackRequest.getCaseDetails());
        } catch (WorkflowException e) {
            throw new JudgeServiceException(e);
        }
    }

}

