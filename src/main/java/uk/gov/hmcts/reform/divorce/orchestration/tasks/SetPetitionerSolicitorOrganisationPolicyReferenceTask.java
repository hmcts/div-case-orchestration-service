package uk.gov.hmcts.reform.divorce.orchestration.tasks;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.OrganisationPolicy;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.generics.SolicitorOrganisationPolicyReferenceTask;

import java.util.Map;

import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.CcdFields.PETITIONER_SOLICITOR_FIRM;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.CcdFields.PETITIONER_SOLICITOR_ORGANISATION_POLICY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.SOLICITOR_REFERENCE_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.tasks.util.TaskUtils.getOptionalPropertyValueAsString;

@Component
@Slf4j
public class SetPetitionerSolicitorOrganisationPolicyReferenceTask extends SolicitorOrganisationPolicyReferenceTask {

    @Override
    protected String getSolicitorReferenceCaseField() {
        return SOLICITOR_REFERENCE_JSON_KEY;
    }

    @Override
    protected String getOrganisationPolicyCaseField() {
        return PETITIONER_SOLICITOR_ORGANISATION_POLICY;
    }

    @Override
    protected String getSolicitorReference(Map<String, Object> caseData, String caseField) {
        return getOptionalPropertyValueAsString(caseData, caseField, null);
    }

    @Override
    protected void populateLegacyFieldsWithValuesFromOrganisationPolicy(String caseId,
                                                                        Map<String, Object> caseData,
                                                                        OrganisationPolicy organisationPolicy) {
        String organisationName = organisationPolicy.getOrganisation().getOrganisationName();

        log.info("CaseId: {}, populate PetitionerOrganisationFirmName = {}", caseId, organisationName);
        caseData.put(PETITIONER_SOLICITOR_FIRM, organisationName);
    }
}
