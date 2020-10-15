package uk.gov.hmcts.reform.divorce.orchestration.tasks.generalreferral;

import com.google.common.collect.ImmutableMap;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.CcdFields;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskException;

import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertThrows;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.YES_VALUE;
import static uk.gov.hmcts.reform.divorce.orchestration.testutil.TaskContextHelper.context;

@RunWith(MockitoJUnitRunner.class)
public class GeneralReferralTaskTest {

    @InjectMocks
    private GeneralReferralTask generalReferralTask;

    private TaskContext taskContext;

    @Before
    public void setUp() {
        taskContext = context();
    }

    @Test
    public void whenCaseDateAndGeneralReferralFeeIsNotEmptyShouldReturnCaseData() {
        Map<String, Object> caseData = ImmutableMap.of(CcdFields.GENERAL_REFERRAL_FEE, YES_VALUE);

        Map<String, Object> result = generalReferralTask.execute(taskContext, caseData);
        assertThat(result, is(caseData));
    }

    @Test
    public void whenCaseDateAndGeneralReferralFeeIsEmptyShouldThrowError() {
        Map<String, Object> caseData = ImmutableMap.of("SomeOtherField", YES_VALUE);

        assertThrows(TaskException.class, () -> generalReferralTask.execute(taskContext, caseData));
    }

}