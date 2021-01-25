package uk.gov.hmcts.reform.divorce.orchestration.workflows;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.Features;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CaseDetails;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.DefaultWorkflow;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.WorkflowException;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.Task;
import uk.gov.hmcts.reform.divorce.orchestration.service.FeatureToggleService;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.AddMiniPetitionDraftTask;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.AddNewDocumentsToCaseDataTask;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.SetClaimCostsFromTask;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.SetNewLegalConnectionPolicyTask;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.SetPetitionerSolicitorOrganisationPolicyReferenceTask;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.SetRespondentSolicitorOrganisationPolicyReferenceTask;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.SetSolicitorCourtDetailsTask;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.AUTH_TOKEN_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CASE_DETAILS_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CASE_ID_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DIVORCE_COSTS_CLAIM_CCD_FIELD;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DIVORCE_COSTS_CLAIM_FROM_CCD_FIELD;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.YES_VALUE;

@Component
@Slf4j
@RequiredArgsConstructor
public class SolicitorCreateWorkflow extends DefaultWorkflow<Map<String, Object>> {

    private final SetSolicitorCourtDetailsTask setSolicitorCourtDetailsTask;
    private final AddMiniPetitionDraftTask addMiniPetitionDraftTask;
    private final AddNewDocumentsToCaseDataTask addNewDocumentsToCaseDataTask;
    private final SetClaimCostsFromTask setClaimCostsFromTask;
    private final SetPetitionerSolicitorOrganisationPolicyReferenceTask setPetitionerSolicitorOrganisationPolicyReferenceTask;
    private final SetRespondentSolicitorOrganisationPolicyReferenceTask setRespondentSolicitorOrganisationPolicyReferenceTask;
    private final FeatureToggleService featureToggleService;
    private final SetNewLegalConnectionPolicyTask setNewLegalConnectionPolicyTask;

    public Map<String, Object> run(CaseDetails caseDetails, String authToken) throws WorkflowException {
        return this.execute(
            getTasks(caseDetails),
            caseDetails.getCaseData(),
            ImmutablePair.of(AUTH_TOKEN_JSON_KEY, authToken),
            ImmutablePair.of(CASE_DETAILS_JSON_KEY, caseDetails),
            ImmutablePair.of(CASE_ID_JSON_KEY, caseDetails.getCaseId())
        );
    }

    private boolean isPetitionerClaimingCostsAndClaimCostsFromIsEmptyIn(CaseDetails caseDetails) {
        Map<String, Object> caseData = caseDetails.getCaseData();
        boolean isPetitionerClaimingCosts = YES_VALUE.equalsIgnoreCase(String.valueOf(caseData.get(DIVORCE_COSTS_CLAIM_CCD_FIELD)));
        boolean claimCostsFromIsEmpty = StringUtils.isEmpty(caseData.get(DIVORCE_COSTS_CLAIM_FROM_CCD_FIELD));

        return isPetitionerClaimingCosts && claimCostsFromIsEmpty;
    }

    private Task<Map<String, Object>>[] getTasks(CaseDetails caseDetails) {
        List<Task<Map<String, Object>>> tasks = new ArrayList<>();

        if (isPetitionerClaimingCostsAndClaimCostsFromIsEmptyIn(caseDetails)) {
            tasks.add(setClaimCostsFromTask);
        }

        tasks.add(setSolicitorCourtDetailsTask);
        tasks.add(addMiniPetitionDraftTask);
        tasks.add(addNewDocumentsToCaseDataTask);
        tasks.add(setNewLegalConnectionPolicyTask);

        if (featureToggleService.isFeatureEnabled(Features.REPRESENTED_RESPONDENT_JOURNEY)) {
            log.info("Adding OrganisationPolicyReferenceTasks, REPRESENTED_RESPONDENT_JOURNEY feature toggle is set to true.");
            tasks.add(setPetitionerSolicitorOrganisationPolicyReferenceTask);
            tasks.add(setRespondentSolicitorOrganisationPolicyReferenceTask);
        }

        return tasks.toArray(new Task[] {});
    }
}
