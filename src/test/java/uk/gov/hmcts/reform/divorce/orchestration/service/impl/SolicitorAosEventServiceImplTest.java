package uk.gov.hmcts.reform.divorce.orchestration.service.impl;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.divorce.orchestration.client.CaseMaintenanceClient;
import uk.gov.hmcts.reform.divorce.orchestration.event.domain.SubmitSolicitorAosEvent;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.DefaultTaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.util.CcdUtil;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.AUTH_TOKEN_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CASE_ID_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D8_RESPONDENT_SOLICITOR_COMPANY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D8_RESPONDENT_SOLICITOR_EMAIL;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D8_RESPONDENT_SOLICITOR_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D8_RESPONDENT_SOLICITOR_PHONE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D8_RESPONDENT_SOLICITOR_REFERENCE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D_8_REASON_FOR_DIVORCE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NO_VALUE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.RECEIVED_AOS_FROM_RESP;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.RECEIVED_AOS_FROM_RESP_DATE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.RESP_ADMIT_OR_CONSENT_TO_FACT;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.RESP_AOS_2_YR_CONSENT;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.RESP_AOS_ADMIT_ADULTERY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.RESP_SOL_REPRESENTED;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.RESP_WILL_DEFEND_DIVORCE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.SOL_AOS_RECEIVED_NO_ADCON_STARTED_EVENT_ID;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.SOL_AOS_SUBMITTED_DEFENDED_EVENT_ID;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.SOL_AOS_SUBMITTED_UNDEFENDED_EVENT_ID;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.UI_ONLY_RESP_WILL_DEFEND_DIVORCE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.YES_VALUE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.constants.TaskContextConstants.CCD_CASE_DATA;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.facts.DivorceFact.ADULTERY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.facts.DivorceFact.SEPARATION_FIVE_YEARS;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.facts.DivorceFact.SEPARATION_TWO_YEARS;

@RunWith(MockitoJUnitRunner.class)
public class SolicitorAosEventServiceImplTest {
    private static final TaskContext TASK_CONTEXT = new DefaultTaskContext();
    private static final String FIXED_DATE = "2019-05-10";

    @Mock
    private CaseMaintenanceClient caseMaintenanceClient;

    @Mock
    private CcdUtil ccdUtil;

    @InjectMocks
    private SolicitorAosEventServiceImpl classUnderTest;

    @Before
    public void before() {
        TASK_CONTEXT.setTransientObject(AUTH_TOKEN_JSON_KEY, AUTH_TOKEN);
        TASK_CONTEXT.setTransientObject(CASE_ID_JSON_KEY, TEST_CASE_ID);
        when(ccdUtil.getCurrentDateCcdFormat()).thenReturn(FIXED_DATE);
    }

    @Test
    public void givenSolicitorIsDefending_whenValuesReceived_caseIsUpdated() {
        final Map<String, Object> caseData = buildSolicitorResponse();
        caseData.put(RESP_WILL_DEFEND_DIVORCE, NO_VALUE);
        TASK_CONTEXT.setTransientObject(CCD_CASE_DATA, caseData);

        final Map<String, Object> expectedData = new HashMap<>();
        expectedData.put(RECEIVED_AOS_FROM_RESP, YES_VALUE);
        expectedData.put(RESP_WILL_DEFEND_DIVORCE, NO_VALUE);
        expectedData.put(RECEIVED_AOS_FROM_RESP_DATE, ccdUtil.getCurrentDateCcdFormat());

        SubmitSolicitorAosEvent event = new SubmitSolicitorAosEvent(TASK_CONTEXT);
        assertEquals(expectedData, classUnderTest.fireSecondaryAosEvent(event));

        verify(caseMaintenanceClient).updateCase(
            eq(AUTH_TOKEN), eq(TEST_CASE_ID), eq(SOL_AOS_SUBMITTED_UNDEFENDED_EVENT_ID), eq(expectedData));
    }

    @Test
    public void given2yearSepAndUndefended_whenEventFired_fieldsAreMappedAndSecondaryEventCorrect() {
        // When Fact = 2 year separation and RespAOS2yrConsent = Yes
        // Set RespAdmitOrConsentToFact = "Yes" and RespWillDefendDivorce = "No"
        final Map<String, Object> caseData = new HashMap<>();
        caseData.put(RESP_SOL_REPRESENTED, YES_VALUE);
        caseData.put(D_8_REASON_FOR_DIVORCE, SEPARATION_TWO_YEARS.getValue());
        caseData.put(RESP_AOS_2_YR_CONSENT, YES_VALUE);
        TASK_CONTEXT.setTransientObject(CCD_CASE_DATA, caseData);

        Map<String, Object> expectedData = new HashMap<>();
        expectedData.put(RECEIVED_AOS_FROM_RESP, YES_VALUE);
        expectedData.put(RECEIVED_AOS_FROM_RESP_DATE, ccdUtil.getCurrentDateCcdFormat());
        expectedData.put(RESP_ADMIT_OR_CONSENT_TO_FACT, YES_VALUE);
        expectedData.put(RESP_WILL_DEFEND_DIVORCE, NO_VALUE);

        SubmitSolicitorAosEvent event = new SubmitSolicitorAosEvent(TASK_CONTEXT);
        assertEquals(expectedData, classUnderTest.fireSecondaryAosEvent(event));

        verify(caseMaintenanceClient).updateCase(
            eq(AUTH_TOKEN), eq(TEST_CASE_ID), eq(SOL_AOS_SUBMITTED_UNDEFENDED_EVENT_ID), eq(expectedData));
    }

