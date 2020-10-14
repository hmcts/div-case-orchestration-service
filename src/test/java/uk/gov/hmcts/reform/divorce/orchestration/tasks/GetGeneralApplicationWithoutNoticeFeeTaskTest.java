package uk.gov.hmcts.reform.divorce.orchestration.tasks;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.divorce.orchestration.client.FeesAndPaymentsClient;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.fees.FeeResponse;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.fees.OrderSummary;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.DefaultTaskContext;

import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_FEE_DESCRIPTION;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_FEE_VERSION;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.CcdFields.GENERAL_APPLICATION_WITHOUT_NOTICE_FEE_SUMMARY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CASE_ID_JSON_KEY;

@RunWith(MockitoJUnitRunner.class)
public class GetGeneralApplicationWithoutNoticeFeeTaskTest {

    @Mock
    private FeesAndPaymentsClient feesAndPaymentsClient;

    @InjectMocks
    private OrderSummary orderSummary;

    @InjectMocks
    private GetGeneralApplicationWithoutNoticeFeeTask classToTest;

    public static final String TEST_GENERAL_APPLICATION_WITHOUT_NOTICE_CODE = "FEE0228";
    public static final double TEST_FEE_AMOUNT_IN_POUNDS = 50d;
    public static final String TEST_FEE_AMOUNT_IN_PENNIES = "5000";

    @Test
    public void shouldReturnGeneralApplicationWithoutFeeValue() {
        FeeResponse feeResponse = FeeResponse.builder()
            .amount(TEST_FEE_AMOUNT_IN_POUNDS)
            .feeCode(TEST_GENERAL_APPLICATION_WITHOUT_NOTICE_CODE)
            .version(TEST_FEE_VERSION)
            .description(TEST_FEE_DESCRIPTION)
            .build();
        when(feesAndPaymentsClient.getGeneralApplicationWithoutFee()).thenReturn(feeResponse);

        DefaultTaskContext taskContext = new DefaultTaskContext();
        taskContext.setTransientObject(CASE_ID_JSON_KEY, TEST_CASE_ID);
        Map<String, Object> returnedCaseData = classToTest.execute(taskContext, new HashMap<>());

        OrderSummary paymentSummary = (OrderSummary) returnedCaseData.get(GENERAL_APPLICATION_WITHOUT_NOTICE_FEE_SUMMARY);
        assertThat(paymentSummary.getPaymentTotal(), is(TEST_FEE_AMOUNT_IN_PENNIES));
        assertThat(paymentSummary.getFees().get(0).getValue().getFeeCode(), equalTo(TEST_GENERAL_APPLICATION_WITHOUT_NOTICE_CODE));
    }
}