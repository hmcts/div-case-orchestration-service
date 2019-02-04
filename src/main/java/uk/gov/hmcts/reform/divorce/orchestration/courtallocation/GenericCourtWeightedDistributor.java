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
        final Map<String, BigDecimal> courtsWorkload = new HashMap<>(desiredWorkloadPerCourt);

        if (!MapUtils.isEmpty(specificCourtsAllocationPerFact)) {
            Map<String, BigDecimal> percentageOfCasesPerFactSpecificCourt = retrievePercentageOfCasesPerSpecifiedCourt(divorceRatioPerFact, specificCourtsAllocationPerFact);
            Set<String> specifiedCourts = percentageOfCasesPerFactSpecificCourt.keySet();

            //Deduct from specified courts the percentage being handled due to specific facts
            Map<String, BigDecimal> specifiedCourtsDiscountedGenericAllocation = courtsWorkload.entrySet().stream()
                .filter(e -> specifiedCourts.contains(e.getKey()))
                .peek(e -> {
                    BigDecimal currentPercentage = e.getValue();
                    BigDecimal percentageOfCasesWithSpecifiedFactHandledByCourt = percentageOfCasesPerFactSpecificCourt.getOrDefault(e.getKey(), BigDecimal.ZERO);//TODO - cater for cases that end up with less than 0%
                    BigDecimal discountedPercentage = currentPercentage.subtract(percentageOfCasesWithSpecifiedFactHandledByCourt);
                    e.setValue(discountedPercentage);
                }).collect(Collectors.toMap(
                    Map.Entry::getKey,
                    Map.Entry::getValue
                ));//TODO - could be stream?

            Map<String, BigDecimal> unspecifiedCourtsGenericAllocation = courtsWorkload.entrySet().stream()
                .filter(e -> !specifiedCourts.contains(e.getKey()))
                .collect(Collectors.toMap(
                    Map.Entry::getKey,
                    Map.Entry::getValue
                ));//TODO - could be stream?
//            Stream<Map.Entry<String, BigDecimal>> deductedGenericCourtAllocation = percentageOfCasesPerSpecifiedCourt.entrySet().stream().peek(e -> {
//                BigDecimal courtWorkloadPercentage = courtsWorkload.get(e.getKey());
//                BigDecimal percentageOfCasesWithSpecifiedFactHandledByCourt = e.getValue();
//                BigDecimal discountedPercentage = courtWorkloadPercentage.subtract(percentageOfCasesWithSpecifiedFactHandledByCourt);
//                e.setValue(discountedPercentage);
//            });

            //Remaining percentage
            BigDecimal percentageOfTotalCasesForAllSpecifiedCourts = percentageOfCasesPerFactSpecificCourt.values().stream()
                .reduce(BigDecimal.ZERO, BigDecimal::add);
            BigDecimal sumOfPercentagesInGenericDistributionForSpecifiedCourts = specifiedCourtsDiscountedGenericAllocation.values().stream()
                .reduce(BigDecimal.ZERO, BigDecimal::add);//TODO - would this be the allocated court percentage?
            BigDecimal remainingPercentageOfCasesToBeHandledByCourts = BigDecimal.ONE.subtract(percentageOfTotalCasesForAllSpecifiedCourts)
                //.subtract(sumOfPercentagesInGenericDistributionForSpecifiedCourts)
                ;

//            Set<String> specifiedCourts = percentageOfCasesPerSpecifiedCourt.keySet();

            //Rebalance generic court allocation
            //Get percentage to be handled by unspecified courts
            //TODO - courts that have been specified should no longer be rebalanced
            Map<String, BigDecimal> tempRenameMe = new HashMap<>();
            tempRenameMe.putAll(unspecifiedCourtsGenericAllocation);
            tempRenameMe.putAll(specifiedCourtsDiscountedGenericAllocation);

            Map<String, BigDecimal> rebalancedGenericCourtAllocation = tempRenameMe.entrySet().stream()
//                .filter(e -> !specifiedCourts.contains(e.getKey()))//TODO - Could I use anyMatch?
                .collect(Collectors.toMap(
                    Map.Entry::getKey,
                    e -> e.getValue().divide(remainingPercentageOfCasesToBeHandledByCourts, 3, DOWN)
                ));

//            rebalancedGenericCourtAllocation.putAll(specifiedCourtsDiscountedGenericAllocation);

            setUnspecifiedCourtsDistributionBasedOnCourtsWorkload(rebalancedGenericCourtAllocation);
        } else {
            setUnspecifiedCourtsDistributionBasedOnCourtsWorkload(courtsWorkload);
        }

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