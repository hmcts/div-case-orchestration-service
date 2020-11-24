package uk.gov.hmcts.reform.divorce.orchestration.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.divorce.model.parties.DivorceParty;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CaseDetails;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.WorkflowException;
import uk.gov.hmcts.reform.divorce.orchestration.service.AosService;
import uk.gov.hmcts.reform.divorce.orchestration.service.CaseOrchestrationServiceException;
import uk.gov.hmcts.reform.divorce.orchestration.workflows.aos.AosNotReceivedWorkflow;
import uk.gov.hmcts.reform.divorce.orchestration.workflows.aos.AosOverdueEligibilityWorkflow;
import uk.gov.hmcts.reform.divorce.orchestration.workflows.aos.AosOverdueForProcessServerCaseWorkflow;
import uk.gov.hmcts.reform.divorce.orchestration.workflows.aos.AosOverdueWorkflow;
import uk.gov.hmcts.reform.divorce.orchestration.workflows.aospack.offline.AosPackOfflineAnswersWorkflow;
import uk.gov.hmcts.reform.divorce.orchestration.workflows.aospack.offline.IssueAosPackOfflineWorkflow;

import java.util.Map;

import static java.lang.String.format;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D_8_REASON_FOR_DIVORCE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.facts.DivorceFacts.ADULTERY;

@Slf4j
@Service
@RequiredArgsConstructor
public class AosServiceImpl implements AosService {

    private final IssueAosPackOfflineWorkflow issueAosPackOfflineWorkflow;
    private final AosPackOfflineAnswersWorkflow aosPackOfflineAnswersWorkflow;
    private final AosOverdueEligibilityWorkflow aosOverdueEligibilityWorkflow;
    private final AosOverdueWorkflow aosOverdueWorkflow;
    private final AosNotReceivedWorkflow aosNotReceivedWorkflow;
    private final AosOverdueForProcessServerCaseWorkflow aosOverdueForProcessServerCaseWorkflow;

    @Override
    public Map<String, Object> issueAosPackOffline(String authToken, CaseDetails caseDetails, DivorceParty divorceParty)
        throws CaseOrchestrationServiceException {

        if (divorceParty.equals(DivorceParty.CO_RESPONDENT)) {
            String reasonForDivorce = (String) caseDetails.getCaseData().get(D_8_REASON_FOR_DIVORCE);
            if (!ADULTERY.getValue().equals(reasonForDivorce)) {
                throw new CaseOrchestrationServiceException(
                    format("Co-respondent AOS pack (offline) cannot be issued for reason \"%s\"", reasonForDivorce));
            }
        }

        try {
            return issueAosPackOfflineWorkflow.run(authToken, caseDetails, divorceParty);
        } catch (WorkflowException exception) {
            String caseId = caseDetails.getCaseId();
            log.error(format("Error occurred issuing Aos Pack Offline for case id %s", caseId), exception);
            throw new CaseOrchestrationServiceException(exception, caseId);
        }
    }

    @Override
    public Map<String, Object> processAosPackOfflineAnswers(String authToken, CaseDetails caseDetails, DivorceParty divorceParty)
        throws CaseOrchestrationServiceException {

        try {
            return aosPackOfflineAnswersWorkflow.run(authToken, caseDetails, divorceParty);
        } catch (WorkflowException e) {
            throw new CaseOrchestrationServiceException(e);
        }
    }

    @Override
    public void findCasesForWhichAosIsOverdue(String authToken) throws CaseOrchestrationServiceException {
        log.info("Searching for cases for which AOS are overdue");

        try {
            aosOverdueEligibilityWorkflow.run(authToken);
        } catch (WorkflowException exception) {
            CaseOrchestrationServiceException caseOrchestrationServiceException = new CaseOrchestrationServiceException(exception);
            log.error("Error trying to find cases for which AOS are overdue", caseOrchestrationServiceException);
            throw caseOrchestrationServiceException;
        }
    }

    @Override
    public void makeCaseAosOverdue(String authToken, String caseId) throws CaseOrchestrationServiceException {
        log.info("Will make AOS overdue for case [id: {}].", caseId);

        try {
            aosOverdueWorkflow.run(authToken, caseId);
        } catch (WorkflowException exception) {
            CaseOrchestrationServiceException caseOrchestrationServiceException = new CaseOrchestrationServiceException(exception, caseId);
            log.error("Error trying to move case {} to AOS Overdue", caseId, caseOrchestrationServiceException);
            throw caseOrchestrationServiceException;
        }

        log.info("Made AOS overdue for case [id: {}].", caseId);
    }

    @Override
    public Map<String, Object> prepareAosNotReceivedEventForSubmission(String authToken,
                                                                       CaseDetails caseDetails) throws CaseOrchestrationServiceException {

        String caseId = caseDetails.getCaseId();
        try {
            return aosNotReceivedWorkflow.prepareForSubmission(authToken, caseId, caseDetails.getCaseData());
        } catch (WorkflowException workflowException) {
            throw new CaseOrchestrationServiceException(workflowException, caseId);
        }

    }

    @Override
    public void markAosNotReceivedForProcessServerCase(String authToken, String caseId) throws CaseOrchestrationServiceException {
        log.info("Will make AOS overdue for case [id: {}] (served by process server).", caseId);

        try {
            aosOverdueForProcessServerCaseWorkflow.run(authToken, caseId);
        } catch (WorkflowException exception) {
            CaseOrchestrationServiceException caseOrchestrationServiceException = new CaseOrchestrationServiceException(exception, caseId);
            log.error("Error trying to move case {} to AOS Overdue (served by process server)", caseId, caseOrchestrationServiceException);
            throw caseOrchestrationServiceException;
        }

        log.info("Made AOS overdue for case [id: {}] (served by process server).", caseId);
    }

}