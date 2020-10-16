package uk.gov.hmcts.reform.divorce.orchestration.domain.model.pay;

import java.util.Arrays;

public enum PaymentStatus {

    SUCCESS("Success"),
    PENDING("Pending"),
    FAILED("Failed");

    private final String status;

    PaymentStatus(String paymentStatus) {
        this.status = paymentStatus;
    }

    public static PaymentStatus getStatus(String status) {
        return Arrays.stream(PaymentStatus.values())
            .filter(s -> s.status.equals(status))
            .findFirst()
            .orElseThrow(() -> new IllegalArgumentException("Unknown payment status"));
    }

    public String value() {
        return status;
    }
}
