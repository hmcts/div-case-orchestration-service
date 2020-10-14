package uk.gov.hmcts.reform.divorce.orchestration.service.common;

import com.google.common.collect.ImmutableMap;
import org.junit.Test;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CaseDetails;

import java.util.HashMap;
import java.util.Map;

import static java.util.Arrays.asList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.CcdFields.SERVICE_APPLICATION_GRANTED;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.CcdStates.AWAITING_DA;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.CcdStates.AWAITING_SERVICE_CONSIDERATION;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NO_VALUE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.YES_VALUE;
import static uk.gov.hmcts.reform.divorce.orchestration.service.common.Conditions.isAwaitingServiceConsideration;
import static uk.gov.hmcts.reform.divorce.orchestration.service.common.Conditions.isServiceApplicationGranted;

public class ConditionsTest {

    @Test
    public void isServiceApplicationGrantedShouldReturnTrue() {
        asList(YES_VALUE, "YES", "yes").forEach(ConditionsTest::assertApplicationIsGrantedForValue);
    }

    @Test
    public void isServiceApplicationGrantedShouldReturnFalse() {
        asList(NO_VALUE, "", "no", "NO", null).forEach(ConditionsTest::assertApplicationIsNotGrantedForValue);
    }

    @Test
    public void isAwaitingServiceConsiderationShouldBeTrue() {
        CaseDetails caseDetails = CaseDetails.builder()
            .state(AWAITING_SERVICE_CONSIDERATION)
            .build();

        assertThat(isAwaitingServiceConsideration(caseDetails), is(true));
    }

    @Test
    public void isAwaitingServiceConsiderationShouldBeFalse() {
        CaseDetails caseDetails = CaseDetails.builder()
            .state(AWAITING_DA)
            .build();

        assertThat(isAwaitingServiceConsideration(caseDetails), is(false));
    }

    @Test
    public void isServiceApplicationGrantedShouldBeTrue() {
        Map<String, Object> caseData = ImmutableMap.of(SERVICE_APPLICATION_GRANTED, YES_VALUE);

        assertThat(isServiceApplicationGranted(caseData), is(true));
    }

    private static void assertApplicationIsNotGrantedForValue(Object value) {
        Map<String, Object> caseData = new HashMap<>();
        caseData.put(SERVICE_APPLICATION_GRANTED, value);

        assertThat(isServiceApplicationGranted(caseData), is(false));
    }

    private static void assertApplicationIsGrantedForValue(Object value) {
        Map<String, Object> caseData = new HashMap<>();
        caseData.put(SERVICE_APPLICATION_GRANTED, value);

        assertThat(isServiceApplicationGranted(caseData), is(true));
    }
}
