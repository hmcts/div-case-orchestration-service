package uk.gov.hmcts.reform.divorce.orchestration.courtallocation;

import org.junit.Before;
import org.junit.Test;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static java.lang.String.format;
import static java.math.BigDecimal.ONE;
import static java.math.BigDecimal.ZERO;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.number.BigDecimalCloseTo.closeTo;

/*
 * These are the tests copied from PFE
 */
public class CandidateCourtAllocatorTest {

    private BigDecimal errorMargin = new BigDecimal("0.005");

    private Map<String, BigDecimal> desiredWorkloadPerCourt;
    private Map<String, BigDecimal> divorceRatioPerFact;
    private Map<String, Map<String, BigDecimal>> specificCourtsAllocationPerFact;

    private Map<String, Map<String, Double>> expectedFactsCourtPercentage;

    @Before
    public void setUp() {
        desiredWorkloadPerCourt = new HashMap<>();//TODO - I'll have to change this as it makes no sense - double check with Qiang
        desiredWorkloadPerCourt.put("CTSC", new BigDecimal("0.4"));//TODO - change CTSC to serviceCentre
        desiredWorkloadPerCourt.put("eastMidlands", new BigDecimal("0"));
        desiredWorkloadPerCourt.put("westMidlands", new BigDecimal("0"));
        desiredWorkloadPerCourt.put("southWest", new BigDecimal("0.30"));
        desiredWorkloadPerCourt.put("northWest", new BigDecimal("0.30"));
        //TODO - rewrite test case numbers

        divorceRatioPerFact = new HashMap();
        divorceRatioPerFact.put("unreasonable-behaviour", new BigDecimal("0.30"));
        divorceRatioPerFact.put("separation-2-years", new BigDecimal("0.37"));
        divorceRatioPerFact.put("separation-5-years", new BigDecimal("0.21"));
        divorceRatioPerFact.put("adultery", new BigDecimal("0.11"));
        divorceRatioPerFact.put("desertion", new BigDecimal("0.01"));

        specificCourtsAllocationPerFact = new HashMap<>();
        HashMap<String, BigDecimal> unreasonableBehaviourCourtsAllocation = new HashMap<>();
        unreasonableBehaviourCourtsAllocation.put("CTSC", new BigDecimal("1"));
        specificCourtsAllocationPerFact.put("unreasonable-behaviour", unreasonableBehaviourCourtsAllocation);

        HashMap<String, BigDecimal> separation5YearsCourtsAllocation = new HashMap<>();
        separation5YearsCourtsAllocation.put("CTSC", new BigDecimal("0.11"));
        specificCourtsAllocationPerFact.put("separation-5-years", separation5YearsCourtsAllocation);

        expectedFactsCourtPercentage = defineExpectedFactsCourtPercentage();
    }

