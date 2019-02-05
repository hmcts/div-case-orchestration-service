package uk.gov.hmcts.reform.divorce.orchestration.config.courtallocation;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.hmcts.reform.divorce.orchestration.courtallocation.CourtAllocationConfiguration;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.core.Is.is;

@SpringBootTest
@RunWith(SpringRunner.class)
public class CourtAllocationConfiguratorIntegrationTest {

    @Autowired
    private CourtAllocationConfiguration courtAllocationConfiguration;

    //TODO - make these pass
    @Test
    public void shouldSetUpConfigurationObjectWithEnvironmentVariableContents() {
        assertThat(courtAllocationConfiguration.getCourtsWeightedDistribution(), containsInAnyOrder(
                allOf(hasProperty("courtId", is("courtNumber1")), hasProperty("weight", is(1))),
                allOf(hasProperty("courtId", is("courtNumber2")), hasProperty("weight", is(2)))
        ));
        assertThat(courtAllocationConfiguration.getCourtsForSpecificReasons(), containsInAnyOrder(
                allOf(hasProperty("courtId", is("courtNumber3")), hasProperty("divorceReason", is("specificReason"))),
                allOf(hasProperty("courtId", is("courtNumber4")), hasProperty("divorceReason", is("someOtherReason")))
        ));
    }

}