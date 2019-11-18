package uk.gov.hmcts.reform.divorce.orchestration.tasks;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.divorce.orchestration.client.CaseMaintenanceClient;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.DefaultTaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskContext;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.AUTH_TOKEN_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CASE_ID_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.IS_RESPONDENT;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.RESPONDENT_LETTER_HOLDER_ID;

@RunWith(MockitoJUnitRunner.class)
public class LinkRespondentUTest {

    @Mock
    private CaseMaintenanceClient caseMaintenanceClient;

    @InjectMocks
    private LinkRespondent classUnderTest;

    @Test
    public void whenExecute_thenProceedAsExpected() {
        final UserDetails userDetails = UserDetails.builder().build();
        final TaskContext taskContext = new DefaultTaskContext();

        taskContext.setTransientObject(AUTH_TOKEN_JSON_KEY, AUTH_TOKEN);
        taskContext.setTransientObject(CASE_ID_JSON_KEY, TEST_CASE_ID);
        taskContext.setTransientObject(RESPONDENT_LETTER_HOLDER_ID, RESPONDENT_LETTER_HOLDER_ID);
        taskContext.setTransientObject(IS_RESPONDENT, true);

        doNothing().when(caseMaintenanceClient).linkRespondent(AUTH_TOKEN, TEST_CASE_ID, RESPONDENT_LETTER_HOLDER_ID);

        UserDetails actual = classUnderTest.execute(taskContext, userDetails);

        assertEquals(userDetails, actual);

        verify(caseMaintenanceClient).linkRespondent(AUTH_TOKEN, TEST_CASE_ID, RESPONDENT_LETTER_HOLDER_ID);
    }
}