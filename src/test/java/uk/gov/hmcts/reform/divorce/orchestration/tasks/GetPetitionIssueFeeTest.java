package uk.gov.hmcts.reform.divorce.orchestration.tasks;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import uk.gov.hmcts.reform.divorce.orchestration.client.FeesAndPaymentsClient;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.fees.FeeResponse;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.DefaultTaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskContext;

import java.util.Collections;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_FEE_AMOUNT;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_FEE_CODE;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_FEE_DESCRIPTION;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_FEE_VERSION;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.PETITION_ISSUE_FEE_JSON_KEY;

@RunWith(MockitoJUnitRunner.class)
public class GetPetitionIssueFeeTest {

    @Mock
    FeesAndPaymentsClient feesAndPaymentsClient;

    @InjectMocks
    GetPetitionIssueFee getPetitionIssueFee;

    private Map<String, Object> testData;
    private TaskContext context;

    @Before
    public void setup() {
        testData = Collections.emptyMap();
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

        when(feesAndPaymentsClient.getPetitionIssueFee()).thenReturn(feeResponse);

        assertEquals(testData, getPetitionIssueFee.execute(context, testData));
        assertEquals(feeResponse, context.getTransientObject(PETITION_ISSUE_FEE_JSON_KEY));

        verify(feesAndPaymentsClient).getPetitionIssueFee();
    }
}
