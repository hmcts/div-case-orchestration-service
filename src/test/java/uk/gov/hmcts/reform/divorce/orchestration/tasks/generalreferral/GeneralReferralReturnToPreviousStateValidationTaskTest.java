package uk.gov.hmcts.reform.divorce.orchestration.tasks.generalreferral;

import junit.framework.TestCase;
import org.hamcrest.CoreMatchers;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.CcdFields;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskException;

import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThrows;
import static uk.gov.hmcts.reform.divorce.orchestration.testutil.TaskContextHelper.context;

@RunWith(MockitoJUnitRunner.class)
public class GeneralReferralReturnToPreviousStateValidationTaskTest extends TestCase {

    @InjectMocks
    private GeneralReferralReturnToPreviousStateValidationTask generalReferralReturnToPreviousStateValidationTask;

    @Test
    public void executeShouldReturnCaseData() throws TaskException {
        Map<String, Object> caseData = new HashMap<>();
        caseData.put(CcdFields.GENERAL_REFERRAL_PREVIOUS_CASE_STATE, "previousCaseState");
        TaskContext context = context();

        Map<String, Object> returnedCaseData = generalReferralReturnToPreviousStateValidationTask
            .execute(context, caseData);

        assertThat(returnedCaseData.isEmpty(), is(false));
        assertSame(returnedCaseData, caseData);
        assertThat(returnedCaseData, is(caseData));
    }

    @Test
    public void executeShouldThrowExceptionIfPreviousCaseStateIsMissing() throws TaskException {
        Map<String, Object> caseData = new HashMap<>();
        TaskContext context = context();

        TaskException exception = assertThrows(TaskException.class, () ->
            generalReferralReturnToPreviousStateValidationTask.execute(context, caseData)
        );
        assertThat(exception.getMessage(),
            CoreMatchers.is("Could not evaluate value of mandatory property \"" + CcdFields.GENERAL_REFERRAL_PREVIOUS_CASE_STATE + "\""));
    }

    @Test
    public void executeShouldThrowExceptionIfCurrentCaseStateIsMissing() throws TaskException {
        Map<String, Object> caseData = new HashMap<>();
        TaskContext context = context();

        TaskException exception = assertThrows(TaskException.class, () ->
            generalReferralReturnToPreviousStateValidationTask.execute(context, caseData)
        );
        assertThat(exception.getMessage(),
            CoreMatchers.is("Could not evaluate value of mandatory property \"" + CcdFields.GENERAL_REFERRAL_PREVIOUS_CASE_STATE + "\""));
    }

    @Test
    public void executeShouldThrowExceptionIfPreviousCaseStateIsEmpty() throws TaskException {
        Map<String, Object> caseData = new HashMap<>();
        caseData.put(CcdFields.GENERAL_REFERRAL_PREVIOUS_CASE_STATE, "");
        TaskContext context = context();

        TaskException exception = assertThrows(TaskException.class, () ->
            generalReferralReturnToPreviousStateValidationTask.execute(context, caseData)
        );
        assertThat(exception.getMessage(),
            CoreMatchers.is("Could not evaluate value of mandatory property \"" + CcdFields.GENERAL_REFERRAL_PREVIOUS_CASE_STATE + "\""));
    }
}
