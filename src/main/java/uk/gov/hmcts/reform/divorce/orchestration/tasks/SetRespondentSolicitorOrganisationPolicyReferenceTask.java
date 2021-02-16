package uk.gov.hmcts.reform.divorce.orchestration.tasks;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.generics.SolicitorOrganisationPolicyReferenceTask;

import java.util.Map;

import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.CcdFields.RESPONDENT_SOLICITOR_ORGANISATION_POLICY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D8_RESPONDENT_SOLICITOR_REFERENCE;
import static uk.gov.hmcts.reform.divorce.orchestration.tasks.util.TaskUtils.getOptionalPropertyValueAsString;
import static uk.gov.hmcts.reform.divorce.orchestration.util.PartyRepresentationChecker.isRespondentRepresented;

@Component
@Slf4j
public class SetRespondentSolicitorOrganisationPolicyReferenceTask extends SolicitorOrganisationPolicyReferenceTask {

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
        if (!isRespondentRepresented(caseData)) {
            return null;
        }
        return getOptionalPropertyValueAsString(caseData, caseField, null);
    }

}