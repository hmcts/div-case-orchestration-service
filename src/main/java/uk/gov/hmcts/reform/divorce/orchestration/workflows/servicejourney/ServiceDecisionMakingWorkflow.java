package uk.gov.hmcts.reform.divorce.orchestration.workflows.servicejourney;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CaseDetails;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.StandardisedWorkflow;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.Task;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.servicejourney.DeemedServiceRefusalOrderDraftTask;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.servicejourney.DispensedServiceRefusalOrderDraftTask;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.AUTH_TOKEN_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CASE_ID_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor.ServiceApplicationDataExtractor.getServiceApplicationType;
import static uk.gov.hmcts.reform.divorce.orchestration.service.common.Conditions.isAwaitingServiceConsideration;
import static uk.gov.hmcts.reform.divorce.orchestration.service.common.Conditions.isServiceApplicationDeemed;
import static uk.gov.hmcts.reform.divorce.orchestration.service.common.Conditions.isServiceApplicationDispensed;
import static uk.gov.hmcts.reform.divorce.orchestration.service.common.Conditions.isServiceApplicationGranted;

@Component
@Slf4j
@RequiredArgsConstructor
public class ServiceDecisionMakingWorkflow extends StandardisedWorkflow {

    private final DeemedServiceRefusalOrderDraftTask deemedServiceRefusalOrderDraftTask;
    private final DispensedServiceRefusalOrderDraftTask dispensedServiceRefusalOrderDraftTask;

    @Override
    protected Task<Map<String, Object>>[] getTasksToExecute(CaseDetails caseDetails) {
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

    @Override
    protected Pair<String, Object>[] prepareContextVariables(CaseDetails caseDetails, String authToken) {
        String caseId = caseDetails.getCaseId();

        return new Pair[] {
            ImmutablePair.of(AUTH_TOKEN_JSON_KEY, authToken),
            ImmutablePair.of(CASE_ID_JSON_KEY, caseId)
        };
    }

}