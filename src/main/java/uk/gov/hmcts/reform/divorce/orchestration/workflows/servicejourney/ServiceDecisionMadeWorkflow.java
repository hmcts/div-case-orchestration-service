package uk.gov.hmcts.reform.divorce.orchestration.workflows.servicejourney;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CaseDetails;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.DefaultWorkflow;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.WorkflowException;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.Task;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.servicejourney.emails.DeemedApprovedEmailTask;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.servicejourney.emails.DeemedNotApprovedEmailTask;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.servicejourney.emails.DispensedApprovedEmailTask;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.servicejourney.emails.DispensedNotApprovedEmailTask;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.servicejourney.emails.SolicitorDeemedApprovedEmailTask;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.AUTH_TOKEN_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CASE_ID_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.service.common.Conditions.isServiceApplicationDeemed;
import static uk.gov.hmcts.reform.divorce.orchestration.service.common.Conditions.isServiceApplicationDispensed;
import static uk.gov.hmcts.reform.divorce.orchestration.util.PartyRepresentationChecker.isPetitionerRepresented;
import static uk.gov.hmcts.reform.divorce.orchestration.util.ServiceApplicationRefusalHelper.getServiceApplicationType;
import static uk.gov.hmcts.reform.divorce.orchestration.util.ServiceApplicationRefusalHelper.isServiceApplicationGranted;

@Component
@Slf4j
@RequiredArgsConstructor
public class ServiceDecisionMadeWorkflow extends DefaultWorkflow<Map<String, Object>> {

    private final DeemedApprovedEmailTask deemedApprovedEmailTask;
    private final DeemedNotApprovedEmailTask deemedNotApprovedEmailTask;
    private final DispensedApprovedEmailTask dispensedApprovedEmailTask;
    private final DispensedNotApprovedEmailTask dispensedNotApprovedEmailTask;
    private final SolicitorDeemedApprovedEmailTask solicitorDeemedApprovedEmailTask;

    public Map<String, Object> run(CaseDetails caseDetails, String authorisation)
        throws WorkflowException {
        String caseId = caseDetails.getCaseId();
        Map<String, Object> caseData = caseDetails.getCaseData();

        log.info("CaseID: {} ServiceDecisionMade workflow is going to be executed.", caseId);

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

        log.info("CaseID: {} Case state is {}.", caseId, caseDetails.getState());

        if (isServiceApplicationGranted(caseData)) {
            log.info("CaseID: {} Service application is granted. No PDFs to generate. Emails might be sent.", caseId);
            if (isServiceApplicationDeemed(caseData)) {
                tasks.add(getTaskForDeemedApproved(caseData, caseId));
            } else if (isServiceApplicationDispensed(caseData)) {
                tasks.add(getTaskForDispensedApproved(caseData, caseId));
            } else {
                log.info("CaseId: {} NOT deemed/dispensed. No email will be sent.", caseId);
            }

            return tasks.toArray(new Task[] {});
        }

        log.info("CaseID: {}, Service application type is {}.", caseId, getServiceApplicationType(caseData));

        if (isServiceApplicationDeemed(caseData)) {
            log.info("CaseID: {}, Deemed and not approved. Adding task to send citizen email.", caseId);
            tasks.add(deemedNotApprovedEmailTask);
        } else if (isServiceApplicationDispensed(caseData)) {
            log.info("CaseID: {}, Dispensed and not approved. Adding task to send citizen email.", caseId);
            tasks.add(dispensedNotApprovedEmailTask);
        } else {
            log.warn("CaseID: {}, NOT Deemed/Dispensed. Do nothing.", caseId);
        }

        return tasks.toArray(new Task[] {});
    }

    private Task<Map<String, Object>> getTaskForDispensedApproved(Map<String, Object> caseData, String caseId) {
        if (isPetitionerRepresented(caseData)) {
            log.info("CaseId: {} dispensed approved solicitor email task adding.", caseId);
        } else {
            log.info("CaseId: {} dispensed approved citizen email task adding.", caseId);
        }

        return dispensedApprovedEmailTask;
    }

    private Task<Map<String, Object>> getTaskForDeemedApproved(Map<String, Object> caseData, String caseId) {
        if (isPetitionerRepresented(caseData)) {
            log.info("CaseId: {} deemed approved solicitor email task adding.", caseId);
            return solicitorDeemedApprovedEmailTask;
        }

        log.info("CaseId: {} deemed approved citizen email task adding.", caseId);
        return deemedApprovedEmailTask;
    }
}
