package uk.gov.hmcts.reform.divorce.orchestration.service.bulk.scan.exception;

public class FormFieldValidationException extends RuntimeException {

    public FormFieldValidationException(String validationErrorMessage) {
        super(validationErrorMessage);
    }

}