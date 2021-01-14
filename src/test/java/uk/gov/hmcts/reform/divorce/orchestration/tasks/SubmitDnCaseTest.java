package uk.gov.hmcts.reform.divorce.orchestration.tasks;

import com.google.common.collect.ImmutableMap;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.divorce.orchestration.client.CaseMaintenanceClient;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CaseDetails;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.DefaultTaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.util.WelshNextEventUtil;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BooleanSupplier;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.CcdStates.AOS_COMPLETED;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.CcdStates.AWAITING_CLARIFICATION;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.CcdStates.AWAITING_DECREE_NISI;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.AUTH_TOKEN_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.BO_WELSH_DN_RECEIVED_AOS_COMPLETED_EVENT_ID;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.BO_WELSH_DN_RECEIVED_EVENT_ID;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.BO_WELSH_DN_RECEIVED_REVIEW;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.BO_WELSH_SUBMIT_DN_CLARIFICATION_EVENT_ID;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CASE_DETAILS_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CASE_ID_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CCD_CASE_DATA_FIELD;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DN_RECEIVED;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DN_RECEIVED_AOS_COMPLETE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DN_RECEIVED_CLARIFICATION;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.LANGUAGE_PREFERENCE_WELSH;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.WELSH_NEXT_EVENT;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.YES_VALUE;

@RunWith(MockitoJUnitRunner.class)
public class SubmitDnCaseTest {

    private static final Map<String, Object> EXPECTED_OUTPUT = Collections.emptyMap();
    private static final Map<String, Object> CASE_UPDATE_RESPONSE = new HashMap<>();
    private static final TaskContext TASK_CONTEXT = new DefaultTaskContext();

    @Mock
    private CaseMaintenanceClient caseMaintenanceClient;

    @InjectMocks
    private SubmitDnCase classUnderTest;

    @Spy
    private WelshNextEventUtil welshNextEventUtil;

    @Captor
    ArgumentCaptor<BooleanSupplier> isWelsh;

    @BeforeClass
    public static void beforeClass() {
        TASK_CONTEXT.setTransientObject(AUTH_TOKEN_JSON_KEY, AUTH_TOKEN);
        TASK_CONTEXT.setTransientObject(CASE_ID_JSON_KEY, TEST_CASE_ID);
        CASE_UPDATE_RESPONSE.put(CCD_CASE_DATA_FIELD, EXPECTED_OUTPUT);
    }

    @Test
    public void givenDnSubmitAndAosNotComplete_whenExecute_thenProceedAsExpected() {
        final Map<String, Object> divorceSession = ImmutableMap.of();
        CaseDetails caseDetails = CaseDetails.builder().caseData(divorceSession).build();
        when(caseMaintenanceClient.retrievePetitionById(AUTH_TOKEN, TEST_CASE_ID)).thenReturn(caseDetails);
        when(caseMaintenanceClient.updateCase(AUTH_TOKEN, TEST_CASE_ID, DN_RECEIVED, caseDetails.getCaseData()))
            .thenReturn(CASE_UPDATE_RESPONSE);

        TASK_CONTEXT.setTransientObject(CASE_DETAILS_JSON_KEY,
            CaseDetails.builder().caseId(TEST_CASE_ID).state(AWAITING_DECREE_NISI).build());

        assertEquals(EXPECTED_OUTPUT, classUnderTest.execute(TASK_CONTEXT, caseDetails.getCaseData()));

        verify(caseMaintenanceClient)
            .updateCase(eq(AUTH_TOKEN), eq(TEST_CASE_ID), eq(DN_RECEIVED), eq(caseDetails.getCaseData()));

        verify(welshNextEventUtil).storeNextEventAndReturnStopEvent(isWelsh.capture(), eq(caseDetails.getCaseData()),
            eq(DN_RECEIVED), eq(BO_WELSH_DN_RECEIVED_EVENT_ID), eq(BO_WELSH_DN_RECEIVED_REVIEW));
        assertThat(isWelsh.getValue().getAsBoolean(), equalTo(false));
    }

