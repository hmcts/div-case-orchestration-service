package uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd;

import com.fasterxml.jackson.annotation.JsonValue;

public enum GeneralOrderParty {
    PETITIONER("petitioner"),
    RESPONDENT("respondent"),
    CO_RESPONDENT("corespondent");

    private final String value;

    GeneralOrderParty(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }

    public static GeneralOrderParty from(String party) {
        if (party == null) {
            return null;
        }

        if (party.equals(PETITIONER.getValue())) {
            return GeneralOrderParty.PETITIONER;
        }

        if (party.equals(RESPONDENT.getValue())) {
            return GeneralOrderParty.RESPONDENT;
        }

        if (party.equals(CO_RESPONDENT.getValue())) {
            return GeneralOrderParty.CO_RESPONDENT;
        }

        return null;
    }
}
