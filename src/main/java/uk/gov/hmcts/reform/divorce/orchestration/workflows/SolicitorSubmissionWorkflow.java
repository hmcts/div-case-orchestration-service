package uk.gov.hmcts.reform.divorce.orchestration.workflows;

import lombok.RequiredArgsConstructor;
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
import uk.gov.hmcts.reform.divorce.orchestration.tasks.PetitionerSolicitorApplicationSubmittedEmailTask;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.ProcessPbaPaymentTask;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.RemoveMiniPetitionDraftDocumentsTask;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.SendPetitionerSubmissionNotificationEmailTask;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.ValidateSolicitorCaseDataTask;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.AUTH_TOKEN_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CASE_ID_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.service.common.Conditions.isPetitionAmended;
import static uk.gov.hmcts.reform.divorce.orchestration.util.PartyRepresentationChecker.isPetitionerRepresented;

@RequiredArgsConstructor
@Component
@Slf4j
public class SolicitorSubmissionWorkflow extends DefaultWorkflow<Map<String, Object>> {

    private final ValidateSolicitorCaseDataTask validateSolicitorCaseDataTask;
    private final ProcessPbaPaymentTask processPbaPaymentTask;
    private final RemoveMiniPetitionDraftDocumentsTask removeMiniPetitionDraftDocumentsTask;
    private final SendPetitionerSubmissionNotificationEmailTask sendPetitionerSubmissionNotificationEmailTask;
    private final PetitionerSolicitorApplicationSubmittedEmailTask petitionerSolicitorApplicationSubmittedEmailTask;

    private final FeatureToggleService featureToggleService;

    public Map<String, Object> run(CcdCallbackRequest ccdCallbackRequest, String authToken) throws WorkflowException {
        final CaseDetails caseDetails = ccdCallbackRequest.getCaseDetails();
        final String caseId = caseDetails.getCaseId();

        log.info("CaseId {}, SolicitorSubmissionWorkflow is about to run.", caseId);

        return this.execute(
            getTasks(caseDetails),
            caseDetails.getCaseData(),
            ImmutablePair.of(AUTH_TOKEN_JSON_KEY, authToken),
            ImmutablePair.of(CASE_ID_JSON_KEY, caseId)
        );
    }

    private Task<Map<String, Object>>[] getTasks(CaseDetails caseDetails) {
        final String caseId = caseDetails.getCaseId();
        List<Task<Map<String, Object>>> tasks = new ArrayList<>();

        tasks.add(validateSolicitorCaseDataTask);
        tasks.add(processPbaPaymentTask);
        tasks.add(removeMiniPetitionDraftDocumentsTask);
        tasks.add(sendPetitionerSubmissionNotificationEmailTask);

        if (isRespondentJourneyFeatureToggleEnabled()
            && isPetitionerRepresented(caseDetails.getCaseData())
            && !isPetitionAmended(caseDetails.getCaseData())) {
            log.info("CaseId: {} adding task to send email to petitioner solicitor", caseId);
            tasks.add(petitionerSolicitorApplicationSubmittedEmailTask);
        } else {
            log.info("CaseId: {} no email to petitioner solicitor", caseId);
        }

        return tasks.toArray(new Task[0]);
    }

    private boolean isRespondentJourneyFeatureToggleEnabled() {
        return featureToggleService.isFeatureEnabled(Features.REPRESENTED_RESPONDENT_JOURNEY);
    }
}