    private Map<String, Map<String, Double>> defineExpectedFactsCourtPercentage() {//TODO - think of different way to assess this
        //This is the percentage of the total of cases
        Map expectedFactsCourtPercentage = new HashMap();
        HashMap<Object, Double> unreasonableBehaviourFactsCourtPercentage = new HashMap<>();
        unreasonableBehaviourFactsCourtPercentage.put("CTSC", 0.3);
        unreasonableBehaviourFactsCourtPercentage.put("eastMidlands", 0.0);
        unreasonableBehaviourFactsCourtPercentage.put("westMidlands", 0.0);
        unreasonableBehaviourFactsCourtPercentage.put("southWest", 0.0);
        unreasonableBehaviourFactsCourtPercentage.put("northWest", 0.0);
        expectedFactsCourtPercentage.put("unreasonable-behaviour", unreasonableBehaviourFactsCourtPercentage);

        HashMap<Object, Double> separation2YearsFactsCourtPercentage = new HashMap<>();
        separation2YearsFactsCourtPercentage.put("CTSC", 0.042);//TODO - check if old tests pass
        separation2YearsFactsCourtPercentage.put("eastMidlands", 0.0);
        separation2YearsFactsCourtPercentage.put("westMidlands", 0.0);
        separation2YearsFactsCourtPercentage.put("southWest", 0.164);
        separation2YearsFactsCourtPercentage.put("northWest", 0.164);
        expectedFactsCourtPercentage.put("separation-2-years", separation2YearsFactsCourtPercentage);

        HashMap<Object, Double> separation5YearsFactsCourtPercentage = new HashMap<>();
        separation5YearsFactsCourtPercentage.put("CTSC", 0.082);
        separation5YearsFactsCourtPercentage.put("eastMidlands", 0.0);
        separation5YearsFactsCourtPercentage.put("westMidlands", 0.0);
        separation5YearsFactsCourtPercentage.put("southWest", 0.093);//TODO - not sure this is right... - problem is - just because 11% of 5ys cases are handled by CTSC, it doesn't mean that the other ones shouldn't also be handled by it as unspecified courts
        separation5YearsFactsCourtPercentage.put("northWest", 0.093);
        expectedFactsCourtPercentage.put("separation-5-years", separation5YearsFactsCourtPercentage);

        HashMap<Object, Double> adulteryFactsCourtPercentage = new HashMap<>();
        adulteryFactsCourtPercentage.put("CTSC", 0.013);
        adulteryFactsCourtPercentage.put("eastMidlands", 0.0);
        adulteryFactsCourtPercentage.put("westMidlands", 0.0);
        adulteryFactsCourtPercentage.put("southWest", 0.049);
        adulteryFactsCourtPercentage.put("northWest", 0.049);
        expectedFactsCourtPercentage.put("adultery", adulteryFactsCourtPercentage);

        HashMap<Object, Double> desertionFactsCourtPercentage = new HashMap<>();
        desertionFactsCourtPercentage.put("CTSC", 0.0);
        desertionFactsCourtPercentage.put("eastMidlands", 0.0);
        desertionFactsCourtPercentage.put("westMidlands", 0.0);
        desertionFactsCourtPercentage.put("southWest", 0.005);
        desertionFactsCourtPercentage.put("northWest", 0.005);
        expectedFactsCourtPercentage.put("desertion", desertionFactsCourtPercentage);

        return expectedFactsCourtPercentage;
    }

    @Test(expected = RuntimeException.class)
    public void errorWhenTotalFactsAllocationGreaterThanCourtAllocation() {
//        Map<String, Map>  localCourts = new HashMap(courts);
//        Map ctsc = localCourts.get("CTSC");
//        Map divorceFactsRatio = (Map) ctsc.get("divorceFactsRatio");
//        divorceFactsRatio.put("adultery", 0.8);
        //TODO - actually - maybe I should make a copy before changing this
        Map<String, BigDecimal> adulteryCourtsAllocation = specificCourtsAllocationPerFact.getOrDefault("adultery", new HashMap<>());
        adulteryCourtsAllocation.put("CTSC", new BigDecimal("0.8"));
        specificCourtsAllocationPerFact.put("adultery", adulteryCourtsAllocation);

        new CandidateCourtAllocator(desiredWorkloadPerCourt, divorceRatioPerFact, specificCourtsAllocationPerFact);
    }

    @Test(expected = RuntimeException.class)
    public void errorWhenFactsAllocationGreaterThanOneHundredPercent() {
//        Map<String, Map>  localCourts = new HashMap(courts);
//        Map ctsc = localCourts.get("southWest");
//        Map divorceFactsRatio = (Map) ctsc.get("divorceFactsRatio");
//        divorceFactsRatio.put("unreasonable-behaviour", 0.4);
        //TODO - actually - maybe I should make a copy before changing this
        Map<String, BigDecimal> unreasonableBehaviourCourtsAllocation = specificCourtsAllocationPerFact.getOrDefault("unreasonable-behaviour", new HashMap<>());
        unreasonableBehaviourCourtsAllocation.put("southWest", new BigDecimal("0.4"));
        specificCourtsAllocationPerFact.put("unreasonable-behaviour", unreasonableBehaviourCourtsAllocation);

        new CandidateCourtAllocator(desiredWorkloadPerCourt, divorceRatioPerFact, specificCourtsAllocationPerFact);
    }

