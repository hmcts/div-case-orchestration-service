package uk.gov.hmcts.reform.divorce.orchestration.workflows.servicejourney;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CaseDetails;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.DefaultWorkflow;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.WorkflowException;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.Task;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.bailiff.BailiffApplicationApprovedDataTask;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.servicejourney.DeemedServiceOrderGenerationTask;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.servicejourney.DeemedServiceRefusalOrderTask;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.servicejourney.DispensedServiceRefusalOrderTask;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.servicejourney.MakeServiceDecisionDateTask;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.servicejourney.OrderToDispenseGenerationTask;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.servicejourney.ServiceApplicationDataTask;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.servicejourney.ServiceApplicationRemovalTask;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.servicejourney.ServiceRefusalDraftRemovalTask;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.AUTH_TOKEN_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CASE_ID_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.service.common.Conditions.isServiceApplicationBailiff;
import static uk.gov.hmcts.reform.divorce.orchestration.service.common.Conditions.isServiceApplicationDeemed;
import static uk.gov.hmcts.reform.divorce.orchestration.service.common.Conditions.isServiceApplicationDispensed;
import static uk.gov.hmcts.reform.divorce.orchestration.service.common.Conditions.isServiceApplicationGranted;

@Component
@Slf4j
@RequiredArgsConstructor
public class MakeServiceDecisionWorkflow extends DefaultWorkflow<Map<String, Object>> {

    private final MakeServiceDecisionDateTask makeServiceDecisionDateTask;
    private final OrderToDispenseGenerationTask orderToDispenseGenerationTask;
    private final DeemedServiceOrderGenerationTask deemedServiceOrderGenerationTask;
    private final DeemedServiceRefusalOrderTask deemedServiceRefusalOrderTask;
    private final DispensedServiceRefusalOrderTask dispensedServiceRefusalOrderTask;
    private final ServiceApplicationDataTask serviceApplicationDataTask;
    private final ServiceRefusalDraftRemovalTask serviceRefusalDraftRemovalTask;
    private final ServiceApplicationRemovalTask serviceApplicationRemovalTask;
    private final BailiffApplicationApprovedDataTask bailiffApplicationApprovedDataTask;

    public Map<String, Object> run(CaseDetails caseDetails, String auth) throws WorkflowException {

        String caseId = caseDetails.getCaseId();
        Map<String, Object> caseData = caseDetails.getCaseData();

        log.info("CaseID: {} Make Service Decision workflow is going to be executed.", caseId);

        List<Task<Map<String, Object>>> tasks = new ArrayList<>();

        tasks.add(makeServiceDecisionDateTask);

        if (isServiceApplicationGranted(caseData)) {
            if (isServiceApplicationDispensed(caseData)) {
                log.info("CaseID: {} application type = dispensed. Order to Dispense will be generated.", caseId);
                tasks.add(orderToDispenseGenerationTask);
            } else if (isServiceApplicationDeemed(caseData)) {
                log.info("CaseID: {} application type = deemed. Deemed Service Order will be generated.", caseId);
                tasks.add(deemedServiceOrderGenerationTask);
            } else {
                log.info("CaseID: {} application type != dispensed/deemed. No pdf will be generated.", caseId);
            }
        } else {
            if (isServiceApplicationDispensed(caseData)) {
                log.info("CaseID: {}, Dispensed. Adding task to generate Dispensed Refusal Order.", caseId);
                tasks.add(dispensedServiceRefusalOrderTask);
            } else if (isServiceApplicationDeemed(caseData)) {
                log.info("CaseID: {}, Deemed. Adding task to generate Deemed Refusal Order.", caseId);
                tasks.add(deemedServiceRefusalOrderTask);
            } else {
                log.info("CaseID: {} application type != dispensed/deemed. No draft pdf will be generated.", caseId);
            }

            log.info("CaseID: {}, Adding task to remove Refusal Order Draft from case data if exists.", caseId);
            tasks.add(serviceRefusalDraftRemovalTask);
        }

        if (isServiceApplicationGranted(caseDetails.getCaseData()) && isServiceApplicationBailiff(caseDetails.getCaseData())) {
            // for "bailiff service application granted" case the application should not be moved into previous
            // applications collection until the end of bailiff workflow; instead need to copy ServiceApplicationGranted CCD field
            // into BailiffApplicationGranted - the latter field is required to display custom label for service application of "bailiff" type
            tasks.add(bailiffApplicationApprovedDataTask);
        } else {
            log.info("CaseID: {}, Adding task to move all service application temp data to collection.", caseId);
            tasks.add(serviceApplicationDataTask);
            log.info("CaseID: {}, Adding task to remove all service application temp data from case data.", caseId);
            tasks.add(serviceApplicationRemovalTask);
        }

        return this.execute(
            tasks.toArray(new Task[0]),
            caseData,
            ImmutablePair.of(CASE_ID_JSON_KEY, caseId),
            ImmutablePair.of(AUTH_TOKEN_JSON_KEY, auth)
        );
    }
}
