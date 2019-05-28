package uk.gov.hmcts.reform.divorce.orchestration.exception;

import static java.lang.String.format;

public class BulkUpdateException extends RuntimeException {
    public BulkUpdateException(String message) {
        super(format("Unable to perform update on case within bulk case: \"%s\"", message));
    }

    public BulkUpdateException(String message, Exception exception) {
        super(format("Unable to perform update on case within bulk case: \"%s\"", message), exception);
    }
}
