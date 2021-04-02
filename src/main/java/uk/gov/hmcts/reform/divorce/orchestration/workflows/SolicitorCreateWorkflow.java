package uk.gov.hmcts.reform.divorce.orchestration.workflows;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.CcdFields;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.Features;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CaseDetails;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.DefaultWorkflow;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.WorkflowException;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.Task;
import uk.gov.hmcts.reform.divorce.orchestration.service.FeatureToggleService;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.AddMiniPetitionDraftTask;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.AddNewDocumentsToCaseDataTask;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.CopyD8JurisdictionConnectionPolicyTask;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.RespondentOrganisationPolicyRemovalTask;
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
import static uk.gov.hmcts.reform.divorce.orchestration.tasks.util.TaskUtils.getOptionalPropertyValueAsString;

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
    private final RespondentOrganisationPolicyRemovalTask respondentOrganisationPolicyRemovalTask;
    private final SetNewLegalConnectionPolicyTask setNewLegalConnectionPolicyTask;
    private final CopyD8JurisdictionConnectionPolicyTask copyD8JurisdictionConnectionPolicyTask;

    private final FeatureToggleService featureToggleService;

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
        final String caseId = caseDetails.getCaseId();
        List<Task<Map<String, Object>>> tasks = new ArrayList<>();

        if (isPetitionerClaimingCostsAndClaimCostsFromIsEmptyIn(caseDetails)) {
            log.info("CaseId: {} petitioner claiming costs", caseId);
            tasks.add(setClaimCostsFromTask);
        }

        tasks.add(setSolicitorCourtDetailsTask);
        tasks.add(setNewLegalConnectionPolicyTask);
        tasks.add(copyD8JurisdictionConnectionPolicyTask);
        tasks.add(addMiniPetitionDraftTask);
        tasks.add(addNewDocumentsToCaseDataTask);

        if (featureToggleService.isFeatureEnabled(Features.REPRESENTED_RESPONDENT_JOURNEY)) {
            log.info("CaseId: {} Adding OrganisationPolicyReferenceTasks", caseId);
            tasks.add(setPetitionerSolicitorOrganisationPolicyReferenceTask);

            if (isRespondentSolicitorDigital(caseDetails.getCaseData())) {
                log.info("CaseId: {} respondent solicitor is digital", caseId);
                tasks.add(setRespondentSolicitorOrganisationPolicyReferenceTask);
            } else {
                log.info("CaseId: {} respondent solicitor is NOT digital", caseId);
                tasks.add(respondentOrganisationPolicyRemovalTask);
            }
        } else {
            log.info("CaseId: {} RRJ is OFF", caseId);
        }

        return tasks.toArray(new Task[] {});
    }

    /**
     * I know there is re-usable method, but that method can (and should) be used after this step of the process.
     * Here we have an edge case scenario when somebody may select Yes, populate it and then select No.
     *
     * <p>We need to cover this scenario and clean up data properly.
     * */
    private boolean isRespondentSolicitorDigital(Map<String, Object> caseData) {
        return getOptionalPropertyValueAsString(caseData, CcdFields.RESPONDENT_SOLICITOR_DIGITAL, "")
            .equalsIgnoreCase(YES_VALUE);
    }
}
