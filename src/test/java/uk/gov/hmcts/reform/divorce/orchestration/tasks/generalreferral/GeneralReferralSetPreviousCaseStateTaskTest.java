package uk.gov.hmcts.reform.divorce.orchestration.tasks.generalreferral;

import com.google.common.collect.ImmutableMap;
import junit.framework.TestCase;
import org.hamcrest.CoreMatchers;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.CcdFields;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskException;
import uk.gov.hmcts.reform.divorce.orchestration.util.GeneralReferralHelper;

import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasEntry;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThrows;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CASE_STATE_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.testutil.TaskContextHelper.context;

@RunWith(MockitoJUnitRunner.class)
public class GeneralReferralSetPreviousCaseStateTaskTest extends TestCase {

    @InjectMocks
    private GeneralReferralSetPreviousCaseStateTask generalReferralSetPreviousCaseStateTask;

    @Test
    public void executeShouldAddCurrentStateToPreviousCaseStateField() throws TaskException {
        Map<String, Object> caseData = new HashMap<>();
        TaskContext context = context();
        context.setTransientObject(CASE_STATE_JSON_KEY, "currentCaseState");

        Map<String, Object> returnedCaseData = generalReferralSetPreviousCaseStateTask
            .execute(context, caseData);

        assertThat(returnedCaseData.isEmpty(), is(false));
        assertSame(returnedCaseData, caseData);
        assertThat(
            returnedCaseData.get(CcdFields.GENERAL_REFERRAL_PREVIOUS_CASE_STATE),
            is("currentCaseState")
        );
    }

    @Test
    public void executeShouldNotAddCurrentStateToPreviousCaseStateFieldWhenStateIsPartOfGeneralReferralWorkflowStates() throws TaskException {
        Map<String, Object> caseData = ImmutableMap.of(CcdFields.GENERAL_REFERRAL_PREVIOUS_CASE_STATE, "existingPreviousState");
        TaskContext context = context();
        context.setTransientObject(CASE_STATE_JSON_KEY, GeneralReferralHelper.GENERAL_REFERRAL_WORKFLOW_STATES.get(0));

        Map<String, Object> returnedCaseData = generalReferralSetPreviousCaseStateTask
            .execute(context, caseData);

        assertThat(returnedCaseData.isEmpty(), is(false));
        assertSame(returnedCaseData, caseData);
        assertThat(returnedCaseData, hasEntry(CcdFields.GENERAL_REFERRAL_PREVIOUS_CASE_STATE, "existingPreviousState"));
    }

    @Test
    public void executeShouldThrowExceptionIfStateIsMissing() throws TaskException {
        Map<String, Object> caseData = new HashMap<>();
        TaskContext context = context();

        TaskException exception = assertThrows(TaskException.class, () ->
            generalReferralSetPreviousCaseStateTask.execute(context, caseData)
        );
        assertThat(exception.getMessage(), CoreMatchers.is("Could not evaluate value of mandatory property \"" + CASE_STATE_JSON_KEY + "\""));
    }
}
