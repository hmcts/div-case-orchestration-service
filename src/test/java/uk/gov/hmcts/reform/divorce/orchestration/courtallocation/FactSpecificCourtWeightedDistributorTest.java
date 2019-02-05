package uk.gov.hmcts.reform.divorce.orchestration.courtallocation;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import static org.junit.rules.ExpectedException.none;

public class FactSpecificCourtWeightedDistributorTest {

    @Rule
    public ExpectedException expectedException = none();

    @Test
    public void shouldThrowExceptionWhenFactSpecificAllocationIsOverOneHundredPercent(){
        expectedException.expect(CourtAllocatorException.class);
        expectedException.expectMessage("Configured fact allocation for \"fact1\" went over 100%.");

        HashMap<String, Map<String, BigDecimal>> specificCourtsAllocationPerFact = new HashMap<>();
        HashMap<String, BigDecimal> courtAllocationForFact = new HashMap<>();
        courtAllocationForFact.put("court1", new BigDecimal("0.5"));
        courtAllocationForFact.put("court2", new BigDecimal("0.5"));
        courtAllocationForFact.put("court3", new BigDecimal("0.5"));
        specificCourtsAllocationPerFact.put("fact1", courtAllocationForFact);
        new FactSpecificCourtWeightedDistributor(specificCourtsAllocationPerFact);
    }

}