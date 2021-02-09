package uk.gov.hmcts.reform.divorce.orchestration.tasks;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.fees.FeeItem;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.fees.FeeResponse;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.fees.FeeValue;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.fees.OrderSummary;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.DefaultTaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskException;
import uk.gov.hmcts.reform.divorce.orchestration.service.FeatureToggleService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.jayway.jsonpath.matchers.JsonPathMatchers.hasJsonPath;
import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasEntry;
import static org.hamcrest.Matchers.hasKey;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_FEE_AMOUNT;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_FEE_CODE;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_FEE_DESCRIPTION;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_FEE_VERSION;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.Features.SOLICITOR_DN_REJECT_AND_AMEND;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.PETITION_FEE_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.PETITION_ISSUE_ORDER_SUMMARY_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.testutil.ObjectMapperTestUtil.convertObjectToJsonString;

@RunWith(MockitoJUnitRunner.class)
public class SetOrderSummaryTest {

    private static final String SOL_APPLICATION_FEE_IN_POUNDS = "solApplicationFeeInPounds";

    @Mock
    private FeatureToggleService featureToggleService;

    @InjectMocks
    SetOrderSummary setOrderSummary;

    private Map<String, Object> testData;
    private TaskContext context;

    @Before
    public void setup() {
        testData = new HashMap<>();
        context = new DefaultTaskContext();
    }

    @Test
    public void shouldReturnFeeInformation_WhenFeatureSwitchedOn() throws Exception {
        when(featureToggleService.isFeatureEnabled(SOLICITOR_DN_REJECT_AND_AMEND)).thenReturn(true);
        FeeResponse feeResponse = FeeResponse.builder()
            .amount(TEST_FEE_AMOUNT)
            .feeCode(TEST_FEE_CODE)
            .version(TEST_FEE_VERSION)
            .description(TEST_FEE_DESCRIPTION)
            .build();
        context.setTransientObject(PETITION_FEE_JSON_KEY, feeResponse);

        Map<String, Object> returnedCaseData = setOrderSummary.execute(context, testData);

        assertReturnedOrderSummary(returnedCaseData, "55000");
        assertThat(returnedCaseData, hasEntry(SOL_APPLICATION_FEE_IN_POUNDS, "550"));
    }

    @Test
    public void shouldReturnFeeInformation_WhenFeeHasPenceValue_WhenFeatureSwitchedOn() throws Exception {
        when(featureToggleService.isFeatureEnabled(SOLICITOR_DN_REJECT_AND_AMEND)).thenReturn(true);
        FeeResponse feeResponse = FeeResponse.builder()
            .amount(85.43)
            .feeCode(TEST_FEE_CODE)
            .version(TEST_FEE_VERSION)
            .description(TEST_FEE_DESCRIPTION)
            .build();
        context.setTransientObject(PETITION_FEE_JSON_KEY, feeResponse);

        Map<String, Object> returnedCaseData = setOrderSummary.execute(context, testData);

        assertReturnedOrderSummary(returnedCaseData, "8543");
        assertThat(returnedCaseData, hasEntry(SOL_APPLICATION_FEE_IN_POUNDS, "85.43"));
    }

    @Test
    public void shouldReturnFeeInformation_WhenFeatureSwitchedOff() throws Exception {
        when(featureToggleService.isFeatureEnabled(SOLICITOR_DN_REJECT_AND_AMEND)).thenReturn(false);
        FeeResponse feeResponse = FeeResponse.builder()
            .amount(TEST_FEE_AMOUNT)
            .feeCode(TEST_FEE_CODE)
            .version(TEST_FEE_VERSION)
            .description(TEST_FEE_DESCRIPTION)
            .build();
        context.setTransientObject(PETITION_FEE_JSON_KEY, feeResponse);

        Map<String, Object> returnedCaseData = setOrderSummary.execute(context, testData);

        assertReturnedOrderSummary(returnedCaseData, "55000");
        assertThat(returnedCaseData, not(hasKey(SOL_APPLICATION_FEE_IN_POUNDS)));
    }

    private void assertReturnedOrderSummary(Map<String, Object> returnedCaseData, String expectedAmountInPence) {
        String jsonOrderSummary = convertObjectToJsonString(returnedCaseData.get(PETITION_ISSUE_ORDER_SUMMARY_JSON_KEY));
        assertThat(jsonOrderSummary, hasJsonPath("PaymentTotal", is(expectedAmountInPence)));
        assertThat(jsonOrderSummary, hasJsonPath("Fees", hasSize(1)));
        assertThat(jsonOrderSummary, hasJsonPath("Fees[0].value", allOf(
            hasJsonPath("FeeCode", is(TEST_FEE_CODE)),
            hasJsonPath("FeeVersion", is(TEST_FEE_VERSION.toString())),
            hasJsonPath("FeeDescription", is(TEST_FEE_DESCRIPTION)),
            hasJsonPath("FeeAmount", is(expectedAmountInPence))
        )));
        assertThat(jsonOrderSummary, hasJsonPath("$.*", hasSize(3)));//Making sure we don't have any accidental extra properties

        OrderSummary returnedOrderSummary = (OrderSummary) returnedCaseData.get(PETITION_ISSUE_ORDER_SUMMARY_JSON_KEY);
        assertThat(returnedOrderSummary.getPaymentTotal(), is(expectedAmountInPence));
        List<FeeItem> fees = returnedOrderSummary.getFees();
        assertThat(fees, hasSize(1));
        FeeValue fee = fees.get(0).getValue();
        assertThat(fee.getFeeCode(), is(TEST_FEE_CODE));
        assertThat(fee.getFeeVersion(), is(TEST_FEE_VERSION.toString()));
        assertThat(fee.getFeeDescription(), is(TEST_FEE_DESCRIPTION));
        assertThat(fee.getFeeAmount(), is(expectedAmountInPence));
    }

    @Test(expected = TaskException.class)
    public void givenMissingFeeWhenExecuteThenThrowTaskException() throws Exception {
        setOrderSummary.execute(context, testData);
    }

}