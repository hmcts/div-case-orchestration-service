package uk.gov.hmcts.reform.divorce.orchestration.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.CaseDataResponse;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CreateEvent;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.WorkflowException;
import uk.gov.hmcts.reform.divorce.orchestration.service.CaseOrchestrationService;
import uk.gov.hmcts.reform.divorce.orchestration.workflows.AuthenticateRespondentWorkflow;
import uk.gov.hmcts.reform.divorce.orchestration.workflows.CcdCallbackWorkflow;
import uk.gov.hmcts.reform.divorce.orchestration.workflows.DeleteDraftWorkflow;
import uk.gov.hmcts.reform.divorce.orchestration.workflows.ProcessPbaPaymentWorkflow;
import uk.gov.hmcts.reform.divorce.orchestration.workflows.RetrieveAosCaseWorkflow;
import uk.gov.hmcts.reform.divorce.orchestration.workflows.RetrieveDraftWorkflow;
import uk.gov.hmcts.reform.divorce.orchestration.workflows.SaveDraftWorkflow;
import uk.gov.hmcts.reform.divorce.orchestration.workflows.SendSubmissionNotificationWorkflow;
import uk.gov.hmcts.reform.divorce.orchestration.workflows.SetOrderSummaryWorkflow;
import uk.gov.hmcts.reform.divorce.orchestration.workflows.SolicitorCreateWorkflow;
import uk.gov.hmcts.reform.divorce.orchestration.workflows.SubmitToCCDWorkflow;
import uk.gov.hmcts.reform.divorce.orchestration.workflows.UpdateToCCDWorkflow;

import java.util.Map;

import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.ID;

@Slf4j
@Service
public class CaseOrchestrationServiceImpl implements CaseOrchestrationService {
    private final CcdCallbackWorkflow ccdCallbackWorkflow;
    private final RetrieveDraftWorkflow retrieveDraftWorkflow;
    private final SaveDraftWorkflow saveDraftWorkflow;
    private final DeleteDraftWorkflow deleteDraftWorkflow;
    private final AuthenticateRespondentWorkflow authenticateRespondentWorkflow;
    private final SubmitToCCDWorkflow submitToCCDWorkflow;
    private final UpdateToCCDWorkflow updateToCCDWorkflow;
    private final RetrieveAosCaseWorkflow retrieveAosCaseWorkflow;
    private final SetOrderSummaryWorkflow setOrderSummaryWorkflow;
    private final ProcessPbaPaymentWorkflow processPbaPaymentWorkflow;
    private final SolicitorCreateWorkflow solicitorCreateWorkflow;
    private final SendSubmissionNotificationWorkflow sendSubmissionNotificationWorkflow;

    @Autowired
    public CaseOrchestrationServiceImpl(CcdCallbackWorkflow ccdCallbackWorkflow,
                                        AuthenticateRespondentWorkflow authenticateRespondentWorkflow,
                                        SubmitToCCDWorkflow submitToCCDWorkflow,
                                        UpdateToCCDWorkflow updateToCCDWorkflow,
                                        RetrieveDraftWorkflow retrieveDraftWorkflow,
                                        SaveDraftWorkflow saveDraftWorkflow,
                                        DeleteDraftWorkflow deleteDraftWorkflow,
                                        RetrieveAosCaseWorkflow retrieveAosCaseWorkflow,
                                        SetOrderSummaryWorkflow setOrderSummaryWorkflow,
                                        ProcessPbaPaymentWorkflow processPbaPaymentWorkflow,
                                        SolicitorCreateWorkflow solicitorCreateWorkflow,
                                        SendSubmissionNotificationWorkflow sendSubmissionNotificationWorkflow) {
        this.ccdCallbackWorkflow = ccdCallbackWorkflow;
        this.authenticateRespondentWorkflow = authenticateRespondentWorkflow;
        this.submitToCCDWorkflow = submitToCCDWorkflow;
        this.updateToCCDWorkflow = updateToCCDWorkflow;
        this.retrieveDraftWorkflow = retrieveDraftWorkflow;
        this.saveDraftWorkflow = saveDraftWorkflow;
        this.deleteDraftWorkflow = deleteDraftWorkflow;
        this.retrieveAosCaseWorkflow = retrieveAosCaseWorkflow;
        this.setOrderSummaryWorkflow = setOrderSummaryWorkflow;
        this.processPbaPaymentWorkflow = processPbaPaymentWorkflow;
        this.solicitorCreateWorkflow = solicitorCreateWorkflow;
        this.sendSubmissionNotificationWorkflow = sendSubmissionNotificationWorkflow;
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
    public Map<String, Object> update(Map<String, Object> divorceSession,
                                      String authToken,
                                      String caseId) throws WorkflowException {
        Map<String, Object> payload = updateToCCDWorkflow.run(divorceSession, authToken, caseId);

        log.info("Case ID is: {}", payload.get(ID));
        return payload;
    }

    @Override
    public Map<String, Object> getDraft(String authToken) throws WorkflowException {
        return retrieveDraftWorkflow.run(authToken);
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
            log.error("Workflow error saving draft");
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
            log.error("Workflow error deleting draft");
            return deleteDraftWorkflow.errors();
        }
    }

    @Override
    public CaseDataResponse retrieveAosCase(boolean checkCcd, String authorizationToken) throws WorkflowException {
        return retrieveAosCaseWorkflow.run(checkCcd, authorizationToken);
    }

    @Override
    public Map<String, Object> sendSubmissionNotificationEmail(
            CreateEvent caseDetailsRequest) throws WorkflowException {
        return sendSubmissionNotificationWorkflow.run(caseDetailsRequest);
    }

    @Override
    public Map<String, Object> setOrderSummary(CreateEvent caseDetailsRequest) throws WorkflowException {
        return setOrderSummaryWorkflow.run(caseDetailsRequest.getCaseDetails().getCaseData());
    }

    @Override
    public Map<String, Object> processPbaPayment(CreateEvent caseDetailsRequest,
                                                 String authToken) throws WorkflowException {
        Map<String, Object> payLoad = processPbaPaymentWorkflow.run(caseDetailsRequest, authToken);

        if (processPbaPaymentWorkflow.errors().isEmpty()) {
            log.info("Callback pay by acccount for solicitor case with id: {} successfully completed",
                    payLoad.get(ID));
            return payLoad;
        } else {
            return processPbaPaymentWorkflow.errors();
        }
    }

    @Override
    public Map<String, Object> solicitorCreate(CreateEvent caseDetailsRequest) throws WorkflowException {
        return solicitorCreateWorkflow.run(caseDetailsRequest.getCaseDetails().getCaseData());
    }
}
