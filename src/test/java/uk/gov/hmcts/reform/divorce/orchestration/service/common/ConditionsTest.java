package uk.gov.hmcts.reform.divorce.orchestration.service.common;

import org.junit.Test;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CaseDetails;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.DivorceServiceApplication;

import java.util.HashMap;
import java.util.Map;

import static java.util.Arrays.asList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.CcdFields.SERVICE_APPLICATION_GRANTED;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.CcdFields.SERVICE_APPLICATION_TYPE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.CcdStates.AWAITING_DA;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.CcdStates.AWAITING_SERVICE_CONSIDERATION;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NO_VALUE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.YES_VALUE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.document.ApplicationServiceTypes.BAILIFF;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.document.ApplicationServiceTypes.DEEMED;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.document.ApplicationServiceTypes.DISPENSED;
import static uk.gov.hmcts.reform.divorce.orchestration.service.common.Conditions.isAwaitingServiceConsideration;
import static uk.gov.hmcts.reform.divorce.orchestration.service.common.Conditions.isServiceApplicationBailiff;
import static uk.gov.hmcts.reform.divorce.orchestration.service.common.Conditions.isServiceApplicationDeemed;
import static uk.gov.hmcts.reform.divorce.orchestration.service.common.Conditions.isServiceApplicationDeemedOrDispensed;
import static uk.gov.hmcts.reform.divorce.orchestration.service.common.Conditions.isServiceApplicationDispensed;
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
    public void isServiceApplicationGrantedForElementShouldReturnTrue() {
        asList(YES_VALUE, "YES", "yes").forEach(ConditionsTest::assertApplicationIsGrantedForElement);
    }

    @Test
    public void isServiceApplicationGrantedForElementShouldReturnFalse() {
        asList(NO_VALUE, "", "no", "NO", null).forEach(ConditionsTest::assertApplicationIsNotGrantedForElement);
    }

    @Test
    public void isServiceApplicationDispensedForValueShouldReturnTrue() {
        assertThat(isServiceApplicationDispensed(buildCaseData(SERVICE_APPLICATION_TYPE, DISPENSED)), is(true));
    }

    @Test
    public void isServiceApplicationDispensedForElementShouldReturnFalse() {
        asList(DEEMED, BAILIFF, "", null).forEach(ConditionsTest::assertApplicationIsNotDispensedForElement);
    }

    @Test
    public void isServiceApplicationDispensedForElementShouldReturnTrue() {
        assertThat(
            isServiceApplicationDispensed(buildModelWithType(DISPENSED)),
            is(true)
        );
    }

    @Test
    public void isServiceApplicationDispensedForValuesShouldReturnFalse() {
        asList(DEEMED, BAILIFF, "", null).forEach(ConditionsTest::assertApplicationIsNotDispensedForElement);
    }

    @Test
    public void isServiceApplicationDeemedForValueShouldReturnTrue() {
        assertThat(isServiceApplicationDeemed(buildCaseData(SERVICE_APPLICATION_TYPE, DEEMED)), is(true));
    }

    @Test
    public void isServiceApplicationDeemedForValuesShouldReturnFalse() {
        asList(DISPENSED, BAILIFF, "", null).forEach(ConditionsTest::assertApplicationIsNotDeemedForValue);
    }

    @Test
    public void isServiceApplicationDeemedForElementShouldReturnTrue() {
        assertThat(
            isServiceApplicationDeemed(buildModelWithType(DEEMED)),
            is(true)
        );
    }

    @Test
    public void isServiceApplicationDeemedForElementShouldReturnFalse() {
        asList(DISPENSED, BAILIFF, "", null).forEach(ConditionsTest::assertApplicationIsNotDeemedForElement);
    }

    @Test
    public void isServiceApplicationBailiffForValueShouldReturnTrue() {
        asList(BAILIFF).forEach(ConditionsTest::assertApplicationIsBailiffForValue);
    }

    @Test
    public void isServiceApplicationBailiffForValuesShouldReturnFalse() {
        asList(DEEMED, DISPENSED, "", null).forEach(ConditionsTest::assertApplicationIsNotBailiffForValue);
    }

    @Test
    public void isServiceApplicationDeemedOrDispensedShouldReturnTrue() {
        asList(DEEMED, DISPENSED).forEach(ConditionsTest::assertApplicationIsDeemedOrDispensed);
    }

    @Test
    public void isServiceApplicationDeemedOrDispensedShouldReturnFalse() {
        asList(BAILIFF, "", "other", null).forEach(ConditionsTest::assertApplicationIsNotDeemedOrDispensed);
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

    private static void assertApplicationIsNotDeemedForValue(String value) {
        Map<String, Object> caseData = buildCaseData(SERVICE_APPLICATION_TYPE, value);

        assertThat(isServiceApplicationDeemed(caseData), is(false));
    }

    private static void assertApplicationIsBailiffForValue(String value) {
        Map<String, Object> caseData = buildCaseData(SERVICE_APPLICATION_TYPE, value);

        assertThat(isServiceApplicationBailiff(caseData), is(true));
    }

    private static void assertApplicationIsNotBailiffForValue(String value) {
        Map<String, Object> caseData = buildCaseData(SERVICE_APPLICATION_TYPE, value);

        assertThat(isServiceApplicationBailiff(caseData), is(false));
    }

    private static void assertApplicationIsDeemedOrDispensed(String value) {
        assertThat(isServiceApplicationDeemedOrDispensed(buildModelWithType(value)), is(true));
    }

    private static void assertApplicationIsNotDeemedOrDispensed(String value) {
        assertThat(isServiceApplicationDeemedOrDispensed(buildModelWithType(value)), is(false));
    }

    private static void assertApplicationIsNotGrantedForValue(String value) {
        Map<String, Object> caseData = buildCaseData(SERVICE_APPLICATION_GRANTED, value);

        assertThat(isServiceApplicationGranted(caseData), is(false));
    }

    private static Map<String, Object> buildCaseData(String field, String value) {
        Map<String, Object> caseData = new HashMap<>();
        caseData.put(field, value);

        return caseData;
    }

    private static void assertApplicationIsNotGrantedForElement(String value) {
        assertThat(
            isServiceApplicationGranted(DivorceServiceApplication.builder().applicationGranted(value).build()),
            is(false)
        );
    }

    private static void assertApplicationIsGrantedForElement(String value) {
        assertThat(
            isServiceApplicationGranted(DivorceServiceApplication.builder().applicationGranted(value).build()),
            is(true)
        );
    }

    private static void assertApplicationIsGrantedForValue(String value) {
        Map<String, Object> caseData = buildCaseData(SERVICE_APPLICATION_GRANTED, value);

        assertThat(isServiceApplicationGranted(caseData), is(true));
    }

    private static void assertApplicationIsNotDispensedForElement(String value) {
        assertThat(
            isServiceApplicationDispensed(buildModelWithType(value)),
            is(false)
        );
    }

    private static void assertApplicationIsNotDeemedForElement(String value) {
        assertThat(
            isServiceApplicationDeemed(buildModelWithType(value)),
            is(false)
        );
    }

    private static DivorceServiceApplication buildModelWithType(String type) {
        return DivorceServiceApplication.builder().type(type).build();
    }
}
