package uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd;

import com.fasterxml.jackson.annotation.JsonValue;

import java.util.Locale;

public enum Gender {
    FEMALE("female"),
    MALE("male");

    private final String value;

    Gender(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }

    public static Gender from(String gender) {
        if (gender == null) {
            return null;
        }
        gender = gender.toLowerCase(Locale.ENGLISH);

        if (gender.equals(MALE.getValue())) {
            return Gender.MALE;
        } else if (gender.equals(FEMALE.getValue())) {
            return Gender.FEMALE;
        }

        return null;
    }
}
