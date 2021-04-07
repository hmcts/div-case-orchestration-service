package uk.gov.hmcts.reform.divorce.orchestration.tasks;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.divorce.orchestration.client.OrganisationClient;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CaseDetails;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.OrganisationPolicy;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.prd.OrganisationsResponse;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskException;

import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_ORGANISATION_POLICY_ID;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_SERVICE_TOKEN;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.CcdFields.PETITIONER_SOLICITOR_ORGANISATION_POLICY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.AUTH_TOKEN_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.testutil.CaseDataTestHelper.buildOrganisationPolicy;
import static uk.gov.hmcts.reform.divorce.orchestration.testutil.TaskContextHelper.contextWithCaseDetails;

@RunWith(MockitoJUnitRunner.class)
public class ValidateSelectedOrganisationTaskTest {

    @Mock
    private OrganisationClient organisationClient;

    @Mock
    private AuthTokenGenerator authTokenGenerator;

    @InjectMocks
    private ValidateSelectedOrganisationTask validateSelectedOrganisationTask;

    @Before
    public void setup() {
        when(authTokenGenerator.generate()).thenReturn(TEST_SERVICE_TOKEN);
    }

    @Test
    public void shouldCallPrdApiSuccessfully() {
        Map<String, Object> input = Map.of(PETITIONER_SOLICITOR_ORGANISATION_POLICY, buildOrganisationPolicy());
        TaskContext context = getContext(input);

        mockMyOrganisationFound();

        Map<String, Object> result = validateSelectedOrganisationTask.execute(context, input);

        assertThat(result, is(input));

        verify(organisationClient)
            .getMyOrganisation(
                context.getTransientObject(AUTH_TOKEN_JSON_KEY),
                TEST_SERVICE_TOKEN
            );
    }

    @Test
    public void shouldThrowTaskExceptionWhenOrganisationNotSelected() {
        Map<String, Object> input = Map.of(
            PETITIONER_SOLICITOR_ORGANISATION_POLICY,
            OrganisationPolicy.builder().build()
        );
        TaskContext context = getContext(input);

        TaskException taskException = assertThrows(
            TaskException.class,
            () -> validateSelectedOrganisationTask.execute(context, input)
        );

        verifyNoInteractions(organisationClient);

        assertThat(taskException.getMessage(), is("Please select an organisation"));
    }

    @Test
    public void shouldThrowTaskExceptionWhenWrongOrganisationSelected() {
        Map<String, Object> input = Map.of(
            PETITIONER_SOLICITOR_ORGANISATION_POLICY,
            buildOrganisationPolicy("I don't belong here!")
        );

        TaskContext context = getContext(input);

        mockMyOrganisationFound();

        TaskException taskException = assertThrows(
            TaskException.class,
            () -> validateSelectedOrganisationTask.execute(context, input)
        );

        verify(organisationClient).getMyOrganisation(AUTH_TOKEN, TEST_SERVICE_TOKEN);
        assertThat(taskException.getMessage(), is("Please select an organisation you belong to"));
    }

    @Test
    public void shouldThrowTaskExceptionWhenPrdRequestFails() {
        Map<String, Object> input = Map.of(PETITIONER_SOLICITOR_ORGANISATION_POLICY, buildOrganisationPolicy());

        TaskContext context = getContext(input);

        TaskException taskException = assertThrows(
            TaskException.class,
            () -> validateSelectedOrganisationTask.execute(context, input)
        );

        verify(organisationClient).getMyOrganisation(AUTH_TOKEN, TEST_SERVICE_TOKEN);
        assertThat(taskException.getMessage(), is("PRD API call failed"));
    }

    private void mockMyOrganisationFound() {
        when(organisationClient.getMyOrganisation(AUTH_TOKEN, TEST_SERVICE_TOKEN)).thenReturn(
            OrganisationsResponse.builder()
                .organisationIdentifier(TEST_ORGANISATION_POLICY_ID)
                .build()
        );
    }

    private TaskContext getContext(Map<String, Object> input) {
        return contextWithCaseDetails(
            CaseDetails.builder()
                .caseId(TEST_CASE_ID)
                .caseData(input)
                .build()
        );
    }
}
