package uk.gov.hmcts.reform.divorce.orchestration.workflows.servicejourney;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CaseDetails;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.document.ServiceRefusalDecision;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.DefaultWorkflow;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.WorkflowException;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.Task;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.servicejourney.DeemedServiceRefusalOrderDraftTask;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.servicejourney.DeemedServiceRefusalOrderTask;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.servicejourney.DispensedServiceRefusalOrderDraftTask;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.servicejourney.DispensedServiceRefusalOrderTask;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.servicejourney.ServiceRefusalDraftRemovalTask;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.CcdStates.AWAITING_SERVICE_CONSIDERATION;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.AUTH_TOKEN_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CASE_ID_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.util.ServiceApplicationRefusalHelper.getCaseReference;
import static uk.gov.hmcts.reform.divorce.orchestration.util.ServiceApplicationRefusalHelper.getServiceApplicationType;
import static uk.gov.hmcts.reform.divorce.orchestration.util.ServiceApplicationRefusalHelper.isAwaitingServiceConsideration;
import static uk.gov.hmcts.reform.divorce.orchestration.util.ServiceApplicationRefusalHelper.isDeemedApplication;
import static uk.gov.hmcts.reform.divorce.orchestration.util.ServiceApplicationRefusalHelper.isDispensedApplication;
import static uk.gov.hmcts.reform.divorce.orchestration.util.ServiceApplicationRefusalHelper.isDraft;
import static uk.gov.hmcts.reform.divorce.orchestration.util.ServiceApplicationRefusalHelper.isFinal;
import static uk.gov.hmcts.reform.divorce.orchestration.util.ServiceApplicationRefusalHelper.isServiceApplicationGranted;

@Component
@Slf4j
@RequiredArgsConstructor
public class ServiceApplicationRefusalOrderWorkflow extends DefaultWorkflow<Map<String, Object>> {

    private final DeemedServiceRefusalOrderTask deemedServiceRefusalOrderTask;
    private final DispensedServiceRefusalOrderTask dispensedServiceRefusalOrderTask;
    private final ServiceRefusalDraftRemovalTask serviceRefusalDraftRemovalTask;
    private final DeemedServiceRefusalOrderDraftTask deemedServiceRefusalOrderDraftTask;
    private final DispensedServiceRefusalOrderDraftTask dispensedServiceRefusalOrderDraftTask;

    public Map<String, Object> run(CaseDetails caseDetails, String authorisation, ServiceRefusalDecision decision) throws WorkflowException {

        String caseId = caseDetails.getCaseId();
        Map<String, Object> caseData = caseDetails.getCaseData();

        log.info("CaseID: {} Service decision made. ServiceDecisionMade workflow is going to be executed.", caseId);

        return this.execute(
            getTasks(caseDetails, decision),
            caseData,
            ImmutablePair.of(AUTH_TOKEN_JSON_KEY, authorisation),
            ImmutablePair.of(CASE_ID_JSON_KEY, caseId)
        );
    }

    private Task[] getTasks(CaseDetails caseDetails, ServiceRefusalDecision decision) {
        Map<String, Object> caseData = caseDetails.getCaseData();
        String caseId = getCaseReference(caseData);

        List<Task> tasks = new ArrayList<>();

        if (!isAwaitingServiceConsideration(caseDetails)) {
            log.info("CaseID: {} Case is not {}. Cannot generate service application refusal order documents",
                caseId, AWAITING_SERVICE_CONSIDERATION);
            return tasks.toArray(new Task[] {});
        }

        if (isServiceApplicationGranted(caseData)) {
            log.info("CaseID: {} Service application is granted. Cannot generate service application refusal order documents", caseId);
            return tasks.toArray(new Task[] {});
        }

        String applicationType = getServiceApplicationType(caseData);

        if (isFinal(decision)) {
            log.info("CaseID: {}, Service application type is {}. Generating Service Refusal Order document", caseId, applicationType);

            if (isDeemedApplication(applicationType)) {
                tasks.add(deemedServiceRefusalOrderTask);
            } else if (isDispensedApplication(applicationType)) {
                tasks.add(dispensedServiceRefusalOrderTask);
            }
            tasks.add(serviceRefusalDraftRemovalTask);

        } else if (isDraft(decision)) {
            log.info("CaseID: {}, Service application type is {}. Generating Service Refusal Order draft document", caseId, applicationType);

            if (isDeemedApplication(applicationType)) {
                tasks.add(deemedServiceRefusalOrderDraftTask);
            } else if (isDispensedApplication(applicationType)) {
                tasks.add(dispensedServiceRefusalOrderDraftTask);
            }
        }

        return tasks.toArray(new Task[] {});
    }
}
