package uk.gov.hmcts.reform.divorce.orchestration.job.quartz;

import org.awaitility.core.ThrowingRunnable;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.TestPropertySource;
import uk.gov.hmcts.reform.divorce.orchestration.job.NfdNotifierJob;
import uk.gov.hmcts.reform.divorce.orchestration.service.NfdNotifierService;

import static org.mockito.Mockito.verify;

@TestPropertySource(properties = {
    "SCHEDULER_NFD_NOTIFIER_ENABLED=true"
})
public class NfdNotifierJobQuartzTest extends QuartzTest {

    @MockBean
    private NfdNotifierService nfdNotifierService;

    @Override
    protected void setUpQuartzTest() {

    }

    @Override
    protected ThrowingRunnable getBasicAssertion() {

        return () -> verify(nfdNotifierService).notifyUnsubmittedApplications();
    }

    @Override
    protected Class<NfdNotifierJob> getJobUnderTest() {
        return NfdNotifierJob.class;
    }
}
