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

/**
 * These are the tests copied from PFE and rewritten in Java as closely to what the JS version is as possible.
 * The expected data was remodelled to fit the new model, but the behaviour is the same with the
 * increased assurance that the end court allocation result respects the desired workload.
 */
public class CourtAllocatorOriginalTest {

    private BigDecimal errorMargin = new BigDecimal("0.005");

    private Map<String, BigDecimal> desiredWorkloadPerCourt;
    private Map<String, BigDecimal> divorceRatioPerFact;
    private Map<String, Map<String, BigDecimal>> specificCourtsAllocationPerFact;

    private Map<String, Map<String, Double>> expectedFactsCourtPercentage;

    @Before
    public void setUp() {
        desiredWorkloadPerCourt = new HashMap<>();
        desiredWorkloadPerCourt.put("serviceCentre", new BigDecimal("0.51"));
        desiredWorkloadPerCourt.put("eastMidlands", ZERO);
        desiredWorkloadPerCourt.put("westMidlands", ZERO);
        desiredWorkloadPerCourt.put("southWest", new BigDecimal("0.245"));
        desiredWorkloadPerCourt.put("northWest", new BigDecimal("0.245"));

        divorceRatioPerFact = new HashMap();
        divorceRatioPerFact.put("unreasonable-behaviour", new BigDecimal("0.30"));
        divorceRatioPerFact.put("separation-2-years", new BigDecimal("0.37"));
        divorceRatioPerFact.put("separation-5-years", new BigDecimal("0.21"));
        divorceRatioPerFact.put("adultery", new BigDecimal("0.11"));
        divorceRatioPerFact.put("desertion", new BigDecimal("0.01"));

        specificCourtsAllocationPerFact = new HashMap<>();
        HashMap<String, BigDecimal> unreasonableBehaviourCourtsAllocation = new HashMap<>();
        unreasonableBehaviourCourtsAllocation.put("serviceCentre", ONE);
        specificCourtsAllocationPerFact.put("unreasonable-behaviour", unreasonableBehaviourCourtsAllocation);

        HashMap<String, BigDecimal> separation5YearsCourtsAllocation = new HashMap<>();
        separation5YearsCourtsAllocation.put("serviceCentre", ONE);
        specificCourtsAllocationPerFact.put("separation-5-years", separation5YearsCourtsAllocation);

        expectedFactsCourtPercentage = defineExpectedFactsCourtPercentage();
    }

    private Map<String, Map<String, Double>> defineExpectedFactsCourtPercentage() {
        //This is the percentage of the total of cases
        HashMap<Object, Double> unreasonableBehaviourFactsCourtPercentage = new HashMap<>();
        unreasonableBehaviourFactsCourtPercentage.put("serviceCentre", 0.3);
        unreasonableBehaviourFactsCourtPercentage.put("eastMidlands", 0.0);
        unreasonableBehaviourFactsCourtPercentage.put("westMidlands", 0.0);
        unreasonableBehaviourFactsCourtPercentage.put("southWest", 0.0);
        unreasonableBehaviourFactsCourtPercentage.put("northWest", 0.0);

        HashMap<Object, Double> separation2YearsFactsCourtPercentage = new HashMap<>();
        separation2YearsFactsCourtPercentage.put("serviceCentre", 0.0);
        separation2YearsFactsCourtPercentage.put("eastMidlands", 0.0);
        separation2YearsFactsCourtPercentage.put("westMidlands", 0.0);
        separation2YearsFactsCourtPercentage.put("southWest", 0.185);
        separation2YearsFactsCourtPercentage.put("northWest", 0.185);

        HashMap<Object, Double> separation5YearsFactsCourtPercentage = new HashMap<>();
        separation5YearsFactsCourtPercentage.put("serviceCentre", 0.21);
        separation5YearsFactsCourtPercentage.put("eastMidlands", 0.0);
        separation5YearsFactsCourtPercentage.put("westMidlands", 0.0);
        separation5YearsFactsCourtPercentage.put("southWest", 0.0);
        separation5YearsFactsCourtPercentage.put("northWest", 0.0);

        HashMap<Object, Double> adulteryFactsCourtPercentage = new HashMap<>();
        adulteryFactsCourtPercentage.put("serviceCentre", 0.0);
        adulteryFactsCourtPercentage.put("eastMidlands", 0.0);
        adulteryFactsCourtPercentage.put("westMidlands", 0.0);
        adulteryFactsCourtPercentage.put("southWest", 0.055);
        adulteryFactsCourtPercentage.put("northWest", 0.055);

        HashMap<Object, Double> desertionFactsCourtPercentage = new HashMap<>();
        desertionFactsCourtPercentage.put("serviceCentre", 0.0);
        desertionFactsCourtPercentage.put("eastMidlands", 0.0);
        desertionFactsCourtPercentage.put("westMidlands", 0.0);
        desertionFactsCourtPercentage.put("southWest", 0.005);
        desertionFactsCourtPercentage.put("northWest", 0.005);

        Map expectedFactsCourtPercentage = new HashMap();
        expectedFactsCourtPercentage.put("unreasonable-behaviour", unreasonableBehaviourFactsCourtPercentage);
        expectedFactsCourtPercentage.put("separation-2-years", separation2YearsFactsCourtPercentage);
        expectedFactsCourtPercentage.put("separation-5-years", separation5YearsFactsCourtPercentage);
        expectedFactsCourtPercentage.put("adultery", adulteryFactsCourtPercentage);
        expectedFactsCourtPercentage.put("desertion", desertionFactsCourtPercentage);
        return expectedFactsCourtPercentage;
    }

