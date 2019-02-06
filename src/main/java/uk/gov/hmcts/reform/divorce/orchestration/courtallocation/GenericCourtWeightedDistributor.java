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

import static java.lang.String.format;
import static java.math.BigDecimal.ZERO;
import static java.math.RoundingMode.DOWN;

/**
 * <p>
 * This class handles court distribution for cases which have facts which were not
 * configured to be allocated to courts in a specific way or that have less than 100% of the
 * specified fact being handled by specific courts.
 * <p>
 * The percentages in this class are relative to the total amount of cases.
 * </p>
 */
public class GenericCourtWeightedDistributor {

    private EnumeratedDistribution<String> genericCourtDistribution;

    public GenericCourtWeightedDistributor(final Map<String, BigDecimal> desiredWorkloadPerCourt,
                                           Map<String, BigDecimal> divorceRatioPerFact,
                                           Map<String, Map<String, BigDecimal>> specificCourtsAllocationPerFact) {

        final Map<String, BigDecimal> courtsWorkload = new HashMap<>(desiredWorkloadPerCourt);

        if (courtsWorkload.values().stream().reduce(ZERO, BigDecimal::add).compareTo(ZERO) <= 0) {
            throw new CourtAllocatorException("No workload was configured for any courts.");
        }

        if (!MapUtils.isEmpty(specificCourtsAllocationPerFact)) {
            Map<String, BigDecimal> courtsWorkloadForSpecifiedFacts = retrieveCourtsWorkloadForSpecifiedFacts(
                divorceRatioPerFact, specificCourtsAllocationPerFact);
            Set<String> specifiedCourts = courtsWorkloadForSpecifiedFacts.keySet();

            //Deduct from specified courts the percentage being handled due to specific facts
            Stream<Map.Entry<String, BigDecimal>> specifiedCourtsRemainingAllocation =
                courtsWorkload.entrySet().stream()
                    .filter(e -> specifiedCourts.contains(e.getKey()))
                    .peek(e -> {
                        String courtId = e.getKey();
                        BigDecimal desiredWorkloadForCourt = e.getValue();
                        BigDecimal courtWorkloadForSpecifiedFacts =
                            courtsWorkloadForSpecifiedFacts.getOrDefault(courtId, ZERO);

                        if (desiredWorkloadForCourt.compareTo(courtWorkloadForSpecifiedFacts) >= 0) {
                            BigDecimal remainingCourtWorkload = desiredWorkloadForCourt
                                .subtract(courtWorkloadForSpecifiedFacts);

                            e.setValue(remainingCourtWorkload);
                        } else {
                            throw new CourtAllocatorException(format(
                                "Court \"%s\" was overallocated. Desired workload is %s but total allocation was %s",
                                courtId, desiredWorkloadForCourt, courtWorkloadForSpecifiedFacts
                            ));
                        }
                    });

            Stream<Map.Entry<String, BigDecimal>> unspecifiedCourtsGenericAllocation =
                courtsWorkload.entrySet().stream().filter(e -> !specifiedCourts.contains(e.getKey()));

            //Remaining percentage
            BigDecimal remainingWorkload = calculateRemainingWorkload(courtsWorkloadForSpecifiedFacts);

            //Rebalance generic court allocation
            Stream<Map.Entry<String, BigDecimal>> rebalancedGenericCourtAllocation = Stream.concat(
                unspecifiedCourtsGenericAllocation,
                specifiedCourtsRemainingAllocation
            ).peek(entry -> entry.setValue(entry.getValue().divide(remainingWorkload, 3, DOWN)));

            setRebalancedCourtsWorkload(rebalancedGenericCourtAllocation);
        } else {
            setRebalancedCourtsWorkload(courtsWorkload.entrySet().stream());
        }
    }

    private Map<String, BigDecimal> retrieveCourtsWorkloadForSpecifiedFacts(
        Map<String, BigDecimal> divorceRatioPerFact,
        Map<String, Map<String, BigDecimal>> specificCourtsAllocationPerFact) {

        Map<String, BigDecimal> courtsWorkloadForSpecifiedFacts = new HashMap<>();

        for (Map.Entry<String, Map<String, BigDecimal>> courtFactDistributionPerFact :
            specificCourtsAllocationPerFact.entrySet()) {

            String fact = courtFactDistributionPerFact.getKey();
            Map<String, BigDecimal> courtDistributionForFact = courtFactDistributionPerFact.getValue();

            for (Map.Entry<String, BigDecimal> thisCourtDistribution : courtDistributionForFact.entrySet()) {
                String courtId = thisCourtDistribution.getKey();
                BigDecimal percentageOfCasesWithFactTakenByCourt = thisCourtDistribution.getValue();

                BigDecimal currentCourtWorkload = courtsWorkloadForSpecifiedFacts.getOrDefault(courtId, ZERO);
                BigDecimal courtWorkloadForFact = divorceRatioPerFact.get(fact)
                    .multiply(percentageOfCasesWithFactTakenByCourt);

                courtsWorkloadForSpecifiedFacts.put(
                    courtId,
                    currentCourtWorkload.add(courtWorkloadForFact)
                );
            }
        }
        return courtsWorkloadForSpecifiedFacts;
    }

    private BigDecimal calculateRemainingWorkload(Map<String, BigDecimal> courtWorkloadForSpecifiedFacts) {
        BigDecimal totalFactSpecificWorkload = courtWorkloadForSpecifiedFacts.values().stream()
            .reduce(ZERO, BigDecimal::add).subtract(BigDecimal.ONE).abs();
        return BigDecimal.ONE.subtract(totalFactSpecificWorkload);
    }

    private void setRebalancedCourtsWorkload(Stream<Map.Entry<String, BigDecimal>> rebalancedCourtsWorkload) {
        List<Pair<String, Double>> courtDistribution = rebalancedCourtsWorkload
            .map(e -> new Pair<>(e.getKey(), e.getValue().doubleValue()))
            .collect(Collectors.toList());
        this.genericCourtDistribution = new EnumeratedDistribution<>(courtDistribution);
    }

    public String selectCourt() {
        return genericCourtDistribution.sample();
    }

}