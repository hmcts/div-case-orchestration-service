package uk.gov.hmcts.reform.divorce.orchestration.tasks;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.OrganisationPolicy;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.Task;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskContext;

import java.util.Map;
import java.util.Optional;

import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.CcdFields.PETITIONER_SOLICITOR_ORGANISATION_POLICY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.SOLICITOR_REFERENCE_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor.SolicitorDataExtractor.getPetitionerOrganisationPolicy;
import static uk.gov.hmcts.reform.divorce.orchestration.tasks.util.TaskUtils.getCaseId;
import static uk.gov.hmcts.reform.divorce.orchestration.tasks.util.TaskUtils.getOptionalPropertyValueAsString;

@Component
@Slf4j
public class SetSolicitorOrganisationPolicyDetailsTask implements Task<Map<String, Object>> {

    @Override
    public Map<String, Object> execute(TaskContext context, Map<String, Object> caseData) {
        return updatePetitionerSolicitorOrgPolicyReference(caseData, getCaseId(context));
    }

    private Map<String, Object> updatePetitionerSolicitorOrgPolicyReference(Map<String, Object> caseData, String caseId) {
        String d8SolicitorReference = getOptionalPropertyValueAsString(caseData, SOLICITOR_REFERENCE_JSON_KEY, null);

        if (d8SolicitorReference == null) {
            log.info("CaseID: {} Solicitor Reference not provided, returning case data", caseId);
            return caseData;
        }

        OrganisationPolicy updatedOrganisationPolicy = getUpdatedOrganisationPolicy(caseData, d8SolicitorReference);

        if (updatedOrganisationPolicy == null) {
            log.info("CaseID: {} Organisation Policy detail is non-existing. No data updated", caseId);
        } else {
            log.info("CaseID: {} Adding Solicitor Reference to Organisation Policy detail", caseId);
            caseData.put(PETITIONER_SOLICITOR_ORGANISATION_POLICY, updatedOrganisationPolicy);
        }

        return caseData;
    }

    private OrganisationPolicy getUpdatedOrganisationPolicy(Map<String, Object> caseData, String d8SolicitorReference) {
        OrganisationPolicy organisationPolicy = Optional.ofNullable(getPetitionerOrganisationPolicy(caseData)).orElse(null);

        if (organisationPolicy != null) {
            organisationPolicy.setOrgPolicyReference(d8SolicitorReference);
        }

        return organisationPolicy;
    }
}