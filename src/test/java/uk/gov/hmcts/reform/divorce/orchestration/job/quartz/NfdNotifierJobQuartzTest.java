package uk.gov.hmcts.reform.divorce.orchestration.job.quartz;

import org.awaitility.core.ThrowingRunnable;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.TestPropertySource;
import uk.gov.hmcts.reform.divorce.orchestration.job.NfdNotifierJob;
import uk.gov.hmcts.reform.divorce.orchestration.service.NfdNotifierService;
import uk.gov.hmcts.reform.divorce.orchestration.util.AuthUtil;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.AUTH_TOKEN;

@TestPropertySource(properties = {
    "SCHEDULER_NFD_NOTIFIER_ENABLED=true"
})
public class NfdNotifierJobQuartzTest extends QuartzTest {

    @MockBean
    private NfdNotifierService nfdNotifierService;

    @MockBean
    private AuthUtil authUtil;

    @Override
    protected void setUpQuartzTest() {
        when(authUtil.getCaseworkerToken()).thenReturn(AUTH_TOKEN);
    }

    @Override
    protected ThrowingRunnable getBasicAssertion() {
        return () -> verify(nfdNotifierService).notifyUnsubmittedApplications(AUTH_TOKEN);
    }

    @Override
    protected Class<NfdNotifierJob> getJobUnderTest() {
        return NfdNotifierJob.class;
    }
}
