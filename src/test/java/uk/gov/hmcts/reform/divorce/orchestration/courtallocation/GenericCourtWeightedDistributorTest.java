package uk.gov.hmcts.reform.divorce.orchestration.courtallocation;

import org.junit.Before;
import org.junit.Test;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import static java.math.BigDecimal.ONE;
import static java.math.BigDecimal.ZERO;
import static java.math.RoundingMode.DOWN;
import static java.util.Collections.singletonMap;
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
        desiredWorkloadPerCourt.put("court1", new BigDecimal("0.3"));
        desiredWorkloadPerCourt.put("court2", new BigDecimal("0.3"));
        desiredWorkloadPerCourt.put("court3", new BigDecimal("0.4"));

        divorceRatioPerFact = new HashMap();
        divorceRatioPerFact.put("unreasonable-behaviour", new BigDecimal("0.30"));
        divorceRatioPerFact.put("separation-2-years", new BigDecimal("0.37"));
        divorceRatioPerFact.put("separation-5-years", new BigDecimal("0.21"));
        divorceRatioPerFact.put("adultery", new BigDecimal("0.11"));
        divorceRatioPerFact.put("desertion", new BigDecimal("0.01"));
    }

    @Test
    public void shouldFollowDesiredWorkloadWhenNoSpecificFactConfigurationIsSet() {
        GenericCourtWeightedDistributor genericCourtWeightedDistributor =
            new GenericCourtWeightedDistributor(desiredWorkloadPerCourt, divorceRatioPerFact, null);

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

    /*
     * This will test that Generic allocation should allocate no cases to Court One and that cases are proportionately
     * distributed between the other courts, since court one's capacity is taken by fact specific allocation.
     */
    @Test
    public void shouldWorkAsExpected_WhenOneCourtIsFullyAllocatedToSpecificFacts() {
        String fact = "unreasonable-behaviour";
        Map<String, Map<String, BigDecimal>> specificCourtsAllocationPerFact = singletonMap(fact,
            singletonMap("court1", ONE)
        );

        GenericCourtWeightedDistributor genericCourtWeightedDistributor = new GenericCourtWeightedDistributor(
            desiredWorkloadPerCourt, divorceRatioPerFact, specificCourtsAllocationPerFact);

        //Run 1M times
        BigDecimal totalNumberOfAttempts = new BigDecimal(1000000);
        HashMap<String, BigDecimal> courtsDistribution = new HashMap<>();
        for (int i = 0; i < totalNumberOfAttempts.intValue(); i++) {
            String selectedCourt = genericCourtWeightedDistributor.selectCourt();
            BigDecimal casesPerCourt = courtsDistribution.getOrDefault(selectedCourt, ZERO);
            courtsDistribution.put(selectedCourt, casesPerCourt.add(ONE));
        }

        //Assert court one was not allocated
        BigDecimal acceptableError = acceptedDeviation.multiply(totalNumberOfAttempts);
        assertThat(courtsDistribution.keySet(), hasSize(2));
        assertThat(courtsDistribution.keySet(), not(contains("court1")));

        //Assert other courts got selected proportionately
        BigDecimal remainingWorkloadDiscountingSpecificFact = ONE.subtract(divorceRatioPerFact.get(fact));
        BigDecimal courtTwoProportionalGenericAllocation = desiredWorkloadPerCourt.get("court2")
            .divide(remainingWorkloadDiscountingSpecificFact, 3, DOWN);
        assertThat(courtsDistribution.get("court2"), closeTo(
            courtTwoProportionalGenericAllocation.multiply(totalNumberOfAttempts), acceptableError
        ));

        BigDecimal courtThreeProportionalGenericAllocation = desiredWorkloadPerCourt.get("court3")
            .divide(remainingWorkloadDiscountingSpecificFact, 3, DOWN);
        assertThat(courtsDistribution.get("court3"), closeTo(
            courtThreeProportionalGenericAllocation.multiply(totalNumberOfAttempts), acceptableError
        ));
    }

}