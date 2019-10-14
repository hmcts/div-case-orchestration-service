package uk.gov.hmcts.reform.divorce.orchestration.tasks;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.divorce.orchestration.client.FeesAndPaymentsClient;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.fees.FeeResponse;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.DefaultTaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskContext;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.DUMMY_CASE_DATA;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.AMEND_PETITION_FEE_JSON_KEY;

@RunWith(MockitoJUnitRunner.class)
public class GetAmendPetitionFeeTaskTest {

    @Mock
    private FeesAndPaymentsClient feesAndPaymentsClient;

    @InjectMocks
    private GetAmendPetitionFeeTask getAmendPetitionFeeTask;

    @Test
    public void whenExecuteTask_ReturnAmendFeesOnContext() {
        FeeResponse expected = FeeResponse.builder().build();
        when(feesAndPaymentsClient.getAmendPetitioneFee()).thenReturn(expected);
        TaskContext context = new DefaultTaskContext();
        assertThat(getAmendPetitionFeeTask.execute(context, DUMMY_CASE_DATA), is(DUMMY_CASE_DATA));
        assertThat(context.getTransientObject(AMEND_PETITION_FEE_JSON_KEY), is(expected));
    }
}
