package uk.gov.hmcts.reform.divorce.orchestration.tasks;


import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.Organisation;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.OrganisationPolicy;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.generics.SolicitorOrganisationPolicyReferenceTask;

import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_ORGANISATION_POLICY_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_SOLICITOR_REFERENCE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.CcdFields.PETITIONER_SOLICITOR_FIRM;
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
        caseData.put(
            getSolicitorOrganisationPolicyCaseField(),
            OrganisationPolicy.builder().organisation(buildOrganisation()).build()
        );
    }

    @Override
    protected void setCaseDataWithoutSolicitorReference() {
        caseData.put("SomeOtherKey", "SomeOtherValue");
    }

    @Override
    protected void setCaseDataSolicitorUpdate() {
        setCaseDataWithoutOrganisationPolicy();
        caseData.put(
            getSolicitorOrganisationPolicyCaseField(),
            OrganisationPolicy.builder()
                .orgPolicyReference("ExistingPolicyReference")
                .organisation(buildOrganisation())
                .build()
        );
    }

    @Test
    public void shouldCopyOrganisationNameToPetitionerSolicitorFirm() {
        setCaseDataSolicitorCreate();

        Map<String, Object> returnCaseData = getTask().execute(context, caseData);

        OrganisationPolicy organisationPolicy = (OrganisationPolicy) returnCaseData
            .get(getSolicitorOrganisationPolicyCaseField());

        assertThat(organisationPolicy, is(notNullValue()));
        assertThat(caseData.get(PETITIONER_SOLICITOR_FIRM), is(organisationPolicy.getOrganisation().getOrganisationName()));
    }

    private Organisation buildOrganisation() {
        return Organisation.builder()
            .organisationName(TEST_ORGANISATION_POLICY_NAME)
            .build();
    }
}
