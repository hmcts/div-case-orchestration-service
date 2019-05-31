package uk.gov.hmcts.reform.divorce.orchestration.config.courtallocation;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.math.BigDecimal;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasEntry;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.number.OrderingComparison.comparesEqualTo;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.courts.CourtEnum.EASTMIDLANDS;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.courts.CourtEnum.NORTHWEST;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.courts.CourtEnum.SERVICE_CENTER;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.courts.CourtEnum.SOUTHWEST;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.courts.CourtEnum.WESTMIDLANDS;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.facts.DivorceFacts.ADULTERY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.facts.DivorceFacts.DESERTION;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.facts.DivorceFacts.SEPARATION_FIVE_YEARS;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.facts.DivorceFacts.SEPARATION_TWO_YEARS;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.facts.DivorceFacts.UNREASONABLE_BEHAVIOUR;

@SpringBootTest
@RunWith(SpringRunner.class)
public class CourtAllocationConfiguratorIntegrationTest {

    @Autowired
    private CourtDistributionConfig courtDistributionConfig;

    @Test
    public void shouldSetUpConfigurationObjectWithEnvironmentVariableContents() {
        assertThat(courtDistributionConfig.getDivorceCasesRatio(), allOf(
            hasEntry(equalTo(UNREASONABLE_BEHAVIOUR), comparesEqualTo(new BigDecimal("0.30"))),
            hasEntry(equalTo(SEPARATION_TWO_YEARS), comparesEqualTo(new BigDecimal("0.37"))),
            hasEntry(equalTo(SEPARATION_FIVE_YEARS), comparesEqualTo(new BigDecimal("0.21"))),
            hasEntry(equalTo(DESERTION), comparesEqualTo(new BigDecimal("0.01"))),
            hasEntry(equalTo(ADULTERY), comparesEqualTo(new BigDecimal("0.11")))
        ));
        assertThat(courtDistributionConfig.getDistribution(), allOf(
            hasEntry(equalTo(SERVICE_CENTER.getId()), comparesEqualTo(new BigDecimal("1"))),
            hasEntry(equalTo(SOUTHWEST.getId()), comparesEqualTo(BigDecimal.ZERO)),
            hasEntry(equalTo(NORTHWEST.getId()), comparesEqualTo(BigDecimal.ZERO)),
            hasEntry(equalTo(WESTMIDLANDS.getId()), comparesEqualTo(BigDecimal.ZERO)),
            hasEntry(equalTo(EASTMIDLANDS.getId()), comparesEqualTo(BigDecimal.ZERO))
        ));

        assertThat(courtDistributionConfig.getFactAllocation(), allOf(
            hasEntry(is(UNREASONABLE_BEHAVIOUR), hasEntry(is(SERVICE_CENTER.getId()), comparesEqualTo(new BigDecimal("0.20")))),
            hasEntry(is(DESERTION), hasEntry(is(SERVICE_CENTER.getId()), comparesEqualTo(new BigDecimal("0.20")))),
            hasEntry(is(SEPARATION_FIVE_YEARS), hasEntry(is(SERVICE_CENTER.getId()), comparesEqualTo(new BigDecimal("0.20"))))
        ));
    }

}