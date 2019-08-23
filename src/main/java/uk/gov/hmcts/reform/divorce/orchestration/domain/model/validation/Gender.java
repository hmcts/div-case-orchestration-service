package uk.gov.hmcts.reform.divorce.orchestration.domain.model.validation;

import com.fasterxml.jackson.annotation.JsonValue;

public enum Gender {
    FEMALE("female"),
    MALE("male"),
    NOT_GIVEN("notGiven");

    private final String value;

    Gender(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }
}
