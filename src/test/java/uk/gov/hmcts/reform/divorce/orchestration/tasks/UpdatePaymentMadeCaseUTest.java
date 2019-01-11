package uk.gov.hmcts.reform.divorce.orchestration.tasks;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.divorce.orchestration.client.CaseMaintenanceClient;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.DefaultTaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskContext;

import java.util.Collections;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.AUTH_TOKEN_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.AWAITING_PAYMENT;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CASE_ID_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CASE_STATE_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D_8_PAYMENTS;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.PAYMENT_MADE_EVENT;

@RunWith(MockitoJUnitRunner.class)
public class UpdatePaymentMadeCaseUTest {
    private static final Map<String, Object> CMS_RESPONSE_DATA = Collections.emptyMap();

    @Mock
    private CaseMaintenanceClient caseMaintenanceClient;

    @InjectMocks
    private UpdatePaymentMadeCase target;

    @Test
    public void givenCaseOnSuccessState_whenExecute_thenReturnNull() {
        final TaskContext context = createContext();
        context.setTransientObject(CASE_STATE_JSON_KEY, "Success");

        final Map<String, Object> inputData = Collections.singletonMap(D_8_PAYMENTS, "World");

        assertNull(target.execute(context, inputData));

        verify(caseMaintenanceClient, never()).updateCase(any(), any(), any(), any());
    }

    @Test
    public void givenCaseWithoutPayment_whenExecute_thenReturnNull() {
        final TaskContext context = createContext();
        context.setTransientObject(CASE_STATE_JSON_KEY, AWAITING_PAYMENT);

        final Map<String, Object> inputData = Collections.singletonMap("Param", "World");

        assertNull(target.execute(context, inputData));

        verify(caseMaintenanceClient, never()).updateCase(any(), any(), any(), any());
    }


    @Test
    public void givenCaseOnAwaitingPaymentAndPaymentObject_whenExecute_thenCallUpdateCase() {
        final TaskContext context = createContext();
        context.setTransientObject(CASE_STATE_JSON_KEY, AWAITING_PAYMENT);

        final Map<String, Object> resultData = Collections.singletonMap(D_8_PAYMENTS, "World");

        when(caseMaintenanceClient.updateCase(AUTH_TOKEN, TEST_CASE_ID, PAYMENT_MADE_EVENT, resultData))
                .thenReturn(CMS_RESPONSE_DATA);

        assertEquals(CMS_RESPONSE_DATA, target.execute(context, resultData));

        verify(caseMaintenanceClient).updateCase(AUTH_TOKEN, TEST_CASE_ID, PAYMENT_MADE_EVENT, resultData);
    }

    private TaskContext createContext() {
        final TaskContext context = new DefaultTaskContext();
        context.setTransientObject(AUTH_TOKEN_JSON_KEY, AUTH_TOKEN);
        context.setTransientObject(CASE_ID_JSON_KEY, TEST_CASE_ID);
        return context;
    }

}