    @Test(expected = CourtAllocatorException.class)
    public void errorWhenTotalFactsAllocationGreaterThanCourtAllocation() {
        Map<String, BigDecimal> adulteryCourtsAllocation =
            specificCourtsAllocationPerFact.getOrDefault("adultery", new HashMap<>());
        adulteryCourtsAllocation.put("serviceCentre", new BigDecimal("0.8"));
        specificCourtsAllocationPerFact.put("adultery", adulteryCourtsAllocation);

        new DefaultCourtAllocator(desiredWorkloadPerCourt, divorceRatioPerFact, specificCourtsAllocationPerFact);
    }

    @Test(expected = CourtAllocatorException.class)
    public void errorWhenFactsAllocationGreaterThanOneHundredPercent() {
        Map<String, BigDecimal> unreasonableBehaviourCourtsAllocation =
            specificCourtsAllocationPerFact.getOrDefault("unreasonable-behaviour", new HashMap<>());
        unreasonableBehaviourCourtsAllocation.put("southWest", new BigDecimal("0.4"));
        specificCourtsAllocationPerFact.put("unreasonable-behaviour", unreasonableBehaviourCourtsAllocation);

        new DefaultCourtAllocator(desiredWorkloadPerCourt, divorceRatioPerFact, specificCourtsAllocationPerFact);
    }

    @Test
    public void whenFactAllocatedToSameOneCourtReturnSameCourt() {
        String fact = "unreasonable-behaviour";
        String court = "serviceCentre";

        Map<String, BigDecimal> separationTwoYearsCourtsAllocation =
            specificCourtsAllocationPerFact.getOrDefault("separation-2-years", new HashMap<>());
        separationTwoYearsCourtsAllocation.put(court, ZERO);
        specificCourtsAllocationPerFact.put("separation-2-years", separationTwoYearsCourtsAllocation);

        Map<String, BigDecimal> unreasonableBehaviourCourtsAllocation =
            specificCourtsAllocationPerFact.getOrDefault(fact, new HashMap<>());
        unreasonableBehaviourCourtsAllocation.put(court, ONE);
        specificCourtsAllocationPerFact.put(fact, unreasonableBehaviourCourtsAllocation);

        CourtAllocator courtAllocator =
            new DefaultCourtAllocator(desiredWorkloadPerCourt, divorceRatioPerFact, specificCourtsAllocationPerFact);

        int iterations = 10;
        for (int i = 0; i < iterations; i++) {
            assertThat(courtAllocator.selectCourtForGivenDivorceFact(Optional.of(fact)), is(court));
        }
    }

    @Test
    public void givenOneMillionRecordsTheDataShouldBeDistributedAsExpectedByOriginalTest() {
        CourtAllocator courtAllocator =
            new DefaultCourtAllocator(desiredWorkloadPerCourt, divorceRatioPerFact, specificCourtsAllocationPerFact);

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

        divorceRatioPerFact.keySet().forEach(fact -> desiredWorkloadPerCourt.keySet().forEach(courtName -> {
            BigDecimal expectedPercentageOfCasesWithGivenFactDistributedToGivenCourt =
                new BigDecimal(expectedFactsCourtPercentage.get(fact).get(courtName).doubleValue());
            BigDecimal actualAllocationForGivenFactAndGivenCourt =
                new BigDecimal(actualFactsAllocation.get(fact).get(courtName));
            BigDecimal actualPercentageOfTotalCasesAllocatedToGivenFactAndCourt =
                actualAllocationForGivenFactAndGivenCourt.divide(numberOfAttempts);
            assertThat(String.format("Fact %s for court %s didn't match", fact, courtName),
                actualPercentageOfTotalCasesAllocatedToGivenFactAndCourt,
                closeTo(expectedPercentageOfCasesWithGivenFactDistributedToGivenCourt, errorMargin)
            );
        }));
    }

}