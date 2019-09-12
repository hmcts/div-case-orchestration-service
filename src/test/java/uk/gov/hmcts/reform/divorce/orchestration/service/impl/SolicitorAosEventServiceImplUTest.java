package uk.gov.hmcts.reform.divorce.orchestration.service.impl;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.divorce.orchestration.client.CaseMaintenanceClient;
import uk.gov.hmcts.reform.divorce.orchestration.event.domain.SubmitSolicitorAosEvent;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.WorkflowException;
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
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.ADULTERY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.AUTH_TOKEN_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CASE_ID_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CCD_CASE_DATA;
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
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.RESP_AOS_ADULTERY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.RESP_SOL_REPRESENTED;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.RESP_WILL_DEFEND_DIVORCE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.SEPARATION_2YRS;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.SOL_AOS_RECEIVED_NO_ADCON_STARTED_EVENT_ID;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.SOL_AOS_SUBMITTED_DEFENDED_EVENT_ID;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.SOL_AOS_SUBMITTED_UNDEFENDED_EVENT_ID;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.YES_VALUE;

@RunWith(MockitoJUnitRunner.class)
public class SolicitorAosEventServiceImplUTest {
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
    public void givenSolicitorIsRepresenting_ValuesForReceivedAosFromResp_AreAddedToCaseData() {
        final Map<String, Object> caseData = buildSolicitorResponse( false);
        TASK_CONTEXT.setTransientObject(CCD_CASE_DATA, caseData);

        final Map<String, Object> expectedData = new HashMap<>(caseData);
        expectedData.put(RECEIVED_AOS_FROM_RESP, YES_VALUE);
        expectedData.put(RECEIVED_AOS_FROM_RESP_DATE, ccdUtil.getCurrentDateCcdFormat());

        SubmitSolicitorAosEvent event = new SubmitSolicitorAosEvent(TASK_CONTEXT);
        assertEquals(expectedData, classUnderTest.fireSecondaryAosEvent(event));

        verify(caseMaintenanceClient).updateCase(
            eq(AUTH_TOKEN), eq(TEST_CASE_ID), eq(SOL_AOS_SUBMITTED_UNDEFENDED_EVENT_ID), eq(expectedData));
    }

    @Test
    public void correctlyMapFieldsInCaseData_When_2yearSepAndRespAos2yrConsentIsYes() {
        // When Fact = 2 year separation and RespAOS2yrConsent = Yes
        // Set RespAdmitOrConsentToFact = "Yes" and RespWillDefendDivorce = "No"
        final Map<String, Object> caseData = new HashMap<>();
        caseData.put(RESP_SOL_REPRESENTED, YES_VALUE);
        caseData.put(D_8_REASON_FOR_DIVORCE, SEPARATION_2YRS);
        caseData.put(RESP_AOS_2_YR_CONSENT, YES_VALUE);
        TASK_CONTEXT.setTransientObject(CCD_CASE_DATA, caseData);

        final Map<String, Object> expectedData = new HashMap<>();
        expectedData.put(RESP_SOL_REPRESENTED, YES_VALUE);
        expectedData.put(RECEIVED_AOS_FROM_RESP, YES_VALUE);
        expectedData.put(RECEIVED_AOS_FROM_RESP_DATE, ccdUtil.getCurrentDateCcdFormat());
        expectedData.put(D_8_REASON_FOR_DIVORCE, SEPARATION_2YRS);
        expectedData.put(RESP_AOS_2_YR_CONSENT, YES_VALUE);
        expectedData.put(RESP_ADMIT_OR_CONSENT_TO_FACT, YES_VALUE);
        expectedData.put(RESP_WILL_DEFEND_DIVORCE, NO_VALUE);

        SubmitSolicitorAosEvent event = new SubmitSolicitorAosEvent(TASK_CONTEXT);
        assertEquals(expectedData, classUnderTest.fireSecondaryAosEvent(event));

        verify(caseMaintenanceClient).updateCase(
            eq(AUTH_TOKEN), eq(TEST_CASE_ID), eq(SOL_AOS_SUBMITTED_UNDEFENDED_EVENT_ID), eq(expectedData));
    }

