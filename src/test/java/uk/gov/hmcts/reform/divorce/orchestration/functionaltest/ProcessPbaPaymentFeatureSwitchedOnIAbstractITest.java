package uk.gov.hmcts.reform.divorce.orchestration.functionaltest;

import org.junit.Before;
import org.springframework.boot.test.context.SpringBootTest;

import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_SOLICITOR_ACCOUNT_NUMBER;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.CcdFields.PBA_NUMBERS;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.DynamicList.asDynamicList;

@SpringBootTest(properties = {"feature-toggle.toggle.pay_by_account=true"})
public class ProcessPbaPaymentFeatureSwitchedOnIAbstractITest extends ProcessPbaPaymentAbstractITest {

    @Before
    public void setup() {
        setPbaNumber();
    }

    @Override
    protected void setPbaNumber() {
        caseData.put(PBA_NUMBERS, asDynamicList(TEST_SOLICITOR_ACCOUNT_NUMBER));
    }
}
