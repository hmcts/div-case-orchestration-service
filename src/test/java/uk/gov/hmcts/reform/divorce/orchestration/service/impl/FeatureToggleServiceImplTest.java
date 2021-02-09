package uk.gov.hmcts.reform.divorce.orchestration.service.impl;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.Features;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

@RunWith(SpringRunner.class)
@SpringBootTest
@TestPropertySource(properties = {
    "FEATURE_SHARE_A_CASE=true",
    "FEATURE_PAPER_UPDATE=true",
    "FEATURE_REPRESENTED_RESPONDENT_JOURNEY=true"
})
public class FeatureToggleServiceImplTest {

    @Autowired
    private FeatureToggleServiceImpl classUnderTest;

    @Test
    public void shouldReturnAdequateValues() {
        //Not registered in application.yml (default value will be false)
        assertThat(classUnderTest.isFeatureEnabled(Features.SOLICITOR_DN_REJECT_AND_AMEND), is(false));
        assertThat(classUnderTest.isFeatureEnabled(Features.PAY_BY_ACCOUNT), is(false));

        //Default values
        assertThat(classUnderTest.isFeatureEnabled(Features.RESPONDENT_SOLICITOR_DETAILS), is(true));
        assertThat(classUnderTest.isFeatureEnabled(Features.DN_REFUSAL), is(true));

        //Modified values
        assertThat(classUnderTest.isFeatureEnabled(Features.PAPER_UPDATE), is(true));
        assertThat(classUnderTest.isFeatureEnabled(Features.SHARE_A_CASE), is(true));
        assertThat(classUnderTest.isFeatureEnabled(Features.REPRESENTED_RESPONDENT_JOURNEY), is(true));
    }

}