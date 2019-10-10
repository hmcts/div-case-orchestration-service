package uk.gov.hmcts.reform.divorce.orchestration.service.impl;

import org.junit.Before;
import org.junit.Test;
import org.springframework.test.util.ReflectionTestUtils;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.Features;

import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

public class FeatureToggleServiceImplTest {

    private FeatureToggleServiceImpl classToTest = new FeatureToggleServiceImpl();


    @Before
    public void setup() {
        Map<String, String> toggles = new HashMap<>();
        toggles.put(Features.DN_REFUSAL.getName(), "true");
        toggles.put(Features.RESPONDENT_SOLICITOR_DETAILS.getName(), "false");

        ReflectionTestUtils.setField(classToTest, "toggle", toggles);
    }

    @Test
    public void givenToggleEnabled_thenReturnTrue() {
        assertThat(classToTest.isFeatureEnabled(Features.DN_REFUSAL), is(true));
    }

    @Test
    public void givenToggleFalse_thenReturnFalse() {
        assertThat(classToTest.isFeatureEnabled(Features.RESPONDENT_SOLICITOR_DETAILS), is(false));
    }
}