    @Test
    public void givenAdulteryAdmission_whenEventFired_fieldsAreMappedAndSecondaryEventCorrect() {
        // When Fact = adultery and RespAOSAdultery = Yes
        // Set RespAdmitOrConsentToFact = "Yes" and RespWillDefendDivorce = "No"
        final Map<String, Object> caseData = new HashMap<>();
        caseData.put(RESP_SOL_REPRESENTED, YES_VALUE);
        caseData.put(D_8_REASON_FOR_DIVORCE, ADULTERY.getValue());
        caseData.put(RESP_AOS_ADMIT_ADULTERY, YES_VALUE);
        TASK_CONTEXT.setTransientObject(CCD_CASE_DATA, caseData);

        final Map<String, Object> expectedData = new HashMap<>();
        expectedData.put(RECEIVED_AOS_FROM_RESP, YES_VALUE);
        expectedData.put(RECEIVED_AOS_FROM_RESP_DATE, ccdUtil.getCurrentDateCcdFormat());
        expectedData.put(RESP_ADMIT_OR_CONSENT_TO_FACT, YES_VALUE);
        expectedData.put(RESP_WILL_DEFEND_DIVORCE, NO_VALUE);

        SubmitSolicitorAosEvent event = new SubmitSolicitorAosEvent(TASK_CONTEXT);
        assertEquals(expectedData, classUnderTest.fireSecondaryAosEvent(event));

        verify(caseMaintenanceClient).updateCase(
            eq(AUTH_TOKEN), eq(TEST_CASE_ID), eq(SOL_AOS_SUBMITTED_UNDEFENDED_EVENT_ID), eq(expectedData));
    }

    @Test
    public void givenDefendedDivorce_whenEventIsTriggered_solAosSubmittedDefendedEventFired() {
        final Map<String, Object> caseData = buildSolicitorResponse();
        caseData.put(RESP_WILL_DEFEND_DIVORCE, YES_VALUE);
        TASK_CONTEXT.setTransientObject(CCD_CASE_DATA, caseData);

        Map<String, Object> expectedData = new HashMap<>();
        expectedData.put(RECEIVED_AOS_FROM_RESP, YES_VALUE);
        expectedData.put(RESP_WILL_DEFEND_DIVORCE, YES_VALUE);
        expectedData.put(RECEIVED_AOS_FROM_RESP_DATE, ccdUtil.getCurrentDateCcdFormat());

        SubmitSolicitorAosEvent event = new SubmitSolicitorAosEvent(TASK_CONTEXT);
        assertEquals(expectedData, classUnderTest.fireSecondaryAosEvent(event));

        verify(caseMaintenanceClient).updateCase(
            eq(AUTH_TOKEN), eq(TEST_CASE_ID), eq(SOL_AOS_SUBMITTED_DEFENDED_EVENT_ID), eq(expectedData));
    }

    @Test
    public void givenNotDefended_whenEventTriggered_solAosSubmittedUndefendedEventFired() {
        final Map<String, Object> caseData = buildSolicitorResponse();
        caseData.put(RESP_WILL_DEFEND_DIVORCE, NO_VALUE);
        TASK_CONTEXT.setTransientObject(CCD_CASE_DATA, caseData);

        Map<String, Object> expectedData = new HashMap<>();
        expectedData.put(RESP_WILL_DEFEND_DIVORCE, NO_VALUE);
        expectedData.put(RECEIVED_AOS_FROM_RESP, YES_VALUE);
        expectedData.put(RECEIVED_AOS_FROM_RESP_DATE, ccdUtil.getCurrentDateCcdFormat());

        SubmitSolicitorAosEvent event = new SubmitSolicitorAosEvent(TASK_CONTEXT);
        assertEquals(expectedData, classUnderTest.fireSecondaryAosEvent(event));

        verify(caseMaintenanceClient)
            .updateCase(eq(AUTH_TOKEN), eq(TEST_CASE_ID), eq(SOL_AOS_SUBMITTED_UNDEFENDED_EVENT_ID), eq(expectedData));
    }

