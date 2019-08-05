package uk.gov.hmcts.reform.divorce.orchestration.domain.model.parties;

public class DivorcePartyNotFoundException extends Exception {

    public DivorcePartyNotFoundException(String key) {
        super("Could not find divorce party with the given description: " + key);
    }

}
