package uk.gov.hmcts.reform.divorce.orchestration.courtallocation;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Optional;

public class CandidateCourtAllocator implements CourtAllocator {

    private final Map<String, Double> divorceRatioPerFact;
    private final Map<String, Map<String, BigDecimal>> specificCourtsAllocationPerFact;

    //TODO - fill up to 100% and have remainder assigned to empty Optional court
    //TODO - in this case the fact specific distributor would have to know about the generic one
    private final FactSpecificCourtWeightedDistributor factSpecificCourtWeightedDistributor;//TODO - maybe these could share interface
    private final GenericCourtWeightedDistributor genericCourtWeightedDistributor;

    public CandidateCourtAllocator(Map<String, BigDecimal> desiredWorkloadPerCourt,
                                   Map<String, Double> divorceRatioPerFact,
                                   Map<String, Map<String, BigDecimal>> specificCourtsAllocationPerFact) {
        this.divorceRatioPerFact = divorceRatioPerFact;
        this.specificCourtsAllocationPerFact = specificCourtsAllocationPerFact;

        this.factSpecificCourtWeightedDistributor = new FactSpecificCourtWeightedDistributor();
        this.genericCourtWeightedDistributor = new GenericCourtWeightedDistributor();
    }

    @Override
    public String selectCourtForGivenDivorceReason(Optional<String> reasonForDivorce) {
        //TODO - make tests pass
        return null;
    }

}