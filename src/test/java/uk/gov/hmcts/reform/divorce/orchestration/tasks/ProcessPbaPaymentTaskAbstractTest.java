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
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.Features;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.courts.CourtEnum;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.fees.FeeResponse;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.fees.OrderSummary;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.pay.CreditAccountPaymentRequest;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.pay.CreditAccountPaymentResponse;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.pay.PaymentItem;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.pay.PaymentStatus;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.DefaultTaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskException;
import uk.gov.hmcts.reform.divorce.orchestration.service.FeatureToggleService;
import uk.gov.hmcts.reform.divorce.orchestration.testutil.ObjectMapperTestUtil;
import uk.gov.hmcts.reform.divorce.orchestration.util.payment.PbaErrorMessage;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static java.lang.String.format;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
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
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.SOLICITOR_FIRM_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.SOLICITOR_HOW_TO_PAY_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.SOLICITOR_PBA_PAYMENT_ERROR_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.SOLICITOR_REFERENCE_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.testutil.PbaClientErrorTestUtil.buildPaymentClientResponse;
import static uk.gov.hmcts.reform.divorce.orchestration.testutil.PbaClientErrorTestUtil.formatMessage;
import static uk.gov.hmcts.reform.divorce.orchestration.testutil.PbaClientErrorTestUtil.getBasicFailedResponse;

@RunWith(MockitoJUnitRunner.class)
public abstract class ProcessPbaPaymentTaskAbstractTest {

    @Mock
    private PaymentClient paymentClient;

    @Mock
    private AuthTokenGenerator serviceAuthGenerator;

    @Mock
    private ResponseEntity<CreditAccountPaymentResponse> responseEntity;

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    protected FeatureToggleService featureToggleService;

    @InjectMocks
    private ProcessPbaPaymentTask processPbaPaymentTask;

    private TaskContext context;
    protected Map<String, Object> caseData;
    private CreditAccountPaymentRequest expectedRequest;
    private OrderSummary orderSummary;
    private CreditAccountPaymentResponse basicFailedResponse;
    private final String errorMessage = "Payment request failed";

