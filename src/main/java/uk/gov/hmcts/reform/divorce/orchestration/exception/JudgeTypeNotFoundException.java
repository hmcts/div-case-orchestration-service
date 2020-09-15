package uk.gov.hmcts.reform.divorce.orchestration.exception;

import static java.lang.String.format;

public class JudgeTypeNotFoundException extends Exception {
    public JudgeTypeNotFoundException(String judgeTypeCode) {
        super(format("Could not find court by using key '%s'", judgeTypeCode));
    }
}
