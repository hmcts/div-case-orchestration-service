package uk.gov.hmcts.reform.divorce.orchestration.tasks;

import com.google.common.collect.ImmutableMap;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.divorce.orchestration.client.CaseMaintenanceClient;
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
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.AUTH_TOKEN_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.AWAITING_ANSWER_AOS_EVENT_ID;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.AWAITING_DN_AOS_EVENT_ID;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CASE_ID_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CCD_CASE_DATA_FIELD;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CCD_DUE_DATE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.COMPLETE_AOS_EVENT_ID;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NO_VALUE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.RECEIVED_AOS_FROM_RESP_DATE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.RESP_ADMIT_OR_CONSENT_CCD_FIELD;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.RESP_DEFENDS_DIVORCE_CCD_FIELD;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.YES_VALUE;

@RunWith(MockitoJUnitRunner.class)
public class SubmitAosCaseUTest {
    private static final Map<String, Object> EXPECTED_OUTPUT = Collections.emptyMap();
    private static final Map<String, Object> CASE_UPDATE_RESPONSE = new HashMap<>();
    private static final TaskContext TASK_CONTEXT = new DefaultTaskContext();
    private static final String AOS_RESPONSE_DATE = "2018-10-22";
    private static final String AOS_DUE_DATE = "2018-11-12";

    private static final CaseDetails AOS_CASE_DETAILS =
        CaseDetails.builder()
            .caseData(
                Collections.singletonMap(
                    RECEIVED_AOS_FROM_RESP_DATE, AOS_RESPONSE_DATE
                )
            ).build();

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
    public void givenConsentAndDefend_whenExecute_thenProceedAsExpected() {
        final Map<String, Object> divorceSession = getCaseDataForDefend(YES_VALUE);

        when(caseMaintenanceClient.retrieveAosCase(AUTH_TOKEN, true)).thenReturn(AOS_CASE_DETAILS);
        when(caseMaintenanceClient.updateCase(AUTH_TOKEN, TEST_CASE_ID, AWAITING_ANSWER_AOS_EVENT_ID, divorceSession))
            .thenReturn(CASE_UPDATE_RESPONSE);


        assertEquals(EXPECTED_OUTPUT, classUnderTest.execute(TASK_CONTEXT, divorceSession));

        verify(caseMaintenanceClient).retrieveAosCase(AUTH_TOKEN, true);
        verify(caseMaintenanceClient)
            .updateCase(AUTH_TOKEN, TEST_CASE_ID, AWAITING_ANSWER_AOS_EVENT_ID, divorceSession);
    }

    @Test
    public void givenNoConsentAndDefend_whenExecute_thenProceedAsExpected() {
        final Map<String, Object> divorceSession = getCaseDataForDefend(NO_VALUE);


        when(caseMaintenanceClient.retrieveAosCase(AUTH_TOKEN, true)).thenReturn(AOS_CASE_DETAILS);
        when(caseMaintenanceClient.updateCase(AUTH_TOKEN, TEST_CASE_ID, AWAITING_ANSWER_AOS_EVENT_ID, divorceSession))
            .thenReturn(CASE_UPDATE_RESPONSE);

        assertEquals(EXPECTED_OUTPUT, classUnderTest.execute(TASK_CONTEXT, divorceSession));

        verify(caseMaintenanceClient).retrieveAosCase(AUTH_TOKEN, true);
        verify(caseMaintenanceClient)
            .updateCase(AUTH_TOKEN, TEST_CASE_ID, AWAITING_ANSWER_AOS_EVENT_ID, divorceSession);
    }

    @Test
    public void givenNoConsentAndNoDefend_whenExecute_thenProceedAsExpected() {
        final Map<String, Object> divorceSession = getCaseDataForNoDefend(NO_VALUE);

        when(caseMaintenanceClient.updateCase(AUTH_TOKEN, TEST_CASE_ID, COMPLETE_AOS_EVENT_ID, divorceSession))
            .thenReturn(CASE_UPDATE_RESPONSE);

        assertEquals(EXPECTED_OUTPUT, classUnderTest.execute(TASK_CONTEXT, divorceSession));

        verify(caseMaintenanceClient).updateCase(AUTH_TOKEN, TEST_CASE_ID, COMPLETE_AOS_EVENT_ID, divorceSession);
    }

    @Test
    public void givenConsentAndNoDefend_whenExecute_thenProceedAsExpected() {
        final Map<String, Object> divorceSession = getCaseDataForNoDefend(YES_VALUE);

        when(caseMaintenanceClient.updateCase(AUTH_TOKEN, TEST_CASE_ID, AWAITING_DN_AOS_EVENT_ID, divorceSession))
            .thenReturn(CASE_UPDATE_RESPONSE);

        assertEquals(EXPECTED_OUTPUT, classUnderTest.execute(TASK_CONTEXT, divorceSession));

        verify(caseMaintenanceClient).updateCase(AUTH_TOKEN, TEST_CASE_ID, AWAITING_DN_AOS_EVENT_ID, divorceSession);
    }

    private Map<String, Object> getCaseDataForDefend(String consent) {
        Map<String, Object> caseData = new HashMap<>();
        caseData.put(RESP_ADMIT_OR_CONSENT_CCD_FIELD, consent);
        caseData.put(RESP_DEFENDS_DIVORCE_CCD_FIELD, YES_VALUE);
        caseData.put(CCD_DUE_DATE, AOS_DUE_DATE);

        return caseData;
    }

    private Map<String, Object> getCaseDataForNoDefend(String consent) {
        return ImmutableMap.of(
            RESP_ADMIT_OR_CONSENT_CCD_FIELD, consent,
            RESP_DEFENDS_DIVORCE_CCD_FIELD, NO_VALUE
        );
    }
}