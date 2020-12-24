package uk.gov.hmcts.reform.divorce.orchestration.tasks;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.divorce.orchestration.client.CaseMaintenanceClient;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskContext;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CO_RESPONDENT_LETTER_HOLDER_ID;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.IS_RESPONDENT;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.RESPONDENT_LETTER_HOLDER_ID;
import static uk.gov.hmcts.reform.divorce.orchestration.testutil.TaskContextHelper.contextWithToken;

@RunWith(MockitoJUnitRunner.class)
public class LinkRespondentTaskTest {

    private static final boolean RESPONDENT = true;
    private static final boolean CO_RESPONDENT = false;

    @Mock
    private CaseMaintenanceClient caseMaintenanceClient;

    @InjectMocks
    private LinkRespondentTask classUnderTest;

    @Test
    public void givenLetterHolderIsRespondent_whenExecute_thenProceedAsExpected() {
        runTestForCaseRole(RESPONDENT);
    }

    @Test
    public void givenLetterHolderIsCoRespondent_whenExecute_thenProceedAsExpected() {
        runTestForCaseRole(CO_RESPONDENT);
    }

    private void runTestForCaseRole(boolean isRespondent) {
        final String letterHolderId;
        final UserDetails userDetails = UserDetails.builder().build();
        final TaskContext taskContext = contextWithToken();

        if (isRespondent) {
            letterHolderId = RESPONDENT_LETTER_HOLDER_ID;
            taskContext.setTransientObject(RESPONDENT_LETTER_HOLDER_ID, letterHolderId);
        } else {
            letterHolderId = CO_RESPONDENT_LETTER_HOLDER_ID;
            taskContext.setTransientObject(CO_RESPONDENT_LETTER_HOLDER_ID, letterHolderId);
        }
        taskContext.setTransientObject(IS_RESPONDENT, isRespondent);

        doNothing().when(caseMaintenanceClient).linkRespondent(AUTH_TOKEN, TEST_CASE_ID, letterHolderId);

        UserDetails actual = classUnderTest.execute(taskContext, userDetails);

        assertEquals(userDetails, actual);

        verify(caseMaintenanceClient).linkRespondent(AUTH_TOKEN, TEST_CASE_ID, letterHolderId);
    }
}
