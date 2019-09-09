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
import uk.gov.hmcts.reform.divorce.orchestration.service.DataExtractionService;

import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class ExtractDataToRoboticsTest {

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Mock
    private DataExtractionService dataExtractionService;

    @Mock
    private JobExecutionContext jobExecutionContext;

    @InjectMocks
    private ExtractDataToRobotics classUnderTest;

    @Test
    public void execute_requestData_callsService() throws JobExecutionException, WorkflowException {
        classUnderTest.execute(jobExecutionContext);
        verify(dataExtractionService).requestDataExtraction();
    }

    @Test
    public void execute_requestData_throwsJobExecutionException() throws JobExecutionException, WorkflowException {
        expectedException.expect(JobExecutionException.class);
        expectedException.expectMessage("ExtractDataToRobotics service failed");
        doThrow(WorkflowException.class).when(dataExtractionService).requestDataExtraction();
        classUnderTest.execute(jobExecutionContext);
    }


}
