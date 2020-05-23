package uk.gov.hmcts.reform.divorce.orchestration.tasks;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.divorce.orchestration.config.WelshStateTransitionConfig;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CaseDetails;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.DefaultTaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskException;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.AOS_OVERDUE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.AOS_STARTED;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.BO_TRANSLATION_REQUESTED;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.BO_WELSH_RESPONSE_AWAITING_REVIEW;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CASE_DETAILS_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.PENDING_REJECTION;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.STATE_CCD_FIELD;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.SUBMITTED;

@RunWith(MockitoJUnitRunner.class)
public class WelshContinueInterceptTaskTest {

    private static final TaskContext TASK_CONTEXT = new DefaultTaskContext();
    private Map<String, Object> actual;
    private Map<String, Object> expected;
    private Map<String, String> stateTransitionMap;

    @Mock
    private WelshStateTransitionConfig welshStateTransitionConfig;

    @InjectMocks
    private WelshContinueInterceptTask welshContinueInterceptTask;

    @Before
    public void setup() {
        stateTransitionMap = new HashMap<>();
        stateTransitionMap.put(BO_TRANSLATION_REQUESTED,SUBMITTED);
        stateTransitionMap.put(SUBMITTED,BO_TRANSLATION_REQUESTED);
        stateTransitionMap.put(PENDING_REJECTION,BO_TRANSLATION_REQUESTED);
        stateTransitionMap.put(AOS_STARTED, BO_WELSH_RESPONSE_AWAITING_REVIEW);
        stateTransitionMap.put(AOS_OVERDUE, BO_WELSH_RESPONSE_AWAITING_REVIEW);
        when(welshStateTransitionConfig.getWelshStopState()).thenReturn(stateTransitionMap);
    }


    @Test
    public void givenCaseStateBOTranslationRequestedExpectedSubmitted() throws TaskException {
        actual = new HashMap<>();
        expected = new HashMap<String, Object>() {
            {
                put(STATE_CCD_FIELD, SUBMITTED);
            }
        };
        TASK_CONTEXT.setTransientObject(CASE_DETAILS_JSON_KEY,
            CaseDetails.builder().caseId(TEST_CASE_ID).state(BO_TRANSLATION_REQUESTED).build());
        assertEquals(expected, welshContinueInterceptTask.execute(TASK_CONTEXT, actual));
    }

    @Test
    public void givenCaseStateSubmittedExpectedBOTranslationRequested() throws TaskException {
        actual = new HashMap<>();
        expected = new HashMap<String, Object>() {
            {
                put(STATE_CCD_FIELD, BO_TRANSLATION_REQUESTED);
            }
        };
        TASK_CONTEXT.setTransientObject(CASE_DETAILS_JSON_KEY,
            CaseDetails.builder().caseId(TEST_CASE_ID).state(SUBMITTED).build());
        assertEquals(expected, welshContinueInterceptTask.execute(TASK_CONTEXT, actual));
    }

    @Test
    public void givenCaseStatePendingRejectionExpectedBOTranslationRequested() throws TaskException {
        actual = new HashMap<>();
        expected = new HashMap<String, Object>() {
            {
                put(STATE_CCD_FIELD, BO_TRANSLATION_REQUESTED);
            }
        };
        TASK_CONTEXT.setTransientObject(CASE_DETAILS_JSON_KEY,
            CaseDetails.builder().caseId(TEST_CASE_ID).state(PENDING_REJECTION).build());
        assertEquals(expected, welshContinueInterceptTask.execute(TASK_CONTEXT, actual));
    }

    @Test
    public void givenCaseStateAosStartedExpectedWelshResponseAwaitingReview() throws TaskException {
        actual = new HashMap<>();
        expected = new HashMap<String, Object>() {
            {
                put(STATE_CCD_FIELD, BO_WELSH_RESPONSE_AWAITING_REVIEW);
            }
        };
        TASK_CONTEXT.setTransientObject(CASE_DETAILS_JSON_KEY,
            CaseDetails.builder().caseId(TEST_CASE_ID).state(AOS_STARTED).build());
        assertEquals(expected, welshContinueInterceptTask.execute(TASK_CONTEXT, actual));
    }

    @Test
    public void givenCaseStateAosOverDueExpectedWelshResponseAwaitingReview() throws TaskException {
        actual = new HashMap<>();
        expected = new HashMap<String, Object>() {
            {
                put(STATE_CCD_FIELD, BO_WELSH_RESPONSE_AWAITING_REVIEW);
            }
        };
        TASK_CONTEXT.setTransientObject(CASE_DETAILS_JSON_KEY,
            CaseDetails.builder().caseId(TEST_CASE_ID).state(AOS_OVERDUE).build());
        assertEquals(expected, welshContinueInterceptTask.execute(TASK_CONTEXT, actual));
    }
}
