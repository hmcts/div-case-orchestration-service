package uk.gov.hmcts.reform.divorce.orchestration.workflows;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.Features;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.courts.Court;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.DefaultWorkflow;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.WorkflowException;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.Task;
import uk.gov.hmcts.reform.divorce.orchestration.service.FeatureToggleService;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.CopyJurisdictionConnectionPolicyTask;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.CourtAllocationTask;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.DeleteDraftTask;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.DuplicateCaseValidationTask;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.FormatDivorceSessionToCaseDataTask;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.SubmitCaseToCCD;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.UpdateRespondentDigitalDetailsTask;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.ValidateCaseDataTask;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.AUTH_TOKEN_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.ID;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.courts.CourtConstants.ALLOCATED_COURT_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.util.PartyRepresentationChecker.isRespondentSolicitorDigitalDivorceSession;

@Slf4j
@Component
public class SubmitToCCDWorkflow extends DefaultWorkflow<Map<String, Object>> {

    public static final String SELECTED_COURT = "selectedCourt";

    @Autowired
    private DuplicateCaseValidationTask duplicateCaseValidationTask;

    @Autowired
    private CourtAllocationTask courtAllocationTask;

    @Autowired
    private FormatDivorceSessionToCaseDataTask formatDivorceSessionToCaseDataTask;

    @Autowired
    private CopyJurisdictionConnectionPolicyTask copyJurisdictionConnectionPolicyTask;

    @Autowired
    private ValidateCaseDataTask validateCaseDataTask;

    @Autowired
    private SubmitCaseToCCD submitCaseToCCD;

    @Autowired
    private DeleteDraftTask deleteDraftTask;

    @Autowired
    private UpdateRespondentDigitalDetailsTask updateRespondentDigitalDetailsTask;

    @Autowired
    private FeatureToggleService featureToggleService;

    public Map<String, Object> run(Map<String, Object> payload, String authToken) throws WorkflowException {
        Map<String, Object> returnFromExecution = this.execute(getTasks(payload),
            payload,
            ImmutablePair.of(AUTH_TOKEN_JSON_KEY, authToken)
        );

        Map<String, Object> response = new HashMap<>(returnFromExecution);
        Court selectedCourt = getContext().getTransientObject(SELECTED_COURT);
        response.put(ALLOCATED_COURT_KEY, selectedCourt);

        String caseId = String.valueOf(returnFromExecution.get(ID));
        log.info("Allocated case with CASE ID: {} to court: {}", caseId, selectedCourt.getCourtId());

        return response;
    }

    private boolean isRepresentedRespondentJourneyEnabled() {
        return featureToggleService.isFeatureEnabled(Features.REPRESENTED_RESPONDENT_JOURNEY);
    }

    private Task<Map<String, Object>>[] getTasks(Map<String, Object> payload) {
        List<Task<Map<String, Object>>> tasks = new ArrayList<>();

        tasks.add(duplicateCaseValidationTask);
        tasks.add(courtAllocationTask);
        tasks.add(copyJurisdictionConnectionPolicyTask);
        tasks.add(formatDivorceSessionToCaseDataTask);
        tasks.add(validateCaseDataTask);
        if (isRepresentedRespondentJourneyEnabled()
            && isRespondentSolicitorDigitalDivorceSession(payload)) {
            tasks.add(updateRespondentDigitalDetailsTask);
        }
        tasks.add(submitCaseToCCD);
        tasks.add(deleteDraftTask);

        return tasks.toArray(new Task[0]);
    }
}