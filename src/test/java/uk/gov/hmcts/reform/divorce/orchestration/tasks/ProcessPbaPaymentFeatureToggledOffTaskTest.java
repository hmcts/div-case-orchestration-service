package uk.gov.hmcts.reform.divorce.orchestration.tasks;

import org.junit.Before;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.Features;

import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_SOLICITOR_ACCOUNT_NUMBER;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.SOLICITOR_FEE_ACCOUNT_NUMBER_JSON_KEY;

@RunWith(MockitoJUnitRunner.class)
public class ProcessPbaPaymentFeatureToggledOffTaskTest extends ProcessPbaPaymentTaskAbstractTest {

    @Before
    public void setup() {
        when(featureToggleService.isFeatureEnabled(Features.PAY_BY_ACCOUNT)).thenReturn(false);
        setPbaNumber();
    }

    @Override
    protected void setPbaNumber() {
        caseData.put(SOLICITOR_FEE_ACCOUNT_NUMBER_JSON_KEY, TEST_SOLICITOR_ACCOUNT_NUMBER);
    }
}
