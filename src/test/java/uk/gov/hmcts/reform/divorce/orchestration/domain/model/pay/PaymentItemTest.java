package uk.gov.hmcts.reform.divorce.orchestration.domain.model.pay;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.fees.FeeResponse;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.fees.OrderSummary;

import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_FEE_AMOUNT;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_FEE_CODE;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_FEE_DESCRIPTION;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_FEE_VERSION;

public class PaymentItemTest {


    private static final String VALID_STRING = "150.50";
    private static final String NULL = null;

    private static final String INVALID_STRING_WORD = "NOT_A_NUMBER";
    private static final String INVALID_DIGIT_EXPONENT = "20e";
    private static final String INVALID_DIGIT_NEGATIVE = "-5";


    @InjectMocks
    private OrderSummary orderSummary;

    @Before
    public void setup() {
        FeeResponse feeResponse = FeeResponse.builder()
            .amount(TEST_FEE_AMOUNT)
            .feeCode(TEST_FEE_CODE)
            .version(TEST_FEE_VERSION)
            .description(TEST_FEE_DESCRIPTION)
            .build();
        orderSummary = new OrderSummary();
        orderSummary.add(feeResponse);

        PaymentItem paymentItem = new PaymentItem();

        paymentItem.setCalculatedAmount(orderSummary.getPaymentTotal());
    }

    @Test
    public void shouldSetCalculatedAmount_WithValidString() {
        PaymentItem paymentItem = new PaymentItem();
        paymentItem.setCalculatedAmount(orderSummary.getPaymentReference());

    }

    @Test
    public void shouldSetCalculatedAmount_AsNull() {

    }

    @Test
    public void shouldNotSetCalculatedAmount_WithWord() {

    }

    @Test
    public void shouldNotSetCalculatedAmount_AsExponent() {

    }

    @Test
    public void shouldNotSetCalculatedAmount_WithNegativeString() {

    }

}