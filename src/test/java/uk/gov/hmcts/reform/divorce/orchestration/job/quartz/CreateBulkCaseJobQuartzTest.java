package uk.gov.hmcts.reform.divorce.orchestration.job.quartz;

import org.awaitility.core.ThrowingRunnable;
import org.quartz.Job;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.TestPropertySource;
import uk.gov.hmcts.reform.divorce.orchestration.job.CreateBulkCaseJob;
import uk.gov.hmcts.reform.divorce.orchestration.service.CaseOrchestrationService;

import static org.mockito.Mockito.verify;

@TestPropertySource(properties = {
    "SCHEDULER_SCHEDULES_CREATE_BULK_CASES_ENABLED=true"
})
public class CreateBulkCaseJobQuartzTest extends QuartzTest {

    @MockBean
    private CaseOrchestrationService orchestrationServiceMock;

    @Override
    protected void setUpQuartzTest() {
    }

    @Override
    protected Class<? extends Job> getJobUnderTest() {
        return CreateBulkCaseJob.class;
    }

    @Override
    protected ThrowingRunnable getBasicAssertion() {
        return () -> verify(orchestrationServiceMock).generateBulkCaseForListing();
    }
}