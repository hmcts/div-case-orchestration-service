package uk.gov.hmcts.reform.divorce.orchestration.job.quartz;

import org.awaitility.core.ThrowingRunnable;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.TestPropertySource;
import uk.gov.hmcts.reform.divorce.orchestration.job.BulkPrintAosJob;
import uk.gov.hmcts.reform.divorce.orchestration.service.impl.PrintRespondentAosPackService;

import static org.mockito.Mockito.verify;

@TestPropertySource(properties = {
    "SCHEDULER_BULK_PRINT_AOS_JOB_ENABLED=true"
})
public class BulkPrintAosJobQuartzTest extends QuartzTest {
    @MockBean
    private PrintRespondentAosPackService printRespondentAosPackService;

    @Override
    protected void setUpQuartzTest() {
    }

    @Override
    protected ThrowingRunnable getBasicAssertion() {
        return () -> verify(printRespondentAosPackService).printAosPacks();
    }

    @Override
    protected Class<BulkPrintAosJob> getJobUnderTest() {
        return BulkPrintAosJob.class;
    }
}
