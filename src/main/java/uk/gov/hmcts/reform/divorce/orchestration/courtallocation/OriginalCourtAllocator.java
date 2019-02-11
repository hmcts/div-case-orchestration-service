package uk.gov.hmcts.reform.divorce.orchestration.courtallocation;

import org.apache.commons.math3.distribution.EnumeratedDistribution;
import org.apache.commons.math3.util.Pair;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toMap;

/**
 * These are the court allocator copied from PFE. I'm leaving it in as reference, at least until
 * the solution is stable and we're more confident we can remove it.
 */
@Deprecated
public class OriginalCourtAllocator implements CourtAllocator {

    private final Map<String, Double> caseDistribution;
    private final Map<String, Map> courts;

    private final Map<String, Double> allocationPerFactLeft = new HashMap<>();
    private final Map<Object, Double> remainingWeightForCourt = new HashMap();
    private final Map<String, Map<String, Double>> weightPerFactPerCourt = new HashMap();

    private final Map<String, EnumeratedDistribution<String>> enumeratedDistributions;

    public OriginalCourtAllocator(Map<String, Double> caseDistribution, Map<String, Map> courts) {
        this.caseDistribution = caseDistribution;
        this.courts = courts;

        calculatePreAllocations();
        allocateRemainders();

        enumeratedDistributions = weightPerFactPerCourt.entrySet().stream().collect(toMap(
            Map.Entry::getKey,
            e -> {
                Map<String, Double> weightPerCourtForFact = e.getValue();
                List<Pair<String, Double>> weightedList = weightPerCourtForFact.entrySet().stream()
                    .map(entry -> new Pair<>(entry.getKey(), entry.getValue()))
                    .collect(Collectors.toList());
                return new EnumeratedDistribution<>(weightedList);
            }));
    }

    @Override
    public String selectCourtForGivenDivorceFact(String fact) {
        return Optional.ofNullable(fact)
            .map(enumeratedDistributions::get)
            .map(EnumeratedDistribution::sample)
            .orElse(null);
    }

    private Double getDivorceFactRatioForCourt(String courtName, String fact) {
        return ((Map<String, Double>) courts.get(courtName).get("divorceFactsRatio")).get(fact);
    }

    private void initialiseAllocationRemainingForCourt(String courtName) {
        if (!remainingWeightForCourt.containsKey(courtName)) {
            remainingWeightForCourt.put(courtName, (Double) courts.get(courtName).get("weight"));
        }
    }

    private void initialiseAllocationRemainingForFact(String fact) {
        if (!allocationPerFactLeft.containsKey(fact)) {
            allocationPerFactLeft.put(fact, 1.0);
        }
    }

    private void initialiseWeightPerFactPerCourt(String fact) {
        if (!weightPerFactPerCourt.containsKey(fact)) {
            weightPerFactPerCourt.put(fact, new HashMap());
        }
    }

    private void updateAllocationRemainingForCourt(String fact, String courtName) {
        remainingWeightForCourt.put(courtName,
            remainingWeightForCourt.get(courtName)
                - (getDivorceFactRatioForCourt(courtName, fact) * caseDistribution.get(fact))
        );

        // this will fail when the allocation configuration is not validated
        // it should break the deployment and it is the expected behaviour
        if (remainingWeightForCourt.get(courtName) < 0) {
            throw new CourtAllocatorException("Total weightage exceeded for court " + courtName);
        }
    }

    private void updateAllocationRemainingForFact(String fact, String courtName) {
        allocationPerFactLeft.put(fact,
            allocationPerFactLeft.get(fact) - getDivorceFactRatioForCourt(courtName, fact)
        );

        // this will fail when the allocation configuration is not validation
        // it should break the deployment and it is the expected behaviour
        if (allocationPerFactLeft.get(fact) < 0) {
            throw new CourtAllocatorException("Total weightage exceeded for fact " + fact);
        }
    }

    private void updateWeightPerFactPerCourt(String fact, String courtName) {
        weightPerFactPerCourt.get(fact).put(courtName, getDivorceFactRatioForCourt(courtName, fact));
    }

    private Map<String, Double> calculateTotalUnAllocatedWeightPerFact() {
        Map<String, Double> totalWeightPerFact = new HashMap();

        caseDistribution.keySet().forEach(fact ->
            courts.keySet().forEach(courtName -> {
                if (!weightPerFactPerCourt.containsKey(fact)
                    || !weightPerFactPerCourt.get(fact).containsKey(courtName)) {
                    if (!totalWeightPerFact.containsKey(fact)) {
                        totalWeightPerFact.put(fact, 0.0);
                    }

                    totalWeightPerFact.put(fact,
                        totalWeightPerFact.get(fact) + (Double) courts.get(courtName).get("weight"));
                }
            }));

        return totalWeightPerFact;
    }

    private void distributeRemainingFactsAllocationToCourts(String fact, String courtName,
                                                            Map<String, Double> totalWeightPerFact) {
        if (!weightPerFactPerCourt.get(fact).containsKey(courtName)) {
            if (totalWeightPerFact.containsKey(fact)) {
                weightPerFactPerCourt.get(fact).put(courtName,
                    allocationPerFactLeft.get(fact)
                        * (remainingWeightForCourt.get(courtName) / totalWeightPerFact.get(fact))
                );
            } else {
                weightPerFactPerCourt.get(fact).put(courtName, 0.0);
            }
        }
    }

    private void calculatePreAllocations() {
        caseDistribution.keySet().forEach(fact -> {
            initialiseAllocationRemainingForFact(fact);

            courts.keySet().forEach(courtName -> {
                initialiseAllocationRemainingForCourt(courtName);

                if (courts.get(courtName).containsKey("divorceFactsRatio")
                    && getDivorceFactRatioForCourt(courtName, fact) != null) {
                    initialiseWeightPerFactPerCourt(fact);
                    updateAllocationRemainingForCourt(fact, courtName);
                    updateWeightPerFactPerCourt(fact, courtName);
                    updateAllocationRemainingForFact(fact, courtName);
                }
            });
        });
    }

    private void allocateRemainders() {
        Map<String, Double> totalWeightPerFact = calculateTotalUnAllocatedWeightPerFact();

        caseDistribution.keySet().forEach(fact -> courts.keySet().forEach(courtName -> {
            initialiseWeightPerFactPerCourt(fact);
            distributeRemainingFactsAllocationToCourts(fact, courtName, totalWeightPerFact);
        }));
    }

}