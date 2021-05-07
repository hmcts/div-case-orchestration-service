package uk.gov.hmcts.reform.divorce.orchestration.tasks;

import org.junit.Before;
import org.junit.Test;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.Features;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.courts.CourtEnum;

import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_SOLICITOR_ACCOUNT_NUMBER;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.CcdFields.PBA_NUMBERS;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CASE_TYPE_ID;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.DynamicList.asDynamicList;


public class ProcessPbaUsingCaseTypeToggleOnTest extends ProcessPbaPaymentTaskAbstractTest {
    @Before
    public void setup() {
        when(featureToggleService.isFeatureEnabled(Features.PAY_BY_ACCOUNT)).thenReturn(true);
        setPbaNumber();
    }

    @Override
    protected void setPbaNumber() {
        caseData.put(PBA_NUMBERS, asDynamicList(TEST_SOLICITOR_ACCOUNT_NUMBER));
    }

    @Test
    public void testPbaPaymentRequestIsUsingSiteId() {
        expectedRequest.setSiteId(CourtEnum.EASTMIDLANDS.getSiteId());
        expectedRequest.setCaseType(null);

        when(featureToggleService.isFeatureEnabled(Features.PBA_USING_CASE_TYPE)).thenReturn(false);

        givenValidData_whenExecuteIsCalled_thenMakePayment();
    }

    @Test
    public void testPbaPaymentRequestIsUsingCaseType() {
        expectedRequest.setSiteId(null);
        expectedRequest.setCaseType(CASE_TYPE_ID);

        when(featureToggleService.isFeatureEnabled(Features.PBA_USING_CASE_TYPE)).thenReturn(true);

        givenValidData_whenExecuteIsCalled_thenMakePayment();
    }
}
