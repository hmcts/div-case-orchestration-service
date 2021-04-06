package uk.gov.hmcts.reform.divorce.orchestration.workflows;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.Features;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CcdCallbackRequest;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.DefaultWorkflow;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.WorkflowException;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.Task;
import uk.gov.hmcts.reform.divorce.orchestration.service.FeatureToggleService;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.AddPetitionerSolicitorRoleTask;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.AllowShareACaseTask;

import java.util.Map;

import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.AUTH_TOKEN_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CASE_DETAILS_JSON_KEY;
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
        final String caseId = ccdCallbackRequest.getCaseDetails().getCaseId();

        if (featureToggleService.isFeatureEnabled(Features.SHARE_A_CASE)) {
            log.info("CaseId {} adding allowShareACaseTask", caseId);

            return this.execute(
                new Task[] {
                    allowShareACaseTask
                },
                ccdCallbackRequest.getCaseDetails().getCaseData(),
                ImmutablePair.of(AUTH_TOKEN_JSON_KEY, authToken),
                ImmutablePair.of(CASE_DETAILS_JSON_KEY, ccdCallbackRequest.getCaseDetails()),
                ImmutablePair.of(CASE_ID_JSON_KEY, caseId)
            );
        } else {
            log.info("CaseId {} adding addPetitionerSolicitorRoleTask", caseId);

            return this.execute(
                new Task[] {
                    addPetitionerSolicitorRoleTask
                },
                ccdCallbackRequest.getCaseDetails().getCaseData(),
                ImmutablePair.of(AUTH_TOKEN_JSON_KEY, authToken),
                ImmutablePair.of(CASE_ID_JSON_KEY, caseId)
            );
        }
    }
}
