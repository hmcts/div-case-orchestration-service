package uk.gov.hmcts.reform.divorce.orchestration.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CreateEvent;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.WorkflowException;
import uk.gov.hmcts.reform.divorce.orchestration.service.CaseOrchestrationService;
import uk.gov.hmcts.reform.divorce.orchestration.workflows.CcdCalllbackWorkflow;
import uk.gov.hmcts.reform.divorce.orchestration.workflows.DraftWorkflow;
import uk.gov.hmcts.reform.divorce.orchestration.workflows.SubmitToCCDWorkflow;

import java.util.Map;

import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.ID;

@Slf4j
@Service
public class CaseOrchestrationServiceImpl implements CaseOrchestrationService {
    private final SubmitToCCDWorkflow submitToCCDWorkflow;

    private final CcdCalllbackWorkflow ccdCallbackWorkflow;

    private final DraftWorkflow draftWorkflow;

    @Autowired
    public CaseOrchestrationServiceImpl(SubmitToCCDWorkflow submitToCCDWorkflow,
                                        CcdCalllbackWorkflow ccdCallbackWorkflow,
                                        DraftWorkflow draftWorkflow) {
        this.submitToCCDWorkflow = submitToCCDWorkflow;
        this.ccdCallbackWorkflow = ccdCallbackWorkflow;
        this.draftWorkflow = draftWorkflow;
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
    public Map<String, Object> getDraft(String authToken) throws WorkflowException {
        Map<String, Object> payLoad = draftWorkflow.run(authToken);
        if (draftWorkflow.errors().isEmpty()) {
            String caseOrDraft = (payLoad != null && payLoad.get(ID) != null)
                    ? "case with ID: " + payLoad.get(ID) : "draft";
            log.info("Get draft returns a {}", caseOrDraft);
            return payLoad;
        } else {
            log.error("Workflow Error retrieving draft");
            return draftWorkflow.errors();
        }
    }
}
