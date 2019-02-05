package uk.gov.hmcts.reform.divorce.orchestration.courtallocation;

import org.apache.commons.collections.MapUtils;
import org.apache.commons.math3.distribution.EnumeratedDistribution;
import org.apache.commons.math3.util.Pair;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.math.RoundingMode.DOWN;

/**
 * This class handles court distribution for cases which have facts which were not
 * configured to be allocated to courts in a specific way or that have less than 100% of the
 * specified fact being handled by specific courts.
 * <p>
 * The percentages in this class are relative to the total amount of cases.
 */
public class GenericCourtWeightedDistributor {

    private EnumeratedDistribution<String> genericCourtDistribution;

    public GenericCourtWeightedDistributor(Map<String, BigDecimal> desiredWorkloadPerCourt, Map<String, BigDecimal> divorceRatioPerFact, Map<String, Map<String, BigDecimal>> specificCourtsAllocationPerFact) {
        final Map<String, BigDecimal> courtsWorkload = new HashMap<>(desiredWorkloadPerCourt);

        if (!MapUtils.isEmpty(specificCourtsAllocationPerFact)) {
            Map<String, BigDecimal> courtWorkloadForSpecifiedFacts = retrievePercentageOfCasesPerSpecifiedCourt(divorceRatioPerFact, specificCourtsAllocationPerFact);
            Set<String> specifiedCourts = courtWorkloadForSpecifiedFacts.keySet();

            //Deduct from specified courts the percentage being handled due to specific facts
            Stream<Map.Entry<String, BigDecimal>> specifiedCourtsRemainingAllocation = courtsWorkload.entrySet().stream()
                .filter(e -> specifiedCourts.contains(e.getKey()))
                .peek(e -> {
                    String courtId = e.getKey();
                    BigDecimal desiredWorkloadForCourt = e.getValue();
                    BigDecimal courtWorkloadDueToSpecifiedFacts = courtWorkloadForSpecifiedFacts.getOrDefault(courtId, BigDecimal.ZERO);
                    BigDecimal remainingCourtWorkload = desiredWorkloadForCourt.subtract(courtWorkloadDueToSpecifiedFacts);
                    e.setValue(remainingCourtWorkload);
                });

            Stream<Map.Entry<String, BigDecimal>> unspecifiedCourtsGenericAllocation = courtsWorkload.entrySet().stream()
                .filter(e -> !specifiedCourts.contains(e.getKey()));

            //Remaining percentage
            BigDecimal remainingWorkloadToBeHandledGenerically = calculateRemainingWorkload(courtWorkloadForSpecifiedFacts);

            //Rebalance generic court allocation
            Stream<Map.Entry<String, BigDecimal>> rebalancedGenericCourtAllocation = Stream.concat(unspecifiedCourtsGenericAllocation, specifiedCourtsRemainingAllocation)
                .peek(entry -> entry.setValue(entry.getValue().divide(remainingWorkloadToBeHandledGenerically, 3, DOWN)));

            setUnspecifiedCourtsDistributionBasedOnCourtsWorkload(rebalancedGenericCourtAllocation);
        } else {
            setUnspecifiedCourtsDistributionBasedOnCourtsWorkload(courtsWorkload.entrySet().stream());
        }
    }

    private Map<String, BigDecimal> retrievePercentageOfCasesPerSpecifiedCourt(Map<String, BigDecimal> divorceRatioPerFact, Map<String, Map<String, BigDecimal>> specificCourtsAllocationPerFact) {
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

    private BigDecimal calculateRemainingWorkload(Map<String, BigDecimal> courtWorkloadForSpecifiedFacts) {
        BigDecimal totalFactSpecificWorkload = courtWorkloadForSpecifiedFacts.values().stream()
            .reduce(BigDecimal.ZERO, BigDecimal::add).subtract(BigDecimal.ONE).abs();
        return BigDecimal.ONE.subtract(totalFactSpecificWorkload);
    }

    private void setUnspecifiedCourtsDistributionBasedOnCourtsWorkload(Stream<Map.Entry<String, BigDecimal>> workloadPerCourt) {
        List<Pair<String, Double>> courtDistribution = workloadPerCourt
            .map(e -> new Pair<>(e.getKey(), e.getValue().doubleValue()))
            .collect(Collectors.toList());
        this.genericCourtDistribution = new EnumeratedDistribution<>(courtDistribution);
    }

    public String selectCourt() {
        return genericCourtDistribution.sample();
    }

}