package uk.gov.hmcts.reform.divorce.orchestration.domain.model.payment;

import feign.FeignException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.HttpStatus;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.pay.CreditAccountPaymentResponse;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.pay.StatusHistoriesItem;
import uk.gov.hmcts.reform.divorce.orchestration.testutil.ObjectMapperTestUtil;
import uk.gov.hmcts.reform.divorce.orchestration.testutil.PbaClientErrorTestUtil;
import uk.gov.hmcts.reform.divorce.orchestration.util.payment.PbaClientError;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static uk.gov.hmcts.reform.divorce.orchestration.testutil.PbaClientErrorTestUtil.buildPaymentClientResponse;

@RunWith(MockitoJUnitRunner.class)
public class PbaClientErrorTest {

    private static final String TEST_REFERENCE = PbaClientErrorTestUtil.TEST_REFERENCE;
    private static final String CONTACT_DETAILS = " For Payment Account support call 01633 652125 (Option 3) "
        + "or email MiddleOffice.DDServices@liberata.com.";

    private CreditAccountPaymentResponse basicFailedResponse;

    @Before
    public void setUp() {
        basicFailedResponse = buildPaymentClientResponse("Failed", null, null);
    }

    @Test
    public void givenAnyClientError_ShouldReturnDefaultErrorMessage() {
        String errorMessage = PbaClientError.getErrorMessage(HttpStatus.INTERNAL_SERVER_ERROR, basicFailedResponse);

        assertThat(errorMessage, is(PbaClientError.getDefaultErrorMessage()));
        assertContactDetailsExists(errorMessage);
    }

    @Test
    public void given403_With_CAE0004_ErrorCode_ShouldReturnCorrectMessage() {
        CreditAccountPaymentResponse failedResponse = buildPaymentClientResponse("Failed", "CA-E0004",
            "Your account is deleted");

        String errorMessage = PbaClientError.getErrorMessage(HttpStatus.FORBIDDEN, failedResponse);
        String expectedContent = "Payment Account " + TEST_REFERENCE + " has been deleted or is on hold. "
            + "Please use a different account or payment method. For Payment Account support call ";

        assertThat(errorMessage, containsString(expectedContent));
        assertContactDetailsExists(errorMessage);
    }

    @Test
    public void given403_With_NoCAE0004_ErrorCode_ShouldReturnCorrectMessage() {
        CreditAccountPaymentResponse failedResponse = buildPaymentClientResponse("Failed", "CA-E0004",
            "Your account is deleted");
        failedResponse.getStatusHistories().set(0, StatusHistoriesItem.builder().build());

        String errorMessage = PbaClientError.getErrorMessage(HttpStatus.FORBIDDEN, failedResponse);

        assertThat(errorMessage, is(PbaClientError.getDefaultErrorMessage()));
        assertThat(errorMessage, containsString("Please use a different account or payment method. For Payment Account support call "));
        assertContactDetailsExists(errorMessage);
    }

    @Test
    public void given403_With_CAE0001_ErrorCode_ShouldReturnCorrectMessage() {
        CreditAccountPaymentResponse failedResponse = buildPaymentClientResponse("Failed",
            "CA-E0001",
            "Payment request failed. PBA account BATCHELORS SOLICITORS have insufficient funds available");

        String errorMessage = PbaClientError.getErrorMessage(HttpStatus.FORBIDDEN, failedResponse);
        String expectedContent = "Fee account " + TEST_REFERENCE + " has insufficient funds available. "
            + "Please use a different account or payment method.";

        assertThat(errorMessage, containsString(expectedContent));
        assertContactDetailsExists(errorMessage);
    }

    @Test
    public void given404_ErrorStatus_ShouldReturnCorrectMessage() {
        String errorMessage = PbaClientError.getErrorMessage(HttpStatus.NOT_FOUND, basicFailedResponse);
        String expectedContent = "Payment Account " + TEST_REFERENCE + " cannot be found. Please use a different account or payment method.";

        assertThat(errorMessage, containsString(expectedContent));
        assertContactDetailsExists(errorMessage);
    }

    @Test
    public void given422_ErrorStatus_ShouldReturnCorrectMessage() {
        String errorMessage = PbaClientError.getErrorMessage(HttpStatus.UNPROCESSABLE_ENTITY, basicFailedResponse);

        assertThat(errorMessage, is(PbaClientError.getDefaultErrorMessage()));
        assertContactDetailsExists(errorMessage);
    }

    @Test
    public void given502_ErrorStatus_ShouldReturnCorrectMessage() {
        String errorMessage = PbaClientError.getErrorMessage(HttpStatus.BAD_GATEWAY, basicFailedResponse);

        assertThat(errorMessage, is(PbaClientError.getDefaultErrorMessage()));
        assertContactDetailsExists(errorMessage);
    }

    @Test
    public void getCreditAccountPaymentResponse_ReturnsValidErrorResponse() {
        String testStatus = "test.status";
        byte[] body = ObjectMapperTestUtil.convertObjectToJsonString(
            CreditAccountPaymentResponse.builder()
                .status(testStatus)
                .build())
            .getBytes();

        CreditAccountPaymentResponse errorMessage = PbaClientError.getPaymentResponse(
            new FeignException.FeignClientException(HttpStatus.NOT_FOUND.value(), "errorMessage", body));

        assertThat(errorMessage.getStatus(), is(testStatus));
    }

    private void assertContactDetailsExists(String errorMessage) {
        assertThat(errorMessage, containsString(CONTACT_DETAILS));
    }
}