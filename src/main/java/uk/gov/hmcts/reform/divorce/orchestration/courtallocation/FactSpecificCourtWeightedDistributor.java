package uk.gov.hmcts.reform.divorce.orchestration.courtallocation;

import org.apache.commons.math3.distribution.EnumeratedDistribution;
import org.apache.commons.math3.util.Pair;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * This class handles court distribution for cases which have facts which were configured
 * to be allocated to courts in a specific way.
 */
public class FactSpecificCourtWeightedDistributor {

    private final Map<String, EnumeratedDistribution<String>> distributionPerFact;

    public FactSpecificCourtWeightedDistributor(Map<String, Map<String, BigDecimal>> specificCourtsAllocationPerFact) {
        //TODO - maybe we should have a unit test for this
        //TODO - What happens if facts are duplicated?
        distributionPerFact = specificCourtsAllocationPerFact.entrySet().stream().collect(Collectors.toMap(entry -> entry.getKey(), entry -> {
            Map<String, BigDecimal> value = entry.getValue();
            List<Pair<String, BigDecimal>> allocationPerCourt = value.entrySet().stream()
                .map(e -> new Pair<String, BigDecimal>(e.getKey(), e.getValue()))
                .collect(Collectors.toList());

            //Get total allocated percentage and allocate the rest to "no court"
            BigDecimal remainingAllocation = allocationPerCourt.stream()
                .map(Pair::getValue)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .subtract(BigDecimal.ONE)
                .abs();
            allocationPerCourt.add(new Pair(null, remainingAllocation));

            List<Pair<String, Double>> listWithDoubles = allocationPerCourt.stream()
                .map(pair -> new Pair<>(pair.getKey(), pair.getValue().doubleValue()))
                .collect(Collectors.toList());

            EnumeratedDistribution<String> enumeratedDistribution = new EnumeratedDistribution(listWithDoubles);
            return enumeratedDistribution;
        }));
    }

    public String selectCourt(String divorceFact) {
        if (distributionPerFact.containsKey(divorceFact)) {
            return distributionPerFact.get(divorceFact).sample();
        } else {
            return null;
        }
    }

}