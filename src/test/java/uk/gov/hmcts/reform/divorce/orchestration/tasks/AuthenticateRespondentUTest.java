package uk.gov.hmcts.reform.divorce.orchestration.tasks;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.divorce.orchestration.client.IdamClient;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.idam.UserDetails;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.DefaultTaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskContext;

import java.util.Arrays;
import java.util.Collections;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

@RunWith(MockitoJUnitRunner.class)
public class AuthenticateRespondentUTest {
    private static final TaskContext TASK_CONTEXT = new DefaultTaskContext();
    private static final String AUTH_TOKEN = "some token";
    private static final String BEARER_AUTH_TOKEN = "Bearer some token";
    private static Boolean PAYLOAD = false;

    @Mock
    private IdamClient idamClient;

    @InjectMocks
    private AuthenticateRespondent classUnderTest;

    @Test
    public void givenUserDetailsIsNull_whenExecute_thenReturnFalse() {
        Mockito.when(idamClient.retrieveUserDetails(BEARER_AUTH_TOKEN)).thenReturn(null);

        assertFalse(classUnderTest.execute(TASK_CONTEXT, PAYLOAD, AUTH_TOKEN));
    }

    @Test
    public void givenRolesIsNull_whenExecute_thenReturnFalse() {
        Mockito.when(idamClient.retrieveUserDetails(BEARER_AUTH_TOKEN)).thenReturn(UserDetails.builder().build());

        assertFalse(classUnderTest.execute(TASK_CONTEXT, PAYLOAD, AUTH_TOKEN));
    }

    @Test
    public void givenRolesIsEmpty_whenExecute_thenReturnFalse() {
        Mockito.when(idamClient.retrieveUserDetails(BEARER_AUTH_TOKEN))
            .thenReturn(
                UserDetails.builder()
                    .roles(Collections.emptyList())
                    .build());

        assertFalse(classUnderTest.execute(TASK_CONTEXT, PAYLOAD, AUTH_TOKEN));
    }

    @Test
    public void givenRolesDoesNotContainLetterHolderRole_whenExecute_thenReturnFalse() {
        Mockito.when(idamClient.retrieveUserDetails(BEARER_AUTH_TOKEN))
            .thenReturn(
                UserDetails.builder()
                    .roles(Arrays.asList(
                        "letter-holder",
                        "letter-loa1"
                    ))
                    .build());

        assertFalse(classUnderTest.execute(TASK_CONTEXT, PAYLOAD, AUTH_TOKEN));
    }

    @Test
    public void givenRolesAreEmptyOrBlank_whenExecute_thenReturnFalse() {
        Mockito.when(idamClient.retrieveUserDetails(BEARER_AUTH_TOKEN))
            .thenReturn(
                UserDetails.builder()
                    .roles(Arrays.asList(
                        "",
                        " "
                    ))
                    .build());

        assertFalse(classUnderTest.execute(TASK_CONTEXT, PAYLOAD, AUTH_TOKEN));
    }

    @Test
    public void givenRolesContainsLetterHolderRole_whenExecute_thenReturnTrue() {
        Mockito.when(idamClient.retrieveUserDetails(BEARER_AUTH_TOKEN))
            .thenReturn(
                UserDetails.builder()
                    .roles(Arrays.asList(
                        "letter-12345",
                        "letter-holder",
                        "letter-loa1"
                    ))
                    .build());

        assertTrue(classUnderTest.execute(TASK_CONTEXT, PAYLOAD, AUTH_TOKEN));
    }
}