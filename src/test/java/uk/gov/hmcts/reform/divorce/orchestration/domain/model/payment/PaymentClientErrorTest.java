package uk.gov.hmcts.reform.divorce.orchestration.domain.model.payment;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.pay.CreditAccountPaymentResponse;
import uk.gov.hmcts.reform.divorce.orchestration.testutil.PbaClientErrorTestUtil;
import uk.gov.hmcts.reform.divorce.orchestration.util.payment.PaymentClientError;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static uk.gov.hmcts.reform.divorce.orchestration.testutil.PbaClientErrorTestUtil.buildFailedResponse;

@RunWith(MockitoJUnitRunner.class)
public class PaymentClientErrorTest {

    private static final String TEST_REFERENCE = PbaClientErrorTestUtil.TEST_REFERENCE;

    @Test
    public void givenAnyClientError_ShouldReturnDefaultErrorMessage() {
        CreditAccountPaymentResponse failedResponse = buildFailedResponse("Failed", null, null);

        String errorMessage = PaymentClientError.getMessage(500, failedResponse);

        assertThat(errorMessage, is(PaymentClientError.getDefault()));
    }

    @Test
    public void given403_With_CAE0004_ErrorCode_ShouldReturnCorrectMessage() {
        CreditAccountPaymentResponse failedResponse = buildFailedResponse("Failed", "CA-E0004",
            "Your account is deleted");

        String errorMessage = PaymentClientError.getMessage(403, failedResponse);

        String expectedContent = "Payment Account " + TEST_REFERENCE + " has been deleted or is on hold.";
        assertThat(errorMessage, containsString(expectedContent));
    }

    @Test
    public void given403_With_CAE0001_ErrorCode_ShouldReturnCorrectMessage() {
        CreditAccountPaymentResponse failedResponse = buildFailedResponse("Failed",
            "CA-E0001",
            "Payment request failed. PBA account BATCHELORS SOLICITORS have insufficient funds available");

        String errorMessage = PaymentClientError.getMessage(403, failedResponse);

        String expectedContent = "Fee account " + TEST_REFERENCE + " has insufficient funds available";
        assertThat(errorMessage, containsString(expectedContent));
    }

    @Test
    public void given404_ErrorStatus_ShouldReturnCorrectMessage() {
        CreditAccountPaymentResponse failedResponse = buildFailedResponse("Failed", null, null);

        String errorMessage = PaymentClientError.getMessage(404, failedResponse);

        String expectedContent = "Payment Account " + TEST_REFERENCE + " cannot be found. Please use a different account or payment method.";
        assertThat(errorMessage, containsString(expectedContent));
    }

    @Test
    public void given422_ErrorStatus_ShouldReturnCorrectMessage() {
        CreditAccountPaymentResponse failedResponse = buildFailedResponse("Failed", null, null);

        String errorMessage = PaymentClientError.getMessage(422, failedResponse);

        assertThat(errorMessage, is(PaymentClientError.getDefault()));
    }

    @Test
    public void given502_ErrorStatus_ShouldReturnCorrectMessage() {
        CreditAccountPaymentResponse failedResponse = buildFailedResponse("Failed", null, null);

        String errorMessage = PaymentClientError.getMessage(504, failedResponse);

        assertThat(errorMessage, is(PaymentClientError.getDefault()));
    }

}