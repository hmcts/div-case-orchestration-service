package uk.gov.hmcts.reform.divorce.orchestration.courtallocation;

import org.apache.commons.math3.distribution.EnumeratedDistribution;
import org.apache.commons.math3.util.Pair;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * This class handles court distribution for cases which have facts which were not
 * configured to be allocated to courts in a specific way.
 */
public class GenericCourtWeightedDistributor {

    private EnumeratedDistribution<String> genericCourtDistribution;

    public GenericCourtWeightedDistributor(Map<String, BigDecimal> desiredWorkloadPerCourt, Map<String, BigDecimal> divorceRatioPerFact, Map<String, Map<String, BigDecimal>> specificCourtsAllocationPerFact) {
        List<Pair<String, Double>> courtDistribution = desiredWorkloadPerCourt.entrySet().stream()
            .map(e -> new Pair<>(e.getKey(), e.getValue().doubleValue()))
            .collect(Collectors.toList());
        this.genericCourtDistribution = new EnumeratedDistribution<>(courtDistribution);

        Map<String, BigDecimal> percentagesOfTotalCasesPerSpecifiedCourt = calculatePercentageOfTotalCasesTakenBySpecifiedCourts(divorceRatioPerFact, specificCourtsAllocationPerFact);
        //TODO - bigdecimal is not quite right
        //Recalculate generic distribution

        //Remainder percentage
        BigDecimal totalPercentageOfCasesTakenBySpecifiedCourts = percentagesOfTotalCasesPerSpecifiedCourt.values()
            .stream()
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal remainderPercentageToBeHandledByUnspecifiedCourts = BigDecimal.ONE.subtract(totalPercentageOfCasesTakenBySpecifiedCourts);

        //Get unspecified courts
        Map<String, BigDecimal> newPercentagesOfTotalCasesHandledPerUnspecifiedCourt = desiredWorkloadPerCourt.entrySet().stream()
            .filter(e -> !percentagesOfTotalCasesPerSpecifiedCourt.keySet().contains(e.getKey()))
            .collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue().divide(remainderPercentageToBeHandledByUnspecifiedCourts, 3, RoundingMode.DOWN)));

        //Add specified courts
//        HashMap<String, BigDecimal> rebalancedDesiredWorkloadPerCourt = new HashMap<>(percentagesOfTotalCasesPerSpecifiedCourt);
//        rebalancedDesiredWorkloadPerCourt.putAll(newPercentagesOfTotalCasesHandledPerUnspecifiedCourt);

        //TODO - copied from top
        List<Pair<String, Double>> courtDistribution2 = newPercentagesOfTotalCasesHandledPerUnspecifiedCourt.entrySet().stream()
            .map(e -> new Pair<>(e.getKey(), e.getValue().doubleValue()))
            .collect(Collectors.toList());
        this.genericCourtDistribution = new EnumeratedDistribution<>(courtDistribution2);//TODO - refactor names
        //TODO - test what happens if enumeration items don't reach 100% or pass 100% percent - this can happen by a bit
    }

    private Map<String, BigDecimal> calculatePercentageOfTotalCasesTakenBySpecifiedCourts(Map<String, BigDecimal> divorceRatioPerFact, Map<String, Map<String, BigDecimal>> specificCourtsAllocationPerFact) {
        Map<String, BigDecimal> percentageOfTotalCasesTakenBySpecifiedCourts = new HashMap<>();
        for (Map.Entry<String, Map<String, BigDecimal>> courtFactDistributionPerFact : specificCourtsAllocationPerFact.entrySet()) {
            String fact = courtFactDistributionPerFact.getKey();
            Map<String, BigDecimal> courtDistributionForFact = courtFactDistributionPerFact.getValue();

            for (Map.Entry<String, BigDecimal> thisCourtDistribution : courtDistributionForFact.entrySet()) {
                String thisCourtId = thisCourtDistribution.getKey();
                BigDecimal percentageOfCasesWithThisFactTakenByThisCourt = thisCourtDistribution.getValue();

                BigDecimal currentPercentageOfTotalCasesTakenByCourt = percentageOfTotalCasesTakenBySpecifiedCourts.getOrDefault(thisCourtId, BigDecimal.ZERO);
                BigDecimal percentageOfTotalTakenByThisCourtForThisFact = divorceRatioPerFact.get(fact).multiply(percentageOfCasesWithThisFactTakenByThisCourt);

                percentageOfTotalCasesTakenBySpecifiedCourts.put(
                    thisCourtId,
                    currentPercentageOfTotalCasesTakenByCourt.add(percentageOfTotalTakenByThisCourtForThisFact)
                );
            }
        }
        return percentageOfTotalCasesTakenBySpecifiedCourts;
    }

    public String selectCourt() {
        return genericCourtDistribution.sample();
    }

}