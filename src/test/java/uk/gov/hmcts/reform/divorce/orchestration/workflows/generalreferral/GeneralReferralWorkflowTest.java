package uk.gov.hmcts.reform.divorce.orchestration.workflows.generalreferral;

import com.google.common.collect.ImmutableMap;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CaseDetails;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.WorkflowException;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.generalreferral.GeneralReferralTask;

import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.divorce.orchestration.testutil.Verificators.verifyTaskWasCalled;

@RunWith(MockitoJUnitRunner.class)
public class GeneralReferralWorkflowTest {

    @InjectMocks
    private GeneralReferralWorkflow classUnderTest;

    @Mock
    private GeneralReferralTask generalReferralTask;

    @Test
    public void shouldCallTheOnlyOneTaskAndReturnData() throws WorkflowException {
        Map<String, Object> caseData = ImmutableMap.of("SomeProperty", "SomeValue");

        CaseDetails caseDetails = CaseDetails.builder()
            .caseData(caseData).build();

        when(generalReferralTask.execute(any(TaskContext.class), anyMap())).thenReturn(caseData);

        Map<String, Object> result = classUnderTest.run(caseDetails, AUTH_TOKEN);

        assertThat(caseData, is(result));
        verifyTaskWasCalled(caseData, generalReferralTask);
    }
}