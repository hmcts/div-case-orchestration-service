package uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd;

public enum DNCostOptionsEnum {
    ORIGINALAMOUNT("originalAmount", "I still want to claim my costs and will let the court decide how much"),
    DIFFERENTAMOUNT("differentAmount", "I want to claim a specific amount"),
    ENDCLAIM("endClaim", "I don't want to claim my costs anymore");

    private String code;
    private String label;

    DNCostOptionsEnum(String code, String label) {
        this.code = code;
        this.label = label;
    }

    public String getCode() {
        return code;
    }

    public String getLabel() {
        return label;
    }
}