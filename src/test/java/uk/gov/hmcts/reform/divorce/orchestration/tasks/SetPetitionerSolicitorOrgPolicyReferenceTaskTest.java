package uk.gov.hmcts.reform.divorce.orchestration.tasks;


import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.Organisation;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.OrganisationPolicy;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.generics.SolicitorOrganisationPolicyReferenceDetailTask;

import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_SOLICITOR_REFERENCE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.CcdFields.PETITIONER_SOLICITOR_ORGANISATION_POLICY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.SOLICITOR_REFERENCE_JSON_KEY;

@RunWith(MockitoJUnitRunner.class)
public class SetPetitionerSolicitorOrgPolicyReferenceTaskTest extends SolicitorOrganisationPolicyReferenceDetailTaskTest {

    @InjectMocks
    private SetPetitionerSolicitorOrgPolicyReferenceTask setSolicitorOrgPolicyReferenceTask;

    @Override
    protected SolicitorOrganisationPolicyReferenceDetailTask getTask() {
        return setSolicitorOrgPolicyReferenceTask;
    }

    @Override
    protected String getSolicitorOrganisationPolicyCaseField() {
        return PETITIONER_SOLICITOR_ORGANISATION_POLICY;
    }

    @Override
    protected void setCaseDataWithoutOrganisationPolicy() {
        caseData.put(SOLICITOR_REFERENCE_JSON_KEY, TEST_SOLICITOR_REFERENCE);
    }

    @Override
    protected void setCaseDataSolicitorCreate() {
        setCaseDataWithoutOrganisationPolicy();
        caseData.put(PETITIONER_SOLICITOR_ORGANISATION_POLICY, OrganisationPolicy.builder()
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
        caseData.put(PETITIONER_SOLICITOR_ORGANISATION_POLICY, OrganisationPolicy.builder()
            .orgPolicyReference("ExistingPolicyReference")
            .organisation(Organisation.builder()
                .build())
            .build());
    }

}