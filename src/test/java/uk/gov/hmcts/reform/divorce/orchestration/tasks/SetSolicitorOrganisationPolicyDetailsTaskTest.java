package uk.gov.hmcts.reform.divorce.orchestration.tasks;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.Organisation;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.OrganisationPolicy;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.DefaultTaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskContext;

import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_SOLICITOR_REFERENCE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.CcdFields.PETITIONER_SOLICITOR_ORGANISATION_POLICY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CASE_ID_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.SOLICITOR_REFERENCE_JSON_KEY;

@RunWith(MockitoJUnitRunner.class)
public class SetSolicitorOrganisationPolicyDetailsTaskTest {

    @InjectMocks
    private SetSolicitorOrganisationPolicyDetailsTask setSolicitorOrganisationPolicyDetailsTask;

    private Map<String, Object> caseData;
    private TaskContext context;

    @Before
    public void setup() {
        caseData = new HashMap<>();
        context = new DefaultTaskContext();
        context.setTransientObject(CASE_ID_JSON_KEY, TEST_CASE_ID);
    }

    @Test
    public void shouldMapD8SolicitorReferenceToPetitionerOrganisationPolicy() {
        caseData.put(SOLICITOR_REFERENCE_JSON_KEY, TEST_SOLICITOR_REFERENCE);
        caseData.put(PETITIONER_SOLICITOR_ORGANISATION_POLICY, OrganisationPolicy.builder()
            .organisation(Organisation.builder().build())
            .build());

        Map<String, Object> returnCaseData = setSolicitorOrganisationPolicyDetailsTask.execute(context, caseData);

        OrganisationPolicy organisationPolicy = (OrganisationPolicy) returnCaseData.get(PETITIONER_SOLICITOR_ORGANISATION_POLICY);

        assertThat(organisationPolicy, is(notNullValue()));
        assertThat(organisationPolicy.getOrgPolicyReference(), is(TEST_SOLICITOR_REFERENCE));
    }

    @Test
    public void shouldNotUpdateCaseDataWhenSolicitorReferenceIsNotProvided() {
        caseData.put("SomeOtherKey", "SomeOtherValue");
        Map<String, Object> returnCaseData = setSolicitorOrganisationPolicyDetailsTask.execute(context, caseData);

        OrganisationPolicy organisationPolicy = (OrganisationPolicy) returnCaseData.get(PETITIONER_SOLICITOR_ORGANISATION_POLICY);

        assertThat(organisationPolicy, is(nullValue()));
        assertThat(caseData, is(returnCaseData));
    }

    @Test
    public void shouldUpdateExistingPetitionerOrganisationPolicyReferenceWhenSolicitorReferenceIsProvided() {
        caseData.put(SOLICITOR_REFERENCE_JSON_KEY, TEST_SOLICITOR_REFERENCE);
        caseData.put(PETITIONER_SOLICITOR_ORGANISATION_POLICY, OrganisationPolicy.builder()
            .orgPolicyReference("ExistingPolicyReference")
            .organisation(Organisation.builder()
                .build())
            .build());

        Map<String, Object> returnCaseData = setSolicitorOrganisationPolicyDetailsTask.execute(context, caseData);

        OrganisationPolicy organisationPolicy = (OrganisationPolicy) returnCaseData.get(PETITIONER_SOLICITOR_ORGANISATION_POLICY);

        assertThat(organisationPolicy, is(notNullValue()));
        assertThat(organisationPolicy.getOrgPolicyReference(), is(TEST_SOLICITOR_REFERENCE));
    }

    @Test
    public void shouldNotAddPetitionerOrganisationPolicyWhenSolicitorReferenceIsProvidedAndPetitionerOrganisationPolicyDoesNotExist() {
        caseData.put(SOLICITOR_REFERENCE_JSON_KEY, TEST_SOLICITOR_REFERENCE);

        Map<String, Object> returnCaseData = setSolicitorOrganisationPolicyDetailsTask.execute(context, caseData);

        OrganisationPolicy organisationPolicy = (OrganisationPolicy) returnCaseData.get(PETITIONER_SOLICITOR_ORGANISATION_POLICY);

        assertThat(organisationPolicy, is(nullValue()));
    }

}
