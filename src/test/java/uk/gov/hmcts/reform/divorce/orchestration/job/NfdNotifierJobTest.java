package uk.gov.hmcts.reform.divorce.orchestration.job;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.quartz.JobExecutionException;
import uk.gov.hmcts.reform.divorce.orchestration.service.CaseOrchestrationServiceException;
import uk.gov.hmcts.reform.divorce.orchestration.service.NfdNotifierService;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertThrows;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class NfdNotifierJobTest {

    @Mock
    private NfdNotifierService notifierService;

    @InjectMocks
    private NfdNotifierJob nfdNotifierJob;


    @Test
    public void shouldCallNotifierService() throws JobExecutionException, CaseOrchestrationServiceException {

        nfdNotifierJob.execute(null);
        verify(notifierService).notifyUnsubmittedApplications();

    }

    @Test
    public void shouldThrowJobExecutionException_WhenServiceFails() throws CaseOrchestrationServiceException {
        doThrow(CaseOrchestrationServiceException.class).when(notifierService).notifyUnsubmittedApplications();

        JobExecutionException jobExecutionException = assertThrows(
            JobExecutionException.class,
            () -> nfdNotifierJob.execute(null)
        );

        assertThat(jobExecutionException.getCause(), is(instanceOf(CaseOrchestrationServiceException.class)));
    }

}