    @Test
    public void whenFactAllocatedToSameOneCourtReturnSameCourt() {
        int iterations = 10;
        String fact = "unreasonable-behaviour";
        String court = "CTSC";

//        Map<String, Map>  localCourts = new HashMap(courts);
//        Map ctsc = localCourts.get(court);
//        Map divorceFactsRatio = (Map) ctsc.get("divorceFactsRatio");
//        divorceFactsRatio.put("separation-2-years", 0.0);
//        divorceFactsRatio.put(fact, 1.0);
        //TODO - actually - maybe I should make a copy before changing this
        Map<String, BigDecimal> separationTwoYearsCourtsAllocation = specificCourtsAllocationPerFact.getOrDefault("separation-2-years", new HashMap<>());
        separationTwoYearsCourtsAllocation.put(court, new BigDecimal("0"));
        specificCourtsAllocationPerFact.put("separation-2-years", separationTwoYearsCourtsAllocation);

        Map<String, BigDecimal> unreasonableBehaviourCourtsAllocation = specificCourtsAllocationPerFact.getOrDefault(fact, new HashMap<>());
        unreasonableBehaviourCourtsAllocation.put(court, new BigDecimal("1"));
        specificCourtsAllocationPerFact.put(fact, unreasonableBehaviourCourtsAllocation);

        CourtAllocator courtAllocator = new CandidateCourtAllocator(desiredWorkloadPerCourt, divorceRatioPerFact, specificCourtsAllocationPerFact);

        for (int i = 0; i < iterations; i++) {
            assertThat(courtAllocator.selectCourtForGivenDivorceFact(Optional.of(fact)), is(court));
        }
    }

    @Test
    public void givenOneMillionRecordsTheDataShouldBeDistributedAsExpected() {
        CourtAllocator courtAllocator = new CandidateCourtAllocator(desiredWorkloadPerCourt, divorceRatioPerFact, specificCourtsAllocationPerFact);

        BigDecimal numberOfAttempts = new BigDecimal(1000000);
        Map<String, Map<String, Integer>> actualFactsAllocation = new HashMap();
        HashMap<String, BigDecimal> courtsDistribution = new HashMap<>();
        divorceRatioPerFact.keySet().forEach(fact -> {
            Map<String, Integer> factDetail = actualFactsAllocation.getOrDefault(fact, new HashMap<>());

            desiredWorkloadPerCourt.keySet().forEach(courtName -> {
                factDetail.put(courtName, 0);
            });

            for (int i = 0; i < divorceRatioPerFact.get(fact).multiply(numberOfAttempts).intValue(); i++) {
                String selectedCourt = courtAllocator.selectCourtForGivenDivorceFact(Optional.of(fact));

                BigDecimal casesPerCourt = courtsDistribution.getOrDefault(selectedCourt, ZERO);
                courtsDistribution.put(selectedCourt, casesPerCourt.add(ONE));

                if (!factDetail.containsKey(selectedCourt)) {
                    throw new RuntimeException(format("No fact detail assigned to \"%s\" court", selectedCourt));
                } else {
                    factDetail.put(selectedCourt, factDetail.get(selectedCourt) + 1);
                }
            }
            actualFactsAllocation.put(fact, factDetail);
        });

        BigDecimal acceptableError = errorMargin.multiply(numberOfAttempts);
        desiredWorkloadPerCourt.entrySet().stream().forEach(e -> {
            assertThat(format("Court %s was not selected as many times as it was expected to have been.", e.getKey()),
                courtsDistribution.getOrDefault(e.getKey(), ZERO),
                closeTo(e.getValue().multiply(numberOfAttempts), acceptableError)
            );
        });

        //TODO - will comment for a bit
//        divorceRatioPerFact.keySet().forEach(fact -> {
//            desiredWorkloadPerCourt.keySet().forEach(courtName -> {
//                BigDecimal expectedPercentageOfCasesWithGivenFactDistributedToGivenCourt = new BigDecimal(expectedFactsCourtPercentage.get(fact).get(courtName).doubleValue());//TODO - total of cases?
//                BigDecimal actualAllocationForGivenFactAndGivenCourt = new BigDecimal(actualFactsAllocation.get(fact).get(courtName));//TODO - this is an int, not a double
//                BigDecimal actualPercentageOfTotalCasesAllocatedToGivenFactAndCourt = actualAllocationForGivenFactAndGivenCourt.divide(numberOfAttempts);
//                assertThat(String.format("Fact %s for court %s didn't match", fact, courtName),
//                    actualPercentageOfTotalCasesAllocatedToGivenFactAndCourt,
//                    closeTo(expectedPercentageOfCasesWithGivenFactDistributedToGivenCourt, errorMargin)
//                );
//            });
//        });

        //TODO - this is how I think it should be: We should assess that the percentages indicated were followed: i.e. at least 11% of 5ys went to CTSC and 100% of UB went to CTSC

    }

}