    @Before()
    public void setUp() {
        context = new DefaultTaskContext();
        context.setTransientObject(AUTH_TOKEN_JSON_KEY, AUTH_TOKEN);
        context.setTransientObject(CASE_ID_JSON_KEY, TEST_CASE_ID);

        basicFailedResponse = getBasicFailedResponse();

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

    protected abstract void setPbaNumber();

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
    public void given403_Returned_whenExecuteIsCalled_thenHandle_CAE0001_Response() {
        CreditAccountPaymentResponse failedResponse = buildPaymentClientResponse("Failed", "CA-E0001");

        setUpCommonFixtures(HttpStatus.FORBIDDEN, failedResponse);

        processPbaPaymentTask.execute(context, caseData);

        runCommonAssertions(format(PbaErrorMessage.CAE0001.value(), TEST_SOLICITOR_ACCOUNT_NUMBER));
        runCommonVerifications();
    }

    @Test
    public void given201_AndPaymentStatus_Success_whenExecuteIsCalled_thenHandleResponse() {
        CreditAccountPaymentResponse successResponse = buildPaymentClientResponse("Success", null);

        setUpCommonFixtures(HttpStatus.OK, successResponse);

        Map<String, Object> caseData = processPbaPaymentTask.execute(context, this.caseData);

        assertThat(caseData.get(ProcessPbaPaymentTask.PAYMENT_STATUS), is(PaymentStatus.SUCCESS.value()));
        runCommonVerifications();
    }

    @Test
    public void given201_AndPaymentStatus_Pending_whenExecuteIsCalled_thenHandleResponse() {
        CreditAccountPaymentResponse successResponse = buildPaymentClientResponse("Pending", null);

        setUpCommonFixtures(HttpStatus.CREATED, successResponse);

        Map<String, Object> caseData = processPbaPaymentTask.execute(context, this.caseData);

        assertThat(caseData.get(ProcessPbaPaymentTask.PAYMENT_STATUS), nullValue());
        runCommonVerifications();
    }

    @Test
    public void given403_Returned_whenExecuteIsCalled_thenHandle_CAE0004_Response() {
        CreditAccountPaymentResponse failedResponse = buildPaymentClientResponse("Failed", "CA-E0004");

        setUpCommonFixtures(HttpStatus.FORBIDDEN, failedResponse);

        processPbaPaymentTask.execute(context, caseData);

        runCommonAssertions(formatMessage(PbaErrorMessage.CAE0004));
        runCommonVerifications();
    }

    @Test
    public void given404_Returned_whenExecuteIsCalled_thenHandleResponse() {

        setUpCommonFixtures(HttpStatus.NOT_FOUND, basicFailedResponse);

        processPbaPaymentTask.execute(context, caseData);

        runCommonAssertions(formatMessage(PbaErrorMessage.NOTFOUND));
        runCommonVerifications();
    }

    @Test
    public void given422_Returned_whenExecuteIsCalled_thenHandleResponse() {

        setUpCommonFixtures(HttpStatus.UNPROCESSABLE_ENTITY, basicFailedResponse);

        processPbaPaymentTask.execute(context, caseData);

        runCommonAssertions(formatMessage(PbaErrorMessage.GENERAL));
        runCommonVerifications();
    }

    @Test
    public void given504_Returned_whenExecuteIsCalled_thenHandleResponse() {

        setUpCommonFixtures(HttpStatus.GATEWAY_TIMEOUT, basicFailedResponse);

        processPbaPaymentTask.execute(context, caseData);

        runCommonAssertions(formatMessage(PbaErrorMessage.GENERAL));
        runCommonVerifications();
    }

    @Test
    public void givenAnyOtherFailedStatus_Returned_whenExecuteIsCalled_thenHandleResponse() {

        setUpCommonFixtures(HttpStatus.BAD_REQUEST, basicFailedResponse);

        processPbaPaymentTask.execute(context, caseData);

        runCommonAssertions(formatMessage(PbaErrorMessage.GENERAL));
        runCommonVerifications();
    }

    @Test
    public void givenNotPayByAccount_whenExecuteIsCalled_thenReturnData() {
        caseData.replace(SOLICITOR_HOW_TO_PAY_JSON_KEY, "NotByAccount");

        assertThat(caseData, is(processPbaPaymentTask.execute(context, caseData)));

        verify(paymentClient, never()).creditAccountPayment(anyString(), anyString(), any(CreditAccountPaymentRequest.class));
        verify(serviceAuthGenerator, never()).generate();
    }

    @Test
    public void givenMissingData_whenExecuteIsCalled_thenThrowTaskException() {
        caseData = new HashMap<>();
        caseData.put(SOLICITOR_HOW_TO_PAY_JSON_KEY, FEE_PAY_BY_ACCOUNT);

        assertThrows(TaskException.class, () -> processPbaPaymentTask.execute(context, caseData));
    }

    private Answer<ResponseEntity<?>> withClientResponse(HttpStatus httpStatus, CreditAccountPaymentResponse paymentResponse) {
        byte[] body = ObjectMapperTestUtil.convertObjectToJsonString(paymentResponse).getBytes();
        return (invocation) -> {
            if (httpStatus.value() >= 400) {
                throw new FeignException.FeignClientException(httpStatus.value(), errorMessage, body);
            }
            return ResponseEntity.status(httpStatus).body(paymentResponse);
        };
    }

    private void setUpCommonFixtures(HttpStatus httpStatus, CreditAccountPaymentResponse paymentResponse) {
        when(objectMapper.convertValue(caseData.get(PETITION_ISSUE_ORDER_SUMMARY_JSON_KEY), OrderSummary.class))
            .thenReturn(orderSummary);
        when(serviceAuthGenerator.generate()).thenReturn(TEST_SERVICE_AUTH_TOKEN);

        when(paymentClient.creditAccountPayment(
            AUTH_TOKEN,
            TEST_SERVICE_AUTH_TOKEN,
            expectedRequest))
            .thenAnswer(withClientResponse(httpStatus, paymentResponse));
    }

    private void runCommonAssertions(String errorMessage) {
        List<String> errorMessages = Optional.<List<String>>ofNullable(context.getTransientObject(SOLICITOR_PBA_PAYMENT_ERROR_KEY))
            .orElseGet(ArrayList::new);

        assertThat(context.hasTaskFailed(), is(true));
        assertThat(errorMessages, notNullValue());
        assertThat(errorMessages.get(0), is(errorMessage));
    }

    private void runCommonVerifications() {
        verify(objectMapper).convertValue(caseData.get(PETITION_ISSUE_ORDER_SUMMARY_JSON_KEY), OrderSummary.class);
        verify(serviceAuthGenerator).generate();
        verify(paymentClient).creditAccountPayment(
            AUTH_TOKEN,
            TEST_SERVICE_AUTH_TOKEN,
            expectedRequest);
        verify(featureToggleService, times(2)).isFeatureEnabled(Features.PAY_BY_ACCOUNT);
    }

}
