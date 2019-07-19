package uk.gov.hmcts.reform.divorce.orchestration.workflows;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.idam.UserDetails;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.WorkflowException;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskException;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.GetCaseWithIdTask;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.LinkRespondent;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.RetrievePinUserDetails;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.UnlinkRespondent;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.UpdateRespondentDetails;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_PIN;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_TOKEN;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.UPDATE_RESPONDENT_DATA_ERROR_KEY;

@RunWith(MockitoJUnitRunner.class)
public class LinkRespondentWorkflowUTest {
    @Mock
    private GetCaseWithIdTask getCaseWithId;
    @Mock
    private RetrievePinUserDetails retrievePinUserDetails;
    @Mock
    private LinkRespondent linkRespondent;
    @Mock
    private UpdateRespondentDetails updateRespondentDetails;
    @Mock
    private UnlinkRespondent unlinkRespondent;

    @InjectMocks
    private LinkRespondentWorkflow classUnderTest;

    @Test
    public void whenRunLinkRespondent_thenProceedAsExpected() throws Exception {
        final UserDetails userDetails = UserDetails.builder().authToken(TEST_TOKEN).build();

        when(getCaseWithId.execute(any(), eq(userDetails))).thenReturn(userDetails);
        when(retrievePinUserDetails.execute(any(), eq(userDetails))).thenReturn(userDetails);
        when(linkRespondent.execute(any(), eq(userDetails))).thenReturn(userDetails);
        when(updateRespondentDetails.execute(any(), eq(userDetails))).thenReturn(userDetails);

        UserDetails actual = classUnderTest.run(TEST_TOKEN, TEST_CASE_ID, TEST_PIN);

        assertEquals(userDetails, actual);
        verify(getCaseWithId, times(1)).execute(classUnderTest.getContext(), userDetails);
        verify(retrievePinUserDetails, times(1)).execute(classUnderTest.getContext(), userDetails);
        verify(linkRespondent, times(1)).execute(classUnderTest.getContext(), userDetails);
        verify(updateRespondentDetails, times(1)).execute(classUnderTest.getContext(), userDetails);
        verify(unlinkRespondent, never()).execute(any(), any());
    }

    @Test
    public void whenUpdateRespondentDetailsFails_thenCallUnlinkRespondent() throws Exception {
        final UserDetails userDetails = UserDetails.builder().authToken(TEST_TOKEN).build();

        when(getCaseWithId.execute(any(), eq(userDetails))).thenReturn(userDetails);
        when(retrievePinUserDetails.execute(any(), eq(userDetails))).thenReturn(userDetails);
        when(linkRespondent.execute(any(), eq(userDetails))).thenReturn(userDetails);
        when(updateRespondentDetails.execute(any(), eq(userDetails))).thenAnswer(invocation -> {
            TaskContext context = invocation.getArgument(0);
            context.setTransientObject(UPDATE_RESPONDENT_DATA_ERROR_KEY, userDetails);
            throw new TaskException("Case update failed");
        });

        try {
            classUnderTest.run(TEST_TOKEN, TEST_CASE_ID, TEST_PIN);
            fail("WorkflowException expected");
        } catch (WorkflowException e) {
            //    Exception expected
        }

        verify(unlinkRespondent, times(1)).execute(any(), eq(userDetails));
    }

    @Test
    public void whenLinkRespondentFails_thenOtherTaskAreNotCalled() throws Exception {
        final UserDetails userDetails = UserDetails.builder().authToken(TEST_TOKEN).build();

        when(getCaseWithId.execute(any(), eq(userDetails))).thenReturn(userDetails);
        when(retrievePinUserDetails.execute(any(), eq(userDetails))).thenReturn(userDetails);
        when(linkRespondent.execute(any(), eq(userDetails))).thenThrow(new RuntimeException("Error"));

        try {
            classUnderTest.run(TEST_TOKEN, TEST_CASE_ID, TEST_PIN);
            fail("WorkflowException expected");
        } catch (RuntimeException e) {
            //    Exception expected
        }

        verify(getCaseWithId, times(1)).execute(classUnderTest.getContext(), userDetails);
        verify(retrievePinUserDetails, times(1)).execute(classUnderTest.getContext(), userDetails);
        verify(linkRespondent, times(1)).execute(classUnderTest.getContext(), userDetails);
        verify(updateRespondentDetails, never()).execute(any(), any());
        verify(unlinkRespondent, never()).execute(any(), any());
    }
}
