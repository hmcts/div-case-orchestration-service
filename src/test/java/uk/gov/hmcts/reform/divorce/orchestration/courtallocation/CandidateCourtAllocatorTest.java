package uk.gov.hmcts.reform.divorce.orchestration.courtallocation;

import org.junit.Before;
import org.junit.Test;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

/*
 * These are the tests copied from PFE
 */
public class CandidateCourtAllocatorTest {

    private Map<String, Double> divorceRatioPerFact;

    private double errorMargin = 0.005;
    private Map<String, Map<String, Double>> expectedFactsCourtPercentage;
    private Map<String, BigDecimal> desiredWorkloadPerCourt;
    private Map<String, Map<String, BigDecimal>> specificCourtsAllocationPerFact;

    @Before
    public void setUp() {
        desiredWorkloadPerCourt = new HashMap<>();
        desiredWorkloadPerCourt.put("CTSC", new BigDecimal("0.51"));//TODO - change CTSC to serviceCentre
        desiredWorkloadPerCourt.put("eastMidlands", new BigDecimal("0"));
        desiredWorkloadPerCourt.put("westMidlands", new BigDecimal("0"));
        desiredWorkloadPerCourt.put("southWest", new BigDecimal("0.30"));
        desiredWorkloadPerCourt.put("northWest", new BigDecimal("0.30"));//TODO - isn't this a percentage? how does this work?

        divorceRatioPerFact = new HashMap();
        divorceRatioPerFact.put("unreasonable-behaviour", 0.30);
        divorceRatioPerFact.put("separation-2-years", 0.37);
        divorceRatioPerFact.put("separation-5-years", 0.21);
        divorceRatioPerFact.put("adultery", 0.11);
        divorceRatioPerFact.put("desertion", 0.01);

        specificCourtsAllocationPerFact = new HashMap<>();
        HashMap<String, BigDecimal> unreasonableBehaviourCourtsAllocation = new HashMap<>();
        unreasonableBehaviourCourtsAllocation.put("CTSC", new BigDecimal("1"));
        specificCourtsAllocationPerFact.put("unreasonable-behaviour", unreasonableBehaviourCourtsAllocation);

        HashMap<String, BigDecimal> separation5YearsCourtsAllocation = new HashMap<>();
        specificCourtsAllocationPerFact.put("separation-5-years", separation5YearsCourtsAllocation);

        expectedFactsCourtPercentage = defineExpectedFactsCourtPercentage();
    }

//    private Map<String, Map> defineCourts() {
//        Map<String, Map> courts = new HashMap<>();
//        HashMap<Object, Object> ctscCourtDetail = new HashMap<>();
//        ctscCourtDetail.put("weight", 0.51);
//        HashMap<String, Double> ctscDivorceFactsRatio = new HashMap<>();
//        ctscDivorceFactsRatio.put("unreasonable-behaviour", 1.0);
//        ctscDivorceFactsRatio.put("separation-2-years", 0.0);
//        ctscDivorceFactsRatio.put("separation-5-years", 1.0);
//        ctscDivorceFactsRatio.put("adultery", 0.0);
//        ctscDivorceFactsRatio.put("desertion", 0.0);
//        ctscCourtDetail.put("divorceFactsRatio", ctscDivorceFactsRatio);
//
//        courts.put("CTSC", ctscCourtDetail);
//
//        HashMap<Object, Object> eastMidlandsDetail = new HashMap<>();
//        eastMidlandsDetail.put("weight", 0.0);
//        courts.put("eastMidlands", eastMidlandsDetail);
//
//        HashMap<Object, Object> westMidlandsDetail = new HashMap<>();
//        westMidlandsDetail.put("weight", 0.0);
//        courts.put("westMidlands", westMidlandsDetail);
//
//        HashMap<Object, Object> southWestDetail = new HashMap<>();
//        southWestDetail.put("weight", 0.30);
//        courts.put("southWest", southWestDetail);
//
//        HashMap<Object, Object> northWestDetail = new HashMap<>();
//        northWestDetail.put("weight", 0.30);
//        courts.put("northWest", northWestDetail);
//
//        return courts;
//    }

