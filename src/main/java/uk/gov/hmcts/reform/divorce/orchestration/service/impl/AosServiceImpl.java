package uk.gov.hmcts.reform.divorce.orchestration.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CaseDetails;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.parties.DivorceParty;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.WorkflowException;
import uk.gov.hmcts.reform.divorce.orchestration.service.AosService;
import uk.gov.hmcts.reform.divorce.orchestration.service.CaseOrchestrationServiceException;
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
    private final AosOverdueWorkflow aosOverdueWorkflow;

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
        } catch (WorkflowException e) {
            log.error(format("Error occurred issuing Aos Pack Offline for case id %s", caseDetails.getCaseId()), e);
            throw new CaseOrchestrationServiceException(e);
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
    public void markCasesToBeMovedToAosOverdue(String authToken) throws CaseOrchestrationServiceException {
        log.info("Searching for cases that are eligible to be moved to AosOverdue");

        try {
            aosOverdueWorkflow.run(authToken);
        } catch (WorkflowException e) {
            CaseOrchestrationServiceException caseOrchestrationServiceException = new CaseOrchestrationServiceException(e);
            log.error("Error trying to find cases to move to AOSOverdue", caseOrchestrationServiceException);
            throw caseOrchestrationServiceException;
        }
    }

    @Override
    public void makeCaseAosOverdue(String authToken, String caseId) {
        log.info("Case id {} should be moved to AOSOverdue.", caseId);
    }

}