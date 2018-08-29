package uk.gov.hmcts.reform.divorce.orchestration.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CreateEvent;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.WorkflowException;
import uk.gov.hmcts.reform.divorce.orchestration.service.CaseOrchestrationService;
import uk.gov.hmcts.reform.divorce.orchestration.workflows.CcdCalllbackWorkflow;
import uk.gov.hmcts.reform.divorce.orchestration.workflows.DeleteDraftWorkflow;
import uk.gov.hmcts.reform.divorce.orchestration.workflows.RetrieveDraftWorkflow;
import uk.gov.hmcts.reform.divorce.orchestration.workflows.SaveDraftWorkflow;
import uk.gov.hmcts.reform.divorce.orchestration.workflows.SubmitToCCDWorkflow;

import java.util.Map;

import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.ID;

@Slf4j
@Service
public class CaseOrchestrationServiceImpl implements CaseOrchestrationService {
    private final SubmitToCCDWorkflow submitToCCDWorkflow;

    private final CcdCalllbackWorkflow ccdCallbackWorkflow;

    private final RetrieveDraftWorkflow retrieveDraftWorkflow;

    private final SaveDraftWorkflow saveDraftWorkflow;

    private final DeleteDraftWorkflow deleteDraftWorkflow;

    @Autowired
    public CaseOrchestrationServiceImpl(SubmitToCCDWorkflow submitToCCDWorkflow,
                                        CcdCalllbackWorkflow ccdCallbackWorkflow,
                                        RetrieveDraftWorkflow retrieveDraftWorkflow,
                                        SaveDraftWorkflow saveDraftWorkflow,
                                        DeleteDraftWorkflow deleteDraftWorkflow) {
        this.submitToCCDWorkflow = submitToCCDWorkflow;
        this.ccdCallbackWorkflow = ccdCallbackWorkflow;
        this.retrieveDraftWorkflow = retrieveDraftWorkflow;
        this.saveDraftWorkflow = saveDraftWorkflow;
        this.deleteDraftWorkflow = deleteDraftWorkflow;
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
        Map<String, Object> response = retrieveDraftWorkflow.run(authToken);
        if (retrieveDraftWorkflow.errors().isEmpty()) {
            String caseOrDraft = (response != null && response.get(ID) != null)
                    ? "case with ID: " + response.get(ID) : "draft";
            log.info("Get draft returns a {}", caseOrDraft);
            return response;
        } else {
            log.error("Workflow Error retrieving draft");
            return retrieveDraftWorkflow.errors();
        }
    }

    @Override
    public Map<String, Object> saveDraft(Map<String, Object> payLoad,
                                         String authToken,
                                         String notificationEmail) throws WorkflowException {
        Map<String, Object> response = saveDraftWorkflow.run(payLoad, authToken, notificationEmail);

        if (saveDraftWorkflow.errors().isEmpty()) {
            log.info("Draft saved");
            return response;
        } else {
            return saveDraftWorkflow.errors();
        }

    }

    @Override
    public Map<String, Object> deleteDraft(String authToken) throws WorkflowException {
        Map<String, Object> response = deleteDraftWorkflow.run(authToken);
        if (deleteDraftWorkflow.errors().isEmpty()) {
            log.info("Draft deleted");
            return response;
        } else {
            log.error("Workflow Error deleting draft");
            return deleteDraftWorkflow.errors();
        }
    }
}
