package uk.gov.hmcts.reform.divorce.orchestration.util.payment;

import java.util.Arrays;

public enum PbaErrorMessage {

    CAE0001("Fee account %s has insufficient funds available."),
    CAE0004("Payment Account %s has been deleted or is on hold."),
    NOTFOUND("Payment Account %s cannot be found."),
    GENERAL("Payment request failed.");

    private final String message;

    public static final String ERROR_INFO = "Please try again after 2 minutes with a different Payment Account, or alternatively "
        + "use a different payment method. For Payment Account support call 01633 652125 (Option 3) or email MiddleOffice.DDServices@liberata.com.";

    PbaErrorMessage(String errorMessage) {
        this.message = errorMessage + " " + ERROR_INFO;
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
