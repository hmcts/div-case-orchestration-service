package uk.gov.hmcts.reform.divorce.orchestration.courtallocation;

import org.apache.commons.math3.distribution.EnumeratedDistribution;
import org.apache.commons.math3.util.Pair;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static java.util.Collections.singletonList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;

public class EnumeratedDistributionExplorationTest {

    @Test
    public void testUnallocatedPercentageBehaviour_ShouldNeverReturnNull() {
        EnumeratedDistribution<String> enumeratedDistribution =
            new EnumeratedDistribution<>(singletonList(new Pair("test", 0.5)));

        Map<String, Integer> courtAllocation = new HashMap<>();
        for (int i = 0; i < 1000000; i++) {
            String selectedItem = enumeratedDistribution.sample();
            courtAllocation.put(selectedItem, courtAllocation.getOrDefault(selectedItem, 0) + 1);
        }

        assertThat(courtAllocation.entrySet(), hasSize(1));
        assertThat(courtAllocation.get("test"), equalTo(1000000));
    }

}