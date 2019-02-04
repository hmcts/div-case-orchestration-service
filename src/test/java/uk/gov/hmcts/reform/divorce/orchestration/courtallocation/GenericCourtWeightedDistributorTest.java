package uk.gov.hmcts.reform.divorce.orchestration.courtallocation;

import org.junit.Before;
import org.junit.Test;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import static java.math.BigDecimal.ONE;
import static java.math.BigDecimal.ZERO;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.closeTo;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.not;

public class GenericCourtWeightedDistributorTest {

    private final BigDecimal acceptedDeviation = new BigDecimal("0.005");

    private Map<String, BigDecimal> desiredWorkloadPerCourt;
    private Map<String, BigDecimal> divorceRatioPerFact;

    @Before
    public void setUp() {
        desiredWorkloadPerCourt = new HashMap<>();
        desiredWorkloadPerCourt.put("court1", new BigDecimal("0.3"));//TODO - understand how this works in PFE - for now, let's use percentage
        desiredWorkloadPerCourt.put("court2", new BigDecimal("0.3"));
        desiredWorkloadPerCourt.put("court3", new BigDecimal("0.4"));

        divorceRatioPerFact = new HashMap();
        divorceRatioPerFact.put("unreasonable-behaviour", new BigDecimal(0.30));
        divorceRatioPerFact.put("separation-2-years", new BigDecimal(0.37));
        divorceRatioPerFact.put("separation-5-years", new BigDecimal(0.21));
        divorceRatioPerFact.put("adultery", new BigDecimal(0.11));
        divorceRatioPerFact.put("desertion", new BigDecimal(0.01));
    }

    @Test
    public void shouldFollowDesiredWorkloadWhenNoSpecificFactConfigurationIsSet() {
        GenericCourtWeightedDistributor genericCourtWeightedDistributor = new GenericCourtWeightedDistributor(desiredWorkloadPerCourt, divorceRatioPerFact, null);

        //Run 1M times
        BigDecimal totalNumberOfAttempts = new BigDecimal(1000000);
        HashMap<String, BigDecimal> courtsDistribution = new HashMap<>();
        for (int i = 0; i < totalNumberOfAttempts.intValue(); i++) {
            String selectedCourt = genericCourtWeightedDistributor.selectCourt();
            BigDecimal casesPerCourt = courtsDistribution.getOrDefault(selectedCourt, ZERO);
            courtsDistribution.put(selectedCourt, casesPerCourt.add(ONE));
        }

        //Assert results are as expected
        BigDecimal acceptableError = acceptedDeviation.multiply(totalNumberOfAttempts);
        assertThat(courtsDistribution.keySet(), hasSize(3));
        desiredWorkloadPerCourt.entrySet().stream().forEach(entry -> {
            BigDecimal expectedNumberOfSelections = entry.getValue().multiply(totalNumberOfAttempts);
            BigDecimal actualNumberOfSelections = courtsDistribution.get(entry.getKey());
            assertThat(actualNumberOfSelections, closeTo(expectedNumberOfSelections, acceptableError));
        });
    }

    @Test
    public void shouldAllocateNoGenericCasesToCourtOne_SinceItsCapacityIsTakenByFactSpecificAllocation() {//TODO - test what happens if I overallocate
        HashMap<String, Map<String, BigDecimal>> specificCourtsAllocationPerFact = new HashMap<>();
        HashMap<String, BigDecimal> unreasonableBehaviourAllocation = new HashMap<>();
        unreasonableBehaviourAllocation.put("court1", BigDecimal.ONE);
        specificCourtsAllocationPerFact.put("unreasonable-behaviour", unreasonableBehaviourAllocation);

        GenericCourtWeightedDistributor genericCourtWeightedDistributor = new GenericCourtWeightedDistributor(desiredWorkloadPerCourt, divorceRatioPerFact, specificCourtsAllocationPerFact);

        //Run 1M times
        BigDecimal totalNumberOfAttempts = new BigDecimal(1000000);
        HashMap<String, BigDecimal> courtsDistribution = new HashMap<>();
        for (int i = 0; i < totalNumberOfAttempts.intValue(); i++) {
            String selectedCourt = genericCourtWeightedDistributor.selectCourt();
            BigDecimal casesPerCourt = courtsDistribution.getOrDefault(selectedCourt, ZERO);
            courtsDistribution.put(selectedCourt, casesPerCourt.add(ONE));
        }

        //Assert results are as expected
        BigDecimal acceptableError = acceptedDeviation.multiply(totalNumberOfAttempts);
        assertThat(courtsDistribution.keySet(), hasSize(2));
        assertThat(courtsDistribution.keySet(), not(contains("court1")));

        assertThat(courtsDistribution.get("court2"), closeTo(new BigDecimal("0.43").multiply(totalNumberOfAttempts), acceptableError));
        assertThat(courtsDistribution.get("court3"), closeTo(new BigDecimal("0.57").multiply(totalNumberOfAttempts), acceptableError));//TODO - make expected object
    }

}