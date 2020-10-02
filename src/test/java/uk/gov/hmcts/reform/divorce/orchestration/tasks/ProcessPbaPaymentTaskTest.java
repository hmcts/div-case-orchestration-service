package uk.gov.hmcts.reform.divorce.orchestration.tasks;

import com.fasterxml.jackson.databind.ObjectMapper;
import feign.FeignException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.divorce.orchestration.client.PaymentClient;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.courts.CourtEnum;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.fees.FeeResponse;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.fees.OrderSummary;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.pay.CreditAccountPaymentRequest;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.pay.CreditAccountPaymentResponse;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.pay.PaymentItem;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.payment.PaymentClientMessage;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.DefaultTaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_FEE_AMOUNT;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_FEE_CODE;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_FEE_DESCRIPTION;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_FEE_VERSION;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_SERVICE_AUTH_TOKEN;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_SOLICITOR_ACCOUNT_NUMBER;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_SOLICITOR_FIRM_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_SOLICITOR_REFERENCE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.AUTH_TOKEN_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CASE_ID_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CURRENCY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DIVORCE_CENTRE_SITEID_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.FEE_PAY_BY_ACCOUNT;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.PETITION_ISSUE_ORDER_SUMMARY_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.SERVICE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.SOLICITOR_FEE_ACCOUNT_NUMBER_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.SOLICITOR_FIRM_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.SOLICITOR_HOW_TO_PAY_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.SOLICITOR_REFERENCE_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.SOLICITOR_PBA_PAYMENT_ERROR_KEY;

@RunWith(MockitoJUnitRunner.class)
public class ProcessPbaPaymentTaskTest {

    @Mock
    private PaymentClient paymentClient;

    @Mock
    private AuthTokenGenerator serviceAuthGenerator;

    @Mock
    private ResponseEntity<CreditAccountPaymentResponse> responseEntity;

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private ProcessPbaPaymentTask processPbaPaymentTask;

    private TaskContext context;
    private Map<String, Object> caseData;
    private CreditAccountPaymentRequest expectedRequest;
    private OrderSummary orderSummary;
    private final static String SOME_PBA_CLIENT_MESSAGE = "Payment request failed";//TODO revisit
    private static final byte[] RESPONSE_DATA = "body".getBytes();//TODO revisit

    @Before
    public void setup() {
        context = new DefaultTaskContext();
        context.setTransientObject(AUTH_TOKEN_JSON_KEY, AUTH_TOKEN);
        context.setTransientObject(CASE_ID_JSON_KEY, TEST_CASE_ID);

        FeeResponse feeResponse = FeeResponse.builder()
            .amount(TEST_FEE_AMOUNT)
            .feeCode(TEST_FEE_CODE)
            .version(TEST_FEE_VERSION)
            .description(TEST_FEE_DESCRIPTION)
            .build();
        orderSummary = new OrderSummary();
        orderSummary.add(feeResponse);

        caseData = new HashMap<>();
        caseData.put(SOLICITOR_HOW_TO_PAY_JSON_KEY, FEE_PAY_BY_ACCOUNT);
        caseData.put(PETITION_ISSUE_ORDER_SUMMARY_JSON_KEY, orderSummary);
        caseData.put(CASE_ID_JSON_KEY, TEST_CASE_ID);
        caseData.put(DIVORCE_CENTRE_SITEID_JSON_KEY, CourtEnum.EASTMIDLANDS.getSiteId());
        caseData.put(SOLICITOR_FEE_ACCOUNT_NUMBER_JSON_KEY, TEST_SOLICITOR_ACCOUNT_NUMBER);
        caseData.put(SOLICITOR_FIRM_JSON_KEY, TEST_SOLICITOR_FIRM_NAME);
        caseData.put(SOLICITOR_REFERENCE_JSON_KEY, TEST_SOLICITOR_REFERENCE);

        expectedRequest = new CreditAccountPaymentRequest();
        expectedRequest.setService(SERVICE);
        expectedRequest.setCurrency(CURRENCY);
        expectedRequest.setAmount(orderSummary.getPaymentTotal());
        expectedRequest.setCcdCaseNumber(TEST_CASE_ID);
        expectedRequest.setSiteId(CourtEnum.EASTMIDLANDS.getSiteId());
        expectedRequest.setAccountNumber(TEST_SOLICITOR_ACCOUNT_NUMBER);
        expectedRequest.setOrganisationName(TEST_SOLICITOR_FIRM_NAME);
        expectedRequest.setCustomerReference(TEST_SOLICITOR_REFERENCE);
        expectedRequest.setDescription(TEST_FEE_DESCRIPTION);

        PaymentItem paymentItem = new PaymentItem();
        paymentItem.setCcdCaseNumber(TEST_CASE_ID);
        paymentItem.setCalculatedAmount(orderSummary.getPaymentTotal());
        paymentItem.setCode(TEST_FEE_CODE);
        paymentItem.setReference(TEST_SOLICITOR_REFERENCE);
        paymentItem.setVersion(TEST_FEE_VERSION.toString());
        expectedRequest.setFees(Collections.singletonList(paymentItem));
    }

