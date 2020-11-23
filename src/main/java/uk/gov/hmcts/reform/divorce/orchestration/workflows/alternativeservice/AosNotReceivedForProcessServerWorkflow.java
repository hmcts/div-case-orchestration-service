package uk.gov.hmcts.reform.divorce.orchestration.workflows.alternativeservice;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CaseDetails;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.DefaultWorkflow;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.WorkflowException;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.Task;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.alternativeservice.AwaitingDnPetitionerEmailTask;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.alternativeservice.AwaitingDnPetitionerSolicitorEmailTask;

import java.util.Map;

import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.CcdFields.SERVED_BY_PROCESS_SERVER;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CASE_ID_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.YES_VALUE;
import static uk.gov.hmcts.reform.divorce.orchestration.util.PartyRepresentationChecker.isPetitionerRepresented;

@Component
@AllArgsConstructor
@Slf4j
public class AosNotReceivedForProcessServerWorkflow extends DefaultWorkflow<Map<String, Object>> {

    private final AwaitingDnPetitionerEmailTask awaitingDnPetitionerEmailTask;
    private final AwaitingDnPetitionerSolicitorEmailTask awaitingDnPetitionerSolicitorEmailTask;

    public Map<String, Object> run(CaseDetails caseDetails) throws WorkflowException {
        String caseId = caseDetails.getCaseId();

        log.info("CaseID: {} AOS not received for process server workflow is going to be executed.", caseId);

        return this.execute(
            getTasks(caseDetails.getCaseData()),
            caseDetails.getCaseData(),
            ImmutablePair.of(CASE_ID_JSON_KEY, caseId)
        );
    }

    private Task<Map<String, Object>>[] getTasks(Map<String, Object> caseData) {
        if (!isServedByProcessServer(caseData)) {
            return new Task[] {};
        }

        return new Task[] {
            isPetitionerRepresented(caseData) ? awaitingDnPetitionerSolicitorEmailTask : awaitingDnPetitionerEmailTask
        };
    }

    private boolean isServedByProcessServer(Map<String, Object> caseData) {
        return YES_VALUE.equalsIgnoreCase((String) caseData.get(SERVED_BY_PROCESS_SERVER));
    }
}
