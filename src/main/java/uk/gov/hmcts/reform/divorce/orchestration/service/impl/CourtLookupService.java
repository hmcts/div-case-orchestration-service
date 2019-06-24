package uk.gov.hmcts.reform.divorce.orchestration.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.divorce.orchestration.config.CourtDetailsConfig;
import uk.gov.hmcts.reform.divorce.orchestration.config.DnCourtDetailsConfig;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.courts.Court;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.courts.DnCourt;
import uk.gov.hmcts.reform.divorce.orchestration.exception.CourtDetailsNotFound;

import java.util.Map;

@Service
public class CourtLookupService {

    private final Map<String, Court> courts;
    private final Map<String, DnCourt> dnCourts;

    @Autowired
    public CourtLookupService(CourtDetailsConfig courtDetailsConfig, DnCourtDetailsConfig dnCourtDetailsConfig) {
        courts = courtDetailsConfig.getLocations();
        dnCourts = dnCourtDetailsConfig.getLocations();
    }

    public Court getCourtByKey(String divorceUnitKey) throws CourtDetailsNotFound {
        if (!courts.containsKey(divorceUnitKey)) {
            throw new CourtDetailsNotFound(divorceUnitKey);
        }

        return courts.get(divorceUnitKey);
    }

    public DnCourt getDnCourtByKey(String courtId) throws CourtDetailsNotFound {
        if (!dnCourts.containsKey(courtId)) {
            throw new CourtDetailsNotFound(courtId);
        }

        return dnCourts.get(courtId);
    }
}
