package uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.extern.slf4j.Slf4j;

@Slf4j
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

    public static Relation from(Gender gender) {
        switch (gender) {
            case MALE: return HUSBAND;
            case FEMALE: return WIFE;
            default:
                log.warn("Missing gender");
                return null;
        }
    }
}
