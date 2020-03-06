package uk.gov.hmcts.reform.divorce.orchestration.domain.model.facts;

import lombok.Getter;

import java.util.Arrays;

@Getter
public enum DivorceFacts {
    UNREASONABLE_BEHAVIOUR("unreasonable-behaviour"),
    SEPARATION_TWO_YEARS("separation-2-years"),
    SEPARATION_FIVE_YEARS("separation-5-years"),
    DESERTION("desertion"),
    ADULTERY("adultery");

    private final String value;
    private static final String error = "No divorce fact for %s";

    DivorceFacts(String value) {
        this.value = value;
    }

    public static DivorceFacts getDivorceFact(String reasonForDivorce) {
        return Arrays.stream(DivorceFacts.values())
                .filter(fact -> fact.getValue().equals(reasonForDivorce)).findFirst()
                .orElseThrow(() -> new IllegalArgumentException(String.format(error, reasonForDivorce)));
    }
}