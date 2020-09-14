package uk.gov.hmcts.reform.divorce.orchestration.job;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import uk.gov.hmcts.reform.divorce.orchestration.service.CaseOrchestrationServiceException;
import uk.gov.hmcts.reform.divorce.orchestration.service.DataExtractionService;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertThrows;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class DataExtractionJobTest {

    @Mock
    private DataExtractionService dataExtractionService;

    @Mock
    private JobExecutionContext jobExecutionContext;

    @InjectMocks
    private DataExtractionJob classUnderTest;

    @Test
    public void execute_requestData_callsService() throws JobExecutionException, CaseOrchestrationServiceException {
        classUnderTest.execute(jobExecutionContext);
        verify(dataExtractionService).requestDataExtractionForPreviousDay();
    }

    @Test
    public void execute_requestData_throwsJobExecutionException() throws CaseOrchestrationServiceException {
        doThrow(CaseOrchestrationServiceException.class).when(dataExtractionService).requestDataExtractionForPreviousDay();

        JobExecutionException jobExecutionException = assertThrows(
            JobExecutionException.class,
            () -> classUnderTest.execute(jobExecutionContext)
        );

        assertThat(jobExecutionException.getMessage(), is("ExtractDataToRobotics service failed"));
    }
}
