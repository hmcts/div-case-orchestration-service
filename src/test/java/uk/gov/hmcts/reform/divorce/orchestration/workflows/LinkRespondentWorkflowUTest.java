package uk.gov.hmcts.reform.divorce.orchestration.workflows;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.idam.UserDetails;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.WorkflowException;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.Task;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.LinkRespondent;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.RetrievePinUserDetails;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.UpdateRespondentDetails;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_PIN;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_TOKEN;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.AUTH_TOKEN_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CASE_ID_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.PIN;

@RunWith(MockitoJUnitRunner.class)
public class LinkRespondentWorkflowUTest {
    @Mock
    private RetrievePinUserDetails retrievePinUserDetails;
    @Mock
    private LinkRespondent linkRespondent;
    @Mock
    private UpdateRespondentDetails updateRespondentDetails;

    @InjectMocks
    private LinkRespondentWorkflow classUnderTest;

    @Test
    public void whenRun_thenProceedAsExpected() throws WorkflowException {
        final UserDetails userDetails = UserDetails.builder().authToken(TEST_TOKEN).build();

        final ImmutablePair<String, Object>  pinPair = ImmutablePair.of(PIN, TEST_PIN);
        final ImmutablePair<String, Object>  authTokenPair = ImmutablePair.of(AUTH_TOKEN_JSON_KEY, TEST_TOKEN);
        final ImmutablePair<String, Object>  caseIdPair = ImmutablePair.of(CASE_ID_JSON_KEY, TEST_CASE_ID);

        final Task[] tasks = new Task[] {
            retrievePinUserDetails,
            linkRespondent,
            updateRespondentDetails
        };

        when(classUnderTest.execute(tasks, userDetails, pinPair, authTokenPair, caseIdPair)).thenReturn(userDetails);

        UserDetails actual = classUnderTest.run(TEST_TOKEN, TEST_CASE_ID, TEST_PIN);

        assertEquals(userDetails, actual);
    }
}