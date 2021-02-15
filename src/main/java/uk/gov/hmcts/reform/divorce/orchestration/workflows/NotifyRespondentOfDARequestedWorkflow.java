package uk.gov.hmcts.reform.divorce.orchestration.workflows;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CaseDetails;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CcdCallbackRequest;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.DefaultWorkflow;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.WorkflowException;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.Task;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.SendDaRequestedNotifyRespondentEmailTask;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.decreeabsolute.DaRequestedPetitionerSolicitorEmailTask;

import java.util.Map;

import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CASE_DETAILS_JSON_KEY;

@Component
@AllArgsConstructor
@Slf4j
public class NotifyRespondentOfDARequestedWorkflow extends DefaultWorkflow<Map<String, Object>> {

    private final SendDaRequestedNotifyRespondentEmailTask sendDaRequestedNotifyRespondentEmailTask;
    private final DaRequestedPetitionerSolicitorEmailTask daRequestedPetitionerSolicitorEmailTask;

    public Map<String, Object> run(final CcdCallbackRequest ccdCallbackRequest) throws WorkflowException {
        CaseDetails caseDetails = ccdCallbackRequest.getCaseDetails();

        log.info("CaseId: {}, Da requested/applied worklow is going to be executed", caseDetails.getCaseId());

        return this.execute(
            new Task[] {
                sendDaRequestedNotifyRespondentEmailTask,
                daRequestedPetitionerSolicitorEmailTask
            },
            ccdCallbackRequest.getCaseDetails().getCaseData(),
            ImmutablePair.of(CASE_DETAILS_JSON_KEY, caseDetails)
        );
    }
}
