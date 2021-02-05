package uk.gov.hmcts.reform.divorce.orchestration.tasks;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.PetitionerOrganisationPolicy;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.Task;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskContext;

import java.util.Map;

import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.CcdFields.SOLICITOR_PETITIONER_ORGANISATION_POLICY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.SOLICITOR_REFERENCE_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor.SolicitorDataExtractor.getPetitionerOrganisationPolicy;
import static uk.gov.hmcts.reform.divorce.orchestration.tasks.util.TaskUtils.getMandatoryPropertyValueAsString;

@Component
public class SetSolicitorOrganisationPolicyDetailsTask implements Task<Map<String, Object>> {

    @Override
    public Map<String, Object> execute(TaskContext context, Map<String, Object> caseData) {
        return updatePetitionerSolicitorOrgPolicyReference(caseData);
    }

    private Map<String, Object> updatePetitionerSolicitorOrgPolicyReference(Map<String, Object> caseData) {
        PetitionerOrganisationPolicy organisationPolicy = getPetitionerOrganisationPolicy(caseData);
        organisationPolicy.setOrgPolicyReference(getMandatoryPropertyValueAsString(caseData, SOLICITOR_REFERENCE_JSON_KEY));

        caseData.put(
            SOLICITOR_PETITIONER_ORGANISATION_POLICY,
            organisationPolicy
        );

        return caseData;
    }
}