    private Map<String, Map<String, Double>> defineExpectedFactsCourtPercentage() {
        Map expectedFactsCourtPercentage = new HashMap();
        HashMap<Object, Double> unreasonableBehaviourFactsCourtPercentage = new HashMap<>();
        unreasonableBehaviourFactsCourtPercentage.put("CTSC", 0.3);
        unreasonableBehaviourFactsCourtPercentage.put("eastMidlands", 0.0);
        unreasonableBehaviourFactsCourtPercentage.put("westMidlands", 0.0);
        unreasonableBehaviourFactsCourtPercentage.put("southWest", 0.0);
        unreasonableBehaviourFactsCourtPercentage.put("northWest", 0.0);
        expectedFactsCourtPercentage.put("unreasonable-behaviour", unreasonableBehaviourFactsCourtPercentage);

        HashMap<Object, Double> separation2YearsFactsCourtPercentage = new HashMap<>();
        separation2YearsFactsCourtPercentage.put("CTSC", 0.0);
        separation2YearsFactsCourtPercentage.put("eastMidlands", 0.0);
        separation2YearsFactsCourtPercentage.put("westMidlands", 0.0);
        separation2YearsFactsCourtPercentage.put("southWest", 0.185);
        separation2YearsFactsCourtPercentage.put("northWest", 0.185);
        expectedFactsCourtPercentage.put("separation-2-years", separation2YearsFactsCourtPercentage);

        HashMap<Object, Double> separation5YearsFactsCourtPercentage = new HashMap<>();
        separation5YearsFactsCourtPercentage.put("CTSC", 0.21);
        separation5YearsFactsCourtPercentage.put("eastMidlands", 0.0);
        separation5YearsFactsCourtPercentage.put("westMidlands", 0.0);
        separation5YearsFactsCourtPercentage.put("southWest", 0.0);
        separation5YearsFactsCourtPercentage.put("northWest", 0.0);
        expectedFactsCourtPercentage.put("separation-5-years", separation5YearsFactsCourtPercentage);


        HashMap<Object, Double> adulteryFactsCourtPercentage = new HashMap<>();
        adulteryFactsCourtPercentage.put("CTSC", 0.0);
        adulteryFactsCourtPercentage.put("eastMidlands", 0.0);
        adulteryFactsCourtPercentage.put("westMidlands", 0.0);
        adulteryFactsCourtPercentage.put("southWest", 0.055);
        adulteryFactsCourtPercentage.put("northWest", 0.055);
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
            assertThat(courtAllocator.selectCourtForGivenDivorceReason(Optional.of(fact)), is(court));
        }
    }

    @Test
    public void givenOneMillionRecordsTheDataShouldBeDistributedAsExpected() {
        double count = 1000000;

//        Map<String, Map> localCourts = new HashMap(courts);
        CourtAllocator courtAllocator = new CandidateCourtAllocator(desiredWorkloadPerCourt, divorceRatioPerFact, specificCourtsAllocationPerFact);

        Map<String, Map<String, Integer>> factsAllocation = new HashMap();

        divorceRatioPerFact.keySet().forEach(fact -> {
            Map<String, Integer> factDetail = factsAllocation.getOrDefault(fact, new HashMap<>());

            desiredWorkloadPerCourt.keySet().forEach(courtName -> {
                factDetail.put(courtName, 0);
            });

            for (int i = 0; i < (count * divorceRatioPerFact.get(fact)); i++) {
                String selectedCourt = courtAllocator.selectCourtForGivenDivorceReason(Optional.of(fact));
                factDetail.put(selectedCourt, factDetail.get(selectedCourt) + 1);
            }
            factsAllocation.put(fact, factDetail);
        });

        divorceRatioPerFact.keySet().forEach(fact -> {
            desiredWorkloadPerCourt.keySet().forEach(courtName -> {
                assertThat("Fact " + fact + " for court " + courtName + " didn't match", new BigDecimal(Math.abs(expectedFactsCourtPercentage.get(fact).get(courtName).doubleValue() - (factsAllocation.get(fact).get(courtName).doubleValue() / count))).compareTo(new BigDecimal(errorMargin)) == -1, is(true));
            });
        });
    }

}