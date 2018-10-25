package uk.gov.hmcts.reform.divorce.orchestration.exception;

public class InvalidPropertyException extends Exception {

    public InvalidPropertyException(String keyValue) {
        super(String.format("Could not evaluate value of property \"%s\"", keyValue));
    }

}