    @Test
    public void correctlyMapFieldsInCaseData_When_AdulteryAndRespAosAdulteryIsYes() {
        // When Fact = adultery and RespAOSAdultery = Yes
        // Set RespAdmitOrConsentToFact = "Yes" and RespWillDefendDivorce = "No"
        final Map<String, Object> caseData = new HashMap<>();
        caseData.put(RESP_SOL_REPRESENTED, YES_VALUE);
        caseData.put(D_8_REASON_FOR_DIVORCE, ADULTERY);
        caseData.put(RESP_AOS_ADULTERY, YES_VALUE);
        TASK_CONTEXT.setTransientObject(CCD_CASE_DATA, caseData);

        final Map<String, Object> expectedData = new HashMap<>();
        expectedData.put(RESP_SOL_REPRESENTED, YES_VALUE);
        expectedData.put(RECEIVED_AOS_FROM_RESP, YES_VALUE);
        expectedData.put(RECEIVED_AOS_FROM_RESP_DATE, ccdUtil.getCurrentDateCcdFormat());
        expectedData.put(D_8_REASON_FOR_DIVORCE, ADULTERY);
        expectedData.put(RESP_AOS_ADULTERY, YES_VALUE);
        expectedData.put(RESP_ADMIT_OR_CONSENT_TO_FACT, YES_VALUE);
        expectedData.put(RESP_WILL_DEFEND_DIVORCE, NO_VALUE);

        SubmitSolicitorAosEvent event = new SubmitSolicitorAosEvent(TASK_CONTEXT);
        assertEquals(expectedData, classUnderTest.fireSecondaryAosEvent(event));

        verify(caseMaintenanceClient).updateCase(
            eq(AUTH_TOKEN), eq(TEST_CASE_ID), eq(SOL_AOS_SUBMITTED_UNDEFENDED_EVENT_ID), eq(expectedData));
    }

    @Test
    public void givenSolicitorIsRepresenting_DefendedDivorce_then_eventTriggeredIs_SolAosSubmittedDefended() {
        final Map<String, Object> caseData = buildSolicitorResponse(true);
        TASK_CONTEXT.setTransientObject(CCD_CASE_DATA, caseData);

        Map<String, Object> expectedData = buildSolicitorResponse(true);
        expectedData.put(RECEIVED_AOS_FROM_RESP, YES_VALUE);
        expectedData.put(RECEIVED_AOS_FROM_RESP_DATE, ccdUtil.getCurrentDateCcdFormat());

        SubmitSolicitorAosEvent event = new SubmitSolicitorAosEvent(TASK_CONTEXT);
        assertEquals(expectedData, classUnderTest.fireSecondaryAosEvent(event));

        verify(caseMaintenanceClient).updateCase(
            eq(AUTH_TOKEN), eq(TEST_CASE_ID), eq(SOL_AOS_SUBMITTED_DEFENDED_EVENT_ID), eq(expectedData));
    }

    @Test
    public void givenSolicitorIsRepresenting_NotDefended_then_eventTriggeredIs_SolAosSubmittedUndefended() {
        final Map<String, Object> caseData = buildSolicitorResponse(false);
        TASK_CONTEXT.setTransientObject(CCD_CASE_DATA, caseData);

        Map<String, Object> expectedData = buildSolicitorResponse(false);
        expectedData.put(RECEIVED_AOS_FROM_RESP, YES_VALUE);
        expectedData.put(RECEIVED_AOS_FROM_RESP_DATE, ccdUtil.getCurrentDateCcdFormat());

        SubmitSolicitorAosEvent event = new SubmitSolicitorAosEvent(TASK_CONTEXT);
        assertEquals(expectedData, classUnderTest.fireSecondaryAosEvent(event));

        verify(caseMaintenanceClient)
                .updateCase(eq(AUTH_TOKEN), eq(TEST_CASE_ID), eq(SOL_AOS_SUBMITTED_UNDEFENDED_EVENT_ID), eq(expectedData));
    }

