package uk.gov.hmcts.reform.divorce.orchestration.job;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.WorkflowException;
import uk.gov.hmcts.reform.divorce.orchestration.service.DecreeAbsoluteService;

import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class UpdateDNPronouncedCaseJobTest {

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Mock
    private DecreeAbsoluteService decreeAbsoluteServiceMock;

    @Mock
    private JobExecutionContext jobExecutionContextMock;

    @InjectMocks
    private UpdateDNPronouncedCaseJob classToTest;

    @Test
    public void execute_updateToAwaitingDA_updateExecuted() throws JobExecutionException, WorkflowException {
        classToTest.execute(jobExecutionContextMock);
        verify(decreeAbsoluteServiceMock, times(1)).enableCaseEligibleForDecreeAbsolute();
    }

    @Test
    public void execute_updateToAwaitingDA_JobExceptionThrown() throws JobExecutionException, WorkflowException {
        expectedException.expect(JobExecutionException.class);
        expectedException.expectMessage("Case update failed");
        doThrow(new WorkflowException("a WorkflowException message")).when(decreeAbsoluteServiceMock).enableCaseEligibleForDecreeAbsolute();

        classToTest.execute(jobExecutionContextMock);
    }
}
