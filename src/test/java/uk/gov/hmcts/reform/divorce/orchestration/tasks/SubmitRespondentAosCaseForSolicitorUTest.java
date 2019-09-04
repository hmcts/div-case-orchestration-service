package uk.gov.hmcts.reform.divorce.orchestration.tasks;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.divorce.orchestration.client.CaseMaintenanceClient;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.WorkflowException;
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
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.ADULTERY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.AUTH_TOKEN_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CASE_ID_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CCD_CASE_DATA_FIELD;
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
public class SubmitRespondentAosCaseForSolicitorUTest {
    private static final Map<String, Object> EXPECTED_OUTPUT = emptyMap();
    private static final Map<String, Object> CASE_UPDATE_RESPONSE = new HashMap<>();
    private static final TaskContext TASK_CONTEXT = new DefaultTaskContext();

    private static final String FIXED_DATE = "2019-05-10";

    private final Map<String, Object> expectedData = new HashMap<>();

    @Mock
    private CaseMaintenanceClient caseMaintenanceClient;

    @Mock
    private CcdUtil ccdUtil;

    @InjectMocks
    private SubmitRespondentAosCaseForSolicitor classUnderTest;

    @Before
    public void before() {
        when(ccdUtil.getCurrentDateCcdFormat()).thenReturn(FIXED_DATE);
        TASK_CONTEXT.setTransientObject(AUTH_TOKEN_JSON_KEY, AUTH_TOKEN);
        TASK_CONTEXT.setTransientObject(CASE_ID_JSON_KEY, TEST_CASE_ID);
        CASE_UPDATE_RESPONSE.put(CCD_CASE_DATA_FIELD, EXPECTED_OUTPUT);
    }

    @Test
    public void givenSolicitorIsRepresenting_ValuesForReceivedAosFromResp_AreAddedToCaseData() {

        final Map<String, Object> divorceSession = buildSolicitorResponse(false, false);

        Map<String, Object> expectedData = new HashMap<>(divorceSession);
        expectedData.put(RECEIVED_AOS_FROM_RESP, YES_VALUE);
        expectedData.put(RECEIVED_AOS_FROM_RESP_DATE, ccdUtil.getCurrentDateCcdFormat());

        assertEquals(EXPECTED_OUTPUT, classUnderTest.execute(TASK_CONTEXT, divorceSession));

        verify(caseMaintenanceClient).updateCase(eq(AUTH_TOKEN), eq(TEST_CASE_ID), eq(SOL_AOS_RECEIVED_NO_ADCON_STARTED_EVENT_ID), eq(expectedData));
    }

    @Test
    public void correctlyMapFieldsInCaseData_When_2yearSepAndRespAos2yrConsentIsYes() throws WorkflowException {
        // When Fact = 2 year separation and RespAOS2yrConsent = Yes
        // Set RespAdmitOrConsentToFact = "Yes" and RespWillDefendDivorce = "No"

        Map<String, Object> divorceSession = new HashMap<>();
        divorceSession.put(RESP_SOL_REPRESENTED, YES_VALUE);
        divorceSession.put(D_8_REASON_FOR_DIVORCE, SEPARATION_2YRS);
        divorceSession.put(RESP_AOS_2_YR_CONSENT, YES_VALUE);

        Map<String, Object> expectedData = new HashMap<>();
        expectedData.put(RESP_SOL_REPRESENTED, YES_VALUE);
        expectedData.put(RECEIVED_AOS_FROM_RESP, YES_VALUE);
        expectedData.put(RECEIVED_AOS_FROM_RESP_DATE, ccdUtil.getCurrentDateCcdFormat());
        expectedData.put(D_8_REASON_FOR_DIVORCE, SEPARATION_2YRS);
        expectedData.put(RESP_AOS_2_YR_CONSENT, YES_VALUE);
        expectedData.put(RESP_ADMIT_OR_CONSENT_TO_FACT, YES_VALUE);
        expectedData.put(RESP_WILL_DEFEND_DIVORCE, NO_VALUE);

        assertEquals(EXPECTED_OUTPUT, classUnderTest.execute(TASK_CONTEXT, divorceSession));

        verify(caseMaintenanceClient)
                .updateCase(eq(AUTH_TOKEN), eq(TEST_CASE_ID), eq(SOL_AOS_SUBMITTED_UNDEFENDED_EVENT_ID), eq(expectedData));
    }

