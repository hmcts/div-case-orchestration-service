package uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.generics;

import com.google.common.collect.ImmutableMap;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.divorce.orchestration.client.FeesAndPaymentsClient;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.fees.FeeResponse;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.fees.OrderSummary;

import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_FEE_DESCRIPTION;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_FEE_VERSION;
import static uk.gov.hmcts.reform.divorce.orchestration.testutil.TaskContextHelper.context;

@RunWith(MockitoJUnitRunner.class)
public class FeeLookupWithoutNoticeTaskTest {

    @Mock
    protected FeesAndPaymentsClient feesAndPaymentsClient;

    public static final String TEST_ORDER_SUMMARY_FIELD = "FeeLookupWithoutNoticeField";
    public static final String TEST_FEE_AMOUNT_FIELD = "FeeAmountWithoutNoticeField";

    public static final String TEST_GENERAL_APPLICATION_WITHOUT_NOTICE_CODE = "FEE0228";
    public static final double TEST_FEE_AMOUNT_IN_POUNDS = 50d;
    public static final String TEST_FEE_AMOUNT_IN_PENNIES = "5000";
    public static final String TEST_FEE_AMOUNT_IN_POUNDS_AS_STRING = "50";

    protected FeeResponse setupFeeResponse() {
        FeeResponse feeResponse = FeeResponse.builder()
            .amount(TEST_FEE_AMOUNT_IN_POUNDS)
            .feeCode(TEST_GENERAL_APPLICATION_WITHOUT_NOTICE_CODE)
            .version(TEST_FEE_VERSION)
            .description(TEST_FEE_DESCRIPTION)
            .build();

        return feeResponse;
    }
    protected FeeLookupWithoutNoticeTask getTask() {
        return new FeeLookupWithoutNoticeTask(feesAndPaymentsClient) {
            @Override
            public String getOrderSummaryFieldName() {
                return TEST_ORDER_SUMMARY_FIELD;
            }

            @Override
            public String getFeeValueFieldName() {
                return TEST_FEE_AMOUNT_FIELD;
            }
        };
    }

    protected FeeLookupWithoutNoticeTask getTaskWithoutFeeValueField() {
        return new FeeLookupWithoutNoticeTask(feesAndPaymentsClient) {
            @Override
            public String getOrderSummaryFieldName() {
                return TEST_ORDER_SUMMARY_FIELD;
            }

            @Override
            public String getFeeValueFieldName() {
                return null;
            }
        };
    }

    @Test
    public void shouldPopulateFieldWithSummaryOrder() {
        runTestFieldIsPopulated();
    }

    @Test
    public void shouldPopulateFeeFieldWithValue() {
        runFeeValueFieldIsPopulated();
    }

    @Test
    public void shouldPopulateFeeFieldWithoutValue() {
        runFeeValueFieldIsNotPopulated();
    }

    protected void runTestFieldIsPopulated() {
        FeeResponse feeResponse = setupFeeResponse();

        FeeLookupWithoutNoticeTask task = getTask();

        when(feesAndPaymentsClient.getGeneralApplicationWithoutFee()).thenReturn(feeResponse);

        Map<String, Object> returnedCaseData = task.execute(context(), new HashMap<>());

        OrderSummary paymentSummary = (OrderSummary) returnedCaseData.get(task.getOrderSummaryFieldName());

        assertThat(paymentSummary.getPaymentTotal(), is(TEST_FEE_AMOUNT_IN_PENNIES));
        assertThat(
            paymentSummary.getFees().get(0).getValue().getFeeCode(),
            equalTo(TEST_GENERAL_APPLICATION_WITHOUT_NOTICE_CODE)
        );
    }

    protected void runFeeValueFieldIsPopulated() {
        FeeResponse feeResponse = setupFeeResponse();

        FeeLookupWithoutNoticeTask task = getTask();

        when(feesAndPaymentsClient.getGeneralApplicationWithoutFee()).thenReturn(feeResponse);

        Map<String, Object> expectedCaseData = ImmutableMap.of(
            TEST_ORDER_SUMMARY_FIELD, getOrderSummaryFrom(feeResponse),
            TEST_FEE_AMOUNT_FIELD, TEST_FEE_AMOUNT_IN_POUNDS_AS_STRING);

        Map<String, Object> returnedCaseData = task.execute(context(), new HashMap<>());

        assertThat(returnedCaseData, is(expectedCaseData));
    }

    protected void runFeeValueFieldIsNotPopulated() {
        FeeResponse feeResponse = setupFeeResponse();

        FeeLookupWithoutNoticeTask task = getTaskWithoutFeeValueField();

        when(feesAndPaymentsClient.getGeneralApplicationWithoutFee()).thenReturn(feeResponse);

        Map<String, Object> expectedCaseData = ImmutableMap.of(
            TEST_ORDER_SUMMARY_FIELD, getOrderSummaryFrom(feeResponse)
        );

        Map<String, Object> returnedCaseData = task.execute(context(), new HashMap<>());

        assertThat(returnedCaseData, is(expectedCaseData));
        assertThat(returnedCaseData.containsKey(TEST_FEE_AMOUNT_FIELD), is(false));
    }

    private OrderSummary getOrderSummaryFrom(FeeResponse feeResponse) {
        OrderSummary orderSummary = new OrderSummary();
        orderSummary.add(feeResponse);
        return orderSummary;
    }
}
