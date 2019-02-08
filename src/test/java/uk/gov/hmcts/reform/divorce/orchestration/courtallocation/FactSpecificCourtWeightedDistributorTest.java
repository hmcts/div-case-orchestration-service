package uk.gov.hmcts.reform.divorce.orchestration.courtallocation;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import static java.math.BigDecimal.ONE;
import static java.math.BigDecimal.ZERO;
import static java.util.Collections.emptyMap;
import static java.util.Collections.singletonMap;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.closeTo;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.rules.ExpectedException.none;

public class FactSpecificCourtWeightedDistributorTest {

    private final BigDecimal acceptedDeviation = new BigDecimal("0.005");

    @Rule
    public ExpectedException expectedException = none();

    @Test
    public void shouldReturnCourtsAccordinglyWithFullAllocation() {
        Map<String, Map<String, BigDecimal>> specificCourtsAllocationPerFact = new HashMap<>();
        Map<String, BigDecimal> factOneCourtAllocation = new HashMap<>();
        factOneCourtAllocation.put("court1", ONE);
        specificCourtsAllocationPerFact.put("fact1", factOneCourtAllocation);
        FactSpecificCourtWeightedDistributor factSpecificCourtWeightedDistributor =
            new FactSpecificCourtWeightedDistributor(specificCourtsAllocationPerFact);

        Map<String, Integer> courtAllocation = new HashMap<>();
        for (int i = 0; i < 1000000; i++) {
            factSpecificCourtWeightedDistributor.selectCourt("fact1").ifPresent(selectedCourt ->
                courtAllocation.put(selectedCourt, courtAllocation.getOrDefault(selectedCourt, 0) + 1));
        }

        assertThat(courtAllocation.get("court1"), equalTo(1000000));
    }

    @Test
    public void shouldReturnCourtsAccordinglyWithPartialAllocation() {
        Map<String, Map<String, BigDecimal>> specificCourtsAllocationPerFact = new HashMap<>();
        Map<String, BigDecimal> factTwoCourtAllocation = new HashMap<>();
        BigDecimal courtAllocation = new BigDecimal("0.5");
        factTwoCourtAllocation.put("court2", courtAllocation);
        specificCourtsAllocationPerFact.put("fact2", factTwoCourtAllocation);
        FactSpecificCourtWeightedDistributor factSpecificCourtWeightedDistributor =
            new FactSpecificCourtWeightedDistributor(specificCourtsAllocationPerFact);

        Map<String, BigDecimal> courtsAllocation = new HashMap<>();
        BigDecimal numberOfAttempts = new BigDecimal("1000000");
        for (int i = 0; i < numberOfAttempts.intValue(); i++) {
            String selectedCourt = factSpecificCourtWeightedDistributor.selectCourt("fact2").orElse(null);
            courtsAllocation.put(selectedCourt, courtsAllocation.getOrDefault(selectedCourt, ZERO).add(ONE));
        }

        BigDecimal errorMargin = acceptedDeviation.multiply(numberOfAttempts);
        assertThat(courtsAllocation.get("court2"), closeTo(courtAllocation.multiply(numberOfAttempts), errorMargin));
        assertThat(courtsAllocation.get(null), closeTo(courtAllocation.multiply(numberOfAttempts), errorMargin));
    }

    @Test
    public void shouldReturnEmptyOptional_IfUnknownFactIsPassed() {
        FactSpecificCourtWeightedDistributor factSpecificCourtWeightedDistributor =
            new FactSpecificCourtWeightedDistributor(singletonMap("fact1", singletonMap("court1", ONE)));

        assertThat(factSpecificCourtWeightedDistributor.selectCourt("unknown-fact").isPresent(), equalTo(false));
    }

    @Test
    public void shouldReturnEmptyOptional_IfNullFactIsPassed() {
        FactSpecificCourtWeightedDistributor factSpecificCourtWeightedDistributor =
            new FactSpecificCourtWeightedDistributor(emptyMap());

        assertThat(factSpecificCourtWeightedDistributor.selectCourt(null).isPresent(), equalTo(false));
    }

    @Test
    public void shouldThrowExceptionWhenFactSpecificAllocationIsOverOneHundredPercent() {
        expectedException.expect(CourtAllocatorException.class);
        expectedException.expectMessage("Configured fact allocation for \"fact1\" went over 100%.");

        Map<String, BigDecimal> courtAllocationForFact = new HashMap<>();
        courtAllocationForFact.put("court1", new BigDecimal("0.5"));
        courtAllocationForFact.put("court2", new BigDecimal("0.5"));
        courtAllocationForFact.put("court3", new BigDecimal("0.5"));
        Map<String, Map<String, BigDecimal>> specificCourtsAllocationPerFact =
            singletonMap("fact1", courtAllocationForFact);

        new FactSpecificCourtWeightedDistributor(specificCourtsAllocationPerFact);
    }

}