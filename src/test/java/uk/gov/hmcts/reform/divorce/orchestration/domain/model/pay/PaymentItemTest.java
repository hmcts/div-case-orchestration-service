package uk.gov.hmcts.reform.divorce.orchestration.domain.model.pay;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.fees.FeeResponse;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.fees.OrderSummary;

import static org.junit.Assert.*;
import static org.hamcrest.core.Is.is;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_FEE_AMOUNT;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_FEE_CODE;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_FEE_DESCRIPTION;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_FEE_VERSION;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_SOLICITOR_REFERENCE;

public class PaymentItemTest {

    private static final String VALID_STRING = "150.50";
    private static final String NULL = null;
    private static final String INVALID_STRING_WORD = "NOT_A_NUMBER";
    private static final String INVALID_DIGIT_EXPONENT = "20e";
    private static final String INVALID_DIGIT_NEGATIVE = "-5";

    @InjectMocks
    private OrderSummary orderSummary;
    private static PaymentItem paymentItem = new PaymentItem();

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
        paymentItem = new PaymentItem();
        paymentItem.setCcdCaseNumber(TEST_CASE_ID);
        paymentItem.setCode(TEST_FEE_CODE);
        paymentItem.setReference(TEST_SOLICITOR_REFERENCE);
        paymentItem.setVersion(TEST_FEE_VERSION.toString());
    }

    private String convertStringToPennies(String amount) {
        double amountAsDouble = Double.parseDouble(amount);
        String amountConvertedToPennies = String.valueOf(amountAsDouble / 100);
        return amountConvertedToPennies;
    }

    @Test
    public void shouldSetCalculatedAmount_WithValidString() {
        paymentItem.setCalculatedAmount(VALID_STRING);
        String amount = convertStringToPennies(VALID_STRING);
        assertEquals(paymentItem.getCalculatedAmount(), amount);
    }

    @Test
    public void shouldSetCalculatedAmount_AsNull() {
        paymentItem.setCalculatedAmount(NULL);
        assertEquals(paymentItem.getCalculatedAmount(), NULL);
    }

    @Test(expected = NumberFormatException.class)
    public void shouldNotSetCalculatedAmount_WithWord() {
        paymentItem.setCalculatedAmount(INVALID_STRING_WORD);
        assertThat(paymentItem.getCalculatedAmount(), is(INVALID_STRING_WORD));
    }

    @Test(expected = NumberFormatException.class)
    public void shouldNotSetCalculatedAmount_AsExponent() {
        paymentItem.setCalculatedAmount(INVALID_DIGIT_EXPONENT);
        assertThat(paymentItem.getCalculatedAmount(), is(INVALID_DIGIT_EXPONENT));
    }

    @Test
    public void shouldNotSetCalculatedAmount_WithNegativeString() {
        paymentItem.setCalculatedAmount(INVALID_DIGIT_NEGATIVE);
        assertNotEquals(paymentItem.getCalculatedAmount(), INVALID_DIGIT_NEGATIVE);
    }

}