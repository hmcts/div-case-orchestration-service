package uk.gov.hmcts.reform.divorce.orchestration.util;

import org.junit.Test;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CaseDetails;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.document.ServiceRefusalDecision;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.InvalidDataForTaskException;

import java.util.HashMap;
import java.util.Map;

import static java.util.Collections.EMPTY_MAP;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.CcdFields.SERVICE_APPLICATION_GRANTED;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.CcdFields.SERVICE_APPLICATION_REFUSAL_REASON;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.CcdFields.SERVICE_APPLICATION_TYPE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.CcdStates.AWAITING_DA;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.CcdStates.AWAITING_SERVICE_CONSIDERATION;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.YES_VALUE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.document.ApplicationServiceTypes.DEEMED;
import static uk.gov.hmcts.reform.divorce.orchestration.util.ServiceApplicationRefusalHelper.getServiceApplicationGranted;
import static uk.gov.hmcts.reform.divorce.orchestration.util.ServiceApplicationRefusalHelper.getServiceApplicationRefusalReason;
import static uk.gov.hmcts.reform.divorce.orchestration.util.ServiceApplicationRefusalHelper.getServiceApplicationType;
import static uk.gov.hmcts.reform.divorce.orchestration.util.ServiceApplicationRefusalHelper.isAwaitingServiceConsideration;
import static uk.gov.hmcts.reform.divorce.orchestration.util.ServiceApplicationRefusalHelper.isDeemedApplication;
import static uk.gov.hmcts.reform.divorce.orchestration.util.ServiceApplicationRefusalHelper.isDispensedApplication;
import static uk.gov.hmcts.reform.divorce.orchestration.util.ServiceApplicationRefusalHelper.isDraft;
import static uk.gov.hmcts.reform.divorce.orchestration.util.ServiceApplicationRefusalHelper.isFinal;
import static uk.gov.hmcts.reform.divorce.orchestration.util.ServiceApplicationRefusalHelper.isServiceApplicationGranted;

public class ServiceApplicationRefusalHelperTest {

    public static final String TEST_SERVICE_APPLICATION_REFUSAL_REASON = "Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod";

    @Test
    public void getServiceApplicationRefusalReasonShouldReturnValidValue() {
        Map<String, Object> caseData = buildCaseDataWithField(
            SERVICE_APPLICATION_REFUSAL_REASON,
            TEST_SERVICE_APPLICATION_REFUSAL_REASON);
        assertThat(getServiceApplicationRefusalReason(caseData), is(TEST_SERVICE_APPLICATION_REFUSAL_REASON));
    }

    @Test
    public void getServiceApplicationGrantedShouldReturnYesValue() {
        Map<String, Object> caseData = buildCaseDataWithField(
            SERVICE_APPLICATION_GRANTED,
            YES_VALUE);
        assertThat(getServiceApplicationGranted(caseData), is(YES_VALUE));
    }

    @Test
    public void getServiceApplicationTypeShouldReturnDeemedValue() {
        Map<String, Object> caseData = buildCaseDataWithField(
            SERVICE_APPLICATION_TYPE,
            DEEMED);
        assertThat(getServiceApplicationType(caseData), is(DEEMED));
    }

    @Test(expected = InvalidDataForTaskException.class)
    public void getServiceApplicationRefusalReasonShouldThrowInvalidData() {
        getServiceApplicationRefusalReason(EMPTY_MAP);
    }

    @Test
    public void isFinalShouldEvaluateToTrueValue() {
        ServiceRefusalDecision decision = ServiceRefusalDecision.FINAL;

        assertThat(isFinal(decision), is(true));
    }

    @Test
    public void isDraftShouldEvaluateToTrueValue() {
        ServiceRefusalDecision decision = ServiceRefusalDecision.DRAFT;

        assertThat(isDraft(decision), is(true));
    }

    @Test
    public void isFinalShouldEvaluateToFalseValue() {
        ServiceRefusalDecision decision = ServiceRefusalDecision.DRAFT;

        assertThat(isFinal(decision), is(false));
    }

    @Test
    public void isDeemedApplicationShouldBeTrue() {
        assertThat(isDeemedApplication("deemed"), is(true));
    }

    @Test
    public void isDeemedApplicationShouldBeFalse() {
        assertThat(isDeemedApplication("another"), is(false));
    }

    @Test
    public void isDispensedApplicationShouldBeTrue() {
        assertThat(isDispensedApplication("dispensed"), is(true));
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
        Map<String, Object> caseData = buildCaseDataWithField(
            SERVICE_APPLICATION_GRANTED,
            YES_VALUE);

        assertThat(isServiceApplicationGranted(caseData), is(true));
    }

    private static Map<String, Object> buildCaseDataWithField(String field, String value) {
        Map<String, Object> caseData = new HashMap<>();
        caseData.put(field, value);

        return caseData;
    }

}