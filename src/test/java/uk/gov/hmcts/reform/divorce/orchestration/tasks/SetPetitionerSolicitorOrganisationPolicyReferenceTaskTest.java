package uk.gov.hmcts.reform.divorce.orchestration.tasks;


import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.Organisation;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.OrganisationPolicy;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.generics.SolicitorOrganisationPolicyReferenceTask;

import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_SOLICITOR_REFERENCE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.CcdFields.PETITIONER_SOLICITOR_ORGANISATION_POLICY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.SOLICITOR_REFERENCE_JSON_KEY;

@RunWith(MockitoJUnitRunner.class)
public class SetPetitionerSolicitorOrganisationPolicyReferenceTaskTest extends SolicitorOrganisationPolicyReferenceTaskTest {

    @InjectMocks
    private SetPetitionerSolicitorOrganisationPolicyReferenceTask setSolicitorOrgPolicyReferenceTask;

    @Override
    protected SolicitorOrganisationPolicyReferenceTask getTask() {
        return setSolicitorOrgPolicyReferenceTask;
    }

    @Override
    protected String getSolicitorOrganisationPolicyCaseField() {
        return PETITIONER_SOLICITOR_ORGANISATION_POLICY;
    }

    @Override
    protected String getSolicitorReferenceCaseField() {
        return SOLICITOR_REFERENCE_JSON_KEY;
    }

    @Override
    protected void setCaseDataWithoutOrganisationPolicy() {
        caseData.put(getSolicitorReferenceCaseField(), TEST_SOLICITOR_REFERENCE);
    }

    @Override
    protected void setCaseDataSolicitorCreate() {
        setCaseDataWithoutOrganisationPolicy();
        caseData.put(getSolicitorOrganisationPolicyCaseField(), OrganisationPolicy.builder()
            .organisation(Organisation.builder().build())
            .build());
    }

    @Override
    protected void setCaseDataWithoutSolicitorReference() {
        caseData.put("SomeOtherKey", "SomeOtherValue");
    }

    @Override
    protected void setCaseDataSolicitorUpdate() {
        setCaseDataWithoutOrganisationPolicy();
        caseData.put(getSolicitorOrganisationPolicyCaseField(), OrganisationPolicy.builder()
            .orgPolicyReference("ExistingPolicyReference")
            .organisation(Organisation.builder()
                .build())
            .build());
    }

}