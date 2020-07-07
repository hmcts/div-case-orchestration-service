package uk.gov.hmcts.reform.divorce.orchestration.workflows;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CaseLink;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.fees.FeeResponse;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.fees.OrderSummary;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.DefaultTaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskException;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.GetAmendPetitionFeeTask;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.GetPetitionIssueFee;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.SetOrderSummary;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_FEE_AMOUNT;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_FEE_CODE;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_FEE_DESCRIPTION;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_FEE_VERSION;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.PETITION_ISSUE_ORDER_SUMMARY_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.PREVIOUS_CASE_ID_CCD_KEY;

@RunWith(MockitoJUnitRunner.class)
public class SetOrderSummaryWorkflowTest {

    @Mock
    private GetPetitionIssueFee getPetitionIssueFee;

    @Mock
    private GetAmendPetitionFeeTask getAmendPetitionFeeTask;

    @Mock
    private SetOrderSummary setOrderSummary;

    @InjectMocks
    private SetOrderSummaryWorkflow setOrderSummaryWorkflow;

    private Map<String, Object> testData;
    private TaskContext context;
    private Map<String, Object> expectedCaseData;

    @Before
    public void setup() throws TaskException {
        testData = new HashMap<>();
        context = new DefaultTaskContext();

        FeeResponse feeResponse = FeeResponse.builder()
            .amount(TEST_FEE_AMOUNT)
            .feeCode(TEST_FEE_CODE)
            .version(TEST_FEE_VERSION)
            .description(TEST_FEE_DESCRIPTION)
            .build();
        OrderSummary orderSummary = new OrderSummary();
        orderSummary.add(feeResponse);
        expectedCaseData = Collections.singletonMap(PETITION_ISSUE_ORDER_SUMMARY_JSON_KEY, orderSummary);

        when(getPetitionIssueFee.execute(context, testData)).thenReturn(testData);
        when(getAmendPetitionFeeTask.execute(context, testData)).thenReturn(testData);
        when(setOrderSummary.execute(context, testData)).thenReturn(expectedCaseData);
    }

    @Test
    public void runGetPetitionIssueFees_WhenCaseIsNotAmendment() throws Exception {
        Map<String, Object> returnedCaseData = setOrderSummaryWorkflow.run(testData);

        assertThat(returnedCaseData, equalTo(expectedCaseData));
        verify(getPetitionIssueFee).execute(context, testData);
        verify(setOrderSummary).execute(context, testData);
        verifyZeroInteractions(getAmendPetitionFeeTask);
    }

    @Test
    public void runGetAmendFees_WhenCaseIsAmendment() throws Exception {
        testData.put(PREVIOUS_CASE_ID_CCD_KEY, new CaseLink("1234567890123456"));

        Map<String, Object> returnedCaseData = setOrderSummaryWorkflow.run(testData);

        assertThat(returnedCaseData, equalTo(expectedCaseData));
        verify(getAmendPetitionFeeTask).execute(context, testData);
        verify(setOrderSummary).execute(context, testData);
        verifyZeroInteractions(getPetitionIssueFee);
    }

}