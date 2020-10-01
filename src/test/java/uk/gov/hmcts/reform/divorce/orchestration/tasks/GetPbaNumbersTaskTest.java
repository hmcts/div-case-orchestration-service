package uk.gov.hmcts.reform.divorce.orchestration.tasks;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.ResponseEntity;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.divorce.orchestration.client.PbaValidationClient;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.fees.OrderSummary;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.DefaultTaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskException;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_RESP_SOLICITOR_EMAIL;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_SERVICE_AUTH_TOKEN;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.AUTH_TOKEN_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CASE_ID_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.FEE_PAY_BY_ACCOUNT;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.PETITION_ISSUE_ORDER_SUMMARY_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.RESPONDENT_SOLICITOR_EMAIL_ADDRESS;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.SOLICITOR_HOW_TO_PAY_JSON_KEY;

@RunWith(MockitoJUnitRunner.class)
public class GetPbaNumbersTaskTest {

    @Mock
    private PbaValidationClient pbaValidationClient;

    @Mock
    private AuthTokenGenerator serviceAuthGenerator;

    @InjectMocks
    private GetPbaNumbersTask getPbaNumbersTask;

    private TaskContext context;
    private Map<String, Object> caseData;

    @Before
    public void setup() {
        context = new DefaultTaskContext();
        context.setTransientObject(AUTH_TOKEN_JSON_KEY, AUTH_TOKEN);
        context.setTransientObject(CASE_ID_JSON_KEY, TEST_CASE_ID);

        caseData = new HashMap<>();
        caseData.put(SOLICITOR_HOW_TO_PAY_JSON_KEY, FEE_PAY_BY_ACCOUNT);
        caseData.put(RESPONDENT_SOLICITOR_EMAIL_ADDRESS, TEST_RESP_SOLICITOR_EMAIL);

        when(pbaValidationClient.retrievePbaNumbers(
            AUTH_TOKEN,
            TEST_SERVICE_AUTH_TOKEN,
            TEST_RESP_SOLICITOR_EMAIL))
            .thenReturn(mock(ResponseEntity.class));
    }

    @Test
    public void givenNotPayByAccount_whenExecuteIsCalled_thenReturnData() {
        caseData.replace(SOLICITOR_HOW_TO_PAY_JSON_KEY, "NotByAccount");

        // Will fail if it attempts to call paymentClient
        assertEquals(caseData, getPbaNumbersTask.execute(context, caseData));
    }

    @Test(expected = TaskException.class)
    public void givenMissingData_whenExecuteIsCalled_thenThrowTaskException() {
        caseData = new HashMap<>();
        caseData.put(SOLICITOR_HOW_TO_PAY_JSON_KEY, FEE_PAY_BY_ACCOUNT);

        getPbaNumbersTask.execute(context, caseData);
    }

    @Test
    public void givenValidData_whenExecuteIsCalled_thenMakePayment() throws Exception {
        when(objectMapper.convertValue(caseData.get(PETITION_ISSUE_ORDER_SUMMARY_JSON_KEY), OrderSummary.class))
            .thenReturn(orderSummary);
        when(serviceAuthGenerator.generate()).thenReturn(TEST_SERVICE_AUTH_TOKEN);

        when(paymentClient.creditAccountPayment(
            AUTH_TOKEN,
            TEST_SERVICE_AUTH_TOKEN,
            expectedRequest))
            .thenReturn(mock(ResponseEntity.class));

        assertEquals(caseData, processPbaPayment.execute(context, caseData));

        verify(objectMapper).convertValue(caseData.get(PETITION_ISSUE_ORDER_SUMMARY_JSON_KEY), OrderSummary.class);
        verify(serviceAuthGenerator).generate();
        verify(paymentClient).creditAccountPayment(
            AUTH_TOKEN,
            TEST_SERVICE_AUTH_TOKEN,
            expectedRequest);
    }
}