    @Test
    public void givenDnSubmitAndAosComplete_whenExecute_thenProceedAsExpected() {
        final Map<String, Object> divorceSession = ImmutableMap.of();
        CaseDetails caseDetails = CaseDetails.builder().caseData(divorceSession).build();
        when(caseMaintenanceClient.retrievePetitionById(AUTH_TOKEN, TEST_CASE_ID)).thenReturn(caseDetails);
        when(caseMaintenanceClient.updateCase(AUTH_TOKEN, TEST_CASE_ID, DN_RECEIVED_AOS_COMPLETE, caseDetails.getCaseData()))
            .thenReturn(CASE_UPDATE_RESPONSE);

        TASK_CONTEXT.setTransientObject(CASE_DETAILS_JSON_KEY,
            CaseDetails.builder().caseId(TEST_CASE_ID).state(AOS_COMPLETED).build());

        assertEquals(EXPECTED_OUTPUT, classUnderTest.execute(TASK_CONTEXT, caseDetails.getCaseData()));

        verify(caseMaintenanceClient)
            .updateCase(eq(AUTH_TOKEN), eq(TEST_CASE_ID), eq(DN_RECEIVED_AOS_COMPLETE), eq(caseDetails.getCaseData()));

        verify(welshNextEventUtil).storeNextEventAndReturnStopEvent(isWelsh.capture(), eq(caseDetails.getCaseData()),
            eq(DN_RECEIVED_AOS_COMPLETE), eq(BO_WELSH_DN_RECEIVED_AOS_COMPLETED_EVENT_ID), eq(BO_WELSH_DN_RECEIVED_REVIEW));
        assertThat(isWelsh.getValue().getAsBoolean(), equalTo(false));
    }

    @Test
    public void givenDnSubmitAndAwaitingClarification_whenExecute_thenProceedAsExpected() {
        final Map<String, Object> divorceSession = ImmutableMap.of();
        CaseDetails caseDetails = CaseDetails.builder().caseData(divorceSession).build();
        when(caseMaintenanceClient.retrievePetitionById(AUTH_TOKEN, TEST_CASE_ID)).thenReturn(caseDetails);
        when(caseMaintenanceClient.updateCase(AUTH_TOKEN, TEST_CASE_ID, DN_RECEIVED_CLARIFICATION, caseDetails.getCaseData()))
            .thenReturn(CASE_UPDATE_RESPONSE);

        TASK_CONTEXT.setTransientObject(CASE_DETAILS_JSON_KEY,
            CaseDetails.builder().caseId(TEST_CASE_ID).state(AWAITING_CLARIFICATION).build());

        assertEquals(EXPECTED_OUTPUT, classUnderTest.execute(TASK_CONTEXT, caseDetails.getCaseData()));

        verify(caseMaintenanceClient)
            .updateCase(eq(AUTH_TOKEN), eq(TEST_CASE_ID), eq(DN_RECEIVED_CLARIFICATION), eq(caseDetails.getCaseData()));

        verify(welshNextEventUtil).storeNextEventAndReturnStopEvent(isWelsh.capture(), eq(caseDetails.getCaseData()),
            eq(DN_RECEIVED_CLARIFICATION), eq(BO_WELSH_SUBMIT_DN_CLARIFICATION_EVENT_ID), eq(BO_WELSH_DN_RECEIVED_REVIEW));
        assertThat(isWelsh.getValue().getAsBoolean(), equalTo(false));
    }

