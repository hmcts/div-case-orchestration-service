package uk.gov.hmcts.reform.divorce.orchestration.tasks;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.divorce.orchestration.client.CaseMaintenanceClient;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CaseDetails;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.DefaultTaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.util.CcdUtil;

import java.util.HashMap;
import java.util.Map;

import static java.util.Collections.emptyMap;
import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.AOS_NOMINATE_SOLICITOR;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.AUTH_TOKEN_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.AWAITING_ANSWER_AOS_EVENT_ID;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.AWAITING_DN_AOS_EVENT_ID;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.BO_WELSH_REVIEW;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CASE_DETAILS_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CASE_ID_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CCD_CASE_DATA_FIELD;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.COMPLETED_AOS_EVENT_ID;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D_8_REASON_FOR_DIVORCE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.LANGUAGE_PREFERENCE_WELSH;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NO_VALUE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.RECEIVED_AOS_FROM_RESP;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.RECEIVED_AOS_FROM_RESP_DATE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.RESP_ADMIT_OR_CONSENT_TO_FACT;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.RESP_WILL_DEFEND_DIVORCE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.WELSH_NEXT_EVENT;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.YES_VALUE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.facts.DivorceFacts.ADULTERY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.facts.DivorceFacts.SEPARATION_TWO_YEARS;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.facts.DivorceFacts.UNREASONABLE_BEHAVIOUR;

@RunWith(MockitoJUnitRunner.class)
public class SubmitRespondentAosCaseUTest {
    private static final Map<String, Object> EXPECTED_OUTPUT = emptyMap();
    private static final Map<String, Object> CASE_UPDATE_RESPONSE = new HashMap<>();
    private static final TaskContext TASK_CONTEXT = new DefaultTaskContext();

    private static final String FIXED_DATE = "2019-05-10";

    @Mock
    private CaseMaintenanceClient caseMaintenanceClient;

    @Mock
    private CcdUtil ccdUtil;

    @InjectMocks
    private SubmitRespondentAosCase classUnderTest;

    @Before
    public void before() {
        when(ccdUtil.getCurrentDateCcdFormat()).thenReturn(FIXED_DATE);
        TASK_CONTEXT.setTransientObject(AUTH_TOKEN_JSON_KEY, AUTH_TOKEN);
        TASK_CONTEXT.setTransientObject(CASE_ID_JSON_KEY, TEST_CASE_ID);
        CASE_UPDATE_RESPONSE.put(CCD_CASE_DATA_FIELD, EXPECTED_OUTPUT);
    }

    @Test
    public void givenConsentAndDefend_whenExecute_thenProceedAsExpected() {
        final Map<String, Object> divorceSession = getCaseData(true, true);
        CaseDetails caseDetails = CaseDetails.builder().caseData(divorceSession).build();
        when(caseMaintenanceClient.retrievePetitionById(AUTH_TOKEN, TEST_CASE_ID)).thenReturn(caseDetails);
        when(caseMaintenanceClient.updateCase(AUTH_TOKEN, TEST_CASE_ID, AWAITING_ANSWER_AOS_EVENT_ID, divorceSession))
            .thenReturn(CASE_UPDATE_RESPONSE);

        Map<String, Object> expectedData = new HashMap<>(divorceSession);
        expectedData.put(RECEIVED_AOS_FROM_RESP, YES_VALUE);
        expectedData.put(RECEIVED_AOS_FROM_RESP_DATE, FIXED_DATE);

        assertEquals(EXPECTED_OUTPUT, classUnderTest.execute(TASK_CONTEXT, divorceSession));

        verify(caseMaintenanceClient)
            .updateCase(eq(AUTH_TOKEN), eq(TEST_CASE_ID), eq(AWAITING_ANSWER_AOS_EVENT_ID), eq(expectedData));
    }

