package uk.gov.hmcts.reform.divorce.orchestration.job;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.WorkflowException;
import uk.gov.hmcts.reform.divorce.orchestration.service.DecreeAbsoluteService;
import uk.gov.hmcts.reform.divorce.orchestration.util.AuthUtil;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.AUTH_TOKEN;

@RunWith(MockitoJUnitRunner.class)
public class MakeCasesEligibleForDAJobTest {

    @Mock
    private DecreeAbsoluteService decreeAbsoluteServiceMock;

    @Mock
    private JobExecutionContext jobExecutionContextMock;

    @Mock
    private AuthUtil authUtil;

    @InjectMocks
    private MakeCasesEligibleForDAJob classToTest;

    @Before
    public void setUp() {
        when(authUtil.getCaseworkerToken()).thenReturn(AUTH_TOKEN);
    }

    @Test
    public void execute_updateToAwaitingDA_updateExecuted() throws JobExecutionException, WorkflowException {
        classToTest.execute(jobExecutionContextMock);

        verify(decreeAbsoluteServiceMock).enableCaseEligibleForDecreeAbsolute(AUTH_TOKEN);
    }

    @Test
    public void execute_updateToAwaitingDA_JobExceptionThrown() throws WorkflowException {
        when(decreeAbsoluteServiceMock.enableCaseEligibleForDecreeAbsolute(AUTH_TOKEN)).thenThrow(WorkflowException.class);

        JobExecutionException jobExecutionException = assertThrows(
            JobExecutionException.class,
            () -> classToTest.execute(jobExecutionContextMock)
        );

        assertThat(jobExecutionException.getMessage(), is("Enable cases eligible for DA service failed"));
    }
}
