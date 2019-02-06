package uk.gov.hmcts.reform.divorce.orchestration.courtallocation;

import org.apache.commons.math3.distribution.EnumeratedDistribution;
import org.apache.commons.math3.util.Pair;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static java.lang.String.format;
import static java.math.BigDecimal.ONE;

/**
 * This class handles court distribution for cases which have facts which were configured
 * to be allocated to courts in a specific way.
 */
public class FactSpecificCourtWeightedDistributor {

    private final Map<String, EnumeratedDistribution<String>> distributionPerFact;

    public FactSpecificCourtWeightedDistributor(Map<String, Map<String, BigDecimal>> specificCourtsAllocationPerFact) {
        distributionPerFact = specificCourtsAllocationPerFact.entrySet().stream().collect(Collectors.toMap(
            Map.Entry::getKey,
            entry -> {
                String fact = entry.getKey();
                Map<String, BigDecimal> courtAllocationForFact = entry.getValue();

                BigDecimal totalAllocationForFact = courtAllocationForFact.values().stream()
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
                if (totalAllocationForFact.compareTo(BigDecimal.ONE) > 0) {
                    throw new CourtAllocatorException(
                        format("Configured fact allocation for \"%s\" went over 100%%.", fact));
                }

                List<Pair<String, BigDecimal>> allocationPerCourt = courtAllocationForFact.entrySet().stream()
                    .map(e -> new Pair<>(e.getKey(), e.getValue()))
                    .collect(Collectors.toList());

                //Get total allocated percentage and allocate the rest to "no court"
                BigDecimal remainingAllocation = ONE.subtract(totalAllocationForFact).abs();
                allocationPerCourt.add(new Pair(null, remainingAllocation));

                List<Pair<String, Double>> listWithDoubles = allocationPerCourt.stream()
                    .map(pair -> new Pair<>(pair.getKey(), pair.getValue().doubleValue()))
                    .collect(Collectors.toList());

                EnumeratedDistribution<String> enumeratedDistribution = new EnumeratedDistribution(listWithDoubles);
                return enumeratedDistribution;
            }
        ));
    }

    public String selectCourt(String divorceFact) {
        if (distributionPerFact.containsKey(divorceFact)) {
            return distributionPerFact.get(divorceFact).sample();
        } else {
            return null;
        }
    }

}