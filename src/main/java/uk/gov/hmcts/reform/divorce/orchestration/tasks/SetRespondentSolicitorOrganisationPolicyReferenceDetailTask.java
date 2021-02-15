package uk.gov.hmcts.reform.divorce.orchestration.tasks;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.generics.SolicitorOrganisationPolicyReferenceDetailTask;

import java.util.Map;
import java.util.Optional;

import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.CcdFields.PETITIONER_SOLICITOR_ORGANISATION_POLICY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.CcdFields.RESPONDENT_SOLICITOR_ORGANISATION_POLICY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D8_RESPONDENT_SOLICITOR_REFERENCE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.SOLICITOR_REFERENCE_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.tasks.util.TaskUtils.getOptionalPropertyValueAsString;
import static uk.gov.hmcts.reform.divorce.orchestration.util.PartyRepresentationChecker.isRespondentRepresented;

@Component
@Slf4j
public class SetRespondentSolicitorOrganisationPolicyReferenceDetailTask extends SolicitorOrganisationPolicyReferenceDetailTask {

    @Override
    protected String getSolicitorReferenceCaseField() {
        return D8_RESPONDENT_SOLICITOR_REFERENCE;
    }

    @Override
    protected String getOrganisationPolicyCaseField() {
        return RESPONDENT_SOLICITOR_ORGANISATION_POLICY;
    }

    @Override
    protected String getSolicitorReference(Map<String, Object> caseData, String caseField) {
        if(!isRespondentRepresented(caseData)){
            return null;
        }
        return Optional.ofNullable(getOptionalPropertyValueAsString(caseData, caseField, null)).orElse(null);
    }

}