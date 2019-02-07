package uk.gov.hmcts.reform.divorce.orchestration.courtallocation;

import org.apache.commons.math3.distribution.EnumeratedDistribution;
import org.apache.commons.math3.util.Pair;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class OriginalCourtAllocator implements CourtAllocator {

    private Map<String, Double> caseDistribution;
    private Map<String, Map> courts;

    private Map<String, Double> allocationPerFactLeft = new HashMap<>();
    private Map<Object, Double> remainingWeightForCourt = new HashMap();
    private Map<String, Map<String, Double>> weightPerFactPerCourt = new HashMap();

    private final Map<String, EnumeratedDistribution<String>> enumeratedDistributions;

    public OriginalCourtAllocator(Map<String, Double> caseDistribution, Map<String, Map> courts) {
        this.caseDistribution = caseDistribution;
        this.courts = courts;

        calculatePreAllocations.run();
        allocateRemainders.run();

        enumeratedDistributions = weightPerFactPerCourt.entrySet().stream().collect(Collectors.toMap(
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
    public String selectCourtForGivenDivorceFact(Optional<String> fact) {
        return fact.map(enumeratedDistributions::get)
            .map(EnumeratedDistribution::sample)
            .orElse(null);
    }


    BiFunction<String, String, Double> getDivorceFactRatioForCourt = (courtName, fact) ->
        ((Map<String, Double>) courts.get(courtName).get("divorceFactsRatio")).get(fact);

    Consumer initialiseAllocationRemainingForFact = fact -> {
        if (!allocationPerFactLeft.containsKey(fact)) {
            allocationPerFactLeft.put(fact.toString(), 1.0);
        }
    };

    Consumer initialiseAllocationRemainingForCourt = courtName -> {
        if (!remainingWeightForCourt.containsKey(courtName)) {
            remainingWeightForCourt.put(courtName, (Double) courts.get(courtName).get("weight"));
        }
    };

    Consumer<String> initialiseWeightPerFactPerCourt = fact -> {
        if (!weightPerFactPerCourt.containsKey(fact)) {
            weightPerFactPerCourt.put(fact, new HashMap());
        }
    };

    BiConsumer<String, String> updateAllocationRemainingForCourt = (fact, courtName) -> {
        remainingWeightForCourt.put(courtName,
            remainingWeightForCourt.get(courtName)
                - (getDivorceFactRatioForCourt.apply(courtName, fact) * caseDistribution.get(fact))
        );

        // this will fail when the allocation configuration is not validation
        // it should break the deployment and it is the expected behaviour
        if (remainingWeightForCourt.get(courtName) < 0) {
            throw new CourtAllocatorException("Total weightage exceeded for court " + courtName);
        }
    };

    BiConsumer<String, String> updateAllocationRemainingForFact = (fact, courtName) -> {
        allocationPerFactLeft.put(fact,
            allocationPerFactLeft.get(fact) - getDivorceFactRatioForCourt.apply(courtName, fact)
        );

        // this will fail when the allocation configuration is not validation
        // it should break the deployment and it is the expected behaviour
        if (allocationPerFactLeft.get(fact) < 0) {
            throw new CourtAllocatorException("Total weightage exceeded for fact " + fact);
        }
    };

    BiConsumer<String, String> updateWeightPerFactPerCourt = (fact, courtName) ->
        weightPerFactPerCourt.get(fact).put(courtName, getDivorceFactRatioForCourt.apply(courtName, fact));

    Supplier<Map<String, Double>> calculateTotalUnAllocatedWeightPerFact = () -> {
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
    };

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

    Runnable calculatePreAllocations = () ->
        caseDistribution.keySet().forEach(fact -> {
            initialiseAllocationRemainingForFact.accept(fact);

            courts.keySet().forEach(courtName -> {
                initialiseAllocationRemainingForCourt.accept(courtName);

                if (courts.get(courtName).containsKey("divorceFactsRatio")
                    && getDivorceFactRatioForCourt.apply(courtName, fact) != null) {
                    initialiseWeightPerFactPerCourt.accept(fact);
                    updateAllocationRemainingForCourt.accept(fact, courtName);
                    updateWeightPerFactPerCourt.accept(fact, courtName);
                    updateAllocationRemainingForFact.accept(fact, courtName);
                }
            });
        });

    Runnable allocateRemainders = () -> {
        Map<String, Double> totalWeightPerFact = calculateTotalUnAllocatedWeightPerFact.get();

        caseDistribution.keySet().forEach(fact -> courts.keySet().forEach(courtName -> {
            initialiseWeightPerFactPerCourt.accept(fact);
            distributeRemainingFactsAllocationToCourts(fact, courtName, totalWeightPerFact);
        }));
    };

}