package uk.gov.hmcts.reform.divorce.orchestration.courtallocation;

import java.util.Map;
import java.util.Optional;

public class PFEClonedCourtAllocator implements CourtAllocator {
    public PFEClonedCourtAllocator(Map caseDistribution, Map localCourts) {
    }

    @Override
    public String selectCourtForGivenDivorceReason(Optional<String> reasonForDivorce) {
        return null;
    }
}
