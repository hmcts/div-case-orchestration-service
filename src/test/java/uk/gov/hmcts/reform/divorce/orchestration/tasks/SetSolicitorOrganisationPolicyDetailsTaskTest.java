package uk.gov.hmcts.reform.divorce.orchestration.tasks;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.PetitionerOrganisationPolicy;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.DefaultTaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskException;

import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThrows;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_SOLICITOR_REFERENCE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.CcdFields.SOLICITOR_PETITIONER_ORGANISATION_POLICY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.SOLICITOR_REFERENCE_JSON_KEY;

@RunWith(MockitoJUnitRunner.class)
public class SetSolicitorOrganisationPolicyDetailsTaskTest {

    @InjectMocks
    private SetSolicitorOrganisationPolicyDetailsTask setSolicitorOrganisationPolicyDetailsTask;

    private Map<String, Object> caseData;
    private TaskContext context;
    public static String SOLICITOR_REFERENCE_MISSING = "Could not evaluate value of mandatory property \"D8SolicitorReference\"";

    @Before
    public void setup() {
        caseData = new HashMap<>();
        context = new DefaultTaskContext();
    }

    @Test
    public void shouldMapD8SolicitorReferenceToPetitionerOrganisationPolicy() {
        caseData.put(SOLICITOR_REFERENCE_JSON_KEY, TEST_SOLICITOR_REFERENCE);

        Map<String, Object> returnCaseData = setSolicitorOrganisationPolicyDetailsTask.execute(context, caseData);

        PetitionerOrganisationPolicy organisationPolicy = (PetitionerOrganisationPolicy) returnCaseData.get(SOLICITOR_PETITIONER_ORGANISATION_POLICY);

        assertThat(organisationPolicy, is(notNullValue()));
        assertThat(organisationPolicy.getOrgPolicyReference(), is(TEST_SOLICITOR_REFERENCE));
    }

    @Test
    public void shouldThrowErrorWhenMapD8SolicitorReferenceDoesNotExist() {
        TaskException taskException = assertThrows(TaskException.class, () -> setSolicitorOrganisationPolicyDetailsTask.execute(context, caseData));

        assertThat(taskException.getMessage(), is(SOLICITOR_REFERENCE_MISSING));
    }
}
