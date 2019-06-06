package uk.gov.hmcts.reform.divorce.orchestration.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.divorce.orchestration.config.CourtDetailsConfig;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.courts.Court;
import uk.gov.hmcts.reform.divorce.orchestration.exception.CourtDetailsNotFound;

import java.util.Map;

@Service
public class CourtLookupService {

    private final Map<String, Court> courts;

    @Autowired
    public CourtLookupService(CourtDetailsConfig courtDetailsConfig) {
        courts = courtDetailsConfig.getLocations();
    }

    public Court getCourtByKey(String divorceUnitKey) throws CourtDetailsNotFound {
        if (!courts.containsKey(divorceUnitKey)) {
            throw new CourtDetailsNotFound(divorceUnitKey);
        }

        return courts.get(divorceUnitKey);
    }
}
