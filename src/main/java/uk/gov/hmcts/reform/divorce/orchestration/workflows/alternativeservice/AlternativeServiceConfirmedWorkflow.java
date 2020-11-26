package uk.gov.hmcts.reform.divorce.orchestration.workflows.alternativeservice;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CaseDetails;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.DefaultWorkflow;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.WorkflowException;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.Task;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.alternativeservice.ApplyForDnPetitionerEmailTask;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.alternativeservice.ApplyForDnPetitionerSolicitorEmailTask;

import java.util.Map;

import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CASE_ID_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.util.PartyRepresentationChecker.isPetitionerRepresented;
import static uk.gov.hmcts.reform.divorce.orchestration.workflows.alternativeservice.AlternativeServiceHelper.isServedByAlternativeMethod;

@Component
@AllArgsConstructor
@Slf4j
public class AlternativeServiceConfirmedWorkflow extends DefaultWorkflow<Map<String, Object>> {

    private final ApplyForDnPetitionerEmailTask applyForDnPetitionerEmailTask;
    private final ApplyForDnPetitionerSolicitorEmailTask applyForDnPetitionerSolicitorEmailTask;

    public Map<String, Object> run(CaseDetails caseDetails) throws WorkflowException {
        String caseId = caseDetails.getCaseId();

        log.info("CaseID: {} alternative service confirm workflow is going to be executed.", caseId);

        return this.execute(
            getTasks(caseDetails.getCaseData(), caseId),
            caseDetails.getCaseData(),
            ImmutablePair.of(CASE_ID_JSON_KEY, caseId)
        );
    }

    private Task<Map<String, Object>>[] getTasks(Map<String, Object> caseData, String caseId) {
        if (!isServedByAlternativeMethod(caseData)) {
            log.warn("CaseID: {} field ServedByAlternativeMethod != 'YES'. No task will be executed.", caseId);
            return new Task[] {};
        }

        return new Task[] {
            isPetitionerRepresented(caseData) ? applyForDnPetitionerSolicitorEmailTask : applyForDnPetitionerEmailTask
        };
    }
}
