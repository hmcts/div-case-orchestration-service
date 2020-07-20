package uk.gov.hmcts.reform.divorce.orchestration.job;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.quartz.JobExecutionException;
import uk.gov.hmcts.reform.divorce.orchestration.service.AosService;
import uk.gov.hmcts.reform.divorce.orchestration.service.CaseOrchestrationServiceException;
import uk.gov.hmcts.reform.divorce.orchestration.util.AuthUtil;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.junit.rules.ExpectedException.none;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.AUTH_TOKEN;

@RunWith(MockitoJUnitRunner.class)
public class AosOverdueJobTest {

    @Rule
    public ExpectedException expectedException = none();

    @Mock
    private AosService aosService;

    @Mock
    private AuthUtil authUtil;

    @InjectMocks
    private AosOverdueJob classUnderTest;

    @Before
    public void setUp() {
        when(authUtil.getCaseworkerToken()).thenReturn(AUTH_TOKEN);
    }

    @Test
    public void shouldCallService() throws CaseOrchestrationServiceException, JobExecutionException {
        classUnderTest.execute(null);

        verify(aosService).markCasesToBeMovedToAosOverdue(AUTH_TOKEN);
    }

    @Test
    public void shouldThrowJobExecutionException_WhenServiceFails() throws CaseOrchestrationServiceException, JobExecutionException {
        doThrow(CaseOrchestrationServiceException.class).when(aosService).markCasesToBeMovedToAosOverdue(AUTH_TOKEN);
        expectedException.expect(JobExecutionException.class);
        expectedException.expectCause(instanceOf(CaseOrchestrationServiceException.class));

        classUnderTest.execute(null);
    }

}