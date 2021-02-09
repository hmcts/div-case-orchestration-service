package uk.gov.hmcts.reform.divorce.orchestration.job.quartz;

import org.awaitility.core.ThrowingRunnable;
import org.quartz.Job;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.TestPropertySource;
import uk.gov.hmcts.reform.divorce.orchestration.job.MakeCasesEligibleForDAJob;
import uk.gov.hmcts.reform.divorce.orchestration.service.DecreeAbsoluteService;
import uk.gov.hmcts.reform.divorce.orchestration.util.AuthUtil;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.AUTH_TOKEN;

@TestPropertySource(properties = {
    "SCHEDULER_MAKE_CASES_ELIGIBLE_DA_ENABLED=true"
})
public class MakeCasesEligibleForDAJobQuartzTest extends QuartzTest {

    @MockBean
    private DecreeAbsoluteService decreeAbsoluteServiceMock;

    @MockBean
    private AuthUtil authUtil;

    @Override
    protected void setUpQuartzTest() {
        when(authUtil.getCaseworkerToken()).thenReturn(AUTH_TOKEN);
    }

    @Override
    protected Class<? extends Job> getJobUnderTest() {
        return MakeCasesEligibleForDAJob.class;
    }

    @Override
    protected ThrowingRunnable getBasicAssertion() {
        return () -> verify(decreeAbsoluteServiceMock).enableCaseEligibleForDecreeAbsolute(AUTH_TOKEN);
    }

}