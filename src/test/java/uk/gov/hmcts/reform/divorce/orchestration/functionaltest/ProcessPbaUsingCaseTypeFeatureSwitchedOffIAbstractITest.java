package uk.gov.hmcts.reform.divorce.orchestration.functionaltest;

import org.junit.Before;
import org.springframework.boot.test.context.SpringBootTest;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.courts.CourtEnum;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.pay.CreditAccountPaymentRequest;

import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_SOLICITOR_ACCOUNT_NUMBER;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.CcdFields.PBA_NUMBERS;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.DynamicList.asDynamicList;


@SpringBootTest(properties = {"feature-toggle.toggle.pay_by_account=true", "feature-toggle.toggle.pba_case_type=false"})
public class ProcessPbaUsingCaseTypeFeatureSwitchedOffIAbstractITest extends ProcessPbaPaymentAbstractITest {

    @Before
    public void setup() {
        setPbaNumber();
    }

    @Override
    protected void setPbaNumber() {
        caseData.put(PBA_NUMBERS, asDynamicList(TEST_SOLICITOR_ACCOUNT_NUMBER));
    }

    @Override
    protected void setSiteIdOrCaseType(CreditAccountPaymentRequest request) {
        request.setSiteId(CourtEnum.EASTMIDLANDS.getSiteId());
    }
}
