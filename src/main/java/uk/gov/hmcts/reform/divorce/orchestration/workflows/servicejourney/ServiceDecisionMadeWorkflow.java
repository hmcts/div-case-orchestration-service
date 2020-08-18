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
import uk.gov.hmcts.reform.divorce.orchestration.tasks.servicejourney.DeemedApprovedEmailNotificationTask;
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
import static uk.gov.hmcts.reform.divorce.orchestration.service.common.Conditions.isServiceApplicationDeemed;
import static uk.gov.hmcts.reform.divorce.orchestration.service.common.Conditions.isServiceApplicationDispensed;
import static uk.gov.hmcts.reform.divorce.orchestration.util.ServiceApplicationRefusalHelper.getServiceApplicationType;
import static uk.gov.hmcts.reform.divorce.orchestration.util.ServiceApplicationRefusalHelper.isAwaitingServiceConsideration;
import static uk.gov.hmcts.reform.divorce.orchestration.util.ServiceApplicationRefusalHelper.isDraft;
import static uk.gov.hmcts.reform.divorce.orchestration.util.ServiceApplicationRefusalHelper.isFinal;
import static uk.gov.hmcts.reform.divorce.orchestration.util.ServiceApplicationRefusalHelper.isServiceApplicationGranted;

@Component
@Slf4j
@RequiredArgsConstructor
public class ServiceDecisionMadeWorkflow extends DefaultWorkflow<Map<String, Object>> {

    private final DeemedServiceRefusalOrderTask deemedServiceRefusalOrderTask;
    private final DispensedServiceRefusalOrderTask dispensedServiceRefusalOrderTask;
    private final ServiceRefusalDraftRemovalTask serviceRefusalDraftRemovalTask;
    private final DeemedServiceRefusalOrderDraftTask deemedServiceRefusalOrderDraftTask;
    private final DispensedServiceRefusalOrderDraftTask dispensedServiceRefusalOrderDraftTask;

    private final DeemedApprovedEmailNotificationTask deemedApprovedEmailNotificationTask;

    public Map<String, Object> run(CaseDetails caseDetails, String authorisation, ServiceRefusalDecision decision)
        throws WorkflowException {
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

    private Task<Map<String, Object>>[] getTasks(CaseDetails caseDetails, ServiceRefusalDecision decision) {
        Map<String, Object> caseData = caseDetails.getCaseData();
        String caseId = caseDetails.getCaseId();

        List<Task<Map<String, Object>>> tasks = new ArrayList<>();

        if (!isAwaitingServiceConsideration(caseDetails)) {
            log.info("CaseID: {} Case state is not {}. No documents will be generated.",
                caseId, AWAITING_SERVICE_CONSIDERATION);
            return tasks.toArray(new Task[] {});
        }

        log.info("CaseID: {} Case state is {}.", caseId, AWAITING_SERVICE_CONSIDERATION);

        if (isServiceApplicationGranted(caseData)) {
            log.info("CaseID: {} Service application is granted. No PDFs to generate. Emails might be sent.", caseId);
            if (isServiceApplicationDeemed(caseData)) {
                log.info("CaseId: {} deemed citizen email task adding.", caseId);
                tasks.add(deemedApprovedEmailNotificationTask);
            } else {
                log.info("CaseId: {} NOT deemed. To be implemented", caseId);
            }

            return tasks.toArray(new Task[] {});
        }

        String applicationType = getServiceApplicationType(caseData);

        if (isFinal(decision)) {
            log.info(
                "CaseID: {}, Service application type is {}. Generating Service Refusal Order document",
                caseId,
                applicationType
            );

            if (isServiceApplicationDeemed(caseData)) {
                log.info("CaseID: {}, Deemed. Adding task to generate Deemed Refusal Order", caseId);
                tasks.add(deemedServiceRefusalOrderTask);
            } else if (isServiceApplicationDispensed(caseData)) {
                log.info("CaseID: {}, Dispensed. Adding task to generate Dispensed Refusal Order", caseId);
                tasks.add(dispensedServiceRefusalOrderTask);
            } else {
                log.warn("CaseID: {}, NOT Deemed/Dispensed. Do nothing.", caseId);
            }

            log.info("CaseID: {}, Adding task to remove Refusal Order Draft from case data.", caseId);
            tasks.add(serviceRefusalDraftRemovalTask);

        } else if (isDraft(decision)) {
            log.info(
                "CaseID: {}, Service application type is {}. Generating Service Refusal Order draft document.",
                caseId,
                applicationType
            );

            if (isServiceApplicationDeemed(caseData)) {
                log.info("CaseID: {}, Deemed. Adding task to generate Refusal Order Draft.", caseId);
                tasks.add(deemedServiceRefusalOrderDraftTask);
            } else if (isServiceApplicationDispensed(caseData)) {
                log.info("CaseID: {}, Dispensed. Adding task to generate Refusal Order Draft.", caseId);
                tasks.add(dispensedServiceRefusalOrderDraftTask);
            } else {
                log.warn("CaseID: {}, NOT Deemed/Dispensed. Do nothing.", caseId);
            }
        }

        return tasks.toArray(new Task[] {});
    }
}
