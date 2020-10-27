package uk.gov.hmcts.reform.divorce.orchestration.tasks.generalreferral;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.CcdFields;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.generics.FeeLookupWithoutNoticeTask;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.generics.FeeLookupWithoutNoticeTaskTest;

import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

@RunWith(MockitoJUnitRunner.class)
public class GeneralReferralApplicationFeeLookupTaskTest extends FeeLookupWithoutNoticeTaskTest {

    @InjectMocks
    private GeneralReferralApplicationFeeLookupTask generalReferralApplicationFeeLookupTask;

    @Override
    protected FeeLookupWithoutNoticeTask getTask() {
        return generalReferralApplicationFeeLookupTask;
    }

    @Test
    public void shouldReturnExpectedField() {
        assertThat(getTask().getFieldName(), is(CcdFields.GENERAL_REFERRAL_WITHOUT_NOTICE_FEE_SUMMARY));
    }

    @Test
    public void shouldAlsoPopulateFeeAmountWithoutNotice() {
        Map<String, Object> returnedCaseData = runTestFieldIsPopulated();

        assertThat(
            returnedCaseData.get(CcdFields.FEE_AMOUNT_WITHOUT_NOTICE),
            is("50")
        );
    }
}
