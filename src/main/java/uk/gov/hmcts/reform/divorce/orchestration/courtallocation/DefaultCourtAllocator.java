package uk.gov.hmcts.reform.divorce.orchestration.courtallocation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.Set;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toMap;

public class DefaultCourtAllocator implements CourtAllocator {

    /*
     * Each court will have a raffle ticket based on its attributed weight
     */
    private String[] raffleTicketsPerCourt;

    private Map<String, String> courtPerReasonForDivorce = new HashMap<>();

    private static final Random random = new Random();

    public DefaultCourtAllocator(CourtAllocationConfiguration courtAllocationConfig) {
        Set<CourtWeight> courtWeights = courtAllocationConfig.getCourtsWeightedDistribution();
        Set<CourtAllocationPerReason> courtsForSpecificReasons = courtAllocationConfig.getCourtsForSpecificReasons();

        if (courtWeights.isEmpty() && courtsForSpecificReasons.isEmpty()) {
            throw new CourtAllocatorException("Cannot build court allocator with empty configuration.");
        }

        createRaffleTicketsBasedOnCourtsWeight(courtWeights);
        allocateCourtsToHandleCertainDivorceReasons(courtsForSpecificReasons);
    }

    public DefaultCourtAllocator(Set<CourtWeight> courtWeights,
                                 Set<CourtAllocationPerReason> courtAllocationForSpecificReasons) {
        this(courtWeights);
        allocateCourtsToHandleCertainDivorceReasons(courtAllocationForSpecificReasons);
    }

    public DefaultCourtAllocator(Set<CourtWeight> courtWeights) {
        createRaffleTicketsBasedOnCourtsWeight(courtWeights);
    }

    private void createRaffleTicketsBasedOnCourtsWeight(Set<CourtWeight> courtWeights) {
        this.raffleTicketsPerCourt = courtWeights.stream()
                .flatMap(this::returnAdequateAmountOfRaffleTicketsPerCourt)
                .map(CourtWeight::getCourtId)
                .toArray(String[]::new);
    }

    private void allocateCourtsToHandleCertainDivorceReasons(
            Set<CourtAllocationPerReason> courtAllocationForSpecificReasons) {
        courtPerReasonForDivorce = courtAllocationForSpecificReasons.stream()
                .collect(toMap(
                        CourtAllocationPerReason::getDivorceReason,
                        CourtAllocationPerReason::getCourtId
                ));
    }

    @Override
    public String selectCourtForGivenDivorceFact(Optional<String> divorceFact) {
        return divorceFact
                .map(courtPerReasonForDivorce::get)
                .orElseGet(this::selectCourtRandomly);
    }

    private String selectCourtRandomly() {
        int amountOfRaffleTickets = raffleTicketsPerCourt.length;
        if (amountOfRaffleTickets > 0) {
            int randomIndex = random.nextInt(amountOfRaffleTickets);
            return raffleTicketsPerCourt[randomIndex];
        } else {
            throw new CourtAllocatorException("Could not find a court.");
        }
    }

    private Stream<? extends CourtWeight> returnAdequateAmountOfRaffleTicketsPerCourt(CourtWeight courtWeight) {
        ArrayList<CourtWeight> courtsList = new ArrayList<>();

        for (int i = 0; i < courtWeight.getWeight(); i++) {
            courtsList.add(courtWeight);
        }

        return courtsList.stream();
    }

}