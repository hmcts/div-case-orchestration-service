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
import uk.gov.hmcts.reform.divorce.orchestration.tasks.SetNewLegalConnectionPolicyTask;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.SetPetitionerSolicitorOrganisationPolicyReferenceTask;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.SetRespondentSolicitorOrganisationPolicyReferenceTask;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.AUTH_TOKEN_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CASE_DETAILS_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CASE_ID_JSON_KEY;

@Component
@Slf4j
@RequiredArgsConstructor
public class SolicitorUpdateWorkflow extends DefaultWorkflow<Map<String, Object>> {

    private final AddMiniPetitionDraftTask addMiniPetitionDraftTask;
    private final AddNewDocumentsToCaseDataTask addNewDocumentsToCaseDataTask;
    private final SetPetitionerSolicitorOrganisationPolicyReferenceTask setPetitionerSolicitorOrganisationPolicyReferenceTask;
    private final SetRespondentSolicitorOrganisationPolicyReferenceTask setRespondentSolicitorOrganisationPolicyReferenceTask;
    private final SetNewLegalConnectionPolicyTask setNewLegalConnectionPolicyTask;
    private final CopyD8JurisdictionConnectionPolicyTask copyD8JurisdictionConnectionPolicyTask;

    private final FeatureToggleService featureToggleService;


    public Map<String, Object> run(CaseDetails caseDetails, final String authToken) throws WorkflowException {
        final String caseId = caseDetails.getCaseId();

        log.info("CaseID: {} SolicitorUpdateWorkflow workflow is going to be executed.", caseId);

        return this.execute(
            getTasks(caseId),
            caseDetails.getCaseData(),
            ImmutablePair.of(AUTH_TOKEN_JSON_KEY, authToken),
            ImmutablePair.of(CASE_DETAILS_JSON_KEY, caseDetails),
            ImmutablePair.of(CASE_ID_JSON_KEY, caseId)
        );
    }

    private Task<Map<String, Object>>[] getTasks(String caseId) {
        List<Task<Map<String, Object>>> tasks = new ArrayList<>();

        tasks.add(getNewLegalConnectionPolicyTask(caseId));
        tasks.add(copyD8JurisdictionConnectionPolicyTask(caseId));
        tasks.add(getAddMiniPetitionDraftTask(caseId));
        tasks.add(getAddNewDocumentsToCaseDataTask(caseId));


        if (featureToggleService.isFeatureEnabled(Features.REPRESENTED_RESPONDENT_JOURNEY)) {
            log.info("Adding OrganisationPolicyReferenceTasks, REPRESENTED_RESPONDENT_JOURNEY feature toggle is set to true.");
            tasks.add(setPetitionerSolicitorOrganisationPolicyReferenceTask);
            tasks.add(setRespondentSolicitorOrganisationPolicyReferenceTask);
        }

        return tasks.toArray(new Task[] {});
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