    @Test
    public void givenValidData_whenExecuteIsCalled_thenMakePayment() {
        when(objectMapper.convertValue(caseData.get(PETITION_ISSUE_ORDER_SUMMARY_JSON_KEY), OrderSummary.class))
            .thenReturn(orderSummary);
        when(serviceAuthGenerator.generate()).thenReturn(TEST_SERVICE_AUTH_TOKEN);

        when(paymentClient.creditAccountPayment(
            AUTH_TOKEN,
            TEST_SERVICE_AUTH_TOKEN,
            expectedRequest))
            .thenReturn(responseEntity);

        assertThat(caseData, is(processPbaPaymentTask.execute(context, caseData)));
        assertThat(context.hasTaskFailed(), is(false));
        runCommonVerifications();
    }

    @Test
    public void given403IsReturned_whenExecuteIsCalled_thenHandleResponse() { //TODO separate multiple into parameterized runner
        setUpCommonFixtures(HttpStatus.FORBIDDEN);

        processPbaPaymentTask.execute(context, caseData);

        runCommonAssertions(PaymentClientMessage.FORBIDDEN_CA_E0004);
        runCommonVerifications();
    }

    @Test
    public void given404IsReturned_whenExecuteIsCalled_thenHandleResponse() { //TODO separate multiple into parameterized runner
        setUpCommonFixtures(HttpStatus.NOT_FOUND);

        processPbaPaymentTask.execute(context, caseData);

        runCommonAssertions(PaymentClientMessage.NOT_FOUND);
        runCommonVerifications();
    }

    @Test
    public void given422IsReturned_whenExecuteIsCalled_thenHandleResponse() { //TODO separate multiple into parameterized runner
        setUpCommonFixtures(HttpStatus.UNPROCESSABLE_ENTITY);

        processPbaPaymentTask.execute(context, caseData);

        runCommonAssertions(PaymentClientMessage.UNPROCESSABLE_ENTITY);
        runCommonVerifications();
    }

    @Test
    public void given504IsReturned_whenExecuteIsCalled_thenHandleResponse() { //TODO separate multiple into parameterized runner
        setUpCommonFixtures(HttpStatus.GATEWAY_TIMEOUT);

        processPbaPaymentTask.execute(context, caseData);

        runCommonAssertions(PaymentClientMessage.GATEWAY_TIMEOUT);
        runCommonVerifications();
    }

    @Test
    public void givenNotPayByAccount_whenExecuteIsCalled_thenReturnData() {
        caseData.replace(SOLICITOR_HOW_TO_PAY_JSON_KEY, "NotByAccount");

        // Will fail if it attempts to call paymentClient
        assertThat(caseData, is(processPbaPaymentTask.execute(context, caseData)));
    }

    @Test(expected = TaskException.class)
    public void givenMissingData_whenExecuteIsCalled_thenThrowTaskException() {
        caseData = new HashMap<>();
        caseData.put(SOLICITOR_HOW_TO_PAY_JSON_KEY, FEE_PAY_BY_ACCOUNT);

        processPbaPaymentTask.execute(context, caseData);
    }

    private Answer<ResponseEntity<?>> withClientException(HttpStatus httpStatus, byte[] body) {
        return (invocation) -> {
            if (httpStatus.value() >= 400) {
                throw new FeignException.FeignClientException(httpStatus.value(), SOME_PBA_CLIENT_MESSAGE, body);
            }
            return ResponseEntity.status(httpStatus).body(SOME_PBA_CLIENT_MESSAGE);
        };
    }

    private void setUpCommonFixtures(HttpStatus httpStatus) {
        when(objectMapper.convertValue(caseData.get(PETITION_ISSUE_ORDER_SUMMARY_JSON_KEY), OrderSummary.class))
            .thenReturn(orderSummary);
        when(serviceAuthGenerator.generate()).thenReturn(TEST_SERVICE_AUTH_TOKEN);

        when(paymentClient.creditAccountPayment(
            AUTH_TOKEN,
            TEST_SERVICE_AUTH_TOKEN,
            expectedRequest))
            .thenAnswer(withClientException(httpStatus, RESPONSE_DATA));
    }

    private void runCommonAssertions(String unprocessableEntity) {
        List<String> errorMessage = Optional.<List<String>>ofNullable(context.getTransientObject(SOLICITOR_PBA_PAYMENT_ERROR_KEY))
            .orElseGet(ArrayList::new);

        assertThat(context.hasTaskFailed(), is(true));
        assertThat(errorMessage, notNullValue());
        assertThat(errorMessage.get(0), is(unprocessableEntity));
    }

    private void runCommonVerifications() {
        verify(objectMapper).convertValue(caseData.get(PETITION_ISSUE_ORDER_SUMMARY_JSON_KEY), OrderSummary.class);
        verify(serviceAuthGenerator).generate();
        verify(paymentClient).creditAccountPayment(
            AUTH_TOKEN,
            TEST_SERVICE_AUTH_TOKEN,
            expectedRequest);
    }
}
