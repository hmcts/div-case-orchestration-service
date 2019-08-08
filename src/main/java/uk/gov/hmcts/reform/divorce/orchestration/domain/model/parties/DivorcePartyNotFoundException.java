package uk.gov.hmcts.reform.divorce.orchestration.domain.model.parties;

public class DivorcePartyNotFoundException extends IllegalArgumentException {

    public DivorcePartyNotFoundException(String key) {
        super("Could not find divorce party with the given description: " + key);
    }

}