    @Test
    public void givenDoesNotConsentTo2YearSepAndDefends_whenEventTriggered_solAosReceivedNoAdConStartedEventFired() {
        final Map<String, Object> caseData = buildSolicitorResponse();
        caseData.put(D_8_REASON_FOR_DIVORCE, SEPARATION_TWO_YEARS.getValue());
        caseData.put(RESP_AOS_2_YR_CONSENT, NO_VALUE);
        caseData.put(UI_ONLY_RESP_WILL_DEFEND_DIVORCE, YES_VALUE);
        TASK_CONTEXT.setTransientObject(CCD_CASE_DATA, caseData);

        Map<String, Object> expectedData = new HashMap<>();
        expectedData.put(RESP_WILL_DEFEND_DIVORCE, YES_VALUE);
        expectedData.put(RESP_ADMIT_OR_CONSENT_TO_FACT, NO_VALUE);
        expectedData.put(RECEIVED_AOS_FROM_RESP, YES_VALUE);
        expectedData.put(RECEIVED_AOS_FROM_RESP_DATE, ccdUtil.getCurrentDateCcdFormat());

        SubmitSolicitorAosEvent event = new SubmitSolicitorAosEvent(TASK_CONTEXT);
        assertEquals(expectedData, classUnderTest.fireSecondaryAosEvent(event));

        verify(caseMaintenanceClient).updateCase(
            eq(AUTH_TOKEN), eq(TEST_CASE_ID), eq(SOL_AOS_RECEIVED_NO_ADCON_STARTED_EVENT_ID), eq(expectedData));
    }

    @Test
    public void giveConsentTo2YearSep_whenEventTriggered_assumeNotDefended() {
        final Map<String, Object> caseData = buildSolicitorResponse();
        caseData.put(D_8_REASON_FOR_DIVORCE, SEPARATION_TWO_YEARS.getValue());
        caseData.put(RESP_AOS_2_YR_CONSENT, YES_VALUE);
        TASK_CONTEXT.setTransientObject(CCD_CASE_DATA, caseData);

        Map<String, Object> expectedData = new HashMap<>();
        expectedData.put(RESP_WILL_DEFEND_DIVORCE, NO_VALUE);
        expectedData.put(RECEIVED_AOS_FROM_RESP, YES_VALUE);
        expectedData.put(RESP_ADMIT_OR_CONSENT_TO_FACT, YES_VALUE);
        expectedData.put(RECEIVED_AOS_FROM_RESP_DATE, ccdUtil.getCurrentDateCcdFormat());

        SubmitSolicitorAosEvent event = new SubmitSolicitorAosEvent(TASK_CONTEXT);
        assertEquals(expectedData, classUnderTest.fireSecondaryAosEvent(event));

        verify(caseMaintenanceClient).updateCase(
            eq(AUTH_TOKEN), eq(TEST_CASE_ID), eq(SOL_AOS_SUBMITTED_UNDEFENDED_EVENT_ID), eq(expectedData));
    }

    @Test
    public void giveAdulteryCaseDoesConsent_whenEventTriggered_assumeNotDefended() {
        final Map<String, Object> caseData = buildSolicitorResponse();
        caseData.put(D_8_REASON_FOR_DIVORCE, ADULTERY.getValue());
        caseData.put(RESP_AOS_ADMIT_ADULTERY, YES_VALUE);
        TASK_CONTEXT.setTransientObject(CCD_CASE_DATA, caseData);

        Map<String, Object> expectedData = new HashMap<>();
        expectedData.put(RESP_WILL_DEFEND_DIVORCE, NO_VALUE);
        expectedData.put(RECEIVED_AOS_FROM_RESP, YES_VALUE);
        expectedData.put(RESP_ADMIT_OR_CONSENT_TO_FACT, YES_VALUE);
        expectedData.put(RECEIVED_AOS_FROM_RESP_DATE, ccdUtil.getCurrentDateCcdFormat());

        SubmitSolicitorAosEvent event = new SubmitSolicitorAosEvent(TASK_CONTEXT);
        assertEquals(expectedData, classUnderTest.fireSecondaryAosEvent(event));

        verify(caseMaintenanceClient).updateCase(
            eq(AUTH_TOKEN), eq(TEST_CASE_ID), eq(SOL_AOS_SUBMITTED_UNDEFENDED_EVENT_ID), eq(expectedData));
    }

