package uk.gov.hmcts.reform.divorce.orchestration.job;

import org.junit.Before;
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
import uk.gov.hmcts.reform.divorce.orchestration.util.AuthUtil;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class MakeCasesDAOverdueJobTest {

    private static final String TEST_AUTH_TOKEN = "testAuthToken";

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Mock
    private DecreeAbsoluteService decreeAbsoluteServiceMock;

    @Mock
    private JobExecutionContext jobExecutionContextMock;

    @Mock
    private AuthUtil authUtil;

    @InjectMocks
    private MakeCasesDAOverdueJob classToTest;

    @Before
    public void setUp() {
        when(authUtil.getCaseworkerToken()).thenReturn(TEST_AUTH_TOKEN);
    }

    @Test
    public void execute_updateToDAOverdue_updateExecuted() throws JobExecutionException, WorkflowException {
        classToTest.execute(jobExecutionContextMock);

        verify(decreeAbsoluteServiceMock).processCaseOverdueForDecreeAbsolute(TEST_AUTH_TOKEN);
    }

    @Test
    public void execute_updateToDAOverdue_JobExceptionThrown() throws JobExecutionException, WorkflowException {
        expectedException.expect(JobExecutionException.class);
        expectedException.expectMessage("Cases overdue for DA failed");
        when(decreeAbsoluteServiceMock.processCaseOverdueForDecreeAbsolute(TEST_AUTH_TOKEN)).thenThrow(WorkflowException.class);

        classToTest.execute(jobExecutionContextMock);
    }

}
