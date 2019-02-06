package uk.gov.hmcts.reform.divorce.orchestration.courtallocation;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.math.BigDecimal;
import java.util.Map;

import static java.util.Collections.singletonMap;
import static org.junit.rules.ExpectedException.none;

/**
 * These are further tests written for the CourtAllocator implementation.
 * I didn't want to change the original tests for now, hence a separate class for further tests.
 */
public class DefaultCourtAllocatorFurtherTests {

    @Rule
    public ExpectedException expectedException = none();

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

//    desiredWorkloadPerCourt = new HashMap<>();TODO - Change this to a value that makes more sense in the new test,
//     TODO - if I can leave it like it is

//        desiredWorkloadPerCourt.put("serviceCentre", new BigDecimal("0.51"));
//        desiredWorkloadPerCourt.put("eastMidlands", ZERO);
//        desiredWorkloadPerCourt.put("westMidlands", ZERO);
//        desiredWorkloadPerCourt.put("southWest", new BigDecimal("0.245"));
//        desiredWorkloadPerCourt.put("northWest", new BigDecimal("0.245"));

    //TODO - should write a test that uses percentages other than 100% for court allocation

    //TODO - should I use the name "random" more?
    //TODO - rewrite these tests
//
//    @Test
//    public void shouldThrowException_IfCourtIsNotFound() {
//        expectedException.expect(RuntimeException.class);
//        expectedException.expectMessage("Could not find a court.");
//
//        CourtAllocator defaultCourtAllocator = new DefaultCourtAllocator(newHashSet(
//            new CourtWeight("eastMidlands", 0),
//            new CourtWeight("westMidlands", 0),
//            new CourtWeight("southWest", 0)
//        ));
//        defaultCourtAllocator.selectCourtForGivenDivorceFact(Optional.empty());
//    }
//
//    @Test
//    public void shouldThrowException_IfUnknownReasonIsUsed_AndCourtAllocatorIsOnlyConfiguredForSpecificReasons() {
//        expectedException.expect(CourtAllocatorException.class);
//        expectedException.expectMessage("Could not find a court.");
//
//        CourtAllocator defaultCourtAllocator = new DefaultCourtAllocator(newHashSet(),
//            newHashSet(
//                new CourtAllocationPerReason("northWest", "adultery")
//            )
//        );
//        defaultCourtAllocator.selectCourtForGivenDivorceFact(Optional.of("unknown-reason"));
//    }
//
//    @Test
//    public void shouldThrowException_WhenUsingEmptyConfiguration() {
//        expectedException.expect(CourtAllocatorException.class);
//        expectedException.expectMessage("Cannot build court allocator with empty configuration.");
//
//        new DefaultCourtAllocator(new CourtAllocationConfiguration(emptyMap(), emptyMap(), emptyMap()));
//    }//TODO

    //TODO - this is how I think it should be: We should assess that the percentages indicated were followed:
    // TODO i.e. at least 11% of 5ys went to CTSC and 100% of UB went to CTSC

}
