package uk.gov.hmcts.reform.divorce.orchestration.exception.bulk.scan;

import static java.lang.String.format;

public class UnsupportedFormTypeException extends RuntimeException {

    public UnsupportedFormTypeException(String formType) {
        super(format("\"%s\" form type is not supported", formType));
    }

}