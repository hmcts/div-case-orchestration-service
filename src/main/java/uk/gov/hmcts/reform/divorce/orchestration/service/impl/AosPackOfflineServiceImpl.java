package uk.gov.hmcts.reform.divorce.orchestration.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CaseDetails;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.parties.DivorceParty;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.WorkflowException;
import uk.gov.hmcts.reform.divorce.orchestration.service.AosPackOfflineService;
import uk.gov.hmcts.reform.divorce.orchestration.service.CaseOrchestrationServiceException;
import uk.gov.hmcts.reform.divorce.orchestration.workflows.aospack.offline.IssueAosPackOfflineWorkflow;

import java.util.Map;

import static java.lang.String.format;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D_8_REASON_FOR_DIVORCE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.facts.DivorceFacts.ADULTERY;

@Slf4j
@Service
public class AosPackOfflineServiceImpl implements AosPackOfflineService {

    @Autowired
    private IssueAosPackOfflineWorkflow workflow;

    @Override
    public Map<String, Object> issueAosPackOffline(String authToken, CaseDetails caseDetails, DivorceParty divorceParty)
        throws CaseOrchestrationServiceException {

        if (divorceParty.equals(DivorceParty.CO_RESPONDENT)) {
            String reasonForDivorce = (String) caseDetails.getCaseData().get(D_8_REASON_FOR_DIVORCE);
            if (!ADULTERY.equals(reasonForDivorce)) {
                throw new CaseOrchestrationServiceException(
                    format("Co-respondent AOS pack (offline) cannot be issued for reason \"%s\"", reasonForDivorce));
            }
        }

        try {
            return workflow.run(authToken, caseDetails, divorceParty);
        } catch (WorkflowException e) {
            log.error(format("Error occurred issuing Aos Pack Offline for case id %s", caseDetails.getCaseId()), e);
            throw new CaseOrchestrationServiceException(e);
        }
    }

}