    @Test
    public void givenNoConsentAndDefend_whenExecute_thenProceedAsExpected() {
        final Map<String, Object> divorceSession = getCaseData(false, true);

        CaseDetails caseDetails = CaseDetails.builder().caseData(divorceSession).build();
        when(caseMaintenanceClient.retrievePetitionById(AUTH_TOKEN, TEST_CASE_ID)).thenReturn(caseDetails);

        when(caseMaintenanceClient.updateCase(AUTH_TOKEN, TEST_CASE_ID, AWAITING_ANSWER_AOS_EVENT_ID, divorceSession))
            .thenReturn(CASE_UPDATE_RESPONSE);

        Map<String, Object> expectedData = new HashMap<>(divorceSession);
        expectedData.put(RECEIVED_AOS_FROM_RESP, YES_VALUE);
        expectedData.put(RECEIVED_AOS_FROM_RESP_DATE, FIXED_DATE);

        assertEquals(EXPECTED_OUTPUT, classUnderTest.execute(TASK_CONTEXT, divorceSession));

        verify(caseMaintenanceClient)
            .updateCase(AUTH_TOKEN, TEST_CASE_ID, AWAITING_ANSWER_AOS_EVENT_ID, expectedData);
    }

    @Test
    public void givenBehaviourNoConsentAndNoDefend_whenExecute_thenProceedAsExpected() {
        final Map<String, Object> divorceSession = getCaseData(false, false);

        final Map<String, Object> existingCaseData = new HashMap<>();
        existingCaseData.put(D_8_REASON_FOR_DIVORCE, UNREASONABLE_BEHAVIOUR.getValue());

        when(caseMaintenanceClient.retrievePetitionById(AUTH_TOKEN, TEST_CASE_ID)).thenReturn(
            CaseDetails.builder().caseId(TEST_CASE_ID).caseData(emptyMap()).build());

        when(caseMaintenanceClient.updateCase(AUTH_TOKEN, TEST_CASE_ID, AWAITING_DN_AOS_EVENT_ID, divorceSession))
            .thenReturn(CASE_UPDATE_RESPONSE);
        Map<String, Object> expectedData = new HashMap<>(divorceSession);
        expectedData.put(RECEIVED_AOS_FROM_RESP, YES_VALUE);
        expectedData.put(RECEIVED_AOS_FROM_RESP_DATE, FIXED_DATE);

        assertEquals(EXPECTED_OUTPUT, classUnderTest.execute(TASK_CONTEXT, divorceSession));

        // we let the case proceed to awaiting DN for unreasonable behaviour regardless of whether they admit or not
        verify(caseMaintenanceClient).updateCase(AUTH_TOKEN, TEST_CASE_ID, AWAITING_DN_AOS_EVENT_ID, expectedData);
    }

    @Test
    public void givenBehaviourNoConsentAndNoDefend_whenExecute_thenProceedAsExpected_Welsh_Case() {
        final Map<String, Object> divorceSession = getCaseData(false, false);
        divorceSession.put(LANGUAGE_PREFERENCE_WELSH, YES_VALUE);

        final Map<String, Object> existingCaseData = new HashMap<>();
        existingCaseData.put(D_8_REASON_FOR_DIVORCE, UNREASONABLE_BEHAVIOUR.getValue());

        when(caseMaintenanceClient.retrievePetitionById(AUTH_TOKEN, TEST_CASE_ID)).thenReturn(
            CaseDetails.builder().caseId(TEST_CASE_ID).caseData(emptyMap()).build());

        Map<String, Object> expectedData = new HashMap<>(divorceSession);
        expectedData.put(RECEIVED_AOS_FROM_RESP, YES_VALUE);
        expectedData.put(RECEIVED_AOS_FROM_RESP_DATE, FIXED_DATE);
        expectedData.put(WELSH_NEXT_EVENT, AWAITING_DN_AOS_EVENT_ID);

        assertEquals(EXPECTED_OUTPUT, classUnderTest.execute(TASK_CONTEXT, divorceSession));

        // we let the case proceed to awaiting DN for unreasonable behaviour regardless of whether they admit or not
        verify(caseMaintenanceClient).updateCase(AUTH_TOKEN, TEST_CASE_ID, BO_WELSH_REVIEW, expectedData);
    }