    @Test
    public void givenSolicitorIsRepresenting_DoesNotConsentTo2YearSep_then_eventTriggeredIs_SolAosReceivedNoAdConStarted() throws WorkflowException {
        final Map<String, Object> caseData = buildSolicitorResponse(true);

        caseData.put(RESP_AOS_2_YR_CONSENT, NO_VALUE);
        TASK_CONTEXT.setTransientObject(CCD_CASE_DATA, caseData);

        Map<String, Object> expectedData = buildSolicitorResponse(true);
        expectedData.put(RESP_AOS_2_YR_CONSENT, NO_VALUE);
        expectedData.put(RECEIVED_AOS_FROM_RESP, YES_VALUE);
        expectedData.put(RECEIVED_AOS_FROM_RESP_DATE, ccdUtil.getCurrentDateCcdFormat());

        SubmitSolicitorAosEvent event = new SubmitSolicitorAosEvent(TASK_CONTEXT);
        assertEquals(expectedData, classUnderTest.fireSecondaryAosEvent(event));

        verify(caseMaintenanceClient).updateCase(
            eq(AUTH_TOKEN), eq(TEST_CASE_ID), eq(SOL_AOS_RECEIVED_NO_ADCON_STARTED_EVENT_ID), eq(expectedData));
    }

    @Test
    public void givenSolicitorIsRepresenting_DoesNotAdmitAdultery_then_eventTriggeredIs_SolAosReceivedNoAdConStarted() throws WorkflowException {
        final Map<String, Object> caseData = buildSolicitorResponse(true);
        caseData.put(RESP_AOS_ADULTERY, NO_VALUE);
        caseData.put(D_8_REASON_FOR_DIVORCE, ADULTERY);
        TASK_CONTEXT.setTransientObject(CCD_CASE_DATA, caseData);

        Map<String, Object> expectedData = buildSolicitorResponse(true);
        expectedData.put(RESP_AOS_ADULTERY, NO_VALUE);
        expectedData.put(D_8_REASON_FOR_DIVORCE, ADULTERY);
        expectedData.put(RECEIVED_AOS_FROM_RESP, YES_VALUE);
        expectedData.put(RECEIVED_AOS_FROM_RESP_DATE, ccdUtil.getCurrentDateCcdFormat());

        SubmitSolicitorAosEvent event = new SubmitSolicitorAosEvent(TASK_CONTEXT);
        assertEquals(expectedData, classUnderTest.fireSecondaryAosEvent(event));

        verify(caseMaintenanceClient).updateCase(
            eq(AUTH_TOKEN), eq(TEST_CASE_ID), eq(SOL_AOS_RECEIVED_NO_ADCON_STARTED_EVENT_ID), eq(expectedData));
    }

    private Map<String, Object> buildSolicitorResponse(boolean defended) {
        Map<String, Object> caseData = new HashMap<>();

        caseData.put(RESP_SOL_REPRESENTED, YES_VALUE);
        caseData.put(D8_RESPONDENT_SOLICITOR_NAME, "Some name");
        caseData.put(D8_RESPONDENT_SOLICITOR_COMPANY, "Awesome Solicitors LLP");
        caseData.put(D8_RESPONDENT_SOLICITOR_EMAIL, "solicitor@localhost.local");
        caseData.put(D8_RESPONDENT_SOLICITOR_PHONE, "2222222222");
        caseData.put(D8_RESPONDENT_SOLICITOR_REFERENCE, "2334234");

        if (defended) {
            caseData.put(RESP_WILL_DEFEND_DIVORCE, YES_VALUE);
        } else {
            caseData.put(RESP_WILL_DEFEND_DIVORCE, NO_VALUE);
        }

        return caseData;
    }
}
