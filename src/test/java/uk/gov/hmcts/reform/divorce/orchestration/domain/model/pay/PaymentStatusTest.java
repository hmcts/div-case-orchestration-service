package uk.gov.hmcts.reform.divorce.orchestration.domain.model.pay;

import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThrows;

public class PaymentStatusTest {

    @Test
    public void shouldGetRightEnumByString() {
        assertThat(PaymentStatus.getStatus("Success"), equalTo(PaymentStatus.SUCCESS));
        assertThat(PaymentStatus.getStatus("Failed"), equalTo(PaymentStatus.FAILED));
        assertThat(PaymentStatus.getStatus("Pending"), equalTo(PaymentStatus.PENDING));
    }

    @Test
    public void shouldThrowExceptionWhenStatusIsInvalid() {
        IllegalArgumentException illegalArgumentException = assertThrows(
            IllegalArgumentException.class,
            () -> PaymentStatus.getStatus("Unknown")
        );

        assertThat(
            illegalArgumentException.getMessage(),
            is("Unknown payment status")
        );
    }

}