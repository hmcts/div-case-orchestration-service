package uk.gov.hmcts.reform.divorce.orchestration.domain.model.document;

public class ServiceRefusalDecisionNotFoundException extends IllegalArgumentException {

    public ServiceRefusalDecisionNotFoundException(String key) {
        super("Could not find refusal decision with the given description: " + key);
    }
}