    @Test
    public void correctlyMapFieldsInCaseData_When_AdulteryAndRespAosAdulteryIsYes() throws WorkflowException {
        // When Fact = adultery and RespAOSAdultery = Yes
        // Set RespAdmitOrConsentToFact = "Yes" and RespWillDefendDivorce = "No"

        Map<String, Object> divorceSession = new HashMap<>();
        divorceSession.put(RESP_SOL_REPRESENTED, YES_VALUE);
        divorceSession.put(D_8_REASON_FOR_DIVORCE, ADULTERY);
        divorceSession.put(RESP_AOS_ADULTERY, YES_VALUE);

        Map<String, Object> expectedData = new HashMap<>();
        expectedData.put(RESP_SOL_REPRESENTED, YES_VALUE);
        expectedData.put(RECEIVED_AOS_FROM_RESP, YES_VALUE);
        expectedData.put(RECEIVED_AOS_FROM_RESP_DATE, ccdUtil.getCurrentDateCcdFormat());
        expectedData.put(D_8_REASON_FOR_DIVORCE, ADULTERY);
        expectedData.put(RESP_AOS_ADULTERY, YES_VALUE);
        expectedData.put(RESP_ADMIT_OR_CONSENT_TO_FACT, YES_VALUE);
        expectedData.put(RESP_WILL_DEFEND_DIVORCE, NO_VALUE);

        assertEquals(EXPECTED_OUTPUT, classUnderTest.execute(TASK_CONTEXT, divorceSession));

        verify(caseMaintenanceClient)
                .updateCase(eq(AUTH_TOKEN), eq(TEST_CASE_ID), eq(SOL_AOS_SUBMITTED_UNDEFENDED_EVENT_ID), eq(expectedData));
    }

    @Test
    public void givenSolicitorIsRepresenting_ConsentAndDefended_then_eventTriggeredIs_SolAosSubmittedDefended() throws WorkflowException {
        final Map<String, Object> divorceSession = buildSolicitorResponse(true, true);

        Map<String, Object> expectedData = divorceSession;
        expectedData.put(RECEIVED_AOS_FROM_RESP, YES_VALUE);
        expectedData.put(RECEIVED_AOS_FROM_RESP_DATE, ccdUtil.getCurrentDateCcdFormat());

        assertEquals(EXPECTED_OUTPUT, classUnderTest.execute(TASK_CONTEXT, divorceSession));

        verify(caseMaintenanceClient)
                .updateCase(eq(AUTH_TOKEN), eq(TEST_CASE_ID), eq(SOL_AOS_SUBMITTED_DEFENDED_EVENT_ID), eq(expectedData));
    }

    @Test
    public void givenSolicitorIsRepresenting_ConsentAndNotDefended_then_eventTriggeredIs_SolAosSubmittedUndefended() throws WorkflowException {

        final Map<String, Object> divorceSession = buildSolicitorResponse(true, false);

        Map<String, Object> expectedData = divorceSession;
        expectedData.put(RECEIVED_AOS_FROM_RESP, YES_VALUE);
        expectedData.put(RECEIVED_AOS_FROM_RESP_DATE, ccdUtil.getCurrentDateCcdFormat());

        assertEquals(EXPECTED_OUTPUT, classUnderTest.execute(TASK_CONTEXT, divorceSession));

        verify(caseMaintenanceClient)
                .updateCase(eq(AUTH_TOKEN), eq(TEST_CASE_ID), eq(SOL_AOS_SUBMITTED_UNDEFENDED_EVENT_ID), eq(expectedData));
    }

    @Test
    public void givenSolicitorIsRepresenting_NoConsentAndDefended_then_eventTriggeredIs_SolAosReceivedNoAdConStarted() throws WorkflowException {
        final Map<String, Object> divorceSession = buildSolicitorResponse(false, true);

        Map<String, Object> expectedData = divorceSession;
        expectedData.put(RECEIVED_AOS_FROM_RESP, YES_VALUE);
        expectedData.put(RECEIVED_AOS_FROM_RESP_DATE, ccdUtil.getCurrentDateCcdFormat());

        assertEquals(EXPECTED_OUTPUT, classUnderTest.execute(TASK_CONTEXT, divorceSession));

        verify(caseMaintenanceClient)
                .updateCase(eq(AUTH_TOKEN), eq(TEST_CASE_ID), eq(SOL_AOS_RECEIVED_NO_ADCON_STARTED_EVENT_ID), eq(expectedData));
    }

    @Test
    public void givenSolicitorIsRepresenting_NoConsentAndNotDefended_then_eventTriggeredIs_solAosSubmittedUndefended() {

        final Map<String, Object> divorceSession = buildSolicitorResponse(false, false);

        Map<String, Object> expectedData = divorceSession;
        expectedData.put(RECEIVED_AOS_FROM_RESP, YES_VALUE);
        expectedData.put(RECEIVED_AOS_FROM_RESP_DATE, ccdUtil.getCurrentDateCcdFormat());

        assertEquals(EXPECTED_OUTPUT, classUnderTest.execute(TASK_CONTEXT, divorceSession));

        verify(caseMaintenanceClient)
                .updateCase(eq(AUTH_TOKEN), eq(TEST_CASE_ID), eq(SOL_AOS_RECEIVED_NO_ADCON_STARTED_EVENT_ID), eq(expectedData));
    }

    private Map<String, Object> buildSolicitorResponse(boolean consented, boolean defended) {
        Map<String, Object> caseData = new HashMap<>();
        // update remainder to use constants
        caseData.put(RESP_SOL_REPRESENTED, YES_VALUE);
        caseData.put("D8RespondentSolicitorName", "Some name");
        caseData.put("D8RespondentSolicitorCompany", "Awesome Solicitors LLP");
        caseData.put("D8RespondentSolicitorEmail", "solicitor@localhost.local");
        caseData.put("D8RespondentSolicitorPhone", "2222222222");
        caseData.put("respondentSolicitorReference", "2334234");

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
}
