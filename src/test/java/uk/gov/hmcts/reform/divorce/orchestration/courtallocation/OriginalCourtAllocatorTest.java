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
public class OriginalCourtAllocatorTest {

    private Map<String, Double> caseDistribution;
    private Map<String, Map> courts;

    private double errorMargin = 0.005;
    private Map<String, Map<String, Double>> expectedFactsCourtPercentage;

    @Before
    public void setUp() {
        caseDistribution = new HashMap();
        caseDistribution.put("unreasonable-behaviour", 0.30);
        caseDistribution.put("separation-2-years", 0.37);
        caseDistribution.put("separation-5-years", 0.21);
        caseDistribution.put("adultery", 0.11);
        caseDistribution.put("desertion", 0.01);

        courts = defineCourts();

        expectedFactsCourtPercentage = defineExpectedFactsCourtPercentage();
    }

    private Map<String, Map> defineCourts() {
        Map<String, Map> courts = new HashMap<>();
        HashMap<Object, Object> ctscCourtDetail = new HashMap<>();
        ctscCourtDetail.put("weight", 0.51);
        HashMap<String, Double> ctscDivorceFactsRatio = new HashMap<>();
        ctscDivorceFactsRatio.put("unreasonable-behaviour", 1.0);
        ctscDivorceFactsRatio.put("separation-2-years", 0.0);
        ctscDivorceFactsRatio.put("separation-5-years", 1.0);
        ctscDivorceFactsRatio.put("adultery", 0.0);
        ctscDivorceFactsRatio.put("desertion", 0.0);
        ctscCourtDetail.put("divorceFactsRatio", ctscDivorceFactsRatio);

        courts.put("CTSC", ctscCourtDetail);

        HashMap<Object, Object> eastMidlandsDetail = new HashMap<>();
        eastMidlandsDetail.put("weight", 0.0);
        courts.put("eastMidlands", eastMidlandsDetail);

        HashMap<Object, Object> westMidlandsDetail = new HashMap<>();
        westMidlandsDetail.put("weight", 0.0);
        courts.put("westMidlands", westMidlandsDetail);

        HashMap<Object, Object> southWestDetail = new HashMap<>();
        southWestDetail.put("weight", 0.30);
        courts.put("southWest", southWestDetail);

        HashMap<Object, Object> northWestDetail = new HashMap<>();
        northWestDetail.put("weight", 0.30);
        courts.put("northWest", northWestDetail);

        return courts;
    }

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
        Map<String, Map>  localCourts = new HashMap(courts);
        Map ctsc = localCourts.get("CTSC");
        Map divorceFactsRatio = (Map) ctsc.get("divorceFactsRatio");
        divorceFactsRatio.put("adultery", 0.8);

        new OriginalCourtAllocator(caseDistribution, localCourts);
    }

    @Test(expected = RuntimeException.class)
    public void errorWhenFactsAllocationGreaterThanOneHundredPercent() {
        Map<String, Map>  localCourts = new HashMap(courts);
        Map ctsc = localCourts.get("southWest");
        Map divorceFactsRatio = (Map) ctsc.get("divorceFactsRatio");
        divorceFactsRatio.put("unreasonable-behaviour", 0.4);

        new OriginalCourtAllocator(caseDistribution, localCourts);
    }

    @Test
    public void whenFactAllocatedToSameOneCourtReturnSameCourt() {
        int iterations = 10;
        String fact = "unreasonable-behaviour";
        String court = "CTSC";

        Map<String, Map>  localCourts = new HashMap(courts);
        Map ctsc = localCourts.get(court);
        Map divorceFactsRatio = (Map) ctsc.get("divorceFactsRatio");
        divorceFactsRatio.put("separation-2-years", 0.0);
        divorceFactsRatio.put(fact, 1.0);

        CourtAllocator courtAllocator = new OriginalCourtAllocator(caseDistribution, localCourts);

        for (int i = 0; i < iterations; i++) {
            assertThat(courtAllocator.selectCourtForGivenDivorceFact(Optional.of(fact)), is(court));
        }
    }

    @Test
    public void givenOneMillionRecordsTheDataShouldBeDistributedAsExpected() {
        double count = 1000000;

        Map<String, Map> localCourts = new HashMap(courts);
        CourtAllocator courtAllocator = new OriginalCourtAllocator(caseDistribution, localCourts);

        Map<String, Map<String, Integer>> factsAllocation = new HashMap();

        caseDistribution.keySet().forEach(fact -> {
            Map<String, Integer> factDetail = factsAllocation.getOrDefault(fact, new HashMap<>());

            localCourts.keySet().forEach(courtName -> {
                factDetail.put(courtName, 0);
            });

            for (int i = 0; i < (count * caseDistribution.get(fact)); i++) {
                String selectedCourt = courtAllocator.selectCourtForGivenDivorceFact(Optional.of(fact));
                factDetail.put(selectedCourt, factDetail.get(selectedCourt) + 1);
            }
            factsAllocation.put(fact, factDetail);
        });

        caseDistribution.keySet().forEach(fact -> {
            localCourts.keySet().forEach(courtName -> {
                assertThat("Fact " + fact + " for court " + courtName + " didn't match", new BigDecimal(Math.abs(expectedFactsCourtPercentage.get(fact).get(courtName).doubleValue() - (factsAllocation.get(fact).get(courtName).doubleValue() / count))).compareTo(new BigDecimal(errorMargin)) == -1, is(true));
            });
        });
    }

}