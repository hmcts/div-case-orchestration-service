package uk.gov.hmcts.reform.divorce.orchestration.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.CaseDataResponse;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CcdCallbackResponse;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CreateEvent;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.WorkflowException;
import uk.gov.hmcts.reform.divorce.orchestration.service.CaseOrchestrationService;
import uk.gov.hmcts.reform.divorce.orchestration.workflows.AuthenticateRespondentWorkflow;
import uk.gov.hmcts.reform.divorce.orchestration.workflows.CcdCallbackWorkflow;
import uk.gov.hmcts.reform.divorce.orchestration.workflows.RespondentSubmittedCallbackWorkflow;
import uk.gov.hmcts.reform.divorce.orchestration.workflows.RetrieveAosCaseWorkflow;
import uk.gov.hmcts.reform.divorce.orchestration.workflows.SubmitToCCDWorkflow;
import uk.gov.hmcts.reform.divorce.orchestration.workflows.UpdateToCCDWorkflow;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.ID;

@Slf4j
@Service
public class CaseOrchestrationServiceImpl implements CaseOrchestrationService {
    private final CcdCallbackWorkflow ccdCallbackWorkflow;
    private final AuthenticateRespondentWorkflow authenticateRespondentWorkflow;
    private final SubmitToCCDWorkflow submitToCCDWorkflow;
    private final UpdateToCCDWorkflow updateToCCDWorkflow;
    private final RetrieveAosCaseWorkflow retrieveAosCaseWorkflow;
    private final RespondentSubmittedCallbackWorkflow aosRespondedWorkflow;


    @Autowired
    public CaseOrchestrationServiceImpl(CcdCallbackWorkflow ccdCallbackWorkflow,
                                        AuthenticateRespondentWorkflow authenticateRespondentWorkflow,
                                        SubmitToCCDWorkflow submitToCCDWorkflow,
                                        UpdateToCCDWorkflow updateToCCDWorkflow,
                                        RetrieveAosCaseWorkflow retrieveAosCaseWorkflow,
                                        RespondentSubmittedCallbackWorkflow aosRespondedWorkflow) {
        this.ccdCallbackWorkflow = ccdCallbackWorkflow;
        this.authenticateRespondentWorkflow = authenticateRespondentWorkflow;
        this.submitToCCDWorkflow = submitToCCDWorkflow;
        this.updateToCCDWorkflow = updateToCCDWorkflow;
        this.retrieveAosCaseWorkflow = retrieveAosCaseWorkflow;
        this.aosRespondedWorkflow = aosRespondedWorkflow;
    }

    @Override
    public Map<String, Object> ccdCallbackHandler(CreateEvent caseDetailsRequest,
                                                  String authToken) throws WorkflowException {
        Map<String, Object> payLoad = ccdCallbackWorkflow.run(caseDetailsRequest, authToken);

        if (ccdCallbackWorkflow.errors().isEmpty()) {
            log.info("Callback for case with id: {} successfully completed", payLoad.get(ID));
            return payLoad;
        } else {
            return ccdCallbackWorkflow.errors();
        }
    }

    @Override
    public Boolean authenticateRespondent(String authToken) throws WorkflowException {
        return authenticateRespondentWorkflow.run(authToken);
    }

    @Override
    public Map<String, Object> submit(Map<String, Object> divorceSession, String authToken) throws WorkflowException {
        Map<String, Object> payload = submitToCCDWorkflow.run(divorceSession, authToken);

        if (submitToCCDWorkflow.errors().isEmpty()) {
            log.info("Case ID is: {}", payload.get(ID));
            return payload;
        } else {
            return submitToCCDWorkflow.errors();
        }
    }

    @Override
    public Map<String, Object> update(Map<String, Object> divorceEventSession,
                                      String authToken,
                                      String caseId) throws WorkflowException {
        Map<String, Object> payload = updateToCCDWorkflow.run(
            divorceEventSession, authToken, caseId);

        log.info("Case ID is: {}", payload.get(ID));
        return payload;
    }

    @Override
    public CaseDataResponse retrieveAosCase(boolean checkCcd, String authorizationToken) throws WorkflowException {
        return retrieveAosCaseWorkflow.run(checkCcd, authorizationToken);
    }

    @Override
    public CcdCallbackResponse aosReceived(CreateEvent caseDetailsRequest, String authToken) throws WorkflowException {
        Map<String, Object> response = aosRespondedWorkflow.run(caseDetailsRequest, authToken);
        if (aosRespondedWorkflow.errors().isEmpty()) {
            return CcdCallbackResponse.builder()
                    .data(response)
                    .build();
        } else {
            Map<String, Object> workflowErrors = aosRespondedWorkflow.errors();
            log.error("Aos received notification failed." + workflowErrors);
            return CcdCallbackResponse
                    .builder()
                    .errors(getNotificationErrors(workflowErrors))
                    .build();
        }
    }

    private List<String> getNotificationErrors(Map<String, Object> notificationErrors) {
        return notificationErrors.entrySet()
                .stream()
                .map(entry -> entry.getValue().toString())
                .collect(Collectors.toList());
    }
}
