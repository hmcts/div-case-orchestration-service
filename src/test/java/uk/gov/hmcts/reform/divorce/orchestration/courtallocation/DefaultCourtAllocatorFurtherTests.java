package uk.gov.hmcts.reform.divorce.orchestration.courtallocation;

/**
 * These are further tests written for the CourtAllocator implementation.
 * I didn't want to change the original tests for now, hence a separate class for further tests.
 */
public class DefaultCourtAllocatorFurtherTests {

//    desiredWorkloadPerCourt = new HashMap<>();//TODO - Change this to a value that makes more sense in the new test, if I can leave it like it is
//        desiredWorkloadPerCourt.put("serviceCentre", new BigDecimal("0.51"));
//        desiredWorkloadPerCourt.put("eastMidlands", ZERO);
//        desiredWorkloadPerCourt.put("westMidlands", ZERO);
//        desiredWorkloadPerCourt.put("southWest", new BigDecimal("0.245"));
//        desiredWorkloadPerCourt.put("northWest", new BigDecimal("0.245"));

    //TODO - should write a test that uses percentages other than 100% for court allocation

    //TODO - do we need a test for when court is overallocated?


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

    //TODO - this is how I think it should be: We should assess that the percentages indicated were followed: i.e. at least 11% of 5ys went to CTSC and 100% of UB went to CTSC

}
