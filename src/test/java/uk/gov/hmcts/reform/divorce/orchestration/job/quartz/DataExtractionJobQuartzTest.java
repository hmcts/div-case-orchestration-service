package uk.gov.hmcts.reform.divorce.orchestration.job.quartz;

import org.awaitility.core.ThrowingRunnable;
import org.quartz.Job;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.TestPropertySource;
import uk.gov.hmcts.reform.divorce.orchestration.job.DataExtractionJob;
import uk.gov.hmcts.reform.divorce.orchestration.service.DataExtractionService;

import static org.mockito.Mockito.verify;

@TestPropertySource(properties = {
    "SCHEDULER_SEND_UPDATED_CASES_TO_ROBOTICS_ENABLED=true"
})
public class DataExtractionJobQuartzTest extends QuartzTest {

    @MockBean
    private DataExtractionService dataExtractionService;

    @Override
    protected void setUpQuartzTest() {
    }

    @Override
    protected Class<? extends Job> getJobUnderTest() {
        return DataExtractionJob.class;
    }

    @Override
    protected ThrowingRunnable getBasicAssertion() {
        return () -> verify(dataExtractionService).requestDataExtractionForPreviousDay();
    }
}
