package uk.gov.hmcts.reform.divorce.orchestration.courtallocation;

import org.apache.commons.math3.distribution.EnumeratedDistribution;
import org.apache.commons.math3.util.Pair;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static java.math.BigDecimal.ONE;
import static java.math.BigDecimal.ZERO;
import static java.util.Collections.emptyMap;
import static java.util.Collections.singletonMap;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.closeTo;
import static org.hamcrest.core.Is.is;
import static org.junit.rules.ExpectedException.none;

/**
 * These are further tests written for the CourtAllocator implementation.
 * I didn't want to change the original tests for now, hence a separate class for further tests.
 */
public class DefaultCourtAllocatorFurtherTests {

    private BigDecimal errorMargin = new BigDecimal("0.005");

    @Rule
    public ExpectedException expectedException = none();

    @Test
    public void shouldRespectDesiredWorkload_RegardlessOfFactRatio_OrFactSpecificAllocation() {
        final BigDecimal desiredWorkload = new BigDecimal("0.5");
        Map<String, BigDecimal> desiredWorkloadPerCourt = new HashMap<>();
        desiredWorkloadPerCourt.put("court1", desiredWorkload);
        desiredWorkloadPerCourt.put("court2", desiredWorkload);

        Map<String, BigDecimal> divorceRatioPerFact = new HashMap<>();
        divorceRatioPerFact.put("fact1", new BigDecimal("0.4"));
        divorceRatioPerFact.put("fact2", new BigDecimal("0.6"));
        List<Pair<String, Double>> pairs = divorceRatioPerFact.entrySet().stream()
            .map(e -> new Pair<>(e.getKey(), e.getValue().doubleValue()))
            .collect(Collectors.toList());
        EnumeratedDistribution<String> factsDistribution = new EnumeratedDistribution<>(pairs);

        Map<String, Map<String, BigDecimal>> specificCourtsAllocationPerFact = new HashMap<>();
        specificCourtsAllocationPerFact.put("fact1", singletonMap("court1", new BigDecimal("0.3")));

        CourtAllocator courtAllocator =
            new DefaultCourtAllocator(desiredWorkloadPerCourt, divorceRatioPerFact, specificCourtsAllocationPerFact);

        BigDecimal numberOfAttempts = new BigDecimal(1000000);
        Map<String, BigDecimal> courtAllocation = new HashMap<>();
        for (int i = 0; i < numberOfAttempts.intValue(); i++) {
            Optional<String> fact = Optional.of(factsDistribution.sample());
            String selectedCourt = courtAllocator.selectCourtForGivenDivorceFact(fact);
            courtAllocation.put(selectedCourt, courtAllocation.getOrDefault(selectedCourt, ZERO).add(ONE));
        }

        assertThat(courtAllocation.get("court1"), closeTo(
            desiredWorkload.multiply(numberOfAttempts), errorMargin.multiply(numberOfAttempts)
        ));
        assertThat(courtAllocation.get("court2"), closeTo(
            desiredWorkload.multiply(numberOfAttempts), errorMargin.multiply(numberOfAttempts)
        ));
    }

    @Test
    public void shouldSelectCourt_IfUnknownReasonIsUsed() {
        CourtAllocator defaultCourtAllocator =
            new DefaultCourtAllocator(singletonMap("court1", ONE), emptyMap(), emptyMap());
        String selectedCourt = defaultCourtAllocator.selectCourtForGivenDivorceFact(Optional.of("unknown-reason"));

        assertThat(selectedCourt, is("court1"));
    }

    @Test
    public void errorWhenTotalFactsAllocationGreaterThanCourtAllocation() {
        expectedException.expect(CourtAllocatorException.class);
        expectedException.expectMessage("Court \"court1\" was overallocated. Desired workload is 0.30 but total allocation was 0.35");

        Map<String, BigDecimal> desiredWorkloadPerCourt = singletonMap("court1", new BigDecimal("0.30"));
        Map<String, BigDecimal> divorceRatioPerFact = singletonMap("fact1", new BigDecimal("0.35"));
        Map<String, Map<String, BigDecimal>> specificCourtsAllocationPerFact = singletonMap(
            "fact1",
            singletonMap("court1", BigDecimal.ONE)
        );

        new DefaultCourtAllocator(desiredWorkloadPerCourt,
            divorceRatioPerFact, specificCourtsAllocationPerFact);
    }

    @Test
    public void shouldThrowException_IfNoWorkloadIsConfiguredForCourts() {
        expectedException.expect(CourtAllocatorException.class);
        expectedException.expectMessage("No workload was configured for any courts.");

        CourtAllocator defaultCourtAllocator =
            new DefaultCourtAllocator(singletonMap("court1", ZERO), emptyMap(), emptyMap());
        defaultCourtAllocator.selectCourtForGivenDivorceFact(Optional.empty());
    }

    @Test
    public void shouldThrowException_WhenUsingEmptyConfiguration() {
        expectedException.expect(CourtAllocatorException.class);
        expectedException.expectMessage("No workload was configured for any courts.");

        new DefaultCourtAllocator(emptyMap(), emptyMap(), emptyMap());
    }

}