    @Test
    public void givenAdulteryNoConsentAndNoDefend_whenExecute_thenProceedAsExpected() {
        final Map<String, Object> divorceSession = getCaseData(false, false);

        final Map<String, Object> existingCaseData = new HashMap<>();
        existingCaseData.put(D_8_REASON_FOR_DIVORCE, ADULTERY.getValue());

        when(caseMaintenanceClient.retrievePetitionById(AUTH_TOKEN, TEST_CASE_ID)).thenReturn(
            CaseDetails.builder().caseId(TEST_CASE_ID).caseData(existingCaseData).build());

        when(caseMaintenanceClient.updateCase(AUTH_TOKEN, TEST_CASE_ID, COMPLETED_AOS_EVENT_ID, divorceSession))
            .thenReturn(CASE_UPDATE_RESPONSE);

        Map<String, Object> expectedData = new HashMap<>(divorceSession);
        expectedData.put(RECEIVED_AOS_FROM_RESP, YES_VALUE);
        expectedData.put(RECEIVED_AOS_FROM_RESP_DATE, FIXED_DATE);

        assertEquals(EXPECTED_OUTPUT, classUnderTest.execute(TASK_CONTEXT, divorceSession));

        verify(caseMaintenanceClient).updateCase(AUTH_TOKEN, TEST_CASE_ID, COMPLETED_AOS_EVENT_ID, expectedData);
    }

    @Test
    public void given2YearSepNoConsentAndNoDefend_whenExecute_thenProceedAsExpected() {
        final Map<String, Object> divorceSession = getCaseData(false, false);

        final Map<String, Object> existingCaseData = new HashMap<>();
        existingCaseData.put(D_8_REASON_FOR_DIVORCE, SEPARATION_TWO_YEARS.getValue());

        when(caseMaintenanceClient.retrievePetitionById(AUTH_TOKEN, TEST_CASE_ID)).thenReturn(
            CaseDetails.builder().caseId(TEST_CASE_ID).caseData(existingCaseData).build());

        when(caseMaintenanceClient.updateCase(AUTH_TOKEN, TEST_CASE_ID, COMPLETED_AOS_EVENT_ID, divorceSession))
            .thenReturn(CASE_UPDATE_RESPONSE);

        Map<String, Object> expectedData = new HashMap<>(divorceSession);
        expectedData.put(RECEIVED_AOS_FROM_RESP, YES_VALUE);
        expectedData.put(RECEIVED_AOS_FROM_RESP_DATE, FIXED_DATE);
        assertEquals(EXPECTED_OUTPUT, classUnderTest.execute(TASK_CONTEXT, divorceSession));

        verify(caseMaintenanceClient).updateCase(AUTH_TOKEN, TEST_CASE_ID, COMPLETED_AOS_EVENT_ID, expectedData);
    }

    @Test
    public void givenConsentAndNoDefend_whenExecute_thenProceedAsExpected() {
        final Map<String, Object> divorceSession = getCaseData(true, false);

        when(caseMaintenanceClient.retrievePetitionById(AUTH_TOKEN, TEST_CASE_ID)).thenReturn(
            CaseDetails.builder().caseId(TEST_CASE_ID).caseData(emptyMap()).build());

        when(caseMaintenanceClient.updateCase(AUTH_TOKEN, TEST_CASE_ID, AWAITING_DN_AOS_EVENT_ID, divorceSession))
            .thenReturn(CASE_UPDATE_RESPONSE);

        Map<String, Object> expectedData = new HashMap<>(divorceSession);
        expectedData.put(RECEIVED_AOS_FROM_RESP, YES_VALUE);
        expectedData.put(RECEIVED_AOS_FROM_RESP_DATE, FIXED_DATE);

        assertEquals(EXPECTED_OUTPUT, classUnderTest.execute(TASK_CONTEXT, divorceSession));

        verify(caseMaintenanceClient).updateCase(AUTH_TOKEN, TEST_CASE_ID, AWAITING_DN_AOS_EVENT_ID, expectedData);
    }

