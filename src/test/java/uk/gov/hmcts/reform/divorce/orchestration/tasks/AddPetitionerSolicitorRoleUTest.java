package uk.gov.hmcts.reform.divorce.orchestration.tasks;

import feign.FeignException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.divorce.orchestration.client.CaseMaintenanceClient;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.DefaultTaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskException;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.UNFORMATTED_CASE_ID;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.AUTH_TOKEN_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CASE_ID_JSON_KEY;

@RunWith(MockitoJUnitRunner.class)
public class AddPetitionerSolicitorRoleUTest {
    @Mock
    private CaseMaintenanceClient caseMaintenanceClient;

    @InjectMocks
    private AddPetitionerSolicitorRole classUnderTest;

    @Test
    public void givenCaseExists_whenAddingPetSolicitorRole_thenReturnExpectedOutput() throws TaskException {
        final DefaultTaskContext context = new DefaultTaskContext();
        context.setTransientObject(AUTH_TOKEN_JSON_KEY, AUTH_TOKEN);
        context.setTransientObject(CASE_ID_JSON_KEY, UNFORMATTED_CASE_ID);

        classUnderTest.execute(context, null);

        verify(caseMaintenanceClient).addPetitionerSolicitorRole(AUTH_TOKEN, UNFORMATTED_CASE_ID);
    }

    @Test
    public void givenCaseExists_whenErrorApplyingRole_thenReturnExpectedOutput() throws TaskException {
        final DefaultTaskContext context = new DefaultTaskContext();
        context.setTransientObject(AUTH_TOKEN_JSON_KEY, AUTH_TOKEN);
        context.setTransientObject(CASE_ID_JSON_KEY, UNFORMATTED_CASE_ID);
        doThrow(FeignException.class)
            .when(caseMaintenanceClient)
            .addPetitionerSolicitorRole(AUTH_TOKEN, UNFORMATTED_CASE_ID);
        classUnderTest.execute(context, null);

        assertThat(context.hasTaskFailed(), is(true));
        assertThat(context.getTransientObject("AddPetitionerSolicitorRole_Error"),
            is("Problem setting the [PETSOLICITOR] role to the case"));
    }
}