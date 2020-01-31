package uk.gov.hmcts.reform.divorce.orchestration.tasks;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.DefaultTaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.util.AuthUtil;
import uk.gov.hmcts.reform.idam.client.IdamClient;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;

import java.util.Arrays;
import java.util.Collections;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.AUTH_TOKEN_JSON_KEY;

@RunWith(MockitoJUnitRunner.class)
public class AuthenticateRespondentUTest {
    private static final String AUTH_TOKEN = "some token";
    private static final String BEARER_AUTH_TOKEN = "Bearer some token";
    private static Boolean PAYLOAD = false;

    private final TaskContext context = new DefaultTaskContext();

    @Mock
    private IdamClient idamClient;

    @Mock
    private AuthUtil authUtil;

    @InjectMocks
    private AuthenticateRespondent classUnderTest;

    @Before
    public void setup() {
        Mockito.when(authUtil.getBearToken(AUTH_TOKEN)).thenCallRealMethod();
        context.setTransientObject(AUTH_TOKEN_JSON_KEY, AUTH_TOKEN);
    }

    @Test
    public void givenUserDetailsIsNull_whenExecute_thenReturnFalse() {
        Mockito.when(idamClient.getUserDetails(BEARER_AUTH_TOKEN)).thenReturn(null);

        assertFalse(classUnderTest.execute(context, PAYLOAD));
    }

    @Test
    public void givenRolesIsNull_whenExecute_thenReturnFalse() {

        Mockito.when(idamClient.getUserDetails(BEARER_AUTH_TOKEN)).thenReturn(UserDetails.builder().build());

        assertFalse(classUnderTest.execute(context, PAYLOAD));
    }

    @Test
    public void givenRolesIsEmpty_whenExecute_thenReturnFalse() {
        Mockito.when(idamClient.getUserDetails(BEARER_AUTH_TOKEN))
            .thenReturn(
                UserDetails.builder()
                    .roles(Collections.emptyList())
                    .build());

        assertFalse(classUnderTest.execute(context, PAYLOAD));
    }

    @Test
    public void givenRolesDoesNotContainLetterHolderRole_whenExecute_thenReturnFalse() {
        Mockito.when(idamClient.getUserDetails(BEARER_AUTH_TOKEN))
            .thenReturn(
                UserDetails.builder()
                    .roles(Collections.singletonList(
                        "letter-loa1"
                    ))
                    .build());

        assertFalse(classUnderTest.execute(context, PAYLOAD));
    }

    @Test
    public void givenRolesAreEmptyOrBlank_whenExecute_thenReturnFalse() {
        Mockito.when(idamClient.getUserDetails(BEARER_AUTH_TOKEN))
            .thenReturn(
                UserDetails.builder()
                    .roles(Arrays.asList(
                        "",
                        " "
                    ))
                    .build());

        assertFalse(classUnderTest.execute(context, PAYLOAD));
    }

    @Test
    public void givenRolesContainsLetterHolderRole_whenExecute_thenReturnTrue() {
        Mockito.when(idamClient.getUserDetails(BEARER_AUTH_TOKEN))
            .thenReturn(
                UserDetails.builder()
                    .roles(Arrays.asList(
                        "letter-holder",
                        "letter-loa1"
                    ))
                    .build());

        assertTrue(classUnderTest.execute(context, PAYLOAD));
    }
}