    @Test
    public void givenDoesNotAdmitAdulteryAndDoesDefend_whenEventTriggered_SolAosReceivedNoAdConStartedAndValuesMapped() {
        final Map<String, Object> caseData = buildSolicitorResponse();
        caseData.put(D_8_REASON_FOR_DIVORCE, ADULTERY.getValue());
        caseData.put(RESP_AOS_ADMIT_ADULTERY, NO_VALUE);
        caseData.put(UI_ONLY_RESP_WILL_DEFEND_DIVORCE, YES_VALUE);
        caseData.put(RESP_WILL_DEFEND_DIVORCE, null);
        TASK_CONTEXT.setTransientObject(CCD_CASE_DATA, caseData);

        Map<String, Object> expectedData = new HashMap<>();
        expectedData.put(RESP_WILL_DEFEND_DIVORCE, YES_VALUE);
        expectedData.put(RESP_ADMIT_OR_CONSENT_TO_FACT, NO_VALUE);
        expectedData.put(RECEIVED_AOS_FROM_RESP, YES_VALUE);
        expectedData.put(RECEIVED_AOS_FROM_RESP_DATE, ccdUtil.getCurrentDateCcdFormat());

        SubmitSolicitorAosEvent event = new SubmitSolicitorAosEvent(TASK_CONTEXT);
        assertEquals(expectedData, classUnderTest.fireSecondaryAosEvent(event));

        verify(caseMaintenanceClient).updateCase(
            eq(AUTH_TOKEN), eq(TEST_CASE_ID), eq(SOL_AOS_RECEIVED_NO_ADCON_STARTED_EVENT_ID), eq(expectedData));
    }

    @Test
    public void given5yrSeparationAndDoesDefend_whenEventTriggered_triggersDefendEventMapsData() {
        final Map<String, Object> caseData = buildSolicitorResponse();
        caseData.put(D_8_REASON_FOR_DIVORCE, SEPARATION_FIVE_YEARS.getValue());
        caseData.put(UI_ONLY_RESP_WILL_DEFEND_DIVORCE, YES_VALUE);
        TASK_CONTEXT.setTransientObject(CCD_CASE_DATA, caseData);

        Map<String, Object> expectedData = new HashMap<>();
        expectedData.put(RESP_WILL_DEFEND_DIVORCE, YES_VALUE);
        expectedData.put(RECEIVED_AOS_FROM_RESP, YES_VALUE);
        expectedData.put(RECEIVED_AOS_FROM_RESP_DATE, ccdUtil.getCurrentDateCcdFormat());

        SubmitSolicitorAosEvent event = new SubmitSolicitorAosEvent(TASK_CONTEXT);
        assertEquals(expectedData, classUnderTest.fireSecondaryAosEvent(event));

        verify(caseMaintenanceClient).updateCase(
            eq(AUTH_TOKEN), eq(TEST_CASE_ID), eq(SOL_AOS_SUBMITTED_DEFENDED_EVENT_ID), eq(expectedData));
    }

    @Test
    public void given5yrSeparationAndDoesNotDefend_whenEventTriggered_triggersUndefendedEventMapsData() {
        final Map<String, Object> caseData = buildSolicitorResponse();
        caseData.put(D_8_REASON_FOR_DIVORCE, SEPARATION_FIVE_YEARS.getValue());
        caseData.put(UI_ONLY_RESP_WILL_DEFEND_DIVORCE, NO_VALUE);
        TASK_CONTEXT.setTransientObject(CCD_CASE_DATA, caseData);

        Map<String, Object> expectedData = new HashMap<>();
        expectedData.put(RESP_WILL_DEFEND_DIVORCE, NO_VALUE);
        expectedData.put(RECEIVED_AOS_FROM_RESP, YES_VALUE);
        expectedData.put(RECEIVED_AOS_FROM_RESP_DATE, ccdUtil.getCurrentDateCcdFormat());

        SubmitSolicitorAosEvent event = new SubmitSolicitorAosEvent(TASK_CONTEXT);
        assertEquals(expectedData, classUnderTest.fireSecondaryAosEvent(event));

        verify(caseMaintenanceClient).updateCase(
            eq(AUTH_TOKEN), eq(TEST_CASE_ID), eq(SOL_AOS_SUBMITTED_UNDEFENDED_EVENT_ID), eq(expectedData));
    }

    private Map<String, Object> buildSolicitorResponse() {
        Map<String, Object> caseData = new HashMap<>();

        caseData.put(RESP_SOL_REPRESENTED, YES_VALUE);
        caseData.put(D8_RESPONDENT_SOLICITOR_NAME, "Some name");
        caseData.put(D8_RESPONDENT_SOLICITOR_COMPANY, "Awesome Solicitors LLP");
        caseData.put(D8_RESPONDENT_SOLICITOR_EMAIL, "solicitor@localhost.local");
        caseData.put(D8_RESPONDENT_SOLICITOR_PHONE, "2222222222");
        caseData.put(D8_RESPONDENT_SOLICITOR_REFERENCE, "2334234");

        return caseData;
    }
}
