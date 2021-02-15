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
import static uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor.SolicitorDataExtractor.getSolicitorOrganisationPolicy;
import static uk.gov.hmcts.reform.divorce.orchestration.tasks.util.TaskUtils.getCaseId;
import static uk.gov.hmcts.reform.divorce.orchestration.tasks.util.TaskUtils.getOptionalPropertyValueAsString;

@Component
@Slf4j
public class SetSolicitorOrganisationPolicyDetailsTask implements Task<Map<String, Object>> {

    @Override
    public Map<String, Object> execute(TaskContext context, Map<String, Object> caseData) {
        String caseId = getCaseId(context);
        String solicitorReferenceCaseField = getSolicitorReferenceCaseField();
        String solicitorReference = getCurrentSolicitorReference(caseData, solicitorReferenceCaseField);

        if (solicitorReference == null) {
            log.info("CaseID: {} Solicitor Reference {} not provided, returning case data", caseId, solicitorReferenceCaseField);
            return caseData;
        }

        OrganisationPolicy updatedOrganisationPolicy = getUpdatedOrganisationPolicy(caseData, solicitorReference);

        if (updatedOrganisationPolicy == null) {
            log.info("CaseID: {} Organisation Policy detail is non-existing. No data updated", caseId);
        } else {
            String organisationPolicyCaseField = getOrganisationPolicyCaseField();
            log.info("CaseID: {} Adding Solicitor Reference to Organisation Policy detail for {}", caseId, organisationPolicyCaseField);
            caseData.put(organisationPolicyCaseField, updatedOrganisationPolicy);
        }

        //if RESP_SOL_REPRESENTED (respondentSolicitorRepresented) == Yes
        // D8_RESPONDENT_SOLICITOR_REFERENCE (respondentSolicitorReference)

        return caseData;
    }

    private String getSolicitorReferenceCaseField() {
        return SOLICITOR_REFERENCE_JSON_KEY;
    }

    private String getOrganisationPolicyCaseField() {
        return PETITIONER_SOLICITOR_ORGANISATION_POLICY;
    }

    private String getCurrentSolicitorReference(Map<String, Object> caseData, String caseField) {
        return Optional.ofNullable(getOptionalPropertyValueAsString(caseData, caseField, null)).orElse(null);
    }

    private OrganisationPolicy getUpdatedOrganisationPolicy(Map<String, Object> caseData, String d8SolicitorReference) {
        OrganisationPolicy organisationPolicy = Optional.ofNullable(getSolicitorOrganisationPolicy(caseData, getOrganisationPolicyCaseField() ))
            .orElse(null);

        if (organisationPolicy != null) {
            organisationPolicy.setOrgPolicyReference(d8SolicitorReference);
        }

        return organisationPolicy;
    }
}