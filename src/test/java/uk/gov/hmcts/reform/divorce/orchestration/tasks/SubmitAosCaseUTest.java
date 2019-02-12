package uk.gov.hmcts.reform.divorce.orchestration.tasks;

import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.divorce.orchestration.client.CaseMaintenanceClient;
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
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.ADULTERY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.AUTH_TOKEN_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.AWAITING_ANSWER_AOS_EVENT_ID;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.AWAITING_DN_AOS_EVENT_ID;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CASE_ID_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CCD_CASE_DATA_FIELD;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.COMPLETED_AOS_EVENT_ID;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D_8_REASON_FOR_DIVORCE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NO_VALUE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.RESP_ADMIT_OR_CONSENT_TO_FACT;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.RESP_WILL_DEFEND_DIVORCE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.SEPARATION2YRS;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.YES_VALUE;

@RunWith(MockitoJUnitRunner.class)
public class SubmitAosCaseUTest {
    private static final Map<String, Object> EXPECTED_OUTPUT = Collections.emptyMap();
    private static final Map<String, Object> CASE_UPDATE_RESPONSE = new HashMap<>();
    private static final TaskContext TASK_CONTEXT = new DefaultTaskContext();
    private static Map<String, Object> DIVORCE_SESSION = new HashMap<>();

    @Mock
    private CaseMaintenanceClient caseMaintenanceClient;

    @InjectMocks
    private SubmitAosCase classUnderTest;

    @BeforeClass
    public static void beforeClass() {
        TASK_CONTEXT.setTransientObject(AUTH_TOKEN_JSON_KEY, AUTH_TOKEN);
        TASK_CONTEXT.setTransientObject(CASE_ID_JSON_KEY, TEST_CASE_ID);
        CASE_UPDATE_RESPONSE.put(CCD_CASE_DATA_FIELD, EXPECTED_OUTPUT);
    }

    @Test
    public void givenDefendAndConsent_whenExecute_thenReturnAosSubmittedDefended() {
        DIVORCE_SESSION = getCaseData(YES_VALUE, true);
        TASK_CONTEXT.setTransientObject(D_8_REASON_FOR_DIVORCE, SEPARATION2YRS);

        when(caseMaintenanceClient.updateCase(AUTH_TOKEN, TEST_CASE_ID, AWAITING_ANSWER_AOS_EVENT_ID, DIVORCE_SESSION))
            .thenReturn(CASE_UPDATE_RESPONSE);

        assertEquals(EXPECTED_OUTPUT, classUnderTest.execute(TASK_CONTEXT, DIVORCE_SESSION));

        verify(caseMaintenanceClient)
            .updateCase(AUTH_TOKEN, TEST_CASE_ID, AWAITING_ANSWER_AOS_EVENT_ID, DIVORCE_SESSION);
    }

    @Test
    public void givenDefendAndNoConsent_whenExecute_thenReturnAosSubmittedDefended() {
        DIVORCE_SESSION = getCaseData(NO_VALUE, true);
        TASK_CONTEXT.setTransientObject(D_8_REASON_FOR_DIVORCE, SEPARATION2YRS);

        when(caseMaintenanceClient.updateCase(AUTH_TOKEN, TEST_CASE_ID, AWAITING_ANSWER_AOS_EVENT_ID, DIVORCE_SESSION))
            .thenReturn(CASE_UPDATE_RESPONSE);

        assertEquals(EXPECTED_OUTPUT, classUnderTest.execute(TASK_CONTEXT, DIVORCE_SESSION));

        verify(caseMaintenanceClient)
            .updateCase(AUTH_TOKEN, TEST_CASE_ID, AWAITING_ANSWER_AOS_EVENT_ID, DIVORCE_SESSION);
    }

    @Test
    public void givenNoDefendAndNoConsentToAdultery_whenExecute_thenReturnAosCompleted() {
        DIVORCE_SESSION = getCaseData(NO_VALUE, false);
        TASK_CONTEXT.setTransientObject(D_8_REASON_FOR_DIVORCE, ADULTERY);

        when(caseMaintenanceClient.updateCase(AUTH_TOKEN, TEST_CASE_ID, COMPLETED_AOS_EVENT_ID, DIVORCE_SESSION))
                .thenReturn(CASE_UPDATE_RESPONSE);

        assertEquals(EXPECTED_OUTPUT, classUnderTest.execute(TASK_CONTEXT, DIVORCE_SESSION));

        verify(caseMaintenanceClient).updateCase(AUTH_TOKEN, TEST_CASE_ID, COMPLETED_AOS_EVENT_ID, DIVORCE_SESSION);
    }

    @Test
    public void givenNoDefendAndConsentsToAdultery_whenExecute_thenReturnAosSubmittedUndefended() {
        DIVORCE_SESSION = getCaseData(YES_VALUE, false);
        TASK_CONTEXT.setTransientObject(D_8_REASON_FOR_DIVORCE, ADULTERY);

        when(caseMaintenanceClient.updateCase(AUTH_TOKEN, TEST_CASE_ID, AWAITING_DN_AOS_EVENT_ID, DIVORCE_SESSION))
                .thenReturn(CASE_UPDATE_RESPONSE);

        assertEquals(EXPECTED_OUTPUT, classUnderTest.execute(TASK_CONTEXT, DIVORCE_SESSION));

        verify(caseMaintenanceClient).updateCase(AUTH_TOKEN, TEST_CASE_ID, AWAITING_DN_AOS_EVENT_ID, DIVORCE_SESSION);
    }

    @Test
    public void givenNoDefendAndConsentsWhenNotAdultery_whenExecute_thenReturnAosSubmittedUndefended() {
        DIVORCE_SESSION = getCaseData(YES_VALUE, false);
        TASK_CONTEXT.setTransientObject(D_8_REASON_FOR_DIVORCE, SEPARATION2YRS);

        when(caseMaintenanceClient.updateCase(AUTH_TOKEN, TEST_CASE_ID, AWAITING_DN_AOS_EVENT_ID, DIVORCE_SESSION))
                .thenReturn(CASE_UPDATE_RESPONSE);

        assertEquals(EXPECTED_OUTPUT, classUnderTest.execute(TASK_CONTEXT, DIVORCE_SESSION));

        verify(caseMaintenanceClient).updateCase(AUTH_TOKEN, TEST_CASE_ID, AWAITING_DN_AOS_EVENT_ID, DIVORCE_SESSION);
    }

    private Map<String, Object> getCaseData(String consent, boolean defended) {
        Map<String, Object> caseData = new HashMap<>();
        caseData.put(RESP_ADMIT_OR_CONSENT_TO_FACT, consent);

        if (defended) {
            caseData.put(RESP_WILL_DEFEND_DIVORCE, YES_VALUE);
        } else {
            caseData.put(RESP_WILL_DEFEND_DIVORCE, NO_VALUE);
        }

        return caseData;
    }
}