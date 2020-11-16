package uk.gov.hmcts.reform.divorce.orchestration.tasks;

import org.junit.Before;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.Features;

import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_SOLICITOR_ACCOUNT_NUMBER;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.CcdFields.PBA_NUMBERS;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.DynamicList.asDynamicList;

@RunWith(MockitoJUnitRunner.class)
public class ProcessPbaPaymentFeatureToggledOnTaskTest extends ProcessPbaPaymentTaskAbstractTest {

    @Before
    public void setup() {
        when(featureToggleService.isFeatureEnabled(Features.PAY_BY_ACCOUNT)).thenReturn(true);
        setPbaNumber();
    }

    @Override
    protected void setPbaNumber() {
        caseData.put(PBA_NUMBERS, asDynamicList(TEST_SOLICITOR_ACCOUNT_NUMBER));
    }
}
