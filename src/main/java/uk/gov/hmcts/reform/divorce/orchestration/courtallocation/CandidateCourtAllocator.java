package uk.gov.hmcts.reform.divorce.orchestration.courtallocation;

import java.util.Map;
import java.util.Optional;

public class CandidateCourtAllocator implements CourtAllocator {

    public CandidateCourtAllocator(Map<String, Double> caseDistribution, Map<String, Map> courts) {
    }

    @Override
    public String selectCourtForGivenDivorceReason(Optional<String> reasonForDivorce) {
        return null;
    }

}