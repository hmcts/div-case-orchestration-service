package uk.gov.hmcts.reform.divorce.orchestration.config.courtallocation;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.hmcts.reform.divorce.orchestration.courtallocation.CourtAllocationConfiguration;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.core.Is.is;
import static uk.gov.hmcts.reform.divorce.orchestration.testutil.ResourceLoader.loadResourceAsString;

@RunWith(SpringRunner.class)
@SpringBootTest
public class CourtAllocationConfiguratorUnitTest {

    @Autowired
    private ObjectMapper objectMapper;

    private CourtAllocationConfigurator courtAllocationConfigurator;

    @Before
    public void setUp() {
        courtAllocationConfigurator = new CourtAllocationConfigurator(objectMapper);
    }

    @Test
    public void shouldConfigureCourtAllocatorWithGeneralCourtWeightOnly() throws Exception {
        String jsonContent = loadResourceAsString("courtAllocation/config-with-court-weight-only.json");

        CourtAllocationConfiguration courtAllocationConfiguration = courtAllocationConfigurator
            .setUpEnvironmentCourtAllocationConfiguration(jsonContent);

        assertThat(courtAllocationConfiguration.getCourtsWeightedDistribution(), containsInAnyOrder(
                allOf(hasProperty("courtId", is("courtNumber1")), hasProperty("weight", is(1))),
                allOf(hasProperty("courtId", is("courtNumber2")), hasProperty("weight", is(2)))
        ));
        assertThat(courtAllocationConfiguration.getCourtsForSpecificReasons(), empty());
    }

    @Test
    public void shouldConfigureCourtAllocatorWithGeneralCourtWeightOnly_AndReasonSpecificCourt() throws Exception {
        String jsonContent = loadResourceAsString("courtAllocation/config-with-court-weight-and-reasons.json");

        CourtAllocationConfiguration courtAllocationConfiguration = courtAllocationConfigurator
            .setUpEnvironmentCourtAllocationConfiguration(jsonContent);

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