package uk.gov.hmcts.reform.divorce.orchestration.util.payment;

import java.util.Arrays;

public enum PbaErrorMessage {

    CAE0001("Fee account %s has insufficient funds available. Please use a different account or payment method."),
    CAE0004("Payment Account %s has been deleted or is on hold. Please use a different account or payment method."),
    NOTFOUND("Payment Account %s cannot be found. Please use a different account or payment method."),
    GENERAL("Payment request failed. Please use a different account or payment method.");

    private final String message;

    PbaErrorMessage(String errorMessage) {
        String contactInfo = "For Payment Account support call 01633 652125 (Option 3) or email MiddleOffice.DDServices@liberata.com.";
        this.message = errorMessage + " " + contactInfo;
    }

    public static PbaErrorMessage getMessage(String enumName) {
        return Arrays.stream(PbaErrorMessage.values())
            .filter(s -> s.name().equals(enumName))
            .findFirst()
            .orElseThrow(() -> new IllegalArgumentException("Unknown error message"));
    }

    public String value() {
        return message;
    }
}
