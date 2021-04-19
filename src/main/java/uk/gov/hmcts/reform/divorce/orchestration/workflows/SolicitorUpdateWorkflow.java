package uk.gov.hmcts.reform.divorce.orchestration.workflows;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.Features;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CaseDetails;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.DefaultWorkflow;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.WorkflowException;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.Task;
import uk.gov.hmcts.reform.divorce.orchestration.service.FeatureToggleService;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.AddMiniPetitionDraftTask;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.AddNewDocumentsToCaseDataTask;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.CopyD8JurisdictionConnectionPolicyTask;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.RespondentDetailsRemovalTask;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.RespondentOrganisationPolicyRemovalTask;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.SetNewLegalConnectionPolicyTask;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.SetPetitionerSolicitorOrganisationPolicyReferenceTask;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.SetRespondentSolicitorOrganisationPolicyReferenceTask;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.ValidateSelectedOrganisationTask;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.AUTH_TOKEN_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CASE_DETAILS_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CASE_ID_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.util.PartyRepresentationChecker.isRespondentRepresented;
import static uk.gov.hmcts.reform.divorce.orchestration.util.PartyRepresentationChecker.isRespondentSolicitorDigitalSelectedYes;

@Component
@Slf4j
@RequiredArgsConstructor
public class SolicitorUpdateWorkflow extends DefaultWorkflow<Map<String, Object>> {

    private final AddMiniPetitionDraftTask addMiniPetitionDraftTask;
    private final AddNewDocumentsToCaseDataTask addNewDocumentsToCaseDataTask;
    private final SetPetitionerSolicitorOrganisationPolicyReferenceTask setPetitionerSolicitorOrganisationPolicyReferenceTask;
    private final SetRespondentSolicitorOrganisationPolicyReferenceTask setRespondentSolicitorOrganisationPolicyReferenceTask;
    private final RespondentOrganisationPolicyRemovalTask respondentOrganisationPolicyRemovalTask;
    private final RespondentDetailsRemovalTask respondentDetailsRemovalTask;
    private final SetNewLegalConnectionPolicyTask setNewLegalConnectionPolicyTask;
    private final CopyD8JurisdictionConnectionPolicyTask copyD8JurisdictionConnectionPolicyTask;
    private final ValidateSelectedOrganisationTask validateSelectedOrganisationTask;

    private final FeatureToggleService featureToggleService;

    public Map<String, Object> run(CaseDetails caseDetails, final String authToken) throws WorkflowException {
        final String caseId = caseDetails.getCaseId();

        log.info("CaseID: {} SolicitorUpdateWorkflow workflow is going to be executed.", caseId);

        return this.execute(
            getTasks(caseDetails),
            caseDetails.getCaseData(),
            ImmutablePair.of(AUTH_TOKEN_JSON_KEY, authToken),
            ImmutablePair.of(CASE_DETAILS_JSON_KEY, caseDetails),
            ImmutablePair.of(CASE_ID_JSON_KEY, caseId)
        );
    }

    private Task<Map<String, Object>>[] getTasks(CaseDetails caseDetails) {
        final String caseId = caseDetails.getCaseId();
        List<Task<Map<String, Object>>> tasks = new ArrayList<>();

        tasks.add(getNewLegalConnectionPolicyTask(caseId));
        tasks.add(copyD8JurisdictionConnectionPolicyTask(caseId));
        tasks.add(getAddMiniPetitionDraftTask(caseId));
        tasks.add(getAddNewDocumentsToCaseDataTask(caseId));

        if (isShareACaseEnabled()) {
            log.info("CaseId: {}, validate selected petitioner organisation", caseId);
            tasks.add(validateSelectedOrganisationTask);
        } else {
            log.info("CaseId: {}, share a case switched OFF, no tasks added", caseId);
        }

        if (isRepresentedRespondentJourneyEnabled()) {
            log.info("CaseId: {}, Adding task to set petSol Organisation Policy Reference details", caseId);
            tasks.add(setPetitionerSolicitorOrganisationPolicyReferenceTask);

            if (isRespondentRepresented(caseDetails.getCaseData())) {
                if (isRespondentSolicitorDigitalSelectedYes(caseDetails.getCaseData())) {
                    log.info("CaseId: {}, Adding task to set respSol Organisation Policy Reference details", caseId);
                    tasks.add(setRespondentSolicitorOrganisationPolicyReferenceTask);
                } else {
                    log.info("CaseId: {}, adding task to remove Organisation Policy details when respSol NOT digital", caseId);
                    tasks.add(respondentOrganisationPolicyRemovalTask);
                }
            } else {
                log.info("CaseId: {}, Respondent not represented, adding task to remove respSol details", caseId);
                tasks.add(respondentDetailsRemovalTask);
            }
        }

        return tasks.toArray(new Task[] {});
    }

    private boolean isRepresentedRespondentJourneyEnabled() {
        return featureToggleService.isFeatureEnabled(Features.REPRESENTED_RESPONDENT_JOURNEY);
    }

    private boolean isShareACaseEnabled() {
        return featureToggleService.isFeatureEnabled(Features.SHARE_A_CASE);
    }

    private Task<Map<String, Object>> getAddNewDocumentsToCaseDataTask(String caseId) {
        log.info("CaseId: {} Adding task to Add new documents to case data.", caseId);
        return addNewDocumentsToCaseDataTask;
    }

    private Task<Map<String, Object>> getAddMiniPetitionDraftTask(String caseId) {
        log.info("CaseId: {} Adding task to Add Mini Petition Draft.", caseId);
        return addMiniPetitionDraftTask;
    }

    private Task<Map<String, Object>> getNewLegalConnectionPolicyTask(String caseId) {
        log.info("CaseId: {} Adding task to set new legal connection policy.", caseId);
        return setNewLegalConnectionPolicyTask;
    }

    private Task<Map<String, Object>> copyD8JurisdictionConnectionPolicyTask(String caseId) {
        log.info("CaseId: {} Adding task to copy new legal connection policy.", caseId);
        return copyD8JurisdictionConnectionPolicyTask;
    }
}
