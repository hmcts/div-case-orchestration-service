package uk.gov.hmcts.reform.divorce.orchestration.workflows;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.Features;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CaseDetails;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CcdCallbackRequest;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.DefaultWorkflow;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.WorkflowException;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.Task;
import uk.gov.hmcts.reform.divorce.orchestration.service.FeatureToggleService;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.SendDaRequestedNotifyRespondentEmailTask;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.decreeabsolute.DaRequestedPetitionerSolicitorEmailTask;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CASE_DETAILS_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CASE_ID_JSON_KEY;

@Component
@AllArgsConstructor
@Slf4j
public class NotifyRespondentOfDARequestedWorkflow extends DefaultWorkflow<Map<String, Object>> {

    private final SendDaRequestedNotifyRespondentEmailTask sendDaRequestedNotifyRespondentEmailTask;
    private final DaRequestedPetitionerSolicitorEmailTask daRequestedPetitionerSolicitorEmailTask;

    private final FeatureToggleService featureToggleService;

    public Map<String, Object> run(final CcdCallbackRequest ccdCallbackRequest) throws WorkflowException {
        final CaseDetails caseDetails = ccdCallbackRequest.getCaseDetails();
        final String caseId = caseDetails.getCaseId();

        log.info("CaseId: {}, DA requested/applied workflow is going to be executed", caseId);

        List<Task<Map<String, Object>>> tasks = new ArrayList<>();
        tasks.add(sendDaRequestedNotifyRespondentEmailTask);

        if (featureToggleService.isFeatureEnabled(Features.REPRESENTED_RESPONDENT_JOURNEY)) {
            log.info("CaseId: {}, feature toogle: ON. Adding daRequestedPetitionerSolicitorEmail task", caseId);
            tasks.add(daRequestedPetitionerSolicitorEmailTask);
        }

        return this.execute(
            tasks.toArray(new Task[0]),
            ccdCallbackRequest.getCaseDetails().getCaseData(),
            ImmutablePair.of(CASE_DETAILS_JSON_KEY, caseDetails),
            ImmutablePair.of(CASE_ID_JSON_KEY, caseId)
        );
    }
}
