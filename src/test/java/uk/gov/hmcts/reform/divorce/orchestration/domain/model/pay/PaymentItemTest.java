package uk.gov.hmcts.reform.divorce.orchestration.domain.model.pay;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.fees.FeeResponse;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.fees.OrderSummary;

import static junit.framework.TestCase.assertNull;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertEquals;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_FEE_AMOUNT;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_FEE_CODE;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_FEE_DESCRIPTION;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_FEE_VERSION;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_SOLICITOR_REFERENCE;

@RunWith(MockitoJUnitRunner.class)
public class PaymentItemTest {

    private static PaymentItem paymentItem = new PaymentItem();
    private static final String VALID_STRING = "150.50";
    private static final String INVALID_STRING_WORD = "NOT_A_NUMBER";
    private static final String INVALID_DIGIT_EXPONENT = "e";
    private static final String INVALID_DIGIT_NEGATIVE = "-5";

    @InjectMocks
    private OrderSummary orderSummary;

    @Rule
    public ExpectedException thrown = ExpectedException.none();

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

    @Test
    public void shouldSetCalculatedAmount_WithValidString() {
        paymentItem.setCalculatedAmount(VALID_STRING);
        assertEquals(paymentItem.getCalculatedAmount(), convertPoundsToPennies(VALID_STRING));
    }

    @Test
    public void shouldSetCalculatedAmount_AsNull() {
        paymentItem.setCalculatedAmount(null);
        assertNull(paymentItem.getCalculatedAmount());
    }

    @Test
    public void shouldSetCalculatedAmount_WithNegativeString() {
        paymentItem.setCalculatedAmount(INVALID_DIGIT_NEGATIVE);
        assertEquals(paymentItem.getCalculatedAmount(), convertPoundsToPennies(INVALID_DIGIT_NEGATIVE));
    }

    @Test
    public void shouldThrowException_WhenValueIsNotANumber() {
        thrown.expect(NumberFormatException.class);
        thrown.expectMessage(is("For input string: \"" + INVALID_STRING_WORD + "\""));
        paymentItem.setCalculatedAmount(INVALID_STRING_WORD);
    }

    @Test
    public void shouldThrowException_AsExponent() {
        thrown.expect(NumberFormatException.class);
        thrown.expectMessage(is("For input string: \"" + INVALID_DIGIT_EXPONENT + "\""));
        paymentItem.setCalculatedAmount(INVALID_DIGIT_EXPONENT);
    }

    private String convertPoundsToPennies(String amount) {
        double amountInPennies = Double.parseDouble(amount);
        return String.valueOf(amountInPennies / 100);
    }

}