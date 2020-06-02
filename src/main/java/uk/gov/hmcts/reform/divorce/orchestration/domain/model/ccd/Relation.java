package uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd;

import com.fasterxml.jackson.annotation.JsonValue;

public enum Relation {
    WIFE("wife"),
    HUSBAND("husband");

    private final String value;

    Relation(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }
}
