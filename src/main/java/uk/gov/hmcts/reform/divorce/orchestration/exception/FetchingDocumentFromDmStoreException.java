package uk.gov.hmcts.reform.divorce.orchestration.exception;

public class FetchingDocumentFromDmStoreException extends RuntimeException {
    public FetchingDocumentFromDmStoreException(String message) {
        super(message);
    }

    public FetchingDocumentFromDmStoreException(String message, Exception exception) {
        super(message, exception);
    }
}
