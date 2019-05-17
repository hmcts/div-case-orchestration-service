package uk.gov.hmcts.reform.divorce.orchestration.config.courtallocation;

import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import uk.gov.hmcts.reform.divorce.orchestration.courtallocation.CourtAllocationConfiguration;

import java.math.BigDecimal;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasEntry;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.number.OrderingComparison.comparesEqualTo;
import static uk.gov.hmcts.reform.divorce.orchestration.testutil.ResourceLoader.loadResourceAsString;

public class CourtAllocationConfiguratorUnitTest {

    @Autowired
    private CourtAllocationConfigurator courtAllocationConfigurator;

    @Before
    public void setUp() {
        courtAllocationConfigurator = new CourtAllocationConfigurator();
    }

    @Test
    public void shouldConfigureCourtAllocatorWithGeneralCourtWeightOnly() throws Exception {
        String jsonContent = loadResourceAsString("courtAllocation/config-with-court-weight-only.json");

        CourtAllocationConfiguration courtAllocationConfiguration = courtAllocationConfigurator
            .setUpCourtAllocationConfiguration();

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
            .setUpCourtAllocationConfiguration();

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