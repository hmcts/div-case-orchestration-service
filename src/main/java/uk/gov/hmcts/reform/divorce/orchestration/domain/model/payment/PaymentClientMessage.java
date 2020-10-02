package uk.gov.hmcts.reform.divorce.orchestration.domain.model.payment;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpStatus;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class PaymentClientMessage {
    // TODO complete message content
    private static final String CONTACT_INFO = "Please use a different account or speak to XXXX....";

    public static final String DEFAULT = "Payment was not successfully processed.";
    // TODO logic to return message based on error code
    public static final String FORBIDDEN_CA_E0004 = "The Fee Account you selected is not active. " + CONTACT_INFO;
    public static final String FORBIDDEN_CA_E001 = "There are insufficient funds in this account. " + CONTACT_INFO;
    public static final String NOT_FOUND = "The Fee account you selected is not active. " + CONTACT_INFO;
    public static final String UNPROCESSABLE_ENTITY = DEFAULT;
    public static final String GATEWAY_TIMEOUT = DEFAULT;

    public static String getValue(int httpStatus) { // TODO encapsulate
        if (httpStatus == HttpStatus.FORBIDDEN.value()) {
            return PaymentClientMessage.FORBIDDEN_CA_E0004; //TODO find out error message structure to extrach error code
        } else if (httpStatus == HttpStatus.NOT_FOUND.value()) {
            return PaymentClientMessage.NOT_FOUND;
        }
        return PaymentClientMessage.DEFAULT;
    }

    public static String getValue(HttpStatus httpStatus) { // TODO encapsulate
        return getValue(httpStatus.value());
    }
}
