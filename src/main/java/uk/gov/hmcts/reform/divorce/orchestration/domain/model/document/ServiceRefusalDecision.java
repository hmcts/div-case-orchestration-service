package uk.gov.hmcts.reform.divorce.orchestration.domain.model.document;

import java.util.Arrays;

public enum ServiceRefusalDecision {
    FINAL("final"),
    DRAFT("draft");

    private final String decision;

    ServiceRefusalDecision(String decision) {
        this.decision = decision;
    }

    public String getValue() {
        return decision;
    }

    public static ServiceRefusalDecision getDecisionByName(String decision) throws ServiceRefusalDecisionNotFoundException {
        return Arrays.stream(ServiceRefusalDecision.values())
            .filter(d -> d.decision.equals(decision))
            .findFirst()
            .orElseThrow(() -> new ServiceRefusalDecisionNotFoundException(decision));
    }

}
