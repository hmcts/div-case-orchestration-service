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

import static java.math.RoundingMode.DOWN;

/**
 * This class handles court distribution for cases which have facts which were not
 * configured to be allocated to courts in a specific way.
 * <p>
 * The percentages in this class are relative to the total amount of cases.
 */
public class GenericCourtWeightedDistributor {//TODO - maybe unspecified, rather than generic

    private EnumeratedDistribution<String> genericCourtDistribution;

    public GenericCourtWeightedDistributor(Map<String, BigDecimal> desiredWorkloadPerCourt, Map<String, BigDecimal> divorceRatioPerFact, Map<String, Map<String, BigDecimal>> specificCourtsAllocationPerFact) {
        Map<String, BigDecimal> finalUnspecifiedCourtsWorkload = new HashMap<>(desiredWorkloadPerCourt);

        if (!MapUtils.isEmpty(specificCourtsAllocationPerFact)) {
            Map<String, BigDecimal> percentageOfCasesPerSpecifiedCourt = retrievePercentageOfCasesPerSpecifiedCourt(divorceRatioPerFact, specificCourtsAllocationPerFact);

            //Remaining percentage
            BigDecimal remainingPercentageOfCasesToBeHandledByUnspecifiedCourts = calculateRemainingPercentageToBeHandledByUnspecifiedCourts(percentageOfCasesPerSpecifiedCourt);

            //Get percentage to be handled by unspecified courts
            Set<String> specifiedCourts = percentageOfCasesPerSpecifiedCourt.keySet();
            finalUnspecifiedCourtsWorkload = desiredWorkloadPerCourt.entrySet().stream()
                .filter(e -> !specifiedCourts.contains(e.getKey()))
                .collect(Collectors.toMap(
                    Map.Entry::getKey,
                    e -> e.getValue().divide(remainingPercentageOfCasesToBeHandledByUnspecifiedCourts, 3, DOWN)
                ));
        }

        setUnspecifiedCourtsDistributionBasedOnCourtsWorkload(finalUnspecifiedCourtsWorkload);
        //TODO - test what happens if enumeration items don't reach 100% or pass 100% percent - this can happen by a bit
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

    private BigDecimal calculateRemainingPercentageToBeHandledByUnspecifiedCourts(Map<String, BigDecimal> percentageOfTotalCasesPerSpecifiedCourt) {
        BigDecimal percentageOfTotalCasesForAllSpecifiedCourts = percentageOfTotalCasesPerSpecifiedCourt.values().stream()
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        return BigDecimal.ONE.subtract(percentageOfTotalCasesForAllSpecifiedCourts);
    }

    private void setUnspecifiedCourtsDistributionBasedOnCourtsWorkload(Map<String, BigDecimal> workloadPerCourt) {
        List<Pair<String, Double>> courtDistribution = workloadPerCourt.entrySet().stream()
            .map(e -> new Pair<>(e.getKey(), e.getValue().doubleValue()))
            .collect(Collectors.toList());
        this.genericCourtDistribution = new EnumeratedDistribution<>(courtDistribution);
    }

    public String selectCourt() {
        return genericCourtDistribution.sample();
    }

}