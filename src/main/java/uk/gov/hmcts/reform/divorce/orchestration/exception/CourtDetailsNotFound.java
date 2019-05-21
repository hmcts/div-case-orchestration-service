package uk.gov.hmcts.reform.divorce.orchestration.exception;

import static java.lang.String.format;

public class CourtDetailsNotFound extends Exception {
    public CourtDetailsNotFound(String divorceUnitKey) {
        super(format("Could not find court by using key \"%s\"", divorceUnitKey));
    }
}
