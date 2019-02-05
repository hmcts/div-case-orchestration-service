package uk.gov.hmcts.reform.divorce.orchestration.config.courtallocation;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.hmcts.reform.divorce.orchestration.courtallocation.CourtAllocationConfiguration;

import java.math.BigDecimal;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasEntry;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.number.OrderingComparison.comparesEqualTo;
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

    //TODO - divorceFactsRatio doesn't really have to be configurable per environment
    @Test
    public void shouldConfigureCourtAllocatorWithGeneralCourtWeightOnly() throws Exception {
        String jsonContent = loadResourceAsString("courtAllocation/config-with-court-weight-only.json");

        CourtAllocationConfiguration courtAllocationConfiguration = courtAllocationConfigurator
            .setUpEnvironmentCourtAllocationConfiguration(jsonContent);

//        assertThat(courtAllocationConfiguration.getDivorceRatioPerFact());//TODO - I'm not going to assert this because I might just hardcode this bit
        assertThat(courtAllocationConfiguration.getDesiredWorkloadPerCourt().size(), is(2));
        assertThat(courtAllocationConfiguration.getDesiredWorkloadPerCourt(), allOf(
            hasEntry(equalTo("courtNumber1"), comparesEqualTo(new BigDecimal("0.33"))),
            hasEntry(equalTo("courtNumber2"), comparesEqualTo(new BigDecimal("0.66")))
        ));
        assertThat(courtAllocationConfiguration.getSpecificCourtsAllocationPerFact().size(), is(0));
    }

    @Test
    public void shouldConfigureCourtAllocatorWithGeneralCourtWeightOnly_AndReasonSpecificCourt() throws Exception {
        String jsonContent = loadResourceAsString("courtAllocation/config-with-court-weight-and-reasons.json");

        CourtAllocationConfiguration courtAllocationConfiguration = courtAllocationConfigurator
            .setUpEnvironmentCourtAllocationConfiguration(jsonContent);

//        assertThat(courtAllocationConfiguration.getDivorceRatioPerFact());//TODO - I'm not going to assert this because I might just hardcode this bit
        assertThat(courtAllocationConfiguration.getDesiredWorkloadPerCourt(), allOf(
            hasEntry(equalTo("courtNumber1"), comparesEqualTo(new BigDecimal("0.33"))),
            hasEntry(equalTo("courtNumber2"), comparesEqualTo(new BigDecimal("0.66")))
        ));
        assertThat(courtAllocationConfiguration.getSpecificCourtsAllocationPerFact(), allOf(
            hasEntry(is("specificReason"), hasEntry(is("courtNumber3"), comparesEqualTo(BigDecimal.ONE))),
            hasEntry(is("someOtherReason"), hasEntry(is("courtNumber4"), comparesEqualTo(BigDecimal.ONE)))
        ));
    }

}