package uk.gov.hmcts.reform.divorce.orchestration.tasks.servicejourney;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.CcdFields;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.generics.FeeLookupWithoutNoticeTask;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.generics.FeeLookupWithoutNoticeTaskTest;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

@RunWith(MockitoJUnitRunner.class)
public class GetGeneralApplicationWithoutNoticeFeeTaskTest extends FeeLookupWithoutNoticeTaskTest {

    @InjectMocks
    private GetGeneralApplicationWithoutNoticeFeeTask getGeneralApplicationWithoutNoticeFeeTask;

    @Override
    protected FeeLookupWithoutNoticeTask getTask() {
        return getGeneralApplicationWithoutNoticeFeeTask;
    }

    @Test
    public void shouldReturnExpectedField() {
        assertThat(getTask().getOrderSummaryFieldName(), is(CcdFields.GENERAL_APPLICATION_WITHOUT_NOTICE_FEE_SUMMARY));
    }
}
