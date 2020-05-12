package uk.gov.hmcts.reform.divorce.orchestration.tasks;

import com.google.common.collect.ImmutableMap;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.divorce.orchestration.client.CaseMaintenanceClient;
import uk.gov.hmcts.reform.divorce.orchestration.config.EventConfig;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.EventType;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.LanguagePreference;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CaseDetails;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.DefaultTaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskContext;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.EventType.dnReceived;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.EventType.dnReceivedAosCompleted;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.EventType.submitDnClarification;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.AOS_COMPLETED;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.AUTH_TOKEN_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.AWAITING_CLARIFICATION;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.AWAITING_DECREE_NISI;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CASE_DETAILS_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CASE_ID_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CCD_CASE_DATA_FIELD;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.LANGUAGE_PREFERENCE_WELSH;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.YES_VALUE;

@RunWith(MockitoJUnitRunner.class)
public class SubmitDnCaseUTest {
    private static final Map<String, Object> EXPECTED_OUTPUT = Collections.emptyMap();
    private static final Map<String, Object> CASE_UPDATE_RESPONSE = new HashMap<>();
    private static final TaskContext TASK_CONTEXT = new DefaultTaskContext();

    @Mock
    private CaseMaintenanceClient caseMaintenanceClient;

    @Mock
    private EventConfig eventConfig;

    @InjectMocks
    private SubmitDnCase classUnderTest;

    @BeforeClass
    public static void beforeClass() {
        TASK_CONTEXT.setTransientObject(AUTH_TOKEN_JSON_KEY, AUTH_TOKEN);
        TASK_CONTEXT.setTransientObject(CASE_ID_JSON_KEY, TEST_CASE_ID);
        CASE_UPDATE_RESPONSE.put(CCD_CASE_DATA_FIELD, EXPECTED_OUTPUT);
    }

    @Before
    public void before() {
        Map<EventType, String> englishEvent = ImmutableMap.of(dnReceived, "dnReceived",
                dnReceivedAosCompleted, "dnReceivedAosCompleted", submitDnClarification,
                "submitDnClarification");
        Map<EventType, String> welshEvent = ImmutableMap.of(dnReceived, "dnReceivedWelshReview",
                dnReceivedAosCompleted, "dnReceivedAosCompletedWelshReview", submitDnClarification,
                "submitDnClarificationWelshReview");
        ImmutableMap<LanguagePreference, Map<EventType, String>> events =
                ImmutableMap.of(LanguagePreference.ENGLISH, englishEvent, LanguagePreference.WELSH, welshEvent);
        when(eventConfig.getEvents()).thenReturn(events);
    }

    @Test
    public void givenDnSubmitAndAosNotComplete_whenExecute_thenProceedAsExpected_Welsh() {
        final Map<String, Object> divorceSession = ImmutableMap.of(LANGUAGE_PREFERENCE_WELSH, YES_VALUE);

        TASK_CONTEXT.setTransientObject(CASE_DETAILS_JSON_KEY,
            CaseDetails.builder().caseId(TEST_CASE_ID).state(AWAITING_DECREE_NISI).caseData(divorceSession).build());

        assertEquals(EXPECTED_OUTPUT, classUnderTest.execute(TASK_CONTEXT, divorceSession));

        verify(caseMaintenanceClient)
            .updateCase(AUTH_TOKEN, TEST_CASE_ID, "dnReceivedWelshReview", divorceSession);
    }

    @Test
    public void givenDnSubmitAndAosNotComplete_whenExecute_thenProceedAsExpected() {
        final Map<String, Object> divorceSession = ImmutableMap.of();

        TASK_CONTEXT.setTransientObject(CASE_DETAILS_JSON_KEY,
            CaseDetails.builder().caseId(TEST_CASE_ID).state(AWAITING_DECREE_NISI).build());

        assertEquals(EXPECTED_OUTPUT, classUnderTest.execute(TASK_CONTEXT, divorceSession));

        verify(caseMaintenanceClient)
            .updateCase(AUTH_TOKEN, TEST_CASE_ID, dnReceived.getEventId(), divorceSession);
    }

    @Test
    public void givenDnSubmitAndAosComplete_whenExecute_thenProceedAsExpected() {
        final Map<String, Object> divorceSession = ImmutableMap.of();

        TASK_CONTEXT.setTransientObject(CASE_DETAILS_JSON_KEY,
            CaseDetails.builder().caseId(TEST_CASE_ID).state(AOS_COMPLETED).build());

        assertEquals(EXPECTED_OUTPUT, classUnderTest.execute(TASK_CONTEXT, divorceSession));

        verify(caseMaintenanceClient)
                .updateCase(AUTH_TOKEN, TEST_CASE_ID, dnReceivedAosCompleted.getEventId(), divorceSession);
    }

    @Test
    public void givenDnSubmitAndAwaitingClarification_whenExecute_thenProceedAsExpected() {
        final Map<String, Object> divorceSession = ImmutableMap.of();

        TASK_CONTEXT.setTransientObject(CASE_DETAILS_JSON_KEY,
            CaseDetails.builder().caseId(TEST_CASE_ID).state(AWAITING_CLARIFICATION).build());

        assertEquals(EXPECTED_OUTPUT, classUnderTest.execute(TASK_CONTEXT, divorceSession));

        verify(caseMaintenanceClient)
            .updateCase(AUTH_TOKEN, TEST_CASE_ID, submitDnClarification.getEventId(), divorceSession);
    }
}