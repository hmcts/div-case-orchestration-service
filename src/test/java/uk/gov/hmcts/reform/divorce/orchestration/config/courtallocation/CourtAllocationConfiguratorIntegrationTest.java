package uk.gov.hmcts.reform.divorce.orchestration.config.courtallocation;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.hmcts.reform.divorce.orchestration.courtallocation.CourtAllocationConfiguration;

import java.math.BigDecimal;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasEntry;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.number.OrderingComparison.comparesEqualTo;

@SpringBootTest
@RunWith(SpringRunner.class)
public class CourtAllocationConfiguratorIntegrationTest {

    @Autowired
    private CourtAllocationConfiguration courtAllocationConfiguration;

    @Test
    public void shouldSetUpConfigurationObjectWithEnvironmentVariableContents() {
        assertThat(courtAllocationConfiguration.getDivorceRatioPerFact(), allOf(
            hasEntry(equalTo("unreasonable-behaviour"), comparesEqualTo(new BigDecimal("0.30"))),
            hasEntry(equalTo("separation-2-years"), comparesEqualTo(new BigDecimal("0.37"))),
            hasEntry(equalTo("separation-5-years"), comparesEqualTo(new BigDecimal("0.21"))),
            hasEntry(equalTo("adultery"), comparesEqualTo(new BigDecimal("0.11"))),
            hasEntry(equalTo("desertion"), comparesEqualTo(new BigDecimal("0.01")))
        ));
        assertThat(courtAllocationConfiguration.getDesiredWorkloadPerCourt(), allOf(
            hasEntry(equalTo("serviceCentre"), comparesEqualTo(new BigDecimal("0.33"))),
            hasEntry(equalTo("eastMidlands"), comparesEqualTo(new BigDecimal("0.66")))
        ));
        assertThat(courtAllocationConfiguration.getSpecificCourtsAllocationPerFact(), allOf(
            hasEntry(is("unreasonable-behaviour"), hasEntry(is("serviceCentre"), comparesEqualTo(BigDecimal.ONE))),
            hasEntry(is("separation-2-years"), hasEntry(is("eastMidlands"), comparesEqualTo(BigDecimal.ONE)))
        ));
    }

}