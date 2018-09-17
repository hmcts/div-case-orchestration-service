package uk.gov.hmcts.reform.divorce.orchestration.tasks;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.junit.MockitoJUnitRunner;

import uk.gov.hmcts.reform.divorce.orchestration.domain.model.fees.FeeItem;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.fees.FeeResponse;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.fees.FeeValue;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.fees.OrderSummary;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.DefaultTaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskException;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_FEE_AMOUNT;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_FEE_CODE;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_FEE_DESCRIPTION;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_FEE_VERSION;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.PETITION_ISSUE_FEE_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.PETITION_ISSUE_ORDER_SUMMARY_JSON_KEY;

@RunWith(MockitoJUnitRunner.class)
public class SetOrderSummaryTest {

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
    public void executeShouldCallCaseMaintenanceClientSubmitEndpoint() throws Exception {
        FeeResponse feeResponse = FeeResponse.builder()
                .amount(TEST_FEE_AMOUNT)
                .feeCode(TEST_FEE_CODE)
                .version(TEST_FEE_VERSION)
                .description(TEST_FEE_DESCRIPTION)
                .build();

        context.setTransientObject(PETITION_ISSUE_FEE_JSON_KEY, feeResponse);

        Map<String, Object> resultData = Collections.singletonMap(
                PETITION_ISSUE_ORDER_SUMMARY_JSON_KEY, buildOrderSummary(
                        TEST_FEE_AMOUNT,
                        TEST_FEE_CODE,
                        TEST_FEE_VERSION,
                        TEST_FEE_DESCRIPTION
                )
        );

        assertEquals(resultData, setOrderSummary.execute(context, testData));
    }

    @Test(expected = TaskException.class)
    public void givenMissingFeeWhenExecuteThenThrowTaskException() throws Exception {
        setOrderSummary.execute(context, testData);
    }

    private OrderSummary buildOrderSummary(Double amount, String feeCode, Integer version, String description) {
        FeeValue feeValue = new FeeValue();
        // Fee amount is stored in passed in as pounds but stored as pence
        NumberFormat formatter = new DecimalFormat("#0");
        feeValue.setFeeAmount(String.valueOf(formatter.format(amount * 100)));
        feeValue.setFeeCode(feeCode);
        feeValue.setFeeVersion(version.toString());
        feeValue.setFeeDescription(description);

        FeeItem feeItem = new FeeItem();
        feeItem.setValue(feeValue);

        OrderSummary orderSummary = new OrderSummary();
        orderSummary.setFees(Collections.singletonList(feeItem));
        orderSummary.setPaymentTotal(String.valueOf(formatter.format(amount * 100)));

        return orderSummary;
    }
}
