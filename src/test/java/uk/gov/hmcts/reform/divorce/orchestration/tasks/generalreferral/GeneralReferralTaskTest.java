package uk.gov.hmcts.reform.divorce.orchestration.tasks.generalreferral;

import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.fail;
import static uk.gov.hmcts.reform.divorce.orchestration.testutil.TaskContextHelper.context;

@RunWith(MockitoJUnitRunner.class)
public class GeneralReferralTaskTest {

    @InjectMocks
    private GeneralReferralTask generalReferralTask;

    // 1.)
    // if 'GeneralReferralFee' as 'Yes' -> State: AwaitingGeneralReferralPayment
    @Test
    @Ignore
    public void whenGeneralReferralFeeIsYesThenCaseStateIsSetToAwaitingGeneralReferralPayment(){
        Map<String, Object> caseData = new HashMap<>();
        generalReferralTask.execute(context(), caseData);
    }


    // 2.)
    // if 'GeneralReferralFee' as 'No' -> State: AwaitingGeneralConsideration
    @Test
    @Ignore
    public void whenGeneralReferralFeeIsNoThenCaseStateIsSetToAwaitingGeneralConsideration(){
        fail("Not yet implemented");
    }


    // TODO 3.)
    // if State: AwaitingGeneralReferralPayment and 'GeneralReferralFee' as 'No' -> State: AwaitingGeneralConsideration
    @Test
    @Ignore
    public void whenStateIsAwaitingGeneralReferralPaymentAndGeneralReferralFeeIsNoThenStateIsSetToAwaitingGeneralConsideration(){
        fail("Not yet implemented");
    }

    // TODO 4.)
    // if State: AwaitingGeneralConsideration and 'GeneralReferralFee' as 'Yes' -> State: AwaitingGeneralReferralPayment
    @Test
    @Ignore
    public void whenStateIsAwaitingGeneralConsiderationAndWhenGeneralReferralFeeIsYesThenStateIsSetToAwaitingGeneralReferralPayment(){
        fail("Not yet implemented");
    }

}