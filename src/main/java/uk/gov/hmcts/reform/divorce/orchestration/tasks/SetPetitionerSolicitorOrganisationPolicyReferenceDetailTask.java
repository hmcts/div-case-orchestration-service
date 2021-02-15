package uk.gov.hmcts.reform.divorce.orchestration.tasks;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.generics.SolicitorOrganisationPolicyReferenceDetailTask;

import java.util.Map;
import java.util.Optional;

import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.CcdFields.PETITIONER_SOLICITOR_ORGANISATION_POLICY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.SOLICITOR_REFERENCE_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.tasks.util.TaskUtils.getOptionalPropertyValueAsString;

@Component
@Slf4j
public class SetPetitionerSolicitorOrganisationPolicyReferenceDetailTask extends SolicitorOrganisationPolicyReferenceDetailTask {

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
        return Optional.ofNullable(getOptionalPropertyValueAsString(caseData, caseField, null)).orElse(null);
    }

}