package uk.gov.hmcts.reform.divorce.orchestration.courtallocation;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.Optional;
import java.util.Set;

import static com.google.common.collect.Sets.newHashSet;
import static java.lang.String.format;
import static java.math.BigDecimal.ONE;
import static java.math.BigDecimal.ZERO;
import static java.util.Collections.emptySet;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.closeTo;
import static org.hamcrest.Matchers.isOneOf;
import static org.hamcrest.core.Is.is;
import static org.junit.rules.ExpectedException.none;

public class DefaultCourtAllocatorTest {

    private final BigDecimal acceptedDeviation = new BigDecimal("0.005");//TODO - delete default court allocation and use the other one - but leave the original one for reference

    @Rule
    public ExpectedException expectedException = none();//TODO - check whether there are test from here that I want to reuse in the new suite - before I delete this.

    @Test
    public void shouldApplyRandomWeightedSelectionToCourts() {

        Set<CourtWeight> courts = newHashSet(
            new CourtWeight("eastMidlands", 1),
            new CourtWeight("westMidlands", 1),
            new CourtWeight("southWest", 1),
            new CourtWeight("northWest", 1),
            new CourtWeight("serviceCentre", 2)
        );
        CourtAllocator courtAllocator = new DefaultCourtAllocator(courts);

        //Select court 1 million times
        BigDecimal totalNumberOfAttempts = new BigDecimal(1000000);
        HashMap<String, BigDecimal> courtsDistribution = new HashMap<>();
        for (int i = 0; i < totalNumberOfAttempts.intValue(); i++) {
            String selectedCourt = courtAllocator.selectCourtForGivenDivorceFact(Optional.empty());
            BigDecimal casesPerCourt = courtsDistribution.getOrDefault(selectedCourt, ZERO);
            courtsDistribution.put(selectedCourt, casesPerCourt.add(ONE));
        }

        //Assert randomisation works as expected
        BigDecimal sumOfWeightPoints = courts.stream()
            .map(CourtWeight::getWeight)
            .map(BigDecimal::new)
            .reduce(ZERO, BigDecimal::add);
        for (CourtWeight courtWeight : courts) {
            BigDecimal individualCourtWeight = BigDecimal.valueOf(courtWeight.getWeight());
            BigDecimal expectedTimesCourtWasChosen = totalNumberOfAttempts
                .divide(sumOfWeightPoints, RoundingMode.CEILING)
                .multiply(individualCourtWeight);

            BigDecimal acceptableError = acceptedDeviation.multiply(expectedTimesCourtWasChosen);
            BigDecimal timesCourtWasChosen = courtsDistribution.get(courtWeight.getCourtId());
            assertThat(format("Court %s was not selected near enough times to how much it was expected to have been.",
                courtWeight.getCourtId()),
                timesCourtWasChosen,
                closeTo(expectedTimesCourtWasChosen, acceptableError));
        }
    }

    @Test
    public void shouldAssignToSpecificCourtIfReasonForDivorceIsSpecified_OrRandomlyChoseCourtsForUnspecifiedReasons() {
        CourtAllocator courtAllocator = new DefaultCourtAllocator(
            newHashSet(
                new CourtWeight("eastMidlands", 1),
                new CourtWeight("westMidlands", 1),
                new CourtWeight("southWest", 1)
            ),
            newHashSet(
                new CourtAllocationPerReason("northWest", "adultery"),
                new CourtAllocationPerReason("serviceCentre", "desertion")
            )
        );

        String courtForAdulteryReason = courtAllocator.selectCourtForGivenDivorceFact(Optional.of("adultery"));
        String courtForDesertionReason = courtAllocator.selectCourtForGivenDivorceFact(Optional.of("desertion"));
        String courtForUnreasonableBehaviourReason = courtAllocator.selectCourtForGivenDivorceFact(
            Optional.of("unreasonable-behaviour"));

        assertThat(courtForAdulteryReason, is("northWest"));
        assertThat(courtForDesertionReason, is("serviceCentre"));
        assertThat(courtForUnreasonableBehaviourReason, isOneOf("eastMidlands", "westMidlands", "southWest"));
    }

    @Test
    public void shouldAssignToSpecificCourtIfReasonForDivorceIsSpecified() {
        CourtAllocator courtAllocator = new DefaultCourtAllocator(
            emptySet(),
            newHashSet(
                new CourtAllocationPerReason("northWest", "adultery"),
                new CourtAllocationPerReason("serviceCentre", "desertion")
            )
        );

        String courtForAdulteryReason = courtAllocator.selectCourtForGivenDivorceFact(Optional.of("adultery"));
        String courtForDesertionReason = courtAllocator.selectCourtForGivenDivorceFact(Optional.of("desertion"));

        assertThat(courtForAdulteryReason, is("northWest"));
        assertThat(courtForDesertionReason, is("serviceCentre"));
    }

    @Test
    public void shouldThrowExceptionIfReasonIsDuplicate_InAllocationPerReason() {
        expectedException.expect(IllegalStateException.class);

        new DefaultCourtAllocator(
            newHashSet(
                new CourtWeight("eastMidlands", 1),
                new CourtWeight("westMidlands", 1),
                new CourtWeight("southWest", 1)
            ),
            newHashSet(
                new CourtAllocationPerReason("northWest", "adultery"),
                new CourtAllocationPerReason("southWest", "adultery")
            )
        );
    }

    @Test
    public void shouldThrowException_IfCourtIsNotFound() {
        expectedException.expect(RuntimeException.class);
        expectedException.expectMessage("Could not find a court.");

        CourtAllocator defaultCourtAllocator = new DefaultCourtAllocator(newHashSet(
            new CourtWeight("eastMidlands", 0),
            new CourtWeight("westMidlands", 0),
            new CourtWeight("southWest", 0)
        ));
        defaultCourtAllocator.selectCourtForGivenDivorceFact(Optional.empty());
    }

    @Test
    public void shouldThrowException_IfUnknownReasonIsUsed_AndCourtAllocatorIsOnlyConfiguredForSpecificReasons() {
        expectedException.expect(CourtAllocatorException.class);
        expectedException.expectMessage("Could not find a court.");

        CourtAllocator defaultCourtAllocator = new DefaultCourtAllocator(newHashSet(),
            newHashSet(
                new CourtAllocationPerReason("northWest", "adultery")
            )
        );
        defaultCourtAllocator.selectCourtForGivenDivorceFact(Optional.of("unknown-reason"));
    }

    @Test
    public void shouldThrowException_WhenUsingEmptyConfiguration() {
        expectedException.expect(CourtAllocatorException.class);
        expectedException.expectMessage("Cannot build court allocator with empty configuration.");

        new DefaultCourtAllocator(new CourtAllocationConfiguration());
    }

}