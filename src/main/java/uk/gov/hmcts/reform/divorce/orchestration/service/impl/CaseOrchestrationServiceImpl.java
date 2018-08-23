package uk.gov.hmcts.reform.divorce.orchestration.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CreateEvent;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.WorkflowException;
import uk.gov.hmcts.reform.divorce.orchestration.service.CaseOrchestrationService;
import uk.gov.hmcts.reform.divorce.orchestration.workflows.AuthenticateRespondentWorkflow;
import uk.gov.hmcts.reform.divorce.orchestration.workflows.CcdCalllbackWorkflow;
import uk.gov.hmcts.reform.divorce.orchestration.workflows.SubmitToCCDWorkflow;

import java.util.Map;

import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.ID;

@Slf4j
@Service
public class CaseOrchestrationServiceImpl implements CaseOrchestrationService {
    private final SubmitToCCDWorkflow submitToCCDWorkflow;

    private final CcdCalllbackWorkflow ccdCallbackWorkflow;

    private final AuthenticateRespondentWorkflow authenticateRespondentWorkflow;

    @Autowired
    public CaseOrchestrationServiceImpl(SubmitToCCDWorkflow submitToCCDWorkflow,
                                        CcdCalllbackWorkflow ccdCallbackWorkflow,
                                        AuthenticateRespondentWorkflow authenticateRespondentWorkflow) {
        this.submitToCCDWorkflow            = submitToCCDWorkflow;
        this.ccdCallbackWorkflow            = ccdCallbackWorkflow;
        this.authenticateRespondentWorkflow = authenticateRespondentWorkflow;
    }

    @Override
    public Map<String, Object> submit(Map<String, Object> payLoad,
                                      String authToken) throws WorkflowException {
        payLoad = submitToCCDWorkflow.run(payLoad, authToken);

        if (submitToCCDWorkflow.errors().isEmpty()) {
            log.info("Case ID is: {}", payLoad.get(ID));
            return payLoad;
        } else {
            return submitToCCDWorkflow.errors();
        }
    }

    @Override
    public Map<String, Object> ccdCallbackHandler(CreateEvent caseDetailsRequest,
                                                  String authToken) throws WorkflowException {
        Map<String, Object> payLoad = ccdCallbackWorkflow.run(caseDetailsRequest, authToken);

        if (ccdCallbackWorkflow.errors().isEmpty()) {
            log.info("Case ID is: {}", payLoad.get(ID));
            return payLoad;
        } else {
            return ccdCallbackWorkflow.errors();
        }
    }

    @Override
    public Boolean authenticateRespondent(String authToken) throws WorkflowException {
        return authenticateRespondentWorkflow.run(authToken);
    }
}
