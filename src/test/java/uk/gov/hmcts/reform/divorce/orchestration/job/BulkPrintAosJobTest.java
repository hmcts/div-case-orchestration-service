package uk.gov.hmcts.reform.divorce.orchestration.job;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.quartz.JobExecutionException;
import uk.gov.hmcts.reform.divorce.orchestration.service.impl.PrintRespondentAosPackService;

import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class BulkPrintAosJobTest {

    @Mock
    private PrintRespondentAosPackService printRespondentAosPackService;

    @InjectMocks
    private BulkPrintAosJob bulkPrintAosJob;


    @Test
    public void shouldCallNotifierService() throws JobExecutionException {

        bulkPrintAosJob.execute(null);
        verify(printRespondentAosPackService).printAosPacks();

    }
}
