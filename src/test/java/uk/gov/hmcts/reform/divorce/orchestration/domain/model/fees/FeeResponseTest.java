package uk.gov.hmcts.reform.divorce.orchestration.domain.model.fees;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class FeeResponseTest {

    @Test
    public void givenWholeNumberWithTwoDecimalDouble_whenGetFormattedFeeAmount_thenReturnFeeInExpectedFormat() {
        assertEquals("11", FeeResponse.builder().amount(11.00d).build().getFormattedFeeAmount());
    }

    @Test
    public void givenWholeNumberWithOneDecimalDouble_whenGetFormattedFeeAmount_thenReturnFeeInExpectedFormat() {
        assertEquals("11", FeeResponse.builder().amount(11.0d).build().getFormattedFeeAmount());
    }

    @Test
    public void givenNoDecimalDouble_whenGetFormattedFeeAmount_thenReturnFeeInExpectedFormat() {
        assertEquals("11", FeeResponse.builder().amount(11d).build().getFormattedFeeAmount());
    }

    @Test
    public void givenOneDecimalDouble_whenGetFormattedFeeAmount_thenReturnFeeInExpectedFormat() {
        assertEquals("11.10", FeeResponse.builder().amount(11.1d).build().getFormattedFeeAmount());
    }

    @Test
    public void givenTwoDecimalDouble_whenGetFormattedFeeAmount_thenReturnFeeInExpectedFormat() {
        assertEquals("11.11", FeeResponse.builder().amount(11.11d).build().getFormattedFeeAmount());
    }

    @Test
    public void givenMoreThanTwoDecimalDouble_whenGetFormattedFeeAmount_thenReturnFeeInExpectedFormat() {
        assertEquals("11.11", FeeResponse.builder().amount(11.11111111d).build().getFormattedFeeAmount());
    }
}
