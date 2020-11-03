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
public class VerifyIsEligibleForStateRollbackBeforeGeneralReferralTaskTest extends TestCase {

    @InjectMocks
    private VerifyIsEligibleForStateRollbackBeforeGeneralReferralTask verifyIsEligibleForStateRollbackBeforeGeneralReferralTask;

    @Test
    public void executeShouldReturnCaseData() throws TaskException {
        Map<String, Object> caseData = new HashMap<>();
        caseData.put(CcdFields.GENERAL_REFERRAL_DECISION, CcdFields.GENERAL_REFERRAL_DECISION_REFUSE);
        caseData.put(CcdFields.GENERAL_REFERRAL_PREVIOUS_CASE_STATE, "previousCaseState");
        TaskContext context = context();

        Map<String, Object> returnedCaseData = verifyIsEligibleForStateRollbackBeforeGeneralReferralTask
            .execute(context, caseData);

        assertThat(returnedCaseData.isEmpty(), is(false));
        assertSame(returnedCaseData, caseData);
        assertThat(returnedCaseData, is(caseData));
    }

    @Test
    public void executeShouldThrowExceptionIfGeneralReferralDecisionIsMissing() throws TaskException {
        Map<String, Object> caseData = new HashMap<>();
        caseData.put(CcdFields.GENERAL_REFERRAL_PREVIOUS_CASE_STATE, "previousCaseState");
        TaskContext context = context();

        TaskException exception = assertThrows(TaskException.class, () ->
            verifyIsEligibleForStateRollbackBeforeGeneralReferralTask.execute(context, caseData)
        );
        assertThat(exception.getMessage(),
            CoreMatchers.containsString("Could not evaluate value of mandatory property \"" + CcdFields.GENERAL_REFERRAL_DECISION + "\""));
    }

    @Test
    public void executeShouldThrowExceptionIfGeneralReferralIsNotRejected() throws TaskException {
        Map<String, Object> caseData = new HashMap<>();
        caseData.put(CcdFields.GENERAL_REFERRAL_DECISION, "notRejected");
        caseData.put(CcdFields.GENERAL_REFERRAL_PREVIOUS_CASE_STATE, "previousCaseState");
        TaskContext context = context();

        TaskException exception = assertThrows(TaskException.class, () ->
            verifyIsEligibleForStateRollbackBeforeGeneralReferralTask.execute(context, caseData)
        );
        assertThat(exception.getMessage(),
            CoreMatchers.is("Your previous general referral application has not been rejected. You cannot rollback the state."));
    }

    @Test
    public void executeShouldThrowExceptionIfPreviousCaseStateIsMissing() throws TaskException {
        Map<String, Object> caseData = new HashMap<>();
        caseData.put(CcdFields.GENERAL_REFERRAL_DECISION, CcdFields.GENERAL_REFERRAL_DECISION_REFUSE);
        TaskContext context = context();

        TaskException exception = assertThrows(TaskException.class, () ->
            verifyIsEligibleForStateRollbackBeforeGeneralReferralTask.execute(context, caseData)
        );
        assertThat(exception.getMessage(),
            CoreMatchers.is("Could not evaluate value of mandatory property \"" + CcdFields.GENERAL_REFERRAL_PREVIOUS_CASE_STATE + "\""));
    }

    @Test
    public void executeShouldThrowExceptionIfPreviousCaseStateIsEmpty() throws TaskException {
        Map<String, Object> caseData = new HashMap<>();
        caseData.put(CcdFields.GENERAL_REFERRAL_DECISION, CcdFields.GENERAL_REFERRAL_DECISION_REFUSE);
        caseData.put(CcdFields.GENERAL_REFERRAL_PREVIOUS_CASE_STATE, "");
        TaskContext context = context();

        TaskException exception = assertThrows(TaskException.class, () ->
            verifyIsEligibleForStateRollbackBeforeGeneralReferralTask.execute(context, caseData)
        );
        assertThat(exception.getMessage(),
            CoreMatchers.is("Could not evaluate value of mandatory property \"" + CcdFields.GENERAL_REFERRAL_PREVIOUS_CASE_STATE + "\""));
    }
}
