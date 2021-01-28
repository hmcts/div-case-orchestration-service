package uk.gov.hmcts.reform.divorce.orchestration.workflows;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CcdCallbackRequest;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.DefaultWorkflow;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.WorkflowException;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.Task;
import uk.gov.hmcts.reform.divorce.orchestration.service.FeatureToggleService;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.AddPetitionerSolicitorRoleTask;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.AllowShareACaseTask;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.Features.SHARE_A_CASE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.AUTH_TOKEN_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CASE_ID_JSON_KEY;

@Component
@Slf4j
@RequiredArgsConstructor
public class PetitionerSolicitorRoleWorkflow extends DefaultWorkflow<Map<String, Object>> {

    private final AddPetitionerSolicitorRoleTask addPetitionerSolicitorRoleTask;
    private final AllowShareACaseTask allowShareACaseTask;

    private final FeatureToggleService featureToggleService;

    public Map<String, Object> run(CcdCallbackRequest ccdCallbackRequest,
                                   String authToken) throws WorkflowException {
        final List<Task<Map<String, Object>>> tasks = new ArrayList<>();
        final String caseId = ccdCallbackRequest.getCaseDetails().getCaseId();

        log.info("CaseId {} adding addPetitionerSolicitorRoleTask", caseId);

        tasks.add(addPetitionerSolicitorRoleTask);

        if (featureToggleService.isFeatureEnabled(SHARE_A_CASE)) {
            log.info("CaseId {} adding allowShareACaseTask", caseId);
            tasks.add(allowShareACaseTask);
        } else {
            log.info("CaseId {} Share a Case is OFF", caseId);
        }

        return this.execute(
            tasks.toArray(new Task[0]),
            ccdCallbackRequest.getCaseDetails().getCaseData(),
            ImmutablePair.of(AUTH_TOKEN_JSON_KEY, authToken),
            ImmutablePair.of(CASE_ID_JSON_KEY, caseId)
        );
    }

}
