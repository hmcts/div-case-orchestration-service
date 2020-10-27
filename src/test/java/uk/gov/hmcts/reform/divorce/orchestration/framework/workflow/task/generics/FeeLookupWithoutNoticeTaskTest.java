package uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.generics;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.divorce.orchestration.client.FeesAndPaymentsClient;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.fees.FeeResponse;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.fees.OrderSummary;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskContext;

import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.divorce.orchestration.testutil.FeesAndPaymentHelper.AMOUNT_IN_PENNIES;
import static uk.gov.hmcts.reform.divorce.orchestration.testutil.FeesAndPaymentHelper.FEE_CODE;
import static uk.gov.hmcts.reform.divorce.orchestration.testutil.FeesAndPaymentHelper.getApplicationWithoutNoticeFee;
import static uk.gov.hmcts.reform.divorce.orchestration.testutil.TaskContextHelper.context;

@RunWith(MockitoJUnitRunner.class)
public class FeeLookupWithoutNoticeTaskTest {

    @Mock
    protected FeesAndPaymentsClient feesAndPaymentsClient;

    public static final String TEST_FIELD = "FeeLookupWithoutNoticeField";

    protected FeeLookupWithoutNoticeTask getTask() {
        return new FeeLookupWithoutNoticeTask(feesAndPaymentsClient) {

            @Override
            protected Map<String, Object> furtherUpdateCaseData(TaskContext context, Map<String, Object> caseData) {
                return caseData;
            }

            @Override
            public String getOrderSummaryFieldName() {
                return TEST_FIELD;
            }

        };
    }

    @Test
    public void shouldPopulateFieldWithSummaryOrder() {
        runTestFieldIsPopulated();
    }

    protected Map<String, Object> runTestFieldIsPopulated() {
        FeeResponse feeResponse = getApplicationWithoutNoticeFee();
        FeeLookupWithoutNoticeTask task = getTask();

        when(feesAndPaymentsClient.getGeneralApplicationWithoutFee()).thenReturn(feeResponse);

        Map<String, Object> returnedCaseData = task.execute(context(), new HashMap<>());

        OrderSummary paymentSummary = (OrderSummary) returnedCaseData.get(task.getOrderSummaryFieldName());

        assertThat(paymentSummary.getPaymentTotal(), is(AMOUNT_IN_PENNIES));
        assertThat(
            paymentSummary.getFees().get(0).getValue().getFeeCode(),
            equalTo(FEE_CODE)
        );

        return returnedCaseData;
    }
}
