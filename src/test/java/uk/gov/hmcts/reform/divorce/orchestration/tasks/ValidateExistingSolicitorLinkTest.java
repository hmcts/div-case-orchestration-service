package uk.gov.hmcts.reform.divorce.orchestration.tasks;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CaseDetails;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.DefaultTaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskException;
import uk.gov.hmcts.reform.idam.client.IdamClient;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;

import java.util.Collections;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.AUTH_TOKEN_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CASE_DETAILS_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.SOLICITOR_LINKED_EMAIL;

@RunWith(MockitoJUnitRunner.class)
public class ValidateExistingSolicitorLinkTest {

    private static final String TOKEN = "token";

    @Mock
    private IdamClient idamClient;

    @InjectMocks
    private ValidateExistingSolicitorLink validateCaseData;

    @Test
    public void doesNothingIfSolicitorLinkedFieldIsNotSet() throws TaskException {
        TaskContext context = new DefaultTaskContext();
        CaseDetails caseDetails = CaseDetails.builder().caseData(Collections.emptyMap()).build();
        context.setTransientObject(CASE_DETAILS_JSON_KEY, caseDetails);
        context.setTransientObject(AUTH_TOKEN_JSON_KEY, TOKEN);

        UserDetails userDetails = UserDetails.builder().email("test@sol.local").build();
        when(idamClient.getUserDetails(TOKEN)).thenReturn(userDetails);

        UserDetails returnedUserDetails = validateCaseData.execute(context, userDetails);
        assertThat(returnedUserDetails, is(userDetails));
    }

    @Test(expected = TaskException.class)
    public void throwExceptionIfCaseSolicitorLinkFieldIsDifferentFromExistingValue() throws TaskException {
        TaskContext context = new DefaultTaskContext();
        CaseDetails caseDetails = CaseDetails.builder().caseData(
                Collections.singletonMap(SOLICITOR_LINKED_EMAIL, "test@sol.local")
        ).build();
        context.setTransientObject(CASE_DETAILS_JSON_KEY, caseDetails);
        context.setTransientObject(AUTH_TOKEN_JSON_KEY, TOKEN);

        UserDetails userDetails = UserDetails.builder().email("test1@sol.local").build();
        when(idamClient.getUserDetails(TOKEN)).thenReturn(userDetails);

        validateCaseData.execute(context, userDetails);
    }
}
