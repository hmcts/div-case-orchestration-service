package uk.gov.hmcts.reform.divorce.orchestration.tasks;

import feign.FeignException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.divorce.orchestration.client.OrganisationClient;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CaseDetails;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.OrganisationPolicy;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.prd.OrganisationsResponse;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskException;
import uk.gov.hmcts.reform.divorce.orchestration.service.AssignCaseAccessService;
import uk.gov.hmcts.reform.divorce.orchestration.service.CcdDataStoreService;

import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThrows;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_ORGANISATION_POLICY_ID;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.CcdFields.PETITIONER_SOLICITOR_ORGANISATION_POLICY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.AUTH_TOKEN_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CASE_DETAILS_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.testutil.CaseDataTestHelper.buildOrganisationPolicy;
import static uk.gov.hmcts.reform.divorce.orchestration.testutil.TaskContextHelper.contextWithCaseDetails;

@RunWith(MockitoJUnitRunner.class)
public class AllowShareACaseTaskTest {

    @Mock
    private AssignCaseAccessService assignCaseAccessService;

    @Mock
    private CcdDataStoreService ccdDataStoreService;

    @Mock
    private OrganisationClient organisationClient;

    @InjectMocks
    private AllowShareACaseTask allowShareACaseTask;

    @Test
    public void shouldAssignAccessToCaseAndRemoveCaseRole() {
        Map<String, Object> input = Map.of(PETITIONER_SOLICITOR_ORGANISATION_POLICY, buildOrganisationPolicy());
        TaskContext context = getContext(input);

        mockMyOrganisationFound();

        Map<String, Object> result = allowShareACaseTask.execute(context, input);

        assertThat(result, is(input));

        verify(organisationClient)
            .getMyOrganisation(
                context.getTransientObject(AUTH_TOKEN_JSON_KEY)
            );
        verify(assignCaseAccessService)
            .assignCaseAccess(
                context.getTransientObject(CASE_DETAILS_JSON_KEY),
                context.getTransientObject(AUTH_TOKEN_JSON_KEY)
            );
        verify(ccdDataStoreService)
            .removeCreatorRole(
                context.getTransientObject(CASE_DETAILS_JSON_KEY),
                context.getTransientObject(AUTH_TOKEN_JSON_KEY)
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
            () -> allowShareACaseTask.execute(context, input)
        );

        verifyNoInteractions(organisationClient, assignCaseAccessService, ccdDataStoreService);

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
            () -> allowShareACaseTask.execute(context, input)
        );

        verify(organisationClient).getMyOrganisation(AUTH_TOKEN);
        verifyNoInteractions(assignCaseAccessService, ccdDataStoreService);
        assertThat(taskException.getMessage(), is("Please select an organisation you belong to"));
    }

    @Test
    public void shouldThrowTaskExceptionWhenPrdRequestFails() {
        Map<String, Object> input = Map.of(PETITIONER_SOLICITOR_ORGANISATION_POLICY, buildOrganisationPolicy());

        TaskContext context = getContext(input);

        TaskException taskException = assertThrows(
            TaskException.class,
            () -> allowShareACaseTask.execute(context, input)
        );

        verify(organisationClient).getMyOrganisation(AUTH_TOKEN);
        verifyNoInteractions(assignCaseAccessService, ccdDataStoreService);
        assertThat(taskException.getMessage(), is("PRD API call failed"));
    }

    @Test
    public void shouldNotRemoveCaseRoleAndReturnErrorWhenAssignAccessApiCallFails() {
        Map<String, Object> input = Map.of(PETITIONER_SOLICITOR_ORGANISATION_POLICY, buildOrganisationPolicy());
        TaskContext context = getContext(input);

        mockMyOrganisationFound();

        doThrow(FeignException.class).when(assignCaseAccessService).assignCaseAccess(
            context.getTransientObject(CASE_DETAILS_JSON_KEY),
            context.getTransientObject(AUTH_TOKEN_JSON_KEY)
        );

        Map<String, Object> result = allowShareACaseTask.execute(context, input);

        assertThat(result, is(input));
        verify(organisationClient).getMyOrganisation(AUTH_TOKEN);
        verify(assignCaseAccessService)
            .assignCaseAccess(
                context.getTransientObject(CASE_DETAILS_JSON_KEY),
                context.getTransientObject(AUTH_TOKEN_JSON_KEY)
            );
        verifyNoInteractions(ccdDataStoreService);

        assertThat(context.hasTaskFailed(), is(true));
        assertThat(context.getTransientObject("AssignCaseAccess_Error"),
            is("Problem calling assign case access API to set the [PETSOLICITOR] role to the case"));
    }

    private void mockMyOrganisationFound() {
        when(organisationClient.getMyOrganisation(AUTH_TOKEN)).thenReturn(
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
