package uk.gov.hmcts.reform.divorce.orchestration.domain.model.payment;

import feign.FeignException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.HttpStatus;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.pay.CreditAccountPaymentResponse;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.pay.StatusHistoriesItem;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskException;
import uk.gov.hmcts.reform.divorce.orchestration.testutil.ObjectMapperTestUtil;
import uk.gov.hmcts.reform.divorce.orchestration.testutil.PbaClientErrorTestUtil;
import uk.gov.hmcts.reform.divorce.orchestration.util.payment.PaymentClientError;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThrows;
import static uk.gov.hmcts.reform.divorce.orchestration.testutil.PbaClientErrorTestUtil.buildFailedResponse;

@RunWith(MockitoJUnitRunner.class)
public class PaymentClientErrorTest {

    private static final String TEST_REFERENCE = PbaClientErrorTestUtil.TEST_REFERENCE;
    private CreditAccountPaymentResponse basicFailedResponse;

    @Before
    public void setUp() {
        basicFailedResponse = buildFailedResponse("Failed", null, null);
    }

    @Test
    public void givenAnyClientError_ShouldReturnDefaultErrorMessage() {
        String errorMessage = PaymentClientError.getMessage(HttpStatus.INTERNAL_SERVER_ERROR, basicFailedResponse);

        assertThat(errorMessage, is(PaymentClientError.getDefault()));
    }

    @Test
    public void given403_With_CAE0004_ErrorCode_ShouldReturnCorrectMessage() {
        CreditAccountPaymentResponse failedResponse = buildFailedResponse("Failed", "CA-E0004",
            "Your account is deleted");

        String errorMessage = PaymentClientError.getMessage(HttpStatus.FORBIDDEN, failedResponse);
        String expectedContent = "Payment Account " + TEST_REFERENCE + " has been deleted or is on hold.";

        assertThat(errorMessage, containsString(expectedContent));
    }

    @Test
    public void given403_With_NoCAE0004_ErrorCode_ShouldReturnCorrectMessage() {
        CreditAccountPaymentResponse failedResponse = buildFailedResponse("Failed", "CA-E0004",
            "Your account is deleted");
        failedResponse.getStatusHistories().set(0, StatusHistoriesItem.builder().build());

        String errorMessage = PaymentClientError.getMessage(HttpStatus.FORBIDDEN, failedResponse);

        assertThat(errorMessage, is(PaymentClientError.getDefault()));
    }

    @Test
    public void given403_With_CAE0001_ErrorCode_ShouldReturnCorrectMessage() {
        CreditAccountPaymentResponse failedResponse = buildFailedResponse("Failed",
            "CA-E0001",
            "Payment request failed. PBA account BATCHELORS SOLICITORS have insufficient funds available");

        String errorMessage = PaymentClientError.getMessage(HttpStatus.FORBIDDEN, failedResponse);
        String expectedContent = "Fee account " + TEST_REFERENCE + " has insufficient funds available";

        assertThat(errorMessage, containsString(expectedContent));
    }

    @Test
    public void given404_ErrorStatus_ShouldReturnCorrectMessage() {
        String errorMessage = PaymentClientError.getMessage(HttpStatus.NOT_FOUND, basicFailedResponse);
        String expectedContent = "Payment Account " + TEST_REFERENCE + " cannot be found. Please use a different account or payment method.";

        assertThat(errorMessage, containsString(expectedContent));
    }

    @Test
    public void given422_ErrorStatus_ShouldReturnCorrectMessage() {
        String errorMessage = PaymentClientError.getMessage(HttpStatus.UNPROCESSABLE_ENTITY, basicFailedResponse);

        assertThat(errorMessage, is(PaymentClientError.getDefault()));
    }

    @Test
    public void given502_ErrorStatus_ShouldReturnCorrectMessage() {
        String errorMessage = PaymentClientError.getMessage(HttpStatus.BAD_GATEWAY, basicFailedResponse);

        assertThat(errorMessage, is(PaymentClientError.getDefault()));
    }

    @Test
    public void getCreditAccountPaymentResponse_ReturnsValidErrorResponse() {
        String testStatus = "test.status";
        byte[] body = ObjectMapperTestUtil.convertObjectToJsonString(
            CreditAccountPaymentResponse.builder()
                .status(testStatus)
                .build()).getBytes();

        CreditAccountPaymentResponse errorMessage = PaymentClientError.getCreditAccountPaymentResponse(
            new FeignException.FeignClientException(HttpStatus.NOT_FOUND.value(), "errorMessage", body));

        assertThat(errorMessage.getStatus(), is(testStatus));
    }

    @Test
    public void getCreditAccountPaymentResponse_ReturnsInValidErrorResponse_Throws() {
        assertThrows(TaskException.class, () -> PaymentClientError.getCreditAccountPaymentResponse(
            new FeignException.FeignClientException(HttpStatus.NOT_FOUND.value(), "errorMessage", "body".getBytes())));
    }
}