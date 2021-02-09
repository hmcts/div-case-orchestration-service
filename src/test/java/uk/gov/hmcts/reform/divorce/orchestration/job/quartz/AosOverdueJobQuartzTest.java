package uk.gov.hmcts.reform.divorce.orchestration.job.quartz;

import org.awaitility.core.ThrowingRunnable;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.TestPropertySource;
import uk.gov.hmcts.reform.divorce.orchestration.job.AosOverdueJob;
import uk.gov.hmcts.reform.divorce.orchestration.service.AosService;
import uk.gov.hmcts.reform.divorce.orchestration.util.AuthUtil;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.AUTH_TOKEN;

@TestPropertySource(properties = {
    "SCHEDULER_MAKE_ELIGIBLE_CASES_AOS_OVERDUE_ENABLED=true"
})
public class AosOverdueJobQuartzTest extends QuartzTest {

    @MockBean
    private AosService aosService;

    @MockBean
    private AuthUtil authUtil;

    @Override
    protected void setUpQuartzTest() {
        when(authUtil.getCaseworkerToken()).thenReturn(AUTH_TOKEN);
    }

    @Override
    protected ThrowingRunnable getBasicAssertion() {
        return () -> verify(aosService).findCasesForWhichAosIsOverdue(AUTH_TOKEN);
    }

    @Override
    protected Class<AosOverdueJob> getJobUnderTest() {
        return AosOverdueJob.class;
    }

}