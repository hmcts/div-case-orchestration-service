package uk.gov.hmcts.reform.divorce.orchestration.job;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.quartz.JobExecutionException;
import uk.gov.hmcts.reform.divorce.orchestration.service.CaseOrchestrationServiceException;
import uk.gov.hmcts.reform.divorce.orchestration.service.NfdNotifierService;
import uk.gov.hmcts.reform.divorce.orchestration.util.AuthUtil;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertThrows;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.AUTH_TOKEN;

@RunWith(MockitoJUnitRunner.class)
public class NfdNotifierJobTest {

    @Mock
    private NfdNotifierService notifierService;
    @Mock
    private AuthUtil authUtil;
    @InjectMocks
    private NfdNotifierJob nfdNotifierJob;

    @Before
    public void setUpTest() {
        when(authUtil.getCaseworkerSuperUserToken()).thenReturn(AUTH_TOKEN);
    }

    @Test
    public void shouldCallNotifierService() throws JobExecutionException, CaseOrchestrationServiceException {

        nfdNotifierJob.execute(null);
        verify(notifierService).notifyUnsubmittedApplications(AUTH_TOKEN);

    }

    @Test
    public void shouldThrowJobExecutionException_WhenServiceFails() throws CaseOrchestrationServiceException {
        doThrow(CaseOrchestrationServiceException.class).when(notifierService).notifyUnsubmittedApplications(AUTH_TOKEN);

        JobExecutionException jobExecutionException = assertThrows(
            JobExecutionException.class,
            () -> nfdNotifierJob.execute(null)
        );

        assertThat(jobExecutionException.getCause(), is(instanceOf(CaseOrchestrationServiceException.class)));
    }

}