package uk.gov.hmcts.reform.divorce.orchestration.workflows.servicejourney;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CaseDetails;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.DivorceServiceApplication;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.StandardisedWorkflow;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.Task;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.servicejourney.emails.DeemedApprovedPetitionerEmailTask;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.servicejourney.emails.DeemedApprovedSolicitorEmailTask;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.servicejourney.emails.DeemedNotApprovedPetitionerEmailTask;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.servicejourney.emails.DeemedNotApprovedSolicitorEmailTask;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.servicejourney.emails.DispensedApprovedPetitionerEmailTask;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.servicejourney.emails.DispensedApprovedSolicitorEmailTask;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.servicejourney.emails.DispensedNotApprovedPetitionerEmailTask;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.servicejourney.emails.DispensedNotApprovedSolicitorEmailTask;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.AUTH_TOKEN_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CASE_ID_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor.ServiceApplicationDataExtractor.getLastServiceApplication;
import static uk.gov.hmcts.reform.divorce.orchestration.service.common.Conditions.isServiceApplicationDeemed;
import static uk.gov.hmcts.reform.divorce.orchestration.service.common.Conditions.isServiceApplicationDispensed;
import static uk.gov.hmcts.reform.divorce.orchestration.service.common.Conditions.isServiceApplicationGranted;
import static uk.gov.hmcts.reform.divorce.orchestration.util.PartyRepresentationChecker.isPetitionerRepresented;

@Component
@Slf4j
@RequiredArgsConstructor
public class ServiceDecisionMadeWorkflow extends StandardisedWorkflow {

    private final DeemedApprovedPetitionerEmailTask deemedApprovedPetitionerEmailTask;
    private final DeemedApprovedSolicitorEmailTask deemedApprovedSolicitorEmailTask;

    private final DispensedApprovedPetitionerEmailTask dispensedApprovedPetitionerEmailTask;
    private final DispensedApprovedSolicitorEmailTask dispensedApprovedSolicitorEmailTask;

    private final DeemedNotApprovedPetitionerEmailTask deemedNotApprovedPetitionerEmailTask;
    private final DeemedNotApprovedSolicitorEmailTask deemedNotApprovedSolicitorEmailTask;

    private final DispensedNotApprovedSolicitorEmailTask dispensedNotApprovedSolicitorEmailTask;
    private final DispensedNotApprovedPetitionerEmailTask dispensedNotApprovedPetitionerEmailTask;

    @Override
    protected Task<Map<String, Object>>[] getTasksToExecute(CaseDetails caseDetails) {
        Map<String, Object> caseData = caseDetails.getCaseData();
        String caseId = caseDetails.getCaseId();
        DivorceServiceApplication lastServiceApplication = getLastServiceApplication(caseData);

        List<Task<Map<String, Object>>> tasks = new ArrayList<>();

        log.info("CaseID: {} Case state is {}.", caseId, caseDetails.getState());

        if (isServiceApplicationGranted(lastServiceApplication)) {
            log.info("CaseID: {} Service application is granted. No PDFs to generate. Emails might be sent.", caseId);
            if (isServiceApplicationDeemed(lastServiceApplication)) {
                tasks.add(getTaskForDeemedApproved(caseData, caseId));
            } else if (isServiceApplicationDispensed(lastServiceApplication)) {
                tasks.add(getTaskForDispensedApproved(caseData, caseId));
            } else {
                log.info("CaseId: {} Application granted. NOT deemed/dispensed. No email will be sent.", caseId);
            }

            return tasks.toArray(new Task[] {});
        }

        log.info("CaseID: {}, Service application type is {}.", caseId, lastServiceApplication.getType());

        if (isServiceApplicationDeemed(lastServiceApplication)) {
            log.info("CaseID: {} Service application is not granted. No PDFs to generate. Emails might be sent.", caseId);
            tasks.add(getTaskForDeemedNotApproved(caseData, caseId));
        } else if (isServiceApplicationDispensed(lastServiceApplication)) {
            tasks.add(getTaskForDispensedNotApproved(caseData, caseId));
        } else {
            log.warn("CaseId: {} Application not granted. NOT deemed/dispensed. No email will be sent.", caseId);
        }

        return tasks.toArray(new Task[] {});
    }

    @Override
    protected Pair<String, Object>[] prepareContextVariables(CaseDetails caseDetails, String authToken) {
        return new Pair[] {
            ImmutablePair.of(AUTH_TOKEN_JSON_KEY, authToken),
            ImmutablePair.of(CASE_ID_JSON_KEY, caseDetails.getCaseId())
        };
    }

    private Task<Map<String, Object>> getTaskForDispensedApproved(Map<String, Object> caseData, String caseId) {
        if (isPetitionerRepresented(caseData)) {
            log.info("CaseId: {} dispensed approved solicitor email task adding to send email.", caseId);
            return dispensedApprovedSolicitorEmailTask;
        }

        log.info("CaseId: {} dispensed approved citizen email task adding to send email.", caseId);

        return dispensedApprovedPetitionerEmailTask;
    }

    private Task<Map<String, Object>> getTaskForDispensedNotApproved(Map<String, Object> caseData, String caseId) {
        if (isPetitionerRepresented(caseData)) {
            log.info("CaseId: {} deemed not approved solicitor email task adding.", caseId);
            return dispensedNotApprovedSolicitorEmailTask;
        }

        log.info("CaseId: {} deemed not approved citizen email task adding.", caseId);
        return dispensedNotApprovedPetitionerEmailTask;
    }

    private Task<Map<String, Object>> getTaskForDeemedApproved(Map<String, Object> caseData, String caseId) {
        if (isPetitionerRepresented(caseData)) {
            log.info("CaseId: {} deemed approved solicitor email task adding to send email.", caseId);
            return deemedApprovedSolicitorEmailTask;
        }

        log.info("CaseId: {} deemed approved citizen email task adding to send email.", caseId);
        return deemedApprovedPetitionerEmailTask;
    }

    private Task<Map<String, Object>> getTaskForDeemedNotApproved(Map<String, Object> caseData, String caseId) {
        if (isPetitionerRepresented(caseData)) {
            log.info("CaseId: {} dispensed not approved solicitor email task adding to send email.", caseId);
            return deemedNotApprovedSolicitorEmailTask;
        }

        log.info("CaseId: {} dispensed not approved citizen email task adding to send email.", caseId);
        return deemedNotApprovedPetitionerEmailTask;
    }

}