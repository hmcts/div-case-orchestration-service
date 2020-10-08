package uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor;

import org.junit.Test;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskException;

import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_SOLICITOR_ACCOUNT_NUMBER;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.CcdFields.PBA_NUMBERS;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.FEE_PAY_BY_ACCOUNT;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.SOLICITOR_FEE_ACCOUNT_NUMBER_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor.SolicitorDataExtractor.CaseDataKeys.SOLICITOR_PAYMENT_METHOD;
import static uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor.SolicitorDataExtractor.CaseDataKeys.SOLICITOR_REFERENCE;
import static uk.gov.hmcts.reform.divorce.orchestration.tasks.bulk.printing.BulkPrintTestData.SOLICITOR_REF;
import static uk.gov.hmcts.reform.divorce.orchestration.testutil.PayByAccountTestUtil.setPbaToggleTo;

public class SolicitorDataExtractorTest {

    @Test
    public void getSolicitorReferenceReturnsValidValueWhenItExists() {
        Map<String, Object> caseData = buildCaseDataWithSolicitorReference(SOLICITOR_REF);
        assertThat(SolicitorDataExtractor.getSolicitorReference(caseData), is(SOLICITOR_REF));
    }

    @Test
    public void getSolicitorReferenceReturnsEmptyStringWhenItIsEmpty() {
        Map<String, Object> caseData = buildCaseDataWithSolicitorReference("");
        assertThat(SolicitorDataExtractor.getSolicitorReference(caseData), is(""));
    }

    @Test
    public void getSolicitorReferenceDoesNotThrowExceptionsWhenItIsNull() {
        Map<String, Object> caseData = buildCaseDataWithSolicitorReference(null);
        assertThat(SolicitorDataExtractor.getSolicitorReference(caseData), is(""));
    }

    @Test
    public void getPaymentMethodReturnsValidValueWhenItExists() {
        Map<String, Object> caseData = buildCaseDataWithSolicitorPaymentMethod(FEE_PAY_BY_ACCOUNT);
        assertThat(SolicitorDataExtractor.getPaymentMethod(caseData), is(FEE_PAY_BY_ACCOUNT));
    }

    @Test
    public void getPaymentMethodReturnsEmptyStringWhenItIsEmpty() {
        Map<String, Object> caseData = buildCaseDataWithSolicitorPaymentMethod("");
        assertThat(SolicitorDataExtractor.getPaymentMethod(caseData), is(""));
    }

    @Test
    public void getPaymentMethodDoesNotThrowExceptionsWhenItIsNull() {
        Map<String, Object> caseData = buildCaseDataWithSolicitorPaymentMethod(null);
        assertThat(SolicitorDataExtractor.getPaymentMethod(caseData), is(""));
    }

    @Test
    public void getPbaNumberReturnsFromNewFieldWhenFeatureToggleIsOn() {
        setPbaToggleTo(true);
        Map<String, Object> caseData = buildCaseDataWithPbaNumberV2(TEST_SOLICITOR_ACCOUNT_NUMBER);
        assertThat(SolicitorDataExtractor.getPbaNumber(caseData), is(TEST_SOLICITOR_ACCOUNT_NUMBER));
    }

    @Test
    public void getPbaNumberReturnsFromOldFieldWhenFeatureToggleIsOff() {
        setPbaToggleTo(false);
        Map<String, Object> caseData = buildCaseDataWithPbaNumberV1(TEST_SOLICITOR_ACCOUNT_NUMBER);
        assertThat(SolicitorDataExtractor.getPbaNumber(caseData), is(TEST_SOLICITOR_ACCOUNT_NUMBER));
    }

    @Test(expected = TaskException.class)
    public void getPbaNumberThrowsExceptionWhenWhenFeatureToggleIsOnAndNewFieldIsNull() {
        setPbaToggleTo(true);
        SolicitorDataExtractor.getPbaNumber(new HashMap<>());
    }

    @Test(expected = TaskException.class)
    public void getPbaNumberThrowsExceptionWhenWhenFeatureToggleIsOnAndOldFieldIsNull() {
        setPbaToggleTo(false);
        SolicitorDataExtractor.getPbaNumber(new HashMap<>());
    }

    private static Map<String, Object> buildCaseDataWithSolicitorReference(String solicitorReference) {
        return buildCaseDataWith(SOLICITOR_REFERENCE, solicitorReference);
    }

    private static Map<String, Object> buildCaseDataWithSolicitorPaymentMethod(String solicitorPaymentMethod) {
        return buildCaseDataWith(SOLICITOR_PAYMENT_METHOD, solicitorPaymentMethod);
    }

    private static Map<String, Object> buildCaseDataWithPbaNumberV1(String pbaNumber) {
        return buildCaseDataWith(SOLICITOR_FEE_ACCOUNT_NUMBER_JSON_KEY, pbaNumber);
    }

    private static Map<String, Object> buildCaseDataWithPbaNumberV2(String pbaNumber) {
        return buildCaseDataWith(PBA_NUMBERS, pbaNumber);
    }

    private static Map<String, Object> buildCaseDataWith(String key, String value) {
        Map<String, Object> caseData = new HashMap<>();
        caseData.put(key, value);

        return caseData;
    }
}
