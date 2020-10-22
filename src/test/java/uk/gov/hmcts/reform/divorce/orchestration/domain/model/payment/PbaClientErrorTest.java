package uk.gov.hmcts.reform.divorce.orchestration.domain.model.payment;

import feign.FeignException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.HttpStatus;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.pay.CreditAccountPaymentResponse;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.pay.StatusHistoriesItem;
import uk.gov.hmcts.reform.divorce.orchestration.util.payment.PbaClientError;
import uk.gov.hmcts.reform.divorce.orchestration.util.payment.PbaErrorMessage;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_SOLICITOR_ACCOUNT_NUMBER;
import static uk.gov.hmcts.reform.divorce.orchestration.testutil.PbaClientErrorTestUtil.buildException;
import static uk.gov.hmcts.reform.divorce.orchestration.testutil.PbaClientErrorTestUtil.buildExceptionWithOutResponseBody;
import static uk.gov.hmcts.reform.divorce.orchestration.testutil.PbaClientErrorTestUtil.buildPaymentClientResponse;
import static uk.gov.hmcts.reform.divorce.orchestration.testutil.PbaClientErrorTestUtil.formatMessage;
import static uk.gov.hmcts.reform.divorce.orchestration.testutil.PbaClientErrorTestUtil.getBasicFailedResponse;

@RunWith(MockitoJUnitRunner.class)
public class PbaClientErrorTest {

    private CreditAccountPaymentResponse basicFailedResponse;

    @Before
    public void setUp() {
        basicFailedResponse = getBasicFailedResponse();
    }

    @Test
    public void given_AnyClientError_ShouldReturnDefaultErrorMessage() {
        FeignException feignException = buildException(HttpStatus.BAD_REQUEST, basicFailedResponse);

        String pbaErrorMessage = PbaClientError.getMessage(TEST_SOLICITOR_ACCOUNT_NUMBER, feignException);

        assertThat(pbaErrorMessage, is(formatMessage(PbaErrorMessage.GENERAL)));
    }

    @Test
    public void given_403_With_CAE0004_ErrorCode_ShouldReturnCorrectMessage() {
        CreditAccountPaymentResponse failedResponse = buildPaymentClientResponse("Failed", "CA-E0004");
        FeignException feignException = buildException(HttpStatus.FORBIDDEN, failedResponse);

        String pbaErrorMessage = PbaClientError.getMessage(TEST_SOLICITOR_ACCOUNT_NUMBER, feignException);

        assertThat(pbaErrorMessage, is(formatMessage(PbaErrorMessage.CAE0004)));
    }

    @Test
    public void given_403_With_No_CAE0004_ErrorCode_ShouldReturnCorrectMessage() {
        CreditAccountPaymentResponse failedResponse = buildPaymentClientResponse("Failed", "CA-E0004");
        failedResponse.getStatusHistories().set(0, StatusHistoriesItem.builder().build());
        FeignException feignException = buildException(HttpStatus.FORBIDDEN, failedResponse);

        String pbaErrorMessage = PbaClientError.getMessage(TEST_SOLICITOR_ACCOUNT_NUMBER, feignException);

        assertThat(pbaErrorMessage, is(formatMessage(PbaErrorMessage.GENERAL)));
    }

    @Test
    public void given_403_With_CAE0001_ErrorCode_ShouldReturnCorrectMessage() {
        CreditAccountPaymentResponse failedResponse = buildPaymentClientResponse("Failed", "CA-E0001");
        FeignException feignException = buildException(HttpStatus.FORBIDDEN, failedResponse);

        String pbaErrorMessage = PbaClientError.getMessage(TEST_SOLICITOR_ACCOUNT_NUMBER, feignException);

        assertThat(pbaErrorMessage, is(formatMessage(PbaErrorMessage.CAE0001)));
    }

    @Test
    public void given_404_ErrorStatus_ShouldReturnCorrectMessage() {
        FeignException feignException = buildException(HttpStatus.NOT_FOUND, basicFailedResponse);

        String pbaErrorMessage = PbaClientError.getMessage(TEST_SOLICITOR_ACCOUNT_NUMBER, feignException);

        assertThat(pbaErrorMessage, is(formatMessage(PbaErrorMessage.NOTFOUND)));
    }

    @Test
    public void given_422_ErrorStatus_ShouldReturnCorrectMessage() {
        FeignException feignException = buildException(HttpStatus.UNPROCESSABLE_ENTITY, basicFailedResponse);

        String pbaErrorMessage = PbaClientError.getMessage(TEST_SOLICITOR_ACCOUNT_NUMBER, feignException);

        assertThat(pbaErrorMessage, is(formatMessage(PbaErrorMessage.GENERAL)));
    }

    @Test
    public void given_502_ErrorStatus_ShouldReturnCorrectMessage() {
        FeignException feignException = buildException(HttpStatus.BAD_GATEWAY, basicFailedResponse);

        String pbaErrorMessage = PbaClientError.getMessage(TEST_SOLICITOR_ACCOUNT_NUMBER, feignException);

        assertThat(pbaErrorMessage, is(formatMessage(PbaErrorMessage.GENERAL)));
    }

    @Test
    public void given_AnyError_WhenNoResponseBody_ShouldReturnDefaultMessage() {
        FeignException feignException = buildExceptionWithOutResponseBody(HttpStatus.BAD_GATEWAY);

        String pbaErrorMessage = PbaClientError.getMessage(TEST_SOLICITOR_ACCOUNT_NUMBER, feignException);

        assertThat(pbaErrorMessage, is(formatMessage(PbaErrorMessage.GENERAL)));
    }

}