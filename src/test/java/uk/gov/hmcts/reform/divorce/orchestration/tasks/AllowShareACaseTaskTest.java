package uk.gov.hmcts.reform.divorce.orchestration.tasks;

import feign.FeignException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.service.AssignCaseAccessService;
import uk.gov.hmcts.reform.divorce.orchestration.service.CcdDataStoreService;

import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
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

    @InjectMocks
    private AllowShareACaseTask allowShareACaseTask;

    @Test
    public void shouldAssignAccessToCaseAndRemoveCaseRole() {
        Map<String, Object> input = Map.of(PETITIONER_SOLICITOR_ORGANISATION_POLICY, buildOrganisationPolicy());
        TaskContext context = contextWithCaseDetails();

        Map<String, Object> result = allowShareACaseTask.execute(context, input);

        assertThat(result, is(input));
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
    public void shouldNotAssignAccessToCaseNorRemoveCaseRole_WhenPetitionerSolicitorIsNotDigital() {
        Map<String, Object> input = new HashMap<>();
        TaskContext context = contextWithCaseDetails();

        Map<String, Object> result = allowShareACaseTask.execute(context, input);

        assertThat(result, is(input));
        verify(assignCaseAccessService, never()).assignCaseAccess(any(), any());
        verify(ccdDataStoreService, never()).removeCreatorRole(any(), any());
    }

    @Test
    public void givenAssignCaseAccessFailure_shouldNotRemoveCaseRoleAndReturnError() {
        Map<String, Object> input = Map.of(PETITIONER_SOLICITOR_ORGANISATION_POLICY, buildOrganisationPolicy());
        TaskContext context = contextWithCaseDetails();

        doThrow(FeignException.class).when(assignCaseAccessService).assignCaseAccess(
            context.getTransientObject(CASE_DETAILS_JSON_KEY),
            context.getTransientObject(AUTH_TOKEN_JSON_KEY));

        Map<String, Object> result = allowShareACaseTask.execute(context, input);

        assertThat(result, is(input));
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
}
