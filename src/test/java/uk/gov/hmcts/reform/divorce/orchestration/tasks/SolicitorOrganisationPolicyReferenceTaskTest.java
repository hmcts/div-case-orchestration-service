package uk.gov.hmcts.reform.divorce.orchestration.tasks;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.OrganisationPolicy;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.DefaultTaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.generics.SolicitorOrganisationPolicyReferenceTask;

import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_SOLICITOR_REFERENCE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CASE_ID_JSON_KEY;

@RunWith(MockitoJUnitRunner.class)
public abstract class SolicitorOrganisationPolicyReferenceTaskTest {

    protected Map<String, Object> caseData;
    protected TaskContext context;

    @Before
    public void setup() {
        caseData = new HashMap<>();
        context = new DefaultTaskContext();
        context.setTransientObject(CASE_ID_JSON_KEY, TEST_CASE_ID);
    }

    protected abstract SolicitorOrganisationPolicyReferenceTask getTask();

    protected abstract String getSolicitorOrganisationPolicyCaseField();

    protected abstract String getSolicitorReferenceCaseField();

    protected abstract void setCaseDataSolicitorCreate();

    protected abstract void setCaseDataSolicitorUpdate();

    protected abstract void setCaseDataWithoutSolicitorReference();

    protected abstract void setCaseDataWithoutOrganisationPolicy();

    @Test
    public void shouldMapSolicitorReferenceToOrganisationPolicyReference() {
        setCaseDataSolicitorCreate();

        Map<String, Object> returnCaseData = getTask().execute(context, caseData);

        OrganisationPolicy organisationPolicy = (OrganisationPolicy) returnCaseData.get(getSolicitorOrganisationPolicyCaseField());

        assertThat(organisationPolicy, is(notNullValue()));
        assertThat(organisationPolicy.getOrgPolicyReference(), is(TEST_SOLICITOR_REFERENCE));
    }

    @Test
    public void shouldNotUpdateCaseDataWhenSolicitorReferenceIsNotProvided() {
        setCaseDataWithoutSolicitorReference();

        Map<String, Object> returnCaseData = getTask().execute(context, caseData);

        OrganisationPolicy organisationPolicy = (OrganisationPolicy) returnCaseData.get(getSolicitorOrganisationPolicyCaseField());

        assertThat(organisationPolicy, is(nullValue()));
        assertThat(caseData, is(returnCaseData));
    }

    @Test
    public void shouldUpdateExistingOrganisationPolicyReferenceWhenSolicitorReferenceIsProvided() {
        setCaseDataSolicitorUpdate();

        Map<String, Object> returnCaseData = getTask().execute(context, caseData);

        OrganisationPolicy organisationPolicy = (OrganisationPolicy) returnCaseData.get(getSolicitorOrganisationPolicyCaseField());

        assertThat(organisationPolicy, is(notNullValue()));
        assertThat(organisationPolicy.getOrgPolicyReference(), is(TEST_SOLICITOR_REFERENCE));
    }

    @Test
    public void shouldNotAddOrganisationPolicyWhenSolicitorReferenceIsProvidedAndOrganisationPolicyDoesNotExist() {
        setCaseDataWithoutOrganisationPolicy();

        Map<String, Object> returnCaseData = getTask().execute(context, caseData);

        OrganisationPolicy organisationPolicy = (OrganisationPolicy) returnCaseData.get(getSolicitorOrganisationPolicyCaseField());

        assertThat(organisationPolicy, is(nullValue()));
    }

}
