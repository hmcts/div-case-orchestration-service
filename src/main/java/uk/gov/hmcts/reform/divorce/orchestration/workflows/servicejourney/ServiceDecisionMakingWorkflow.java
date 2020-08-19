package uk.gov.hmcts.reform.divorce.orchestration.workflows.servicejourney;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CaseDetails;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.DefaultWorkflow;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.WorkflowException;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.Task;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.servicejourney.DeemedServiceRefusalOrderDraftTask;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.servicejourney.DispensedServiceRefusalOrderDraftTask;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.AUTH_TOKEN_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CASE_ID_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.service.common.Conditions.isServiceApplicationDeemed;
import static uk.gov.hmcts.reform.divorce.orchestration.service.common.Conditions.isServiceApplicationDispensed;
import static uk.gov.hmcts.reform.divorce.orchestration.util.ServiceApplicationRefusalHelper.getServiceApplicationType;
import static uk.gov.hmcts.reform.divorce.orchestration.util.ServiceApplicationRefusalHelper.isAwaitingServiceConsideration;
import static uk.gov.hmcts.reform.divorce.orchestration.util.ServiceApplicationRefusalHelper.isServiceApplicationGranted;

@Component
@Slf4j
@RequiredArgsConstructor
public class ServiceDecisionMakingWorkflow extends DefaultWorkflow<Map<String, Object>> {

    private final DeemedServiceRefusalOrderDraftTask deemedServiceRefusalOrderDraftTask;
    private final DispensedServiceRefusalOrderDraftTask dispensedServiceRefusalOrderDraftTask;

    public Map<String, Object> run(CaseDetails caseDetails, String authorisation)
        throws WorkflowException {
        String caseId = caseDetails.getCaseId();
        Map<String, Object> caseData = caseDetails.getCaseData();

        log.info("CaseID: {} ServiceDecisionMaking workflow is going to be executed.", caseId);

        return this.execute(
            getTasks(caseDetails),
            caseData,
            ImmutablePair.of(AUTH_TOKEN_JSON_KEY, authorisation),
            ImmutablePair.of(CASE_ID_JSON_KEY, caseId)
        );
    }

    private Task<Map<String, Object>>[] getTasks(CaseDetails caseDetails) {
        Map<String, Object> caseData = caseDetails.getCaseData();
        String caseId = caseDetails.getCaseId();

        List<Task<Map<String, Object>>> tasks = new ArrayList<>();

        if (isServiceApplicationGranted(caseData) || !isAwaitingServiceConsideration(caseDetails)) {
            log.warn("CaseID: {} Case is in invalid state for ServiceDecisionMaking workflow.", caseId);
            return tasks.toArray(new Task[] {});
        }

        log.info("CaseID: {}, Service application type is {}.", caseId, getServiceApplicationType(caseData));

        if (isServiceApplicationDeemed(caseData)) {
            log.info("CaseID: {}, Deemed. Adding task to generate Refusal Order Draft.", caseId);
            tasks.add(deemedServiceRefusalOrderDraftTask);
        } else if (isServiceApplicationDispensed(caseData)) {
            log.info("CaseID: {}, Dispensed. Adding task to generate Refusal Order Draft.", caseId);
            tasks.add(dispensedServiceRefusalOrderDraftTask);
        } else {
            log.warn("CaseID: {}, NOT Deemed/Dispensed. Do nothing.", caseId);
        }

        return tasks.toArray(new Task[] {});
    }
}