    @Test
    public void givenSolicitorIsRepresented_whenExecute_thenNextStateIsAosAwaiting() {
        final Map<String, Object> divorceSession = buildSolicitorResponse();
        Map<String, Object> expectedData = new HashMap<>(divorceSession);

        assertEquals(EXPECTED_OUTPUT, classUnderTest.execute(TASK_CONTEXT, divorceSession));

        verify(caseMaintenanceClient)
                .updateCase(eq(AUTH_TOKEN), eq(TEST_CASE_ID), eq(AOS_NOMINATE_SOLICITOR), eq(expectedData));
    }

    @Test
    public void givenConsentAndDefend_whenExecute_thenProceedAsExpected_with_WelshEvent() {
        final Map<String, Object> divorceSession = getCaseData(true, true);
        divorceSession.put(LANGUAGE_PREFERENCE_WELSH, YES_VALUE);

        Map<String, Object> expectedData = new HashMap<>(divorceSession);
        expectedData.put(RECEIVED_AOS_FROM_RESP, YES_VALUE);
        expectedData.put(RECEIVED_AOS_FROM_RESP_DATE, FIXED_DATE);
        expectedData.put(WELSH_NEXT_EVENT, AWAITING_ANSWER_AOS_EVENT_ID);

        assertEquals(EXPECTED_OUTPUT, classUnderTest.execute(TASK_CONTEXT, divorceSession));

        verify(caseMaintenanceClient)
                .updateCase(eq(AUTH_TOKEN), eq(TEST_CASE_ID), eq(BO_WELSH_REVIEW), eq(expectedData));
    }

    @Test
    public void givenConsentAndDefend_whenExecute_thenProceedAsExpected_with_WelshEvent_transient_CaseDetails() {
        final Map<String, Object> divorceSession = getCaseData(true, true);
        divorceSession.put(LANGUAGE_PREFERENCE_WELSH, YES_VALUE);
        CaseDetails caseDetails = CaseDetails.builder().caseData(divorceSession).build();
        TASK_CONTEXT.setTransientObject(CASE_DETAILS_JSON_KEY, caseDetails);

        when(caseMaintenanceClient.updateCase(eq(AUTH_TOKEN), eq(TEST_CASE_ID),
                eq(BO_WELSH_REVIEW), eq(divorceSession)))
                .thenReturn(CASE_UPDATE_RESPONSE);

        Map<String, Object> expectedData = new HashMap<>(divorceSession);
        expectedData.put(RECEIVED_AOS_FROM_RESP, YES_VALUE);
        expectedData.put(RECEIVED_AOS_FROM_RESP_DATE, FIXED_DATE);
        expectedData.put(WELSH_NEXT_EVENT, AWAITING_ANSWER_AOS_EVENT_ID);

        assertEquals(EXPECTED_OUTPUT, classUnderTest.execute(TASK_CONTEXT, divorceSession));

        verify(caseMaintenanceClient)
                .updateCase(eq(AUTH_TOKEN), eq(TEST_CASE_ID), eq(BO_WELSH_REVIEW), eq(expectedData));
    }

    private Map<String, Object> getCaseData(boolean consented, boolean defended) {
        Map<String, Object> caseData = new HashMap<>();

        if (consented) {
            caseData.put(RESP_ADMIT_OR_CONSENT_TO_FACT, YES_VALUE);
        } else {
            caseData.put(RESP_ADMIT_OR_CONSENT_TO_FACT, NO_VALUE);
        }

        if (defended) {
            caseData.put(RESP_WILL_DEFEND_DIVORCE, YES_VALUE);
        } else {
            caseData.put(RESP_WILL_DEFEND_DIVORCE, NO_VALUE);
        }

        return caseData;
    }

    private Map<String, Object> buildSolicitorResponse() {
        Map<String, Object> caseData = new HashMap<>();
        caseData.put("respondentSolicitorRepresented", YES_VALUE);
        caseData.put("D8RespondentSolicitorName", "Some name");
        caseData.put("D8RespondentSolicitorCompany", "Awesome Solicitors LLP");
        caseData.put("D8RespondentSolicitorEmail", "solicitor@localhost.local");
        caseData.put("D8RespondentSolicitorPhone", "2222222222");
        caseData.put("respondentSolicitorReference", "2334234");

        return caseData;
    }
}