    @Test
    public void givenDnSubmitAndAwaitingClarification_whenExecute_thenProceedAsExpected_welsh() {
        final Map<String, Object> divorceSession = new HashMap<>();
        divorceSession.put(LANGUAGE_PREFERENCE_WELSH, YES_VALUE);
        CaseDetails caseDetails = CaseDetails.builder().caseData(divorceSession).build();

        Map<String, Object> expectedData = new HashMap<>(divorceSession);
        expectedData.put(WELSH_NEXT_EVENT, BO_WELSH_SUBMIT_DN_CLARIFICATION_EVENT_ID);

        TASK_CONTEXT.setTransientObject(CASE_DETAILS_JSON_KEY,
            CaseDetails.builder().caseId(TEST_CASE_ID).state(AWAITING_CLARIFICATION).build());

        assertEquals(EXPECTED_OUTPUT, classUnderTest.execute(TASK_CONTEXT, caseDetails.getCaseData()));

        verify(caseMaintenanceClient)
            .updateCase(eq(AUTH_TOKEN), eq(TEST_CASE_ID), eq(BO_WELSH_DN_RECEIVED_REVIEW), eq(expectedData));

        verify(welshNextEventUtil).storeNextEventAndReturnStopEvent(isWelsh.capture(), eq(caseDetails.getCaseData()),
            eq(DN_RECEIVED_CLARIFICATION), eq(BO_WELSH_SUBMIT_DN_CLARIFICATION_EVENT_ID), eq(BO_WELSH_DN_RECEIVED_REVIEW));
        assertThat(isWelsh.getValue().getAsBoolean(), equalTo(true));
    }

    @Test
    public void givenDnSubmitAndAosComplete_whenExecute_thenProceedAsExpected_welsh() {
        final Map<String, Object> divorceSession = new HashMap<>();
        divorceSession.put(LANGUAGE_PREFERENCE_WELSH, YES_VALUE);
        CaseDetails caseDetails = CaseDetails.builder().caseData(divorceSession).build();

        Map<String, Object> expectedData = new HashMap<>(divorceSession);
        expectedData.put(WELSH_NEXT_EVENT, BO_WELSH_DN_RECEIVED_AOS_COMPLETED_EVENT_ID);

        TASK_CONTEXT.setTransientObject(CASE_DETAILS_JSON_KEY,
            CaseDetails.builder().caseId(TEST_CASE_ID).state(AOS_COMPLETED).build());

        assertEquals(EXPECTED_OUTPUT, classUnderTest.execute(TASK_CONTEXT, caseDetails.getCaseData()));

        verify(caseMaintenanceClient)
            .updateCase(eq(AUTH_TOKEN), eq(TEST_CASE_ID), eq(BO_WELSH_DN_RECEIVED_REVIEW), eq(expectedData));

        verify(welshNextEventUtil).storeNextEventAndReturnStopEvent(isWelsh.capture(), eq(caseDetails.getCaseData()),
            eq(DN_RECEIVED_AOS_COMPLETE), eq(BO_WELSH_DN_RECEIVED_AOS_COMPLETED_EVENT_ID), eq(BO_WELSH_DN_RECEIVED_REVIEW));
        assertThat(isWelsh.getValue().getAsBoolean(), equalTo(true));
    }

    @Test
    public void givenDnSubmitAndAosNotComplete_whenExecute_thenProceedAsExpected_welsh() {
        final Map<String, Object> divorceSession = new HashMap<>();
        divorceSession.put(LANGUAGE_PREFERENCE_WELSH, YES_VALUE);
        CaseDetails caseDetails = CaseDetails.builder().caseData(divorceSession).build();

        Map<String, Object> expectedData = new HashMap<>(divorceSession);
        expectedData.put(WELSH_NEXT_EVENT, BO_WELSH_DN_RECEIVED_EVENT_ID);

        TASK_CONTEXT.setTransientObject(CASE_DETAILS_JSON_KEY,
            CaseDetails.builder().caseId(TEST_CASE_ID).state(AWAITING_DECREE_NISI).build());

        assertEquals(EXPECTED_OUTPUT, classUnderTest.execute(TASK_CONTEXT, caseDetails.getCaseData()));

        verify(caseMaintenanceClient)
            .updateCase(eq(AUTH_TOKEN), eq(TEST_CASE_ID), eq(BO_WELSH_DN_RECEIVED_REVIEW), eq(expectedData));

        verify(welshNextEventUtil).storeNextEventAndReturnStopEvent(isWelsh.capture(), eq(caseDetails.getCaseData()),
            eq(DN_RECEIVED), eq(BO_WELSH_DN_RECEIVED_EVENT_ID), eq(BO_WELSH_DN_RECEIVED_REVIEW));
        assertThat(isWelsh.getValue().getAsBoolean(), equalTo(true